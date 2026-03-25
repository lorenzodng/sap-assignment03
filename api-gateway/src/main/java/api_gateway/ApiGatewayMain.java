package api_gateway;

import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import api_gateway.infrastructure.ApiGatewayController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiGatewayMain {

    private static final Logger log = LoggerFactory.getLogger(ApiGatewayMain.class);

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().directory("api-gateway").load(); //carica il .env
        String requestManagementUrl = dotenv.get("REQUEST_MANAGEMENT_URL"); //legge il primo url
        String deliveryManagementUrl = dotenv.get("DELIVERY_MANAGEMENT_URL"); //legge il secondo url

        int port = Integer.parseInt(System.getenv("PORT"));

        Vertx vertx = Vertx.vertx();

        //crea il controller
        ApiGatewayController apiGatewayController = new ApiGatewayController(vertx, requestManagementUrl, deliveryManagementUrl);

        //crea il router e registra le rotte
        Router router = Router.router(vertx);
        apiGatewayController.registerRoutes(router);

        //avvia il server HTTP
        vertx.createHttpServer().requestHandler(router).listen(port);

        log.info("ApiGateway microservice started");
    }
}