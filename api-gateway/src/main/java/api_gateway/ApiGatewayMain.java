package api_gateway;

import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import api_gateway.infrastructure.ApiGatewayController;

public class ApiGatewayMain {

    public static void main(String[] args) {
        Dotenv.load(); //carica le variabili del file .env
        int port = Integer.parseInt(System.getenv("PORT"));

        Vertx vertx = Vertx.vertx();

        //crea il controller
        ApiGatewayController apiGatewayController = new ApiGatewayController(vertx);

        //crea il router e registra le rotte
        Router router = Router.router(vertx);
        apiGatewayController.registerRoutes(router);

        //avvia il server HTTP
        vertx.createHttpServer().requestHandler(router).listen(port);
    }
}