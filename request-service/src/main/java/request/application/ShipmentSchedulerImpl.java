package request.application;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import request.domain.Shipment;
import java.time.Duration;
import java.time.LocalDateTime;

public class ShipmentSchedulerImpl implements ShipmentScheduler {

    private final ShipmentRequestEventProducer eventProducer;
    private final Vertx vertx;

    public ShipmentSchedulerImpl(ShipmentRequestEventProducer eventProducer, Vertx vertx) {
        this.eventProducer = eventProducer;
        this.vertx = vertx;
    }

    @Override
    public Future<Void> schedule(Shipment shipment) {
        LocalDateTime pickupDateTime = LocalDateTime.of(shipment.getPickupDate(), shipment.getPickupTime());
        long delayMs = Duration.between(LocalDateTime.now(), pickupDateTime).toMillis();

        //se la data/ora è già passata o è adesso, il drone parte
        if (delayMs <= 0) {
            return eventProducer.publishShipmentRequested(shipment);
        } else {//altrimenti attende
            vertx.setTimer(delayMs, id -> eventProducer.publishShipmentRequested(shipment));
            return Future.succeededFuture();
        }
    }
}