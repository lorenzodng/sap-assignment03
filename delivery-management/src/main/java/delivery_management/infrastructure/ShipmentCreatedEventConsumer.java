package delivery_management.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.json.JSONObject;
import delivery_management.domain.Shipment;
import java.util.HashMap;
import java.util.Map;

@Adapter
public class ShipmentCreatedEventConsumer {

    private static final String TOPIC = "shipment-created";
    private final KafkaConsumer<String, String> consumer;
    private final Map<String, Shipment> shipments;

    public ShipmentCreatedEventConsumer(Vertx vertx, Map<String, Shipment> shipments) {
        this.shipments = shipments;
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("group.id", "delivery-management-group");
        config.put("auto.offset.reset", "earliest");
        this.consumer = KafkaConsumer.create(vertx, config);
        this.consumer.subscribe(TOPIC);
        this.consumer.handler(record -> createShipment(record.value()));
    }

    // crea la spedizione con stato REQUESTED
    private void createShipment(String message) {
        JSONObject event = new JSONObject(message);
        String shipmentId = event.getString("shipmentId");
        if (!shipments.containsKey(shipmentId)) {
            Shipment shipment = new Shipment(shipmentId);
            shipments.put(shipmentId, shipment);
        }
    }
}