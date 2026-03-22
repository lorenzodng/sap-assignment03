package delivery_management.infrastructure;

import buildingblocks.infrastructure.Adapter;
import delivery_management.domain.Position;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import delivery_management.domain.Shipment;
import org.json.JSONObject;
import java.util.Map;

//traccia la spedizione (stato e posizione del drone)
@Adapter
public class TrackingDeliveryController {

    private final Map<String, Shipment> shipments; //mappa che tiene traccia di tutte le spedizioni attive (la chiave è l'id della spedizione)

    public TrackingDeliveryController(Map<String, Shipment> shipments) {
        this.shipments = shipments;
    }

    //registra le rotte
    public void registerRoutes(Router router) {
        router.get("/shipments/:id/status").handler(this::getShipmentStatus);
        router.get("/shipments/:id/position").handler(this::getDronePosition);
        router.get("/shipments/:id/remaining-time").handler(this::getRemainingTime);
    }

    //recupera lo stato della spedizione
    private void getShipmentStatus(RoutingContext ctx) {
        String id = ctx.pathParam("id"); //estrae l'id dall'url del messaggio http
        Shipment shipment = shipments.get(id); //recupera la spedizione dalla mappa
        if (shipment != null) {
            ctx.response().setStatusCode(200).putHeader("Content-Type", "application/json").end(shipment.getStatus().name()); //costruisce il messaggio di risposta e lo invia all'api-gateway
        } else {
            ctx.response().setStatusCode(404).end("Shipment not found");
        }
    }

    //recupera la posizione del drone
    private void getDronePosition(RoutingContext ctx) {
        String id = ctx.pathParam("id"); //estrae l'id dall'url del messaggio http
        Shipment shipment = shipments.get(id); //recupera la spedizione dalla mappa
        if (shipment != null) {
            Position currentPosition = shipment.calculateCurrentDronePosition();

            //costruisce il messaggio json
            JSONObject position = new JSONObject();
            position.put("latitude", currentPosition.getLatitude());
            position.put("longitude", currentPosition.getLongitude());
            ctx.response().setStatusCode(200).putHeader("Content-Type", "application/json").end(position.toString()); //costruisce il messaggio di risposta e lo invia all'api-gateway
        } else {
            ctx.response().setStatusCode(404).end("Position not found");
        }
    }

    //recupera il tempo rimanente alla consegna
    private void getRemainingTime(RoutingContext ctx) {
        String id = ctx.pathParam("id"); //estrae l'id dall'url del messaggio http
        Shipment shipment = shipments.get(id); //recupera la spedizione dalla mappa
        if (shipment != null) { //se la spedizione esiste
            double remainingMinutes = shipment.calculateRemainingTime(); //calcola il tempo rimanente

            //costruisce il messaggio json
            JSONObject response = new JSONObject();
            response.put("remainingMinutes", remainingMinutes);
            ctx.response().setStatusCode(200).putHeader("Content-Type", "application/json").end(response.toString()); //costruisce il messaggio di risposta e lo invia all'api-gateway
        } else {
            ctx.response().setStatusCode(404).end("Shipment not found");
        }
    }
}