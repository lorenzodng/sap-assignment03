package drone.infrastructure;

import buildingblocks.infrastructure.Adapter;
import drone.application.DroneEventProducer;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import org.json.JSONObject;
import drone.domain.Drone;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//producer kafka che pubblica gli eventi di assegnazione drone
//proxy
@Adapter
public class KafkaDroneEventProducer implements DroneEventProducer {

    private static final Logger log = LoggerFactory.getLogger(KafkaDroneEventProducer.class);
    private static final String TOPIC = "drone-assigned";
    private final KafkaProducer<String, String> producer;

    public KafkaDroneEventProducer(Vertx vertx, String bootstrapServers) {
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", bootstrapServers);
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        this.producer = KafkaProducer.create(vertx, config);
    }

    //pubblica l'evento di drone assegnato sul canale dedicato
    @Override
    public void publishDroneAssigned(String shipmentId, Drone drone, double pickupLatitude, double pickupLongitude, double deliveryLatitude, double deliveryLongitude) {
        //costruisce l'evento json con tutte le informazioni necessarie per il calcolo della posizione del drone/pacco
        JSONObject event = new JSONObject();
        event.put("shipmentId", shipmentId);
        event.put("droneId", drone.getId());
        event.put("droneSpeed", Drone.SPEED);
        event.put("droneLatitude", drone.getPosition().getLatitude());
        event.put("droneLongitude", drone.getPosition().getLongitude());
        event.put("pickupLatitude", pickupLatitude);
        event.put("pickupLongitude", pickupLongitude);
        event.put("deliveryLatitude", deliveryLatitude);
        event.put("deliveryLongitude", deliveryLongitude);
        event.put("assignedAt", System.currentTimeMillis());
        KafkaProducerRecord<String, String> record = KafkaProducerRecord.create(TOPIC, shipmentId, event.toString());
        producer.send(record);
        log.info("Drone {} assigned to shipment {}", drone.getId(), shipmentId);
        log.info("Shipment {} drone assigned event published", shipmentId);
    }

    //pubblica l'evento di drone non disponibile sul canale dedicato
    @Override
    public void publishDroneNotAvailable(String shipmentId) {
        JSONObject event = new JSONObject();
        event.put("shipmentId", shipmentId);
        KafkaProducerRecord<String, String> record = KafkaProducerRecord.create("drone-not-available", shipmentId, event.toString());
        producer.send(record);
        log.warn("No available drones for shipment {}", shipmentId);
        log.warn("Shipment {} drone not available event published", shipmentId);
    }
}