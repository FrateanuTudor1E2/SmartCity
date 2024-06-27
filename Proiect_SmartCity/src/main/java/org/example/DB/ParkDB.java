package org.example.DB;

import java.sql.*;
import org.example.Classes.Car;
import org.example.Classes.ParkingSpace;
import org.example.Interfaces.Parkable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ParkDB implements Parkable {
    private static final String URL = "jdbc:mysql://localhost:3306/SmartCity";
    private static final String USER = "root";
    private static final String PASSWORD = "1234";

    private static Connection connection = null;
    private static PreparedStatement statement = null;
    private static ResultSet resultSet = null;

    public static void ConnectToDB() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to the database");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ShowTable is used only for debugging purposes
    public static void ShowTable() throws SQLException {
        statement = connection.prepareStatement("SELECT * FROM ParkingLots");
        resultSet = statement.executeQuery();
        while (resultSet.next()) {
            System.out.println(resultSet.getInt(1) + " " +
                    resultSet.getString(2) + " " +
                    resultSet.getString(3));
        }
    }

    public static void InsertIntoTable(ParkingSpace parkingSpace) throws SQLException {
        statement = connection.prepareStatement("INSERT INTO ParkingLots (idParkingLots, LicensePlate, ParkTimeIn) VALUES (?, ?, ?)");
        statement.setInt(1, parkingSpace.getId());
        statement.setString(2, parkingSpace.getCar().getLicensePlate());
        statement.setString(3, parkingSpace.getEntryTime());
        statement.executeUpdate();
        System.out.println("Row inserted successfully");
    }

    public static boolean isLicensePlatePresent(String licensePlate) throws SQLException {
        statement = connection.prepareStatement("SELECT COUNT(*) FROM ParkingLots WHERE LicensePlate = ?");
        statement.setString(1, licensePlate);
        resultSet = statement.executeQuery();
        resultSet.next();
        return resultSet.getInt(1) > 0;
    }

    public static ResultSet getCarDetails(String licensePlate) throws SQLException {
        statement = connection.prepareStatement("SELECT * FROM ParkingLots WHERE LicensePlate = ?");
        statement.setString(1, licensePlate);
        resultSet = statement.executeQuery();
        return resultSet;
    }

    public static ResultSet getCarDetailsByLotID(int lotID) throws SQLException {
        statement = connection.prepareStatement("SELECT * FROM ParkingLots WHERE idParkingLots = ?");
        statement.setInt(1, lotID);
        resultSet = statement.executeQuery();
        return resultSet;
    }

    public static void removeCar(String licensePlate) throws SQLException {
        statement = connection.prepareStatement("DELETE FROM ParkingLots WHERE LicensePlate = ?");
        statement.setString(1, licensePlate);
        statement.executeUpdate();
    }

    public static ResultSet getParkingStatus() throws SQLException {
        statement = connection.prepareStatement("SELECT idParkingLots, LicensePlate IS NOT NULL AS occupied FROM ParkingLots");
        resultSet = statement.executeQuery();
        return resultSet;
    }

    @Override
    public void parkCar(Car car, int spaceId, String entryTime) {
        try {
            ParkingSpace parkingSpace = new ParkingSpace(spaceId);
            parkingSpace.parkCar(car, spaceId, entryTime);
            if (isLicensePlatePresent(car.getLicensePlate())) {
                System.out.println("License plate already exists in the parking lot.");
            } else {
                InsertIntoTable(parkingSpace);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeCar(Car car) {
        try {
            removeCar(car.getLicensePlate());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
