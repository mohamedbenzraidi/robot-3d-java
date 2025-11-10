package org.example.scene;

import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class GuiManager extends Application {

    @Override
    public void start(Stage primaryStage) {
        File file = new File("src/main/resources/Textures/musPic.png");

        Image museumImage = new Image(file.toURI().toString());
        ImageView imageView = new ImageView(museumImage);
        imageView.setFitWidth(400);
        imageView.setPreserveRatio(true);

        // Title label
        Label title = new Label("Welcome to the Museum Tour");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");


        Button startButton = new Button("Start Tour");

        startButton.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        startButton.setAlignment(Pos.CENTER);

        startButton.setMaxWidth(Double.MAX_VALUE);
        startButton.setMaxHeight(50);


        startButton.setStyle(
                "-fx-background-color: #1A237E;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-family: Arial;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 24px;"
        );

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        VBox.setVgrow(startButton, Priority.ALWAYS);

        Scene scene = new Scene(root, 500, 600);

        startButton.setOnAction(e -> {
            new Thread(JmeApp::main).start();
            primaryStage.close();
        });

        root.getChildren().addAll(imageView, title, startButton);

        primaryStage.setTitle("Museum Tour");
        primaryStage.getIcons().add(new Image(new File("src/main/resources/Textures/museum-16.png").toURI().toString()));
        primaryStage.setScene(scene);
        primaryStage.show();
        
    }

    public static void main(String[] args) {
        launch(args);
    }
}
