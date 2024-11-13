package com.mbc.jaspercompiler.models;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.stream.Stream;

public class JasperCompilerAPI {
    private int totalFilesToCompile = 0;
    private int currentCompileFilesCount = 0;
    private int successCompileFilesCount = 0;
    private int failCompileFilesCount = 0;
    private String currentJrxmlCompileFilename = null;
    private String fontName = "mbc";
    private final String jrxmlFilesDirectory;
    private final String jasperOuputDirectory;
    private Label compilingLabel;
    private Label currentCompiledLabel;
    private Label totalCompileIndexLabel;
    private Label compileFilenameLabel;
    private Label totalCompileLabel;
    private Label successCompileLabel;
    private Label failCompileLabel;
    private ProgressBar progressBar;
    private Label percentLabel;
    private Thread progressBarThread;
    private boolean affectToOriginalJrxmlFiles;
    private boolean highPerformanceMode;
    private File jrxmlTempChangeDirectory;

    public JasperCompilerAPI(String jrxmlFilesDirectory, String jasperOutputDirectory) {
        this.jrxmlFilesDirectory = jrxmlFilesDirectory;
        this.jasperOuputDirectory = jasperOutputDirectory;
        this.affectToOriginalJrxmlFiles = false;
    }

    public JasperCompilerAPI(String jrxmlFilesDirectory, String jasperOutputDirectory, Label compilingLabel,
                             Label currentCompiledLabel, Label totalCompileIndexLabel, Label compileFilenameLabel, Label totalCompileLabel,
                             Label successCompileLabel, Label failCompileLabel, ProgressBar progressBar, Label percentLabel) {
        this.jrxmlFilesDirectory = jrxmlFilesDirectory;
        this.jasperOuputDirectory = jasperOutputDirectory;
        this.affectToOriginalJrxmlFiles = false;
        this.compilingLabel = compilingLabel;
        this.currentCompiledLabel = currentCompiledLabel;
        this.totalCompileIndexLabel = totalCompileIndexLabel;
        this.compileFilenameLabel = compileFilenameLabel;
        this.totalCompileLabel = totalCompileLabel;
        this.successCompileLabel = successCompileLabel;
        this.failCompileLabel = failCompileLabel;
        this.progressBar = progressBar;
        this.percentLabel = percentLabel;
    }



    public int getTotalJrxmlFiles() {
        return getListOfJrxmlFilenameList(jrxmlFilesDirectory).size();
    }
    public int getTotalFilesToCompile() {
        return totalFilesToCompile;
    }
    public int getCurrentCompileFilesCount() {
        return currentCompileFilesCount;
    }
    public int getSuccessCompileFilesCount() {
        return successCompileFilesCount;
    }
    public int getFailCompileFilesCount() {
        return failCompileFilesCount;
    }
    public Thread getProgressBarThread() { return progressBarThread; }
    public void setHighPerformanceMode(boolean set) {
        highPerformanceMode = set;
    }

    public void compileAndExportReport() {
        jrxmlTempChangeDirectory = new File(jrxmlFilesDirectory+"\\jrxml_temp_change");
        jrxmlTempChangeDirectory.mkdir();
        initialize();
        makeChanges();
        compileReport();
        if ( affectToOriginalJrxmlFiles ) deleteTempChangeDirectory();
    }

    private void initialize() {
        totalFilesToCompile = 0;
        currentCompileFilesCount = 0;
        successCompileFilesCount = 0;
        failCompileFilesCount = 0;
    }

    private void compileReport() {
        startProgressBarThread();
        getListOfJrxmlFilenameList(jrxmlTempChangeDirectory.getAbsolutePath()).forEach(jrxmlChangedFilename -> {
            String sourceJrxmlFilePath = jrxmlTempChangeDirectory.getAbsolutePath()+"\\"+jrxmlChangedFilename;
            String targetJasperFilePath = jasperOuputDirectory+"\\"+jrxmlChangedFilename.replace("_temp_changed.jrxml", ".jasper");
            currentJrxmlCompileFilename = jrxmlFilesDirectory+"\\"+jrxmlChangedFilename.replace("_temp_changed.jrxml", ".jrxml");
            updateUI();
            try {
                if (!highPerformanceMode) Thread.sleep(new Random().nextInt(3000));
                JasperCompileManager.compileReportToFile(sourceJrxmlFilePath, targetJasperFilePath);
                successCompileFilesCount++;
            } catch (JRException e) {
                failCompileFilesCount++;
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if ( affectToOriginalJrxmlFiles ) {
                new File(sourceJrxmlFilePath).delete();
            }
            currentCompileFilesCount++;
            System.out.println("Compiled reports successfully...");
        });
        Platform.runLater(()->compilingLabel.setText("all reports compiled successfully..."));
        updateUI();
    }
    private void makeChanges() {
        if (highPerformanceMode) {
            getListOfJrxmlFilenameList(jrxmlFilesDirectory).parallelStream().forEach(jrxmlFileName -> {
                try {
                    String jrxmlFilePath = jrxmlFilesDirectory+"\\"+jrxmlFileName;
                    String jrxmlChangedFilepath = jrxmlTempChangeDirectory.getAbsolutePath()+"\\"+jrxmlFileName.substring(0, jrxmlFileName.lastIndexOf("."))+"_temp_changed.jrxml";
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(jrxmlFilePath));
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(jrxmlChangedFilepath));
                    String readLine = null;
                    while ( (readLine = bufferedReader.readLine()) != null ) {
                        if ( readLine.contains("fontName")) {
                            readLine = readLine.replaceAll("fontName=\"[^\"]*\"", "fontName=\""+fontName+"\"");
                        }
                        bufferedWriter.write(readLine.concat("\n"));
                    }
                    bufferedReader.close();
                    bufferedWriter.close();
                    if (affectToOriginalJrxmlFiles) {
                        File originalJrxmlFile = new File(jrxmlFilePath);
                        originalJrxmlFile.delete();
                        File changedJrxmlFile = new File(jrxmlChangedFilepath);
                        Path sourcePath = Paths.get(changedJrxmlFile.getAbsolutePath());
                        Path destinationPath = Paths.get(originalJrxmlFile.getAbsolutePath());
                        try {
                            // Copy file to the destination, overwriting if necessary
                            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error in make changes."+ e.getMessage());
                    e.printStackTrace();
                }
            });
        } else {
            getListOfJrxmlFilenameList(jrxmlFilesDirectory).forEach(jrxmlFileName -> {
                try {
                    String jrxmlFilePath = jrxmlFilesDirectory+"\\"+jrxmlFileName;
                    String jrxmlChangedFilepath = jrxmlTempChangeDirectory.getAbsolutePath()+"\\"+jrxmlFileName.substring(0, jrxmlFileName.lastIndexOf("."))+"_temp_changed.jrxml";
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(jrxmlFilePath));
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(jrxmlChangedFilepath));
                    String readLine = null;
                    while ( (readLine = bufferedReader.readLine()) != null ) {
                        if ( readLine.contains("fontName")) {
                            readLine = readLine.replaceAll("fontName=\"[^\"]*\"", "fontName=\""+fontName+"\"");
                        }
                        bufferedWriter.write(readLine.concat("\n"));
                    }
                    bufferedReader.close();
                    bufferedWriter.close();
                    if (affectToOriginalJrxmlFiles) {
                        File originalJrxmlFile = new File(jrxmlFilePath);
                        originalJrxmlFile.delete();
                        File changedJrxmlFile = new File(jrxmlChangedFilepath);
                        Path sourcePath = Paths.get(changedJrxmlFile.getAbsolutePath());
                        Path destinationPath = Paths.get(originalJrxmlFile.getAbsolutePath());
                        try {
                            // Copy file to the destination, overwriting if necessary
                            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Error in make changes."+ e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    private void deleteTempChangeDirectory() {
        try {
            Files.delete(Paths.get(jrxmlTempChangeDirectory.getAbsolutePath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> getListOfJrxmlFilenameList(String sourceDir) {
        List<String> jrxmlFileList = new ArrayList<>();
        Path dirPath = Paths.get(sourceDir);
        try (Stream<Path> paths = Files.walk(dirPath, 1)) {
            paths.filter(Files::isRegularFile)
                    .forEach(path -> {
                        System.out.println(path.getFileName());
                        // Get file extension...
                        String fileExtension = path.getFileName().toString().substring(path.getFileName().toString().lastIndexOf("."));
                        if (fileExtension.equalsIgnoreCase(".jrxml")) {
                            jrxmlFileList.add(path.getFileName().toString());
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        if ( totalFilesToCompile == 0 ) totalFilesToCompile = jrxmlFileList.size();
//        updateUI();
        return jrxmlFileList;
    }

    public void setAffectToOriginalJrxmlFiles(boolean set) {
        affectToOriginalJrxmlFiles = set;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }

    public void updateUI() {
        Platform.runLater(() -> {
            compilingLabel.setText("compiling reports...");
            currentCompiledLabel.setText(String.valueOf(currentCompileFilesCount));
            totalCompileIndexLabel.setText(String.valueOf(totalFilesToCompile));
            compileFilenameLabel.setText(currentJrxmlCompileFilename);
            totalCompileLabel.setText(String.valueOf(totalFilesToCompile));
            successCompileLabel.setText(String.valueOf(successCompileFilesCount));
            failCompileLabel.setText(String.valueOf(failCompileFilesCount));
            percentLabel.setText(String.valueOf(getCurrentCompilePercentage()).concat("%"));
            System.out.println(getCurrentCompilePercentage());
        });
    }

    private int getCurrentCompilePercentage() {
        return (int)(((double)currentCompileFilesCount/(double)totalFilesToCompile)*100);
    }

    private void startProgressBarThread() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                while(currentCompileFilesCount <= totalFilesToCompile) {
                    updateProgress(currentCompileFilesCount, totalFilesToCompile);
                }
                return null;
            }
        };
        Platform.runLater(() -> progressBar.progressProperty().bind(task.progressProperty()));

        // Run the task on a background thread
        progressBarThread = new Thread(task);
        progressBarThread.setDaemon(true);  // The thread will terminate when the application ends
        progressBarThread.start();
    }
}
