package request_management;

import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import request_management.application.CreateShipmentRequestImpl;
import request_management.application.ValidateShipmentRequestImpl;
import request_management.infrastructure.ShipmentEventProducer;
import request_management.infrastructure.ShipmentRequestController;

public class RequestManagementMain {

    public static void main(String[] args) {
        Dotenv.load(); //carica le variabili del file .env
        int port = Integer.parseInt(System.getenv("PORT"));

        Vertx vertx = Vertx.vertx();

        //crea i use case
        CreateShipmentRequestImpl createShipmentRequest = new CreateShipmentRequestImpl();
        ValidateShipmentRequestImpl validateShipmentRequest = new ValidateShipmentRequestImpl();

        //crea il producer Kafka
        ShipmentEventProducer eventProducer = new ShipmentEventProducer(vertx);

        //crea il controller REST
        ShipmentRequestController controller = new ShipmentRequestController(createShipmentRequest, validateShipmentRequest, eventProducer);

        //crea il router e registra la rotta
        Router router = Router.router(vertx);
        controller.registerRoutes(router);

        //avvia il server HTTP
        vertx.createHttpServer().requestHandler(router).listen(port);
    }
}