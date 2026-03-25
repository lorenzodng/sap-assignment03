package delivery_management;

import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import delivery_management.domain.Shipment;
import delivery_management.infrastructure.DroneAvailableEventConsumer;
import delivery_management.infrastructure.DroneNotAvailableEventConsumer;
import delivery_management.infrastructure.ShipmentCreatedEventConsumer;
import delivery_management.infrastructure.TrackingDeliveryController;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeliveryManagementMain {

    private static final Logger log = LoggerFactory.getLogger(DeliveryManagementMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("delivery-management").load(); //carica le variabili del file .env
        String bootstrap = dotenv.get("KAFKA_BOOTSTRAP_SERVERS"); //legge il campo

        int port = Integer.parseInt(System.getenv("PORT"));

        Vertx vertx = Vertx.vertx();

        //crea i consumer Kafka
        Map<String, Shipment> shipments = new HashMap<>();
        new DroneAvailableEventConsumer(vertx, bootstrap, shipments);
        new DroneNotAvailableEventConsumer(vertx, bootstrap, shipments);
        new ShipmentCreatedEventConsumer(vertx, bootstrap, shipments);

        //crea il controller REST
        TrackingDeliveryController trackingController = new TrackingDeliveryController(shipments);

        //crea il router e registra le rotte
        Router router = Router.router(vertx);
        trackingController.registerRoutes(router);

        //avvia il server HTTP
        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("DeliveryManagement microservice started");
    }
}