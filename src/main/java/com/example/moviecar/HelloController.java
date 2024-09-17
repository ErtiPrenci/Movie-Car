package com.example.moviecar;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class HelloController {
    @FXML
    private Label lblyear;
    @FXML
    private Label lblNoCars;
    @FXML
    private TextField txtMovieTitle;
    @FXML
    private TableView carTableview;


    //Die Methode zur Verarbeitung des Button um alle Autos und Filmdaten zu bekommen
    @FXML
    protected void getCarsOnAction() throws IOException, InterruptedException {
        // Erstellen eines Gson-Objekts zur konvertierung zwischen Java-Objekten und JSON
        Gson gson = new Gson();
        // Eingabe um die Filmtitle zu bekommen
        String movieTitle = txtMovieTitle.getText();
        // Die Spaces werden mit %20 umwandelt
        String movieTitle2 = movieTitle.replaceAll(" ", "%20");
        // eine Http request erstellen um die Filmdaten von der API zu bekommen
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://movies-tv-shows-database.p.rapidapi.com/?title=" + movieTitle2))
                .header("Type", "get-movies-by-title")
                .header("X-RapidAPI-Key", "b1b96dfe93mshb6d914d6c12a9b2p12ef45jsn251634bd689c")
                .header("X-RapidAPI-Host", "movies-tv-shows-database.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response;

        try {
            // schickt den Request un bekommt den Response zurück
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Analyse der JSON-Antwort für die Filminformationen
        JsonObject jsonResponse = gson.fromJson(response.body(), JsonObject.class);

        String finalyear = "";
        // Überprüft ob die Filminformationen Ergebnisse enthalten
        if (jsonResponse.has("movie_results")) {
            // die filminformationen auslesen und das Jahr anzeigen
            JsonArray movies = jsonResponse.getAsJsonArray("movie_results");
            for (JsonElement movieElement : movies) {
                JsonObject movie = movieElement.getAsJsonObject();
                String year = movie.get("year").getAsString();
                // Das Jahr in einen Label ausgeben
                lblyear.setText(year);
                finalyear = year;
            }
            // eine Http request erstellen um die Autodaten von der API zu bekommen
            HttpRequest request2 = HttpRequest.newBuilder()
                    .uri(URI.create("https://car-data.p.rapidapi.com/cars?limit=10&page=0&year=" + finalyear))
                    .header("X-RapidAPI-Key", "b1b96dfe93mshb6d914d6c12a9b2p12ef45jsn251634bd689c")
                    .header("X-RapidAPI-Host", "car-data.p.rapidapi.com")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
            // schickt den Request un bekommt den Response zurück
            HttpResponse<String> response2 = HttpClient.newHttpClient().send(request2, HttpResponse.BodyHandlers.ofString());
            // Analyse der JSON-Antwort für die Autodaten
            JsonArray jsonResponse2 = gson.fromJson(response2.body(), JsonArray.class);

            if (!jsonResponse2.isEmpty()) {
                //  die Vorherige Spalten löschen
                carTableview.getColumns().clear();
                //Tabellenspalten erstellen für die Spalten: Make,Model,Type und Year
                TableColumn<String[], String> tcMake = new TableColumn<>("Make");
                tcMake.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[0]));

                TableColumn<String[], String> tcModel = new TableColumn<>("Model");
                tcModel.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[1]));

                TableColumn<String[], String> tcType = new TableColumn<>("Type");
                tcType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[2]));

                TableColumn<String[], String> tcYear = new TableColumn<>("Year");
                tcYear.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue()[3]));
                //Die Spalten in der Tabelle hinzufügen
                carTableview.getColumns().addAll(tcMake, tcModel, tcType, tcYear);
                // eine ObservableList um die Autodaten zu halten
                ObservableList<String[]> cardata = FXCollections.observableArrayList();
                //Autodaten from der Json-response befüllen
                for (int i = 0; i < jsonResponse2.size(); i++) {
                    // Ein Stringarray mit Autodaten mit 4 Elementen erstellen
                    String[] rowData = new String[4];
                    //das Extrahieren und Konvertierung der Werte von der Json-response in einen String und dann füge ich sie ihn in die richtige Arrayelemente
                    rowData[0] = String.valueOf(jsonResponse2.get(i).getAsJsonObject().get("make"));
                    rowData[1] = String.valueOf(jsonResponse2.get(i).getAsJsonObject().get("model"));
                    rowData[2] = String.valueOf(jsonResponse2.get(i).getAsJsonObject().get("type"));
                    rowData[3] = String.valueOf(jsonResponse2.get(i).getAsJsonObject().get("year"));

                    cardata.add(rowData);
                }
                //Autodaten auf der Tabelle setzen
                carTableview.setItems(cardata);

            } else {
                //Wenn keine Autos gibt dann wird diese Nachricht gezeigt
                lblNoCars.setText("No car results found.");
            }
        }
    }
}