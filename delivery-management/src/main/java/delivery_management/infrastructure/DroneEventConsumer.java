package delivery_management.infrastructure;

import buildingblocks.infrastructure.Adapter;
import delivery_management.domain.Position;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.json.JSONObject;
import delivery_management.application.UpdateShipmentStatus;
import delivery_management.domain.Shipment;
import delivery_management.domain.ShipmentStatus;
import java.util.HashMap;
import java.util.Map;

//recupera l'evento di assegnazione drone pubblicato dal gestore droni
@Adapter
public class DroneEventConsumer {

    private static final String TOPIC = "drone-assigned";
    private final KafkaConsumer<String, String> consumer;
    private final UpdateShipmentStatus updateShipmentStatus;
    private final Map<String, Shipment> shipments; //mappa che tiene traccia di tutte le spedizioni attive (la chiave è l'id della spedizione)

    public DroneEventConsumer(Vertx vertx, UpdateShipmentStatus updateShipmentStatus, Map<String, Shipment> shipments) {
        this.updateShipmentStatus = updateShipmentStatus;
        this.shipments = shipments;
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS"));
        config.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        config.put("group.id", "delivery-management-group");
        config.put("auto.offset.reset", "earliest");
        this.consumer = KafkaConsumer.create(vertx, config);
        this.consumer.subscribe(TOPIC);
        this.consumer.handler(record -> scheduleShipment(record.value()));
    }

    //aggiorna lo stato della richiesta in "scheduled"
    private void scheduleShipment(String message) {
        JSONObject event = new JSONObject(message);
        String shipmentId = event.getString("shipmentId");
        if (!shipments.containsKey(shipmentId)) {
            Position droneInitialPosition = new Position(event.getDouble("droneLatitude"), event.getDouble("droneLongitude"));
            Position pickupPosition = new Position(event.getDouble("pickupLatitude"), event.getDouble("pickupLongitude"));
            Position deliveryPosition = new Position(event.getDouble("deliveryLatitude"), event.getDouble("deliveryLongitude"));
            long assignedAt = event.getLong("assignedAt");
            double deliverySpeed = event.getDouble("droneSpeed");

            Shipment shipment = new Shipment(shipmentId, droneInitialPosition, pickupPosition, deliveryPosition, assignedAt, deliverySpeed);
            shipments.put(shipmentId, shipment);
            updateShipmentStatus.update(shipment, ShipmentStatus.SCHEDULED);
        }
    }
}