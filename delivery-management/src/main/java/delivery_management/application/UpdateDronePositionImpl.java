package delivery_management.application;

import delivery_management.domain.Position;
import delivery_management.domain.Shipment;

public class UpdateDronePositionImpl implements UpdateDronePosition {

    @Override
    public void update(Shipment shipment, Position newPosition) {
        shipment.updateDronePosition(newPosition);
    }
}