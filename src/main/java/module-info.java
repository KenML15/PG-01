module ucr.lab.pg01 {
    requires javafx.controls;
    requires javafx.fxml;

    //requires ucr.lab.pg01;


    opens ucr.lab.pg01 to javafx.fxml;
    exports ucr.lab.pg01;
    exports controller;
    opens controller to javafx.fxml;
}