# Assignment 03 — Shipping on the Air

## Overview

This document describes the architectural decisions and implementation choices made for Assignment 03, which extends the *Shipping on the Air* system in two directions: the adoption of an event-driven architecture for selected microservices, and the definition of Service Level Objectives with the corresponding indicators and a Kubernetes-based deployment.

---

## Event-Driven Architecture

### Candidate Selection

Three criteria were considered to identify the best candidates for event-driven reengineering:

- **Fault tolerance**: a microservice is a strong candidate if its failure has a significant impact on the user experience.  
A failure of `request-service` would prevent new shipment requests from being created, but would not affect ongoing deliveries.   
In contrast, a failure of `delivery-service` would prevent users from tracking active shipments and monitoring drone positions — which is a core functionality of the service. 
For this reason, `delivery-service` is the most critical candidate from a domain perspective.


- **Internal decoupling**: a strong candidate is typically characterised by a high number of direct dependencies on other microservices, since that is where decoupling via Kafka brings the greatest architectural benefit.  
Analysing the internal flow, `drone-service` receives events from `request-service` and produces events towards `delivery-service`, making it the central node of the internal communication chain and therefore the primary candidate from an architectural perspective.


- **Scalability**: another relevant factor is whether the microservice is subject to a high and variable workload. `delivery-service` is the most frequently accessed service by users for shipment tracking. 
With a REST architecture, a high number of simultaneous requests would make it a bottleneck; with Kafka, consumers can scale horizontally and independently to absorb the load.

Having identified two primary candidates with different motivations, both `drone-service` and `delivery-service` were reengineered with an event-driven architecture.   
To maintain consistency of the internal flow and avoid synchronous communication points that would have broken the event chain, `request-service` was also adapted to the event-driven model, in order to ensure a consistent asynchronous communication model and avoid hybrid interaction patterns.

### Kafka Communication Flow

The system follows an event-driven choreography where each microservice reacts to events published on the Kafka broker and produces new events based on its local processing logic.  
The communication between microservices is based on two Kafka topics:

- `shipment-requested`: produced by `request-service`, consumed by `drone-service`.
- `drone-assigned` and `drone-not-available`: produced by `drone-service`, consumed by `delivery-service`.

**`request-service`** acts as the entry point of the event chain: when a shipment request is validated, `ShipmentRequestOrchestratorImpl` coordinates the creation and validation flow, then publishes a `shipment-requested` event via `KafkaShipmentRequestEventProducer`.

**`drone-service`** consumes `shipment-requested` events via `ShipmentRequestedEventConsumer` and delegates to `DroneAssignmentOrchestratorImpl`, which coordinates the drone assignment flow by attempting to assign an available drone.
Depending on the outcome, it publishes either a `drone-assigned` event — with all the information needed for delivery scheduling — or a `drone-not-available` event, both produced via `KafkaDroneEventProducer`.

**`delivery-service`** consumes both events via two dedicated consumers: `DroneAssignedEventConsumer`, which schedules the shipment, and `DroneUnavailableEventConsumer`, which cancels it. 
Both delegate to `ShipmentManagerImpl`, which coordinates the delivery flow by creating or cancelling shipments and persisting their state via a repository.

**`api-gateway`** uses a non-blocking request/response model for communication with `request-service` and `delivery-service`, as it is the entry point for client interactions and does not participate in the internal event chain.

---

## Service Level Objectives and Indicators

### SLO Definitions

Two SLOs were defined based on the quality attributes established in Assignment 01.

- **SLO 1 — Availability**:
  - SLI: ratio of successful requests (non-5xx responses) over total requests.
  - SLO: at least 99% of requests must result in a non-5xx response.


- **SLO 2 — Performance**:
  - SLI: latency of shipment creation requests at the `request-service` level.
  - SLO: 95% of shipment creation requests must complete within 500ms.

Measuring end-to-end latency across the full microservice chain was not feasible due to the asynchronous nature of the Kafka-based communication between microservices. 
Therefore, the performance SLO was scoped to the shipment creation process at the `request-service` level because it represents the most relevant synchronous interaction for the user. 

### SLI Measurement

Application Metrics were reintroduced in Assignment 03 to support SLI measurement using Prometheus, following the same pattern adopted in Assignment 02.  

- **Availability SLI** is measured at the `api-gateway` level by `PrometheusApiGatewayMetricsProxy`, which exposes a `gateway_shipments_requests_total` counter labelled by endpoint, HTTP method, and response status.  
Using the status label, the ratio of non-5xx responses over total responses yields the availability SLI.


- **Performance SLI** is measured at the `request-service` level by `PrometheusRequestMetricsProxy`, which exposes a `request_orchestration_duration_seconds` histogram with upper bounds at: 0.1, 0.3, 0.5, 1.0, and 2.0 seconds.  

---

## Kubernetes Deployment

The system is deployed on Kubernetes within a dedicated namespace, `sap-assignment03`, ensuring logical isolation and avoiding resource conflicts. The deployment manifests are organized into two directories:

- `k8s/infrastructure`: contains shared components, specifically `kafka.yml` and `prometheus.yaml`.
  - Kafka is deployed in KRaft mode, significantly reducing the system's footprint and complexity.
  - Prometheus is used for centralized monitoring. A key aspect of this deployment is that Prometheus configuration is managed via a `ConfigMap`, which separates monitoring configuration and scraping rules from the Prometheus deployment.


- `k8s/services`: contains microservice-specific manifests (`api-gateway.yml`, `request-service.yml`, `drone-service.yml`, `delivery-service.yml`), each defining a Deployment to manage the container lifecycle and a Service to handle networking.  
Each microservice defines its configuration through a dedicated `.env` file, whose values are injected into the Kubernetes deployments as environment variables.

### Networking and Service Discovery

The architecture leverages Kubernetes' advanced networking capabilities: 

- External Access: the api-gateway serves as the primary entry point for external traffic, exposed via a `LoadBalancer` service on port 30080.
- Internal Service Discovery: all other microservices (`request-service`, `drone-service`, `delivery-service`) are configured with internal `ClusterIP` services.   

This setup leverages Kubernetes internal DNS, allowing microservices to discover and resolve each other automatically using only their Service names, removing the need for hardcoded IP addresses and ensuring seamless communication even if pods are rescheduled.

### Observability and Event-Driven Communication

For observability, Prometheus scraping annotations are implemented in the service manifests. Specifically, the `api-gateway` and `request-service` expose metrics on dedicated ports (9080 and 9081), configured as environment variables which Prometheus discovers automatically through its dynamic configuration.


As for asynchronous communication, all microservices receive the Kafka bootstrap server address via the `KAFKA_BOOTSTRAP_SERVERS` environment variable, pointing to `kafka-service:9092` within the cluster.

---

## Sources

- Apache Kafka documentation — https://kafka.apache.org/documentation/
- Prometheus documentation — https://prometheus.io/docs/
- Kubernetes documentation — https://kubernetes.io/docs/

