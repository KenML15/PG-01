package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import model.Recursion;
import model.RecursionEngine;
import model.TreePainter;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

public class MainController implements Initializable {

    @javafx.fxml.FXML
    private Canvas canvasTree;
    @javafx.fxml.FXML
    private Button btnFactReset;
    @javafx.fxml.FXML
    private Button btnFactCalc;
    @javafx.fxml.FXML
    private Label lblComplexity;
    @javafx.fxml.FXML
    private Slider sliderFactN;
    @javafx.fxml.FXML
    private Label lblFactCalls;
    @javafx.fxml.FXML
    private Label lblFactN;
    @javafx.fxml.FXML
    private ListView<String> listSteps;
    @javafx.fxml.FXML
    private Label lblFactResult;
    @javafx.fxml.FXML
    private BarChart<String, Number> chartTime;
    @javafx.fxml.FXML
    private BarChart<String, Number> chartCalls;



    //atributos internos del controller
    private final RecursionEngine engine = new RecursionEngine();
    private RecursionEngine.CallNode lastRoot;
    private List<RecursionEngine.CallNode> factBFS;
    private final TreePainter painter = new TreePainter();


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupFactTab();
        runBenchmark();
        System.out.println("El controlador cargó correctamente");
    }

    private void setupFactTab() {
        sliderFactN.setMin(1);
        sliderFactN.setMax(12);
        sliderFactN.setValue(5);
        sliderFactN.setMajorTickUnit(1);
        sliderFactN.setSnapToTicks(true);
        sliderFactN.valueProperty().addListener((observable, oldValue, newValue) -> {
            lblFactN.setText(String.valueOf(newValue.intValue()));
        });
        btnFactCalc.setOnAction(event -> runFactorial());
        btnFactReset.setOnAction(event -> resetFactTab());

    }

    private void runFactorial() {
        int n = (int) sliderFactN.getValue();
//        AtomicInteger counter= new AtomicInteger(0);
//        long result= Recursion.factorial(n,counter);
//        lblFactResult.setText(util.Utility.format(result));
//        lblFactCalls.setText(String.valueOf(counter));

        engine.computeFactorial(n);
        lastRoot = engine.getTreeRoot();
        factBFS = TreePainter.collectBFS(lastRoot);

        //llenamos la lista de pasos
        ObservableList<String> items = FXCollections.observableArrayList();
        for (int i = 0; i < n; i++) {
            RecursionEngine.Step step = engine.getSteps().get(i);
            items.add(String.format("[%02d] %s", i + 1, step.description));
        }
        listSteps.setItems(items); //setteamos la lista de pasos recursivos
        lblFactResult.setText(util.Utility.format(engine.getTreeRoot().result));
        lblFactCalls.setText(String.valueOf(engine.getCallCount()));
        lblComplexity.setText("O(n) = O(" + n + ") llamadas ");

        //se dibuja el árbol de llamadas en el canva
        painter.paint(canvasTree, lastRoot, factBFS.size(), factBFS);

    }

    private void resetFactTab() {
        lblFactResult.setText("-");
        lblFactCalls.setText("-");
        lblComplexity.setText("-");
        listSteps.getItems().clear();
    }

    @SuppressWarnings("unchecked")
    private void runBenchmark() {
        Platform.runLater(() -> {
            //limpiar
            chartTime.getData().clear();
            chartCalls.getData().clear();

            // tiempo
            XYChart.Series<String, Number> seriesTimeSimple = new XYChart.Series<>();
            seriesTimeSimple.setName("Fib Recursive");

            XYChart.Series<String, Number> seriesTimeHash = new XYChart.Series<>();
            seriesTimeHash.setName("Fib Memo HashMap");

            XYChart.Series<String, Number> seriesTimeArray = new XYChart.Series<>();
            seriesTimeArray.setName("Fib Memo Array");

            // llamdas
            XYChart.Series<String, Number> seriesCallsSimple = new XYChart.Series<>();
            seriesCallsSimple.setName("Fib Recursive");

            XYChart.Series<String, Number> seriesCallsHash = new XYChart.Series<>();
            seriesCallsHash.setName("Fib Memo HashMap");

            XYChart.Series<String, Number> seriesCallsArray = new XYChart.Series<>();
            seriesCallsArray.setName("Fib Memo Array");

            int[] valuesNTiempo = {10, 15, 25, 50}; //empieza en 10
            int[] valuesN_Llamadas = {5, 10, 15, 25, 50}; //empieza en 5

            for (int n : valuesNTiempo) {

                //simple
                if (n <= 15) {
                    long start = System.nanoTime();
                    Recursion.fibonacci(n, new AtomicInteger(0));
                    long total = System.nanoTime() - start;
                    //ajuste para que sea a escala
                    long val = (n == 15) ? (total * 200) : (total * 10);
                    seriesTimeSimple.getData().add(new XYChart.Data<>(String.valueOf(n), Math.min(val, 100000)));
                }

                //hashmap
                Map<Integer, Long> map = new HashMap<>();
                long startH = System.nanoTime();
                Recursion.fibMemo(n, map, new AtomicInteger(0));
                seriesTimeHash.getData().add(new XYChart.Data<>(String.valueOf(n), (System.nanoTime() - startH) /5));

                long[] memo = new long[n + 1];
                java.util.Arrays.fill(memo, -1);
                long startA = System.nanoTime();
                Recursion.fibMemoArray(n, memo, new AtomicInteger(0));
                seriesTimeArray.getData().add(new XYChart.Data<>(String.valueOf(n), (System.nanoTime() - startA) /5));


            }
            for (int n : valuesN_Llamadas) {
                AtomicInteger counter = new AtomicInteger(0);
                //simple (Rojo)
                if (n <= 15) {
                    Recursion.fibonacci(n, counter);
                    seriesCallsSimple.getData().add(new XYChart.Data<>(String.valueOf(n), Math.min(counter.get(), 200)));
                }
                //memoria
                counter.set(0);
                Recursion.fibMemo(n, new HashMap<>(), counter);
                seriesCallsHash.getData().add(new XYChart.Data<>(String.valueOf(n), counter.get()));

                counter.set(0);
                Recursion.fibMemoArray(n, new long[n+1], counter);
                seriesCallsArray.getData().add(new XYChart.Data<>(String.valueOf(n), counter.get()));
            }

            //se agrega a los graficos los calculos
            chartTime.getData().addAll(seriesTimeArray, seriesTimeHash, seriesTimeSimple);
            chartCalls.getData().addAll(seriesCallsArray, seriesCallsHash, seriesCallsSimple);
        });


    }


}
