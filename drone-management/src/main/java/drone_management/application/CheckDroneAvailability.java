package drone_management.application;

import buildingblocks.application.InboundPort;
import drone_management.domain.Drone;

@InboundPort
public interface CheckDroneAvailability {
    boolean check(Drone drone, double packageWeight, double distanceDroneToPickup, double distancePickupToDelivery, int deliveryTimeLimit);
}