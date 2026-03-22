package delivery_management.application;

import buildingblocks.application.InboundPort;
import delivery_management.domain.Position;
import delivery_management.domain.Shipment;

@InboundPort
public interface UpdateDronePosition {
    void update(Shipment shipment, Position newPosition);
}