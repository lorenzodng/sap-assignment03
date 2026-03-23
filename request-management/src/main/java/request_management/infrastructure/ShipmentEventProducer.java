package request_management.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;
import org.json.JSONObject;
import request_management.domain.Shipment;
import java.util.HashMap;
import java.util.Map;

//producer kafka che pubblica gli eventi di richiesta spedizione
@Adapter
public class ShipmentEventProducer {

    private static final String TOPIC_REQUESTED = "shipment-requested"; //nome del topic su cui sono pubblicati gli eventi di richieste di spedizione
    private static final String TOPIC_CREATED = "shipment-created";
    private final KafkaProducer<String, String> producer; //producer kafka che invia gli eventi

    public ShipmentEventProducer(Vertx vertx) {
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", System.getenv("KAFKA_BOOTSTRAP_SERVERS")); //legge dal file .env
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer"); //la chiave dell'evento è in formato stinga
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer"); //il valore dell'evento è in formato stringa
        this.producer = KafkaProducer.create(vertx, config); //crea il producer kafka
    }

    //pubblica l'evento di richiesta spedizione verso drone-management
    public void publishShipmentRequested(Shipment shipment) {

        //costruisce l'evento
        JSONObject event = new JSONObject();
        event.put("shipmentId", shipment.getId());
        event.put("pickupLatitude", shipment.getPickupLocation().getLatitude());
        event.put("pickupLongitude", shipment.getPickupLocation().getLongitude());
        event.put("deliveryLatitude", shipment.getDeliveryLocation().getLatitude());
        event.put("deliveryLongitude", shipment.getDeliveryLocation().getLongitude());
        event.put("packageWeight", shipment.getPackage().getWeight());
        event.put("deliveryTimeLimit", shipment.getDeliveryTimeLimit());

        KafkaProducerRecord<String, String> record = KafkaProducerRecord.create(TOPIC_REQUESTED, shipment.getId(), event.toString()); //crea l'evento
        producer.send(record); //pubblica l'evento
    }

    //pubblica l'evento di creazione richiesta spedizione verso delivery-management
    public void publishShipmentCreated(String shipmentId) {
        JSONObject event = new JSONObject();
        event.put("shipmentId", shipmentId);
        KafkaProducerRecord<String, String> record = KafkaProducerRecord.create(TOPIC_CREATED, shipmentId, event.toString());
        producer.send(record);
    }
}