package org.example.scene;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;


import javax.swing.*;
import java.awt.*;
import java.io.File;

public class GuiManager extends Application {

    private static Stage primaryStage;
    private static final String DARK_BG = "#1e1e1e";
    private static final String CARD_BG = "#2c2c2c";
    private static final String CARD_HOVER = "#343434";
    private static final String TEXT_MAIN = "#ffffff";
    private static final String TEXT_SECOND = "#c0c0c0";

    @Override
    public void start(Stage primaryStage) {
        GuiManager.primaryStage = primaryStage;
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: " + DARK_BG + ";");

        VBox header = createHeader();


        HBox cardRow1 = new HBox(20);
        cardRow1.setAlignment(Pos.CENTER);
        cardRow1.getChildren().addAll(
                createCard("Musée du Louvre", "src/main/resources/Textures/Musée du Louvre.jpg", 1),
                createCard("Metropolitan Museum of Art", "src/main/resources/Textures/Metropolitan Museum of Art.png", 2)
        );

        HBox cardRow2 = new HBox();
        cardRow2.setAlignment(Pos.CENTER);
        cardRow2.getChildren().add(createCard("British Museum", "src/main/resources/Textures/british_museum.png", 3));

        VBox cardsContainer = new VBox(25);
        cardsContainer.getChildren().addAll(cardRow1, cardRow2);

        mainContainer.getChildren().addAll(header, cardsContainer);

        Scene scene = new Scene(mainContainer, 1000, 700);
        primaryStage.setTitle("Museum Tour");
        primaryStage.getIcons().add(new Image(new File("src/main/resources/Textures/museum-16.png").toURI().toString()));
        primaryStage.setScene(scene);
        primaryStage.show();

    }

    private VBox createHeader() {
        VBox header = new VBox(15);
        header.setPadding(new Insets(0, 0, 20, 0));

        HBox titleRow = new HBox(25);
        titleRow.setAlignment(Pos.CENTER);

        ImageView icon = createImageView("src/main/resources/Textures/musPic.png", 80, 80);

        VBox textContainer = new VBox(5);
        textContainer.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Welcome to the Museum Tour");
        title.setTextFill(Color.web(TEXT_MAIN));
        title.setFont(Font.font("System", FontWeight.BOLD, 30));

        Label subTitle = new Label("Please select a museum");
        subTitle.setFont(Font.font("System", FontWeight.SEMI_BOLD, 16));
        subTitle.setTextFill(Color.web(TEXT_SECOND));

        textContainer.getChildren().addAll(title, subTitle);
        titleRow.getChildren().addAll(icon, textContainer);

        header.getChildren().add(titleRow);

        return header;
    }

    private VBox createCard(String title, String path, int nbrMuseum) {
        VBox card = new VBox(0);
        card.setStyle("-fx-background-color: " + CARD_BG + "; " +
                "-fx-background-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(400);
        card.setPrefHeight(230);

        ImageView cardImage = createImageView(path, 420, 200);
        cardImage.setPreserveRatio(false);

        Label cardTitle = new Label(title);
        cardTitle.setTextFill(Color.web(TEXT_MAIN));
        cardTitle.setPadding(new Insets(12, 5, 12, 5));
        cardTitle.setFont(Font.font("System", FontWeight.BOLD, 14));

        card.getChildren().addAll(cardImage, cardTitle);

        card.setOnMouseEntered(e ->
                card.setStyle("-fx-background-color:" + CARD_HOVER + "; " +
                        "-fx-background-radius: 8; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 3); " +
                        "-fx-cursor: hand;")
        );
        card.setOnMouseExited(e ->
                card.setStyle("-fx-background-color: " + CARD_BG + "; " +
                        "-fx-background-radius: 8; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);")
        );

        card.setOnMouseClicked(e -> {
            switch (nbrMuseum) {
                case 1:
                    new Thread(JmeApp::main).start();
                    primaryStage.hide();
                    break;

                case 2:
                    new Thread(JmeMetApp::main).start();
                    primaryStage.hide();
                    break;

                case 3:
                    new Thread(JmeThApp::main).start();
                    primaryStage.hide();
                    break;
                default:
                    System.out.println("Error: unknown number!");
            }
        });


        return card;
    }


    private ImageView createImageView(String path, double width, double height) {
        ImageView imageView = new ImageView(new Image(new File(path).toURI().toString()));
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false);
        return imageView;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
