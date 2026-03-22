package delivery_management.application;

import buildingblocks.application.InboundPort;
import delivery_management.domain.Shipment;
import delivery_management.domain.ShipmentStatus;

@InboundPort
public interface UpdateShipmentStatus {
    void update(Shipment shipment, ShipmentStatus newStatus);
}