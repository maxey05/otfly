package com.otfly.view;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainWindow extends Application
{
    public static void main(String[] args) 
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        primaryStage.setTitle("otfly");
        primaryStage.show();
    }
}
