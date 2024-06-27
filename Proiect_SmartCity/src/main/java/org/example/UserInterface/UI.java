package org.example.UserInterface;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.Classes.Car;
import org.example.Classes.ParkingSpace;
import org.example.DB.ParkDB;
import org.example.Interfaces.Parkable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UI extends Application {

    public Map<Integer, Circle> statusCircles = new HashMap<>();
    private TextField licensePlateField;
    private TextField checkoutField;
    private Parkable parkDB;
    private Map<Integer, ParkingSpace> parkingSpaces = new HashMap<>();

    @Override
    public void start(Stage stage) {
        Pane pane = new Pane();

        int numberOfLotsPerRow = 10;
        double lotSizeH = 100;
        double lotSizeW = 60;

        double totalWidth = lotSizeW * numberOfLotsPerRow;
        double totalHeight = lotSizeH * numberOfLotsPerRow * 0.8;

        parkDB = new ParkDB();
        ParkDB.ConnectToDB();
        initializeParkingSpacesFromDB();

        int LotID = 1;
        for (int i = 0; i < numberOfLotsPerRow; i++) {
            for (int j = 0; j < numberOfLotsPerRow; j += 2) {
                Rectangle parkingLot = new Rectangle();
                parkingLot.setX(i * lotSizeW);
                parkingLot.setY(j * lotSizeH);
                parkingLot.setWidth(lotSizeW);
                parkingLot.setHeight(lotSizeH);
                parkingLot.setId(String.valueOf(LotID));
                parkingLot.setStyle("-fx-fill: white; -fx-stroke: black; -fx-stroke-width: 1;");

                Text lotIDText = new Text(String.valueOf(LotID));
                lotIDText.setFont(new Font("Arial", 16));
                lotIDText.setFill(Color.BLACK);
                lotIDText.setTextAlignment(TextAlignment.CENTER);
                lotIDText.setX(parkingLot.getX() + (lotSizeW - lotIDText.getLayoutBounds().getWidth()) / 2);
                lotIDText.setY(parkingLot.getY() + (lotSizeH - lotIDText.getLayoutBounds().getHeight()) / 2 - 20);

                Circle status = new Circle();
                status.setCenterX(parkingLot.getX() + lotSizeW / 2);
                status.setCenterY(parkingLot.getY() + lotSizeH / 2);
                status.setRadius(20);
                status.setFill(Color.GREEN);
                status.setOnMouseClicked(event -> handleParkingLotClick(Integer.parseInt(parkingLot.getId())));
                pane.getChildren().addAll(parkingLot, lotIDText, status);
                statusCircles.put(LotID, status);

                LotID++;
            }
        }

        double fieldWidth = 200;
        double buttonWidth = 100;
        double spacing = 40;

        licensePlateField = new TextField();
        licensePlateField.setPromptText("Enter license plate");
        licensePlateField.setPrefWidth(fieldWidth);
        licensePlateField.setLayoutX(totalWidth - fieldWidth + 500);
        licensePlateField.setLayoutY(totalHeight - 500);
        pane.getChildren().add(licensePlateField);

        Button checkInButton = new Button("Check In");
        checkInButton.setLayoutX(totalWidth - buttonWidth + 400);
        checkInButton.setLayoutY(totalHeight - 500 + spacing);
        checkInButton.setOnAction(event -> handleCheckIn());
        pane.getChildren().add(checkInButton);

        checkoutField = new TextField();
        checkoutField.setPromptText("Enter license plate to check out");
        checkoutField.setPrefWidth(fieldWidth);
        checkoutField.setLayoutX(totalWidth - fieldWidth + 500);
        checkoutField.setLayoutY(totalHeight - 500 + 2 * spacing);
        pane.getChildren().add(checkoutField);

        Button checkOutButton = new Button("Check Out");
        checkOutButton.setLayoutX(totalWidth - buttonWidth + 400);
        checkOutButton.setLayoutY(totalHeight - 500 + 3 * spacing);
        checkOutButton.setOnAction(event -> handleCheckOut());
        pane.getChildren().add(checkOutButton);

        Scene scene = new Scene(pane, 1500.0, totalHeight + 100.0);
        stage.setScene(scene);
        stage.setTitle("Smart City");
        stage.show();

        updateParkingStatusVisuals();

    }

    private void initializeParkingSpacesFromDB() {
        try {
            System.out.println("DEBUG: Initializing parking spaces from database...");
            int totalLots = 50;
            for (int lotID = 1; lotID <= totalLots; lotID++) {
                ParkingSpace parkingSpace = new ParkingSpace(lotID);
                parkingSpaces.put(lotID, parkingSpace);
                System.out.println("DEBUG: Initialized empty ParkingSpace ID " + lotID);
            }
            ResultSet resultSet = ParkDB.getParkingStatus();
            while (resultSet.next()) {
                int lotID = resultSet.getInt("idParkingLots");
                boolean isOccupied = resultSet.getBoolean("occupied");
                ParkingSpace parkingSpace = new ParkingSpace(lotID);

                if (isOccupied) {
                    ResultSet carDetails = ParkDB.getCarDetailsByLotID(lotID);
                    if (carDetails.next()) {
                        Car car = new Car(carDetails.getString("LicensePlate"));
                        String entryTime = carDetails.getString("ParkTimeIn");
                        parkingSpace.parkCar(car, lotID, entryTime);
                        parkingSpace.setOccupied(true);
                        System.out.println("DEBUG: Parking lot " + lotID + " is occupied by car " + car.getLicensePlate());
                    }
                } else {
                    System.out.println("DEBUG: Parking lot " + lotID + " is not occupied.");
                }

                parkingSpaces.put(lotID, parkingSpace);
                System.out.println("DEBUG: Added ParkingSpace ID " + lotID + " to map: " + parkingSpace);
            }

            System.out.println("DEBUG: Parking spaces after initialization: " + parkingSpaces);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleParkingLotClick(int lotID) {
        String licensePlate = licensePlateField.getText();
        if (licensePlate == null || licensePlate.isEmpty()) {
            showMessage("Please enter a license plate before selecting a parking space.");
            return;
        }

        if (statusCircles.get(lotID).getFill() == Color.RED) {
            showMessage("This parking space is already occupied.");
            return;
        }

        licensePlate = licensePlate.toUpperCase();
        String currentTime = getCurrentTime();
        Car car = new Car(licensePlate);
        ParkingSpace parkingSpace = parkingSpaces.get(lotID);


        if (parkingSpace == null) {
            System.out.println("DEBUG: ParkingSpace for lotID " + lotID + " is null. Check initialization.");
        } else {
            System.out.println("DEBUG: ParkingSpace for lotID " + lotID + " is not null. Proceeding with parking.");
        }

        try {
            if (ParkDB.isLicensePlatePresent(licensePlate)) {
                showMessage("This car is already parked in another lot.");
            } else {
                if (parkingSpace != null) { // Check if parkingSpace is not null
                    parkingSpace.parkCar(car, lotID, currentTime);
                    parkDB.parkCar(car, lotID, currentTime);
                    changeStatus(lotID, true);
                    showMessage("Car parked successfully!");
                } else {
                    showMessage("Error: Parking space not found.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showMessage("Error with parking handling.");
        }
    }

    private void handleCheckIn() {
        String licensePlate = licensePlateField.getText();
        if (licensePlate == null || licensePlate.isEmpty()) {
            showMessage("Please enter a license plate.");
        } else {
            showMessage("Now select a parking space to park the car.");
        }
    }

    private void handleCheckOut() {
        String licensePlate = checkoutField.getText();
        if (licensePlate == null || licensePlate.isEmpty()) {
            showMessage("Please enter a license plate to check out.");
            return;
        }
        licensePlate = licensePlate.toUpperCase();
        try {
            ResultSet resultSet = ParkDB.getCarDetails(licensePlate);
            if (resultSet.next()) {
                String entryTime = resultSet.getString("ParkTimeIn");
                String currentTime = getCurrentTime();
                double fee = calculateParkingFee(entryTime, currentTime);
                showMessage("Fee: " + fee + " RON.");
                Car car = new Car(licensePlate);
                int lotID = resultSet.getInt("idParkingLots");
                ParkingSpace parkingSpace = parkingSpaces.get(lotID);

                System.out.println("DEBUG: Checking out car with license plate: " + licensePlate);
                System.out.println("DEBUG: ParkingSpace ID " + lotID + " state before checkout: " + parkingSpace.isOccupied() + ", Car: " + (parkingSpace.getCar() != null ? parkingSpace.getCar().getLicensePlate() : "None"));

                if (parkingSpace != null && parkingSpace.hasCar(car)) {
                    parkingSpace.removeCar(car);
                    parkDB.removeCar(car);
                    updateParkingStatusVisuals();
                    PauseTransition pause = new PauseTransition(Duration.seconds(2));
                    pause.setOnFinished(event -> {
                        changeStatus(lotID, false);
                        showMessage("Payment successful! Have a good day!");
                    });
                    pause.play();
                } else {
                    showMessage("Car not found in the parking space.");
                }
            } else {
                showMessage("Car not found in the parking lot.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showMessage("Error during checkout.");
        }
    }

    private void showMessage(String message) {
        Label messageLabel = new Label(message);
        Stage messageStage = new Stage();
        Pane pane = new Pane();
        pane.getChildren().add(messageLabel);
        Scene scene = new Scene(pane, 300, 100);
        messageStage.setScene(scene);
        messageStage.setTitle("Message");
        messageStage.show();
    }

    private void changeStatus(int lotID, boolean isOccupied) {
        Circle status = statusCircles.get(lotID);
        if (status != null) {
            status.setFill(isOccupied ? Color.RED : Color.GREEN);
        }
    }

    private void updateParkingStatusVisuals() {
        try {
            System.out.println("DEBUG: Updating parking status visuals");
            ResultSet resultSet = ParkDB.getParkingStatus();
            while (resultSet.next()) {
                int lotID = resultSet.getInt("idParkingLots");
                boolean isOccupied = resultSet.getBoolean("occupied");

                changeStatus(lotID, isOccupied);
            }

            for (Map.Entry<Integer, ParkingSpace> entry : parkingSpaces.entrySet()) {
                ParkingSpace ps = entry.getValue();
                System.out.println("DEBUG: Final state of ParkingSpace ID " + ps.getId() + ": Occupied - " + ps.isOccupied() + ", Car - " + (ps.getCar() != null ? ps.getCar().getLicensePlate() : "None"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static double calculateParkingFee(String entryTime, String exitTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        try {
            Date entryDate = sdf.parse(entryTime);
            Date exitDate = sdf.parse(exitTime);
            if(entryDate.after(exitDate)){
                exitDate.setTime(exitDate.getTime() + 24 * 60 * 60 * 1000);// add 1 day
            }
            long differenceInMilliseconds = exitDate.getTime() - entryDate.getTime();
            long differenceInHours = differenceInMilliseconds / (60 * 60 * 1000);
            return differenceInHours * 2.0; // 2 RON per hour
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.format(new Date());
    }
}
