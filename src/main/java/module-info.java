module com.cloud.cloudgeek {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.cloud.cloudgeek to javafx.fxml;
    exports com.cloud.cloudgeek;
}