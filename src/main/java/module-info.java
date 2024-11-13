module com.mbc.jaspercompiler {
    requires javafx.controls;
    requires javafx.fxml;
    requires jasperreports;


    opens com.mbc.jaspercompiler to javafx.fxml;
    exports com.mbc.jaspercompiler;
    exports com.mbc.jaspercompiler.controllers;
    opens com.mbc.jaspercompiler.controllers to javafx.fxml;
}