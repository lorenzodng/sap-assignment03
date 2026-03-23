package request_management.application;

import buildingblocks.application.InboundPort;
import request_management.domain.Package;
import request_management.domain.User;
import request_management.domain.Position;
import request_management.domain.Shipment;
import java.time.LocalDate;
import java.time.LocalTime;

@InboundPort
public interface CreateShipmentRequest {
    Shipment create(String id, User user, Position pickupLocation, Position deliveryLocation, LocalDate pickupDate, LocalTime pickupTime, int deliveryTimeLimit, Package pack);
}