package delivery_management.domain;

import buildingblocks.domain.AggregateRoot;

//questo è un esempio della proprietà di modello indipendente del bounded context: Shipment di questo microservizio è diverso da Shipment del gestore richieste

public class Shipment implements AggregateRoot<String> {

    private final String id;
    private final Position droneInitialPosition;
    private final Position pickupPosition;
    private final Position deliveryPosition;
    private final long assignedAt;
    private ShipmentStatus status;
    private final double deliverySpeed;

    public Shipment(String id, Position droneInitialPosition, Position pickupPosition, Position deliveryPosition, long assignedAt, double deliverySpeed) {
        this.id = id;
        this.droneInitialPosition = droneInitialPosition;
        this.pickupPosition = pickupPosition;
        this.deliveryPosition = deliveryPosition;
        this.assignedAt = assignedAt;
        this.status = ShipmentStatus.SCHEDULED;
        this.deliverySpeed = deliverySpeed;
    }

    //calcola la posizione attuale del drone
    public Position calculateCurrentDronePosition() {
        double elapsedHours = (System.currentTimeMillis() - assignedAt) / 3600000.0; //calcolo le ore trascorse dall'assegnazione del drone
        double distanceCovered = deliverySpeed * elapsedHours; //calcola la distanza percorsa dal drone

        //prima fase: drone si muove verso il luogo di ritiro
        double distanceToPickup = calculateDistance(droneInitialPosition, pickupPosition); //calcola la distanza dalla base del drone al luogo di ritiro
        if (distanceCovered < distanceToPickup) { //se la distanza percorsa è minore della distanza verso il ritiro (il drone è in viaggio)
            return interpolate(droneInitialPosition, pickupPosition, distanceCovered / distanceToPickup); //calcola la posizione
        }

        //seconda fase: drone si muove verso la destinazione
        double distanceCovered2 = distanceCovered - distanceToPickup; //aggiorno la distanza ignorando quella già percorsa verso il ritiro
        double distanceToDelivery = calculateDistance(pickupPosition, deliveryPosition); //calcola la distanza dal luogo di ritiro al luogo di destinazione
        if (distanceCovered2 < distanceToDelivery) { //se la distanza percorsa è minore della distanza verso la destinazione (il drone è in viaggio)
            return interpolate(pickupPosition, deliveryPosition, distanceCovered2 / distanceToDelivery); //calcola la posizione
        }

        // drone arrivato a destinazione
        return deliveryPosition;
    }

    //calcola la posizione intermedia tra due punti
    private Position interpolate(Position from, Position to, double fraction) {
        double lat = from.getLatitude() + (to.getLatitude() - from.getLatitude()) * fraction;
        double lon = from.getLongitude() + (to.getLongitude() - from.getLongitude()) * fraction;
        return new Position(lat, lon);
    }

    //calcola la distanza tra due punti
    private double calculateDistance(Position p1, Position p2) {
        double latDiff = p1.getLatitude() - p2.getLatitude();
        double lonDiff = p1.getLongitude() - p2.getLongitude();
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff);
    }

    //calcola il tempo rimanente alla consegna
    public double calculateRemainingTime() {
        double elapsedHours = (System.currentTimeMillis() - assignedAt) / 3600000.0; //calcola le ore trascorse dall'assegnazione del drone
        double distanceCovered = deliverySpeed * elapsedHours; //calcola la distanza totale percorsa dal drone
        double totalDistance = calculateDistance(droneInitialPosition, pickupPosition) + calculateDistance(pickupPosition, deliveryPosition); //calcola la distanza totale che il drone deve percorrere (base->ritiro + ritiro->destinazione)
        double remainingDistance = Math.max(0, totalDistance - distanceCovered); //calcola la distanza rimanente (distanza totale - distanza già percorsa)
        return (remainingDistance / deliverySpeed) * 60; //converte la distanza rimanente in minuti
    }

    @Override
    public String getId() {
        return id;
    }
    public ShipmentStatus getStatus() {
        return status;
    }
    public void updateStatus(ShipmentStatus newStatus) {
        this.status = newStatus;
    }
}