package request_management.application;

import request_management.domain.Package;
import request_management.domain.Position;
import request_management.domain.Shipment;
import java.time.LocalDate;
import java.time.LocalTime;

public class CreateShipmentRequestImpl implements CreateShipmentRequest {

    @Override
    public Shipment create(String id, Position pickupLocation, Position deliveryLocation, LocalDate pickupDate, LocalTime pickupTime, int deliveryTimeLimit, Package pack) {
        return new Shipment(id, pickupLocation, deliveryLocation, pickupDate, pickupTime, deliveryTimeLimit, pack);
    }
}