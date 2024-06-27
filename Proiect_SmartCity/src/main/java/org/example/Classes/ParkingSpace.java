package org.example.Classes;

import org.example.Interfaces.Parkable;

public class ParkingSpace implements Parkable {
    private int id;
    private boolean occupied;
    private Car car;
    private String entryTime;

    public ParkingSpace(int id) {
        this.id = id;
        this.occupied = false;
    }

    public int getId() {
        return id;
    }

    public boolean isOccupied() {
        return occupied;
    }

    public Car getCar() {
        return car;
    }

    public String getEntryTime() {
        return entryTime;
    }

    @Override
    public void parkCar(Car car, int spaceId, String entryTime) {
        if (this.id == spaceId && !this.occupied) {
            this.car = car;
            this.entryTime = entryTime;
            this.occupied = true;
            System.out.println("DEBUG: Car " + car.getLicensePlate() + " parked in lot " + spaceId + " at " + entryTime);
        } else {
            throw new IllegalArgumentException("This parking space is occupied.");
        }
    }

    @Override
    public void removeCar(Car car) {
        if (this.car != null && this.car.getLicensePlate().equals(car.getLicensePlate())) {
            System.out.println("DEBUG: Removing car " + car.getLicensePlate() + " from lot " + id);
            this.car = null;
            this.entryTime = null;
            this.occupied = false;
        } else {
            throw new IllegalArgumentException("Car not found");
        }
    }

    public boolean hasCar(Car car) {
        boolean hasCar = this.car != null && this.car.getLicensePlate().equals(car.getLicensePlate());
        System.out.println("DEBUG: Checking if lot " + id + " has car " + car.getLicensePlate() + ": " + hasCar);
        return hasCar;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }
}
