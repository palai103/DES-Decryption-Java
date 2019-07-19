package it.unifi.desdecryption;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Graph extends Stage {
    private final NumberAxis xAxis = new NumberAxis();
    private final NumberAxis yAxis = new NumberAxis();
    private final LineChart<Number, Number> lineChart;
    private XYChart.Series firstChunkSeries = new XYChart.Series();
    private XYChart.Series secondChunkSeries = new XYChart.Series();
    private XYChart.Series thirdChunkSeries = new XYChart.Series();
    private XYChart.Series fourthChunkSeries = new XYChart.Series();
    private Scene scene;
    private String path;

    public Graph(String title, String path) {
        xAxis.setLabel("Number of threads");
        yAxis.setLabel("Speed-Up");
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle(title);
        this.path = path;
    }

    public void setSeriesTitle(String clearPsw, String series) {
        switch (series) {
            case "first":
                firstChunkSeries.setName("Speed-Up First Chunk password: " + clearPsw);
                break;

            case "second":
                secondChunkSeries.setName("Speed-Up Second Chunk password: " + clearPsw);
                break;

            case "third":
                thirdChunkSeries.setName("Speed-Up Third Chunk password: " + clearPsw);
                break;

            case "fourth":
                fourthChunkSeries.setName("Speed-Up Fourth Chunk password: " + clearPsw);
                break;
        }
    }

    public void addData(int xVal, Float yVal, String chunk) {
        switch (chunk) {
            case "first":
                firstChunkSeries.getData().add(new XYChart.Data(xVal, yVal));
                break;

            case "second":
                secondChunkSeries.getData().add(new XYChart.Data(xVal, yVal));
                break;

            case "third":
                thirdChunkSeries.getData().add(new XYChart.Data(xVal, yVal));
                break;

            case "fourth":
                fourthChunkSeries.getData().add(new XYChart.Data(xVal, yVal));
                break;
        }
    }

    public void showGraph() {
        lineChart.getData().addAll(firstChunkSeries, secondChunkSeries/*, thirdChunkSeries, fourthChunkSeries*/);
        lineChart.setAnimated(false);

        FlowPane flowPane = new FlowPane();
        flowPane.getChildren().addAll(lineChart);
        scene = new Scene(flowPane, 600, 500);
        this.setScene(scene);
        this.show();
    }

    public void saveAsPngAndTxt(String title, ArrayList<Float> resultsOne, ArrayList<Float> resultsTwo) {
        DateFormat dateFormat = new SimpleDateFormat("dd_MM_HH_mm");
        Date date = new Date();

        /*Save the PNG chart*/
        WritableImage image = scene.snapshot(null);
        File file = new File(path + dateFormat.format(date) + "_" + title + ".png");
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*Save the results in a TXT file*/
        Path resultsFile = Paths.get(path + dateFormat.format(date) + "_" + title + ".txt");
        List<String> results = Arrays.asList("======================== RESULTS ========================",
                "First chunk results: ",
                resultsOne.toString(),
                "Second chunk results: ",
                resultsTwo.toString(),
                "==========================================================");
        try {
            Files.write(resultsFile, results, Charset.forName("UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
