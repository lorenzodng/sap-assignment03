package drone_management;

import io.github.cdimascio.dotenv.Dotenv;
import io.vertx.core.Vertx;
import drone_management.application.AssignDroneImpl;
import drone_management.application.CheckDroneAvailabilityImpl;
import drone_management.domain.Drone;
import drone_management.domain.Position;
import drone_management.infrastructure.DroneEventProducer;
import drone_management.infrastructure.ShipmentRequestedEventConsumer;
import java.util.ArrayList;
import java.util.List;

public class DroneManagementMain {

    public static void main(String[] args) {
        Dotenv.load();

        Vertx vertx = Vertx.vertx();

        // crea i use case
        CheckDroneAvailabilityImpl checkDroneAvailability = new CheckDroneAvailabilityImpl();
        AssignDroneImpl assignDrone = new AssignDroneImpl(checkDroneAvailability);

        // crea la flotta di droni
        List<Drone> drones = new ArrayList<>();
        drones.add(new Drone("drone-1", new Position(45.46, 9.19)));
        drones.add(new Drone("drone-2", new Position(45.47, 9.20)));
        drones.add(new Drone("drone-3", new Position(45.48, 9.21)));

        // crea il producer Kafka
        DroneEventProducer eventProducer = new DroneEventProducer(vertx);

        // crea il consumer Kafka
        new ShipmentRequestedEventConsumer(vertx, assignDrone, drones, eventProducer);
    }
}