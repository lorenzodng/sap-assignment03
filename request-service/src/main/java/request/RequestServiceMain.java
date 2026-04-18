package request;

import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import request.application.*;
import request.infrastructure.KafkaShipmentRequestEventProducer;
import request.infrastructure.PrometheusRequestMetricsProxy;
import request.infrastructure.ShipmentRequestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestServiceMain {

    private static final Logger log = LoggerFactory.getLogger(RequestServiceMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("request-service").ignoreIfMissing().load();
        String bootstrap = System.getenv("KAFKA_BOOTSTRAP_SERVERS") != null ? System.getenv("KAFKA_BOOTSTRAP_SERVERS") : dotenv.get("KAFKA_BOOTSTRAP_SERVERS");
        int port = System.getenv("PORT") != null ? Integer.parseInt(System.getenv("PORT")) : Integer.parseInt(dotenv.get("PORT"));
        int metricsPort = System.getenv("METRICS_PORT") != null ? Integer.parseInt(System.getenv("METRICS_PORT")) : Integer.parseInt(dotenv.get("METRICS_PORT"));

        //istanza che contiene l'event loop per gestire le richieste in modo asincrono
        Vertx vertx = Vertx.vertx();

        //metriche
        RequestMetrics metrics = null;
        try {
            metrics = new PrometheusRequestMetricsProxy(metricsPort);
            log.info("Prometheus metrics exporter started on port {}", metricsPort);
        } catch (Exception e) {
            log.error("Failed to start Prometheus metrics exporter: {}", e.getMessage());
        }

        //crea il producer Kafka
        ShipmentRequestEventProducer eventProducer = new KafkaShipmentRequestEventProducer(vertx, bootstrap);

        //crea i use case
        CreateShipmentRequestImpl createShipmentRequest = new CreateShipmentRequestImpl();
        ValidateShipmentRequestImpl validateShipmentRequest = new ValidateShipmentRequestImpl();
        ShipmentScheduler shipmentScheduler = new ShipmentSchedulerImpl(eventProducer, vertx);

        //crea l'orchestratore
        ShipmentRequestOrchestrator orchestrator = new ShipmentRequestOrchestratorImpl(createShipmentRequest, validateShipmentRequest, shipmentScheduler, metrics);

        //crea il controller REST
        ShipmentRequestController controller = new ShipmentRequestController(orchestrator);

        //crea il router e registra la rotta
        Router router = Router.router(vertx);
        controller.registerRoutes(router);

        //avvia il server HTTP
        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("Request service started on port {}", port);
    }
}