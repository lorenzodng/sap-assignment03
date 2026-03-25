package api_gateway.infrastructure;

import buildingblocks.infrastructure.Adapter;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.handler.BodyHandler;

@Adapter
public class ApiGatewayController {

    private final WebClient client;
    private final String requestManagementUrl;
    private final String deliveryManagementUrl;

    public ApiGatewayController(Vertx vertx, String requestManagementUrl, String deliveryManagementUrl) {
        this.client = WebClient.create(vertx);
        this.requestManagementUrl = requestManagementUrl;
        this.deliveryManagementUrl = deliveryManagementUrl;
    }

    //registra le rotte che il client può richiamare
    public void registerRoutes(Router router) {
        router.post("/shipments").handler(BodyHandler.create()).handler(this::createShipment);
        router.get("/shipments/:id/status").handler(this::getShipmentStatus);
        router.get("/shipments/:id/position").handler(this::getDronePosition);
        router.get("/shipments/:id/remaining-time").handler(this::getRemainingTime);
    }

    //inoltra la richiesta di creazione spedizione a request-management
    private void createShipment(RoutingContext ctx) {
        /*
        1) crea una richiesta post verso il microservizio request-management a uno specifico indirizzo
        2) se la chiamata ha successo, recupera il codice di stato e invia al client il body come stringa
        3) se fallisce, invia al client un messaggio di errore
         */
        client.postAbs(requestManagementUrl + "/shipments").sendBuffer(Buffer.buffer(ctx.body().asString())).onSuccess(response -> {
            ctx.response().setStatusCode(response.statusCode()).end(response.bodyAsString());
        }).onFailure(err -> {
            ctx.response().setStatusCode(500).end("Error forwarding request");
        });
    }

    //inoltra la richiesta di tracking a delivery-management
    private void getShipmentStatus(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        client.getAbs(deliveryManagementUrl + "/shipments/" + id + "/status").send().onSuccess(response -> {
            ctx.response().setStatusCode(response.statusCode()).end(response.bodyAsString());
        }).onFailure(err -> {
            ctx.response().setStatusCode(500).end("Error forwarding request");
        });
    }

    //inoltra la richiesta di posizione a delivery-management
    private void getDronePosition(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        client.getAbs(deliveryManagementUrl + "/shipments/" + id + "/position").send().onSuccess(response -> {
            ctx.response().setStatusCode(response.statusCode()).end(response.bodyAsString());
        }).onFailure(err -> {
            ctx.response().setStatusCode(500).end("Error forwarding request");
        });
    }

    //inoltra la richiesta di tempo rimanente a delivery-management
    private void getRemainingTime(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        client.getAbs(deliveryManagementUrl + "/shipments/" + id + "/remaining-time").send().onSuccess(response -> {
            ctx.response().setStatusCode(response.statusCode()).end(response.bodyAsString());
        }).onFailure(err -> {
            ctx.response().setStatusCode(500).end("Error forwarding request");
        });
    }
}