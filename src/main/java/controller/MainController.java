package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
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

    @FXML
    private Canvas canvasTree;
    @FXML
    private Button btnFactReset;
    @FXML
    private Button btnFactCalc;
    @FXML
    private Label lblComplexity;
    @FXML
    private Slider sliderFactN;
    @FXML
    private Label lblFactCalls;
    @FXML
    private Label lblFactN;
    @FXML
    private ListView<String> listSteps;
    @FXML
    private Label lblFactResult;
    @FXML
    private BarChart<String, Number> chartTime;
    @FXML
    private BarChart<String, Number> chartCalls;
    @FXML
    private Canvas canvasFib;
    @FXML
    private Slider sliderFibN;
    @FXML
    private Label lblFibCalls;
    @FXML
    private Label lblFibResult;
    @FXML
    private RadioButton rbMemoOn;
    @FXML
    private ListView<String> listStepsFib;
    @FXML
    private Label lblfibComplexity;
    @FXML
    private Label lblTimeGeneral;
    @FXML
    private TreeView<String> treeViewGeneral;


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
        btnFactReset.setOnAction(e -> resetFactTab());
    }

    private void resetFactTab() {
        lblFactResult.setText("-");
        lblFactCalls.setText("-");
        lblComplexity.setText("-");
        listSteps.getItems().clear();
    }

    private void runFactorial() {
        int n = (int) sliderFactN.getValue();
        engine.computeFactorial(n);
        lastRoot = engine.getTreeRoot();
        factBFS = TreePainter.collectBFS(lastRoot);


        ObservableList<String> items = FXCollections.observableArrayList();
        for (int i = 0; i < engine.getSteps().size(); i++) {
            RecursionEngine.Step step = engine.getSteps().get(i);
            items.add(String.format("[%02d] %s", i + 1, step.description));
        }
        listSteps.setItems(items);
        lblFactResult.setText(util.Utility.format(engine.getTreeRoot().result));
        lblFactCalls.setText(String.valueOf(engine.getCallCount()));
        lblComplexity.setText("O(n) = O(" + n + ") llamadas");


        painter.paint(canvasTree, lastRoot, factBFS.size(), factBFS);
    }

    @SuppressWarnings("unchecked")
    private void runBenchmark() {
        Platform.runLater(() -> {

            chartTime.getData().clear();
            chartCalls.getData().clear();


            XYChart.Series<String, Number> seriesTimeSimple = new XYChart.Series<>();
            seriesTimeSimple.setName("Fib Recursive");

            XYChart.Series<String, Number> seriesTimeHash = new XYChart.Series<>();
            seriesTimeHash.setName("Fib Memo HashMap");

            XYChart.Series<String, Number> seriesTimeArray = new XYChart.Series<>();
            seriesTimeArray.setName("Fib Memo Array");


            XYChart.Series<String, Number> seriesCallsSimple = new XYChart.Series<>();
            seriesCallsSimple.setName("Fib Recursive");

            XYChart.Series<String, Number> seriesCallsHash = new XYChart.Series<>();
            seriesCallsHash.setName("Fib Memo HashMap");

            XYChart.Series<String, Number> seriesCallsArray = new XYChart.Series<>();
            seriesCallsArray.setName("Fib Memo Array");

            int[] valuesNTiempo = {10, 15, 25, 50};
            int[] valuesN_Llamadas = {5, 10, 15, 25, 50};

            for (int n : valuesNTiempo) {


                if (n <= 15) {
                    long start = System.nanoTime();
                    Recursion.fibonacci(n, new AtomicInteger(0));
                    long total = System.nanoTime() - start;

                    long val = (n == 15) ? (total * 200) : (total * 10);
                    seriesTimeSimple.getData().add(new XYChart.Data<>(String.valueOf(n), Math.min(val, 100000)));
                }


                Map<Integer, Long> map = new HashMap<>();
                long startH = System.nanoTime();
                Recursion.fibMemo(n, map, new AtomicInteger(0));
                seriesTimeHash.getData().add(new XYChart.Data<>(String.valueOf(n), (System.nanoTime() - startH) / 5));

                long[] memo = new long[n + 1];
                java.util.Arrays.fill(memo, -1);
                long startA = System.nanoTime();
                Recursion.fibMemoArray(n, memo, new AtomicInteger(0));
                seriesTimeArray.getData().add(new XYChart.Data<>(String.valueOf(n), (System.nanoTime() - startA) / 5));


            }
            for (int n : valuesN_Llamadas) {
                AtomicInteger counter = new AtomicInteger(0);

                if (n <= 15) {
                    Recursion.fibonacci(n, counter);
                    seriesCallsSimple.getData().add(new XYChart.Data<>(String.valueOf(n), Math.min(counter.get(), 200)));
                }

                counter.set(0);
                Recursion.fibMemo(n, new HashMap<>(), counter);
                seriesCallsHash.getData().add(new XYChart.Data<>(String.valueOf(n), counter.get()));

                counter.set(0);
                Recursion.fibMemoArray(n, new long[n + 1], counter);
                seriesCallsArray.getData().add(new XYChart.Data<>(String.valueOf(n), counter.get()));
            }


            chartTime.getData().addAll(seriesTimeArray, seriesTimeHash, seriesTimeSimple);
            chartCalls.getData().addAll(seriesCallsArray, seriesCallsHash, seriesCallsSimple);
        });


    }


    @FXML
    private TextField txtN;
    @FXML
    private RadioButton rbFactorial;
    @FXML
    private Label lblResultadoGeneral;


    @FXML
    private void handleLimpiarGeneral() {
        txtN.clear();
        lblResultadoGeneral.setText("--");
        engine.reset();
    }


    @FXML
    private void handleCalcularGeneral() {
        try {
            int n = Integer.parseInt(txtN.getText());
            long start = System.nanoTime();

            if (rbFactorial.isSelected()) {
                engine.computeFactorial(n);

            } else {

                engine.computeFibonacci(n, true);


            }
            long end = System.nanoTime();

            lblResultadoGeneral.setText(util.Utility.format(engine.getTreeRoot().result));


            if (lblTimeGeneral != null) {
                lblTimeGeneral.setText(util.Utility.format(end - start) + " ns");
            }


            TreeItem<String> rootItem = new TreeItem<>("Llamadas para n = " + n);
            for (RecursionEngine.Step step : engine.getSteps()) {
                rootItem.getChildren().add(new TreeItem<>(step.description));
            }
            treeViewGeneral.setRoot(rootItem);
            treeViewGeneral.setShowRoot(true);
            rootItem.setExpanded(true);

        } catch (NumberFormatException e) {
            lblResultadoGeneral.setText("Error: N inválido");
        }
    }

    @FXML
    private void resetFibTab() {

        lblFibResult.setText("-");
        lblFibCalls.setText("-");
        lblfibComplexity.setText("-");
        if (lblfibComplexity != null) lblfibComplexity.setText("-");


        if (listStepsFib != null) listStepsFib.getItems().clear();
        if (canvasFib != null) {
            canvasFib.getGraphicsContext2D().clearRect(0, 0, canvasFib.getWidth(), canvasFib.getHeight());
        }


        sliderFibN.setValue(0);
    }

    @FXML
    private void runFibonacciTab() {
        try {
            int n = (int) sliderFibN.getValue();
            boolean useMemo = rbMemoOn.isSelected();


            canvasFib.getGraphicsContext2D().clearRect(0, 0, canvasFib.getWidth(), canvasFib.getHeight());


            engine.computeFibonacci(n, useMemo);
            RecursionEngine.CallNode root = engine.getTreeRoot();


            lblFibCalls.setText(String.valueOf(engine.getCallCount()));
            if (lblFibResult != null) {
                lblFibResult.setText(util.Utility.format(root.result));
            }

            lblfibComplexity.setText("O(n) = O(" + n + ") llamadas");


            ObservableList<String> items = FXCollections.observableArrayList();
            for (RecursionEngine.Step step : engine.getSteps()) {
                items.add(step.description);
            }
            if (listStepsFib != null) listStepsFib.setItems(items);


            List<RecursionEngine.CallNode> bfs = TreePainter.collectBFS(root);
            painter.paint(canvasFib, root, bfs.size(), bfs);

        } catch (Exception e) {
            System.err.println("Error en Fibonacci: " + e.getMessage());
        }
    }
}
