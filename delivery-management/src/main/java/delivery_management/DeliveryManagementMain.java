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

public class DeliveryManagementMain {

    public static void main(String[] args) {
        Dotenv.load(); //carica le variabili del file .env
        int port = Integer.parseInt(System.getenv("PORT"));

        Vertx vertx = Vertx.vertx();

        //crea i consumer Kafka
        Map<String, Shipment> shipments = new HashMap<>();
        new DroneAvailableEventConsumer(vertx, shipments);
        new DroneNotAvailableEventConsumer(vertx, shipments);
        new ShipmentCreatedEventConsumer(vertx, shipments);

        //crea il controller REST
        TrackingDeliveryController trackingController = new TrackingDeliveryController(shipments);

        //crea il router e registra le rotte
        Router router = Router.router(vertx);
        trackingController.registerRoutes(router);

        //avvia il server HTTP
        vertx.createHttpServer().requestHandler(router).listen(port);
    }
}