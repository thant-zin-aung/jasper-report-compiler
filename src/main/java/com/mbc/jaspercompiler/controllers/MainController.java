package com.mbc.jaspercompiler.controllers;

import com.mbc.jaspercompiler.Main;
import com.mbc.jaspercompiler.models.JasperCompilerAPI;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.File;

public class MainController {
    @FXML
    private Label jrxmlFilePathLabel;
    @FXML
    private Label jasperFilePathLabel;
    @FXML
    private HBox jasperPathTextBox;
    @FXML
    private Button jasperFileChooser;
    @FXML
    private TextField fontNameTextField;
    @FXML
    private HBox highPerformanceButton;
    @FXML
    private HBox highPerformanceToggle;
    @FXML
    private HBox recursiveModeButton;
    @FXML
    private HBox recursiveModeToggle;
    @FXML
    private Label highPerformanceEnableLabel;
    @FXML
    private Label recursiveEnableLabel;

    private Label compilingLabel;
    private Label currentCompiledLabel;
    private Label totalCompileIndexLabel;
    private Label compileFilenameLabel;
    private Label totalCompileLabel;
    private Label successCompileLabel;
    private Label failCompileLabel;
    private ProgressBar progressBar;
    private Label percenLabel;
    private Label highPerformanceModeIndicatorLabel;


    private File selectedJrxmlDirectory;
    private File selectedJapserDirectory;

    private JasperCompilerAPI jasperCompilerAPI;

    private boolean highPerformanceMode;
    private boolean recursiveMode;

    @FXML
    public void clickOnJrxmlFileChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        selectedJrxmlDirectory = directoryChooser.showDialog(Main.primaryStage);
        if ( selectedJrxmlDirectory != null ) {
            jrxmlFilePathLabel.setText(selectedJrxmlDirectory.getAbsolutePath());
        } else {
            jrxmlFilePathLabel.setText("..........");
        }
    }

    @FXML
    public void clickOnJasperFileChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        selectedJapserDirectory = directoryChooser.showDialog(Main.primaryStage);
        if ( selectedJrxmlDirectory != null ) {
            jasperFilePathLabel.setText(selectedJapserDirectory.getAbsolutePath());
        } else {
            jasperFilePathLabel.setText("..........");
        }
    }

    @FXML
    public void clickOnHighPerformanceButton() {
        switchButton(highPerformanceButton, highPerformanceToggle, highPerformanceEnableLabel, highPerformanceMode);
        highPerformanceMode = !highPerformanceMode;
    }

    @FXML
    public void clickOnRecursiveButton() {
        switchButton(recursiveModeButton, recursiveModeToggle, recursiveEnableLabel, recursiveMode);
        recursiveMode = !recursiveMode;
        if (recursiveMode) {
            jasperPathTextBox.setDisable(true);
            jasperFileChooser.setDisable(true);
        } else {
            jasperPathTextBox.setDisable(false);
            jasperFileChooser.setDisable(false);
        }
    }

    private void switchButton(Node button, Node toggle, Label enabledLabel, boolean mode) {
        if (mode) {
            toggle.setStyle("-fx-translate-x: 0px; -fx-background-radius: 0px; -fx-border-radius: 0px; -fx-background-color: white;");
            button.setStyle("-fx-background-color: transparent; -fx-border-radius: 0px; -fx-background-radius: 0px; -fx-border-width: 1px; -fx-border-style: solid; -fx-border-color: white;");
            enabledLabel.setVisible(false);
        } else {
            toggle.setStyle("-fx-translate-x: 20px; -fx-background-radius: 0px; -fx-border-radius: 0px; -fx-background-color: white;");
            button.setStyle("-fx-background-color: #497075; -fx-border-radius: 0px; -fx-background-radius: 0px; -fx-border-width: 1px; -fx-border-style: solid; -fx-border-color: white;");
            enabledLabel.setVisible(true);
        }
    }

    @FXML
    public void clickOnCompileButton() {
        createAndShowCompileUI();
    }

    private void initializeBeforeCompile() {
        compilingLabel.setText("initializing reports...");
        currentCompiledLabel.setText("0");
        totalCompileIndexLabel.setText("0");
        totalCompileLabel.setText("0");
        successCompileLabel.setText("0");
        failCompileLabel.setText("0");
        highPerformanceModeIndicatorLabel.setText(highPerformanceMode?"ON":"OFF");
    }

    private void createAndShowCompileUI() {
        Main.primaryStage.hide();
        Stage compileStage = new Stage();

        HBox root = new HBox();
        root.setPrefWidth(770);
        root.setPrefHeight(270);
        root.setStyle("-fx-background-color: #314e52");
        root.setPadding(new Insets(15, 10, 15, 10));
        root.setSpacing(1);

        VBox rightContentWrapper = new VBox();
        rightContentWrapper.setPrefWidth(200);
        rightContentWrapper.setPadding(new Insets(0, 0, 0, 10));
        rightContentWrapper.setSpacing(28);
        rightContentWrapper.setAlignment(Pos.TOP_CENTER);


        VBox leftContentWrapper = new VBox();
        leftContentWrapper.setPrefWidth(550);
//        leftContentWrapper.setPrefHeight(270);
//        leftContentWrapper.setStyle("-fx-background-color: #314e52");
        leftContentWrapper.setPadding(new Insets(0, 15, 0, 0));
        leftContentWrapper.setSpacing(1);
        leftContentWrapper.setStyle(" -fx-border-width: 0 1px 0 0; -fx-border-style: solid; -fx-border-color: #497075;");
        compilingLabel = new Label("compiling reports...");
        compilingLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-padding: 0 0 5px 0;");

        HBox progressBarWrapper = new HBox();
        progressBarWrapper.setPadding(new Insets(5, 0 , 0 , 0));
        progressBarWrapper.setFillHeight(false);
        progressBarWrapper.setAlignment(Pos.CENTER);
        progressBarWrapper.setSpacing(10);
        progressBar = new ProgressBar();
        progressBar.getStyleClass().add("custom-progress-bar");
        progressBar.setPrefWidth(480);
        progressBar.setPrefHeight(45);
        percenLabel = new Label("0%");
        percenLabel.setPrefWidth(50);
        percenLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold");
        percenLabel.setAlignment(Pos.CENTER_RIGHT);
        progressBarWrapper.getChildren().addAll(progressBar, percenLabel);

        HBox compileIndexWrapper = new HBox();
//        copyIndexWrapper.setPadding(new Insets(5, 0 , 0 , 0));
        compileIndexWrapper.setFillHeight(false);
        compileIndexWrapper.setAlignment(Pos.CENTER_LEFT);
        currentCompiledLabel = new Label("0");
        currentCompiledLabel.setStyle("-fx-text-fill: white; -fx-font-size: 40px; -fx-font-weight: bold");
        Label compiledLabel = new Label(" compiled out of ");
        compiledLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px");
        totalCompileIndexLabel = new Label(" 0");
        totalCompileIndexLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold");
        compileIndexWrapper.getChildren().addAll(currentCompiledLabel, compiledLabel, totalCompileIndexLabel);

        HBox compilingFileWrapper = new HBox();
        compilingFileWrapper.setFillHeight(false);
        compilingFileWrapper.setAlignment(Pos.CENTER_LEFT);
        compilingFileWrapper.setPadding(new Insets(0, 0, 3, 0));
        Label compilingTextLabel = new Label("compiling: ");
        compilingTextLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-min-width: 65px; -fx-pref-width: 65px;");
        compileFilenameLabel = new Label("N/A");
        compileFilenameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");
        compilingFileWrapper.getChildren().addAll(compilingTextLabel, compileFilenameLabel);

        HBox totalLabelWrapper = new HBox();
        totalLabelWrapper.setAlignment(Pos.CENTER_LEFT);
        totalLabelWrapper.setPadding(new Insets(0, 0, 2, 0));
        HBox successLabelWrapper = new HBox();
        successLabelWrapper.setAlignment(Pos.CENTER_LEFT);
        successLabelWrapper.setPadding(new Insets(0, 0, 2, 0));
        HBox failLabelWrapper = new HBox();
        failLabelWrapper.setAlignment(Pos.CENTER_LEFT);
        failLabelWrapper.setPadding(new Insets(0, 0, 2, 0));
        Label totalLabel = new Label("Total: ");
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: white;");
        Label successLabel = new Label("Success: ");
        successLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: white;");
        Label failLabel = new Label("Failed: ");
        failLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: white;");
        totalCompileLabel = new Label("0");
        totalCompileLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");
        successCompileLabel = new Label("0");
        successCompileLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");
        failCompileLabel = new Label("0");
        failCompileLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");

        totalLabelWrapper.getChildren().addAll(totalLabel, totalCompileLabel);
        successLabelWrapper.getChildren().addAll(successLabel, successCompileLabel);
        failLabelWrapper.getChildren().addAll(failLabel, failCompileLabel);

        HBox highPerformanceModeLabelWrapper = new HBox();
        highPerformanceModeLabelWrapper.setAlignment(Pos.CENTER_LEFT);
        Label highPerformanceModeLabel = new Label("High Performance Mode: ");
        highPerformanceModeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white; -fx-font-weight: bold;");
        highPerformanceModeIndicatorLabel = new Label("-");
        highPerformanceModeIndicatorLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");
        highPerformanceModeLabelWrapper.getChildren().addAll(highPerformanceModeLabel, highPerformanceModeIndicatorLabel);

        leftContentWrapper.getChildren().addAll(compilingLabel,progressBarWrapper, compileIndexWrapper, compilingFileWrapper,
                totalLabelWrapper, successLabelWrapper, failLabelWrapper, highPerformanceModeLabelWrapper);


        Label instructionLabel = new Label("All the logs, errors and fail report list are saved under C:\\ProgramData\\JasperReportCompileLogs.txt C:\\ProgramData\\JasperReportCompileErrors.txt C:\\ProgramData\\JasperReportCompileFailList.txt " +
                "----- Developed by MBC Software Development Centre(Thant Zin Aung) - 2024");
        instructionLabel.setWrapText(true);
        instructionLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: white;");
        Button cancelButton = new Button("CANCEL");
        cancelButton.setPrefWidth(200);
        cancelButton.setPrefHeight(42);
        cancelButton.getStyleClass().add("cancel-button");
        cancelButton.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-color: #497075; " +
                "-fx-background-radius: 3px; ");
        cancelButton.setOnMouseClicked(event-> {
            System.exit(0);
            Platform.exit();
        });


        rightContentWrapper.getChildren().addAll(instructionLabel, cancelButton);

        root.getChildren().addAll(leftContentWrapper, rightContentWrapper);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Main.class.getResource("css/progress-bar-style.css").toExternalForm());
        compileStage.setScene(scene);
        compileStage.initStyle(StageStyle.UNDECORATED);
        compileStage.show();
        compileReport();
    }

    private void compileReport() {
        initializeBeforeCompile();
        jasperCompilerAPI = new JasperCompilerAPI(selectedJrxmlDirectory.getAbsolutePath(), selectedJapserDirectory.getAbsolutePath(),
                compilingLabel, currentCompiledLabel, totalCompileIndexLabel, compileFilenameLabel, totalCompileLabel, successCompileLabel, failCompileLabel, progressBar, percenLabel);
        jasperCompilerAPI.setFontName(fontNameTextField.getText().trim());
        jasperCompilerAPI.setHighPerformanceMode(highPerformanceMode);
        Thread reportCompilerThread = new Thread(() -> {
            jasperCompilerAPI.compileAndExportReport();
        });
        reportCompilerThread.setDaemon(true);
        reportCompilerThread.start();
    }

    @FXML
    public void clickOnCloseButton() {
        Platform.exit();
    }
}