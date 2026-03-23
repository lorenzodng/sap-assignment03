package drone_management.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.json.JSONObject;
import drone_management.application.AssignDrone;
import drone_management.domain.Drone;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//recupera l'evento di creazione richiesta spedizione pubblicato dal gestore richieste
@Adapter
public class ShipmentRequestedEventConsumer {

    private static final String TOPIC = "shipment-requested";
    private final KafkaConsumer<String, String> consumer;
    private final AssignDrone assignDrone;
    private final List<Drone> drones;
    private final DroneEventProducer eventProducer;

    public ShipmentRequestedEventConsumer(Vertx vertx, AssignDrone assignDrone, List<Drone> drones, DroneEventProducer eventProducer) {
        this.assignDrone = assignDrone;
        this.drones = drones;
        this.eventProducer = eventProducer;
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("group.id", "drone-management-group"); //gruppo di consumer Kafka usato per tenere traccia di quali messaggi sono già stati letti da un gruppo di istanze di uno stesso microservizio - in questo modo kafka distribuisce i messaggi tra loro senza duplicati
        config.put("auto.offset.reset", "earliest"); //legge tutti i messaggi dall'inizio del topic, anche quelli pubblicati prima dell'esistenza del consumer
        this.consumer = KafkaConsumer.create(vertx, config);
        this.consumer.subscribe(TOPIC); //sottoscrive il consumatore al topic di richieste
        this.consumer.handler(record -> assignDroneToShipment(record.value())); //registra il metodo di gestioine delle richieste
    }

    //assegna un drone alla spedizione e invoca il broker kafka
    private void assignDroneToShipment(String message) {
        JSONObject event = new JSONObject(message);
        String shipmentId = event.getString("shipmentId");
        double pickupLatitude = event.getDouble("pickupLatitude");
        double pickupLongitude = event.getDouble("pickupLongitude");
        double deliveryLatitude = event.getDouble("deliveryLatitude");
        double deliveryLongitude = event.getDouble("deliveryLongitude");
        double packageWeight = event.getDouble("packageWeight");
        int deliveryTimeLimit = event.getInt("deliveryTimeLimit");
        double distancePickupToDelivery = calculateDistance(pickupLatitude, pickupLongitude, deliveryLatitude, deliveryLongitude);
        Drone assignedDrone = assignDrone.assign(drones, packageWeight, pickupLatitude, pickupLongitude, distancePickupToDelivery, deliveryTimeLimit);
        if (assignedDrone != null) {
            assignedDrone.setAvailable(false);
            eventProducer.publishDroneAssigned(shipmentId, assignedDrone, pickupLatitude, pickupLongitude, deliveryLatitude, deliveryLongitude);
        } else {
            System.out.println("No drone available for shipment: " + shipmentId);
            eventProducer.publishDroneNotAvailable(shipmentId);
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double latDiff = lat1 - lat2;
        double lonDiff = lon1 - lon2;
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff);
    }
}