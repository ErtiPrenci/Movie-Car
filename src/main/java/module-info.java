module com.example.moviecar {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires com.google.gson;


    opens com.example.moviecar to javafx.fxml;
    exports com.example.moviecar;
}