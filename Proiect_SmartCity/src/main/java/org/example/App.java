package org.example;
import org.example.UserInterface.UI;
import javafx.stage.Stage;
import javafx.application.Application;
import org.example.DB.ParkDB;

import static org.example.DB.ParkDB.*;

public  class App
{
    public static void main(String[] args )
    {
        Application.launch(UI.class, args);

    }

}
