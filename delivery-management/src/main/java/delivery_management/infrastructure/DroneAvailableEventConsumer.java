package delivery_management.infrastructure;

import buildingblocks.infrastructure.Adapter;
import delivery_management.domain.Position;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.json.JSONObject;
import delivery_management.domain.Shipment;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//recupera l'evento di assegnazione drone pubblicato dal gestore droni
@Adapter
public class DroneAvailableEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(DroneAvailableEventConsumer.class);
    private static final String TOPIC = "drone-assigned";
    private final KafkaConsumer<String, String> consumer;
    private final Map<String, Shipment> shipments; //mappa che tiene traccia di tutte le spedizioni attive (la chiave è l'id della spedizione)

    public DroneAvailableEventConsumer(Vertx vertx, String bootstrapServers, Map<String, Shipment> shipments) {
        this.shipments = shipments;
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", bootstrapServers);
        config.put("key.deserializer", "org.apache.kafka.com mon.serialization.StringDeserializer");
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
        Shipment shipment = shipments.get(shipmentId);
        if (shipment != null) {
            Position droneInitialPosition = new Position(event.getDouble("droneLatitude"), event.getDouble("droneLongitude"));
            Position pickupPosition = new Position(event.getDouble("pickupLatitude"), event.getDouble("pickupLongitude"));
            Position deliveryPosition = new Position(event.getDouble("deliveryLatitude"), event.getDouble("deliveryLongitude"));
            long assignedAt = event.getLong("assignedAt");
            double deliverySpeed = event.getDouble("droneSpeed");
            shipment.schedule(droneInitialPosition, pickupPosition, deliveryPosition, assignedAt, deliverySpeed);
            log.info("Shipment {} scheduled", shipmentId);
        }
    }
}