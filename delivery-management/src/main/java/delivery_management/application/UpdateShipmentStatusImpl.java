package delivery_management.application;

import delivery_management.domain.Shipment;
import delivery_management.domain.ShipmentStatus;

public class UpdateShipmentStatusImpl implements UpdateShipmentStatus {

    @Override
    public void update(Shipment shipment, ShipmentStatus newStatus) {
        shipment.updateStatus(newStatus);
    }
}