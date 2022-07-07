module com.cloudgb.cloud {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires io.netty.codec;
    requires com.cloud.cloudmodel;

    opens com.cloudgb.cloud to javafx.fxml;
    exports com.cloudgb.cloud;
}