package request_management.application;

import request_management.domain.Shipment;

//verifica che i dati della richiesti siano validi (non che la consegna sia fattibile)
public class ValidateShipmentRequestImpl implements ValidateShipmentRequest {

    @Override
    public boolean validate(Shipment shipment) {
        // verifica che il peso del pacco sia maggiore di zero
        if (shipment.getPackage().getWeight() <= 0) {
            return false;
        }

        // verifica che il limite di tempo sia maggiore di zero
        if (shipment.getDeliveryTimeLimit() <= 0) {
            return false;
        }

        // verifica che le coordinate siano valide
        if (shipment.getPickupLocation().getLatitude() < -90 || shipment.getPickupLocation().getLatitude() > 90 || shipment.getPickupLocation().getLongitude() < -180 || shipment.getPickupLocation().getLongitude() > 180) {
            return false;
        }

        if (shipment.getDeliveryLocation().getLatitude() < -90 || shipment.getDeliveryLocation().getLatitude() > 90 || shipment.getDeliveryLocation().getLongitude() < -180 || shipment.getDeliveryLocation().getLongitude() > 180) {
            return false;
        }

        return true;
    }
}