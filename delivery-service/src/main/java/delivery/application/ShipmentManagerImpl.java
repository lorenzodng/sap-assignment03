package delivery.application;

import delivery.domain.Position;
import delivery.domain.Shipment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShipmentManagerImpl implements ShipmentManager {

    private static final Logger log = LoggerFactory.getLogger(ShipmentManagerImpl.class);
    private final ShipmentRepository repository;

    public ShipmentManagerImpl(ShipmentRepository repository) {
        this.repository = repository;
    }

    /*
    crea la spedizione scheduled o cancelled
    sono passati i valori "di base" perchè, essendo il metodo richiamato da ShipmentAssignment al livello infrastructure, non dovrebbe creare elementi di dominio (e quindi Position)
    */
    @Override
    public void createShipmentFromAssignment(String id, boolean assigned, Double droneLat, Double droneLon, Double pickupLat, Double pickupLon, Double deliveryLat, Double deliveryLon, Long assignedAt, Double speed) {
        Shipment shipment = new Shipment(id, new Position(droneLat, droneLon), new Position(pickupLat, pickupLon), new Position(deliveryLat, deliveryLon), assignedAt, speed);
        if (assigned) {
            log.info("Shipment {} scheduled", id);
        } else {
            shipment.cancelled();
            log.info("Shipment {} cancelled", id);
        }
        repository.save(shipment);
    }

    //recupera le informazioni di una spedizione
    @Override
    public Shipment getShipmentDetails(String id) {
        return repository.findById(id).orElse(null);
    }
}