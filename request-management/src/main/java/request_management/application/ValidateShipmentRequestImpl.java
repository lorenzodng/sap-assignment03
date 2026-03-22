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

        // verifica che le posizioni siano valide
        if (shipment.getPickupLocation() == null || shipment.getDeliveryLocation() == null) {
            return false;
        }

        return true;
    }
}