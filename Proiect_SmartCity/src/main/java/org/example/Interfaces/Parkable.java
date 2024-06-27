package org.example.Interfaces;

import org.example.Classes.Car;
import org.example.Classes.ParkingSpace;

public interface Parkable {
    void parkCar(Car car, int SpaceId, String entryTime);
    void removeCar(Car car);
}
