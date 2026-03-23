package delivery_management.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.json.JSONObject;
import delivery_management.domain.Shipment;
import delivery_management.domain.ShipmentStatus;
import java.util.HashMap;
import java.util.Map;

@Adapter
public class DroneNotAvailableEventConsumer {

    private static final String TOPIC = "drone-not-available";
    private final KafkaConsumer<String, String> consumer;
    private final Map<String, Shipment> shipments;

    public DroneNotAvailableEventConsumer(Vertx vertx, Map<String, Shipment> shipments) {
        this.shipments = shipments;
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("group.id", "delivery-management-group");
        config.put("auto.offset.reset", "earliest");
        this.consumer = KafkaConsumer.create(vertx, config);
        this.consumer.subscribe(TOPIC);
        this.consumer.handler(record -> cancelShipment(record.value()));
    }

    //crea la spedizione con stato CANCELLED
    private void cancelShipment(String message) {
        JSONObject event = new JSONObject(message);
        String shipmentId = event.getString("shipmentId");
        Shipment shipment = shipments.get(shipmentId);
        if (shipment != null) {
            shipment.updateStatus(ShipmentStatus.CANCELLED);
        }
    }
}