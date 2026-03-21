package controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    @javafx.fxml.FXML
    private Canvas canvasFib;
    @javafx.fxml.FXML
    private Slider sliderFibN;
    @javafx.fxml.FXML
    private Label lblFibCalls;
    @javafx.fxml.FXML
    private Label lblFibResult; // Corregido para coincidir con FXML
    @javafx.fxml.FXML
    private RadioButton rbMemoOn;
    @javafx.fxml.FXML
    private ListView<String> listStepsFib;
    @javafx.fxml.FXML
    private Label lblfibComplexity;
    @javafx.fxml.FXML
    private Label lblTimeGeneral;
    @javafx.fxml.FXML
    private TreeView<String> treeViewGeneral;



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

        //llenamos la lista de pasos
        ObservableList<String> items = FXCollections.observableArrayList();
        for (int i = 0; i < engine.getSteps().size(); i++) {
            RecursionEngine.Step step = engine.getSteps().get(i);
            items.add(String.format("[%02d] %s", i + 1, step.description));
        }
        listSteps.setItems(items); //setteamos la lista de pasos recursivos
        lblFactResult.setText(util.Utility.format(engine.getTreeRoot().result));
        lblFactCalls.setText(String.valueOf(engine.getCallCount()));
        lblComplexity.setText("O(n) = O(" + n + ") llamadas");

        //dibujamos el árbol de llamadas en el canva
        painter.paint(canvasTree, lastRoot, factBFS.size(), factBFS);
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
                seriesTimeHash.getData().add(new XYChart.Data<>(String.valueOf(n), (System.nanoTime() - startH) / 5));

                long[] memo = new long[n + 1];
                java.util.Arrays.fill(memo, -1);
                long startA = System.nanoTime();
                Recursion.fibMemoArray(n, memo, new AtomicInteger(0));
                seriesTimeArray.getData().add(new XYChart.Data<>(String.valueOf(n), (System.nanoTime() - startA) / 5));


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
                Recursion.fibMemoArray(n, new long[n + 1], counter);
                seriesCallsArray.getData().add(new XYChart.Data<>(String.valueOf(n), counter.get()));
            }

            //se agrega a los graficos los calculos
            chartTime.getData().addAll(seriesTimeArray, seriesTimeHash, seriesTimeSimple);
            chartCalls.getData().addAll(seriesCallsArray, seriesCallsHash, seriesCallsSimple);
        });


    }

    private void runFibonacci() {
        int n = (int) sliderFactN.getValue(); // Puedes usar el mismo slider o crear uno nuevo
        boolean useMemo = true; // Esto podría venir de un CheckBox en la UI

        engine.computeFibonacci(n, useMemo);
        lastRoot = engine.getTreeRoot();
        factBFS = TreePainter.collectBFS(lastRoot);

        // Actualizar UI
        ObservableList<String> items = FXCollections.observableArrayList();
        for (RecursionEngine.Step step : engine.getSteps()) {
            items.add(step.description);
        }
        listSteps.setItems(items);

        lblFactResult.setText(String.valueOf(lastRoot.result));
        lblFactCalls.setText(String.valueOf(engine.getCallCount()));

        // Dibujar el árbol binario de Fibonacci
        painter.paint(canvasTree, lastRoot, factBFS.size(), factBFS);
    }

    @javafx.fxml.FXML
    private TextField txtN;
    @javafx.fxml.FXML
    private RadioButton rbFactorial;
    @javafx.fxml.FXML
    private Label lblResultadoGeneral;



    @javafx.fxml.FXML
    private void handleLimpiarGeneral() {
        txtN.clear();
        lblResultadoGeneral.setText("--");
        engine.reset();
    }


    @javafx.fxml.FXML
    private void handleFibonacciTab() {
        int n = (int) sliderFibN.getValue();
        boolean useMemo = rbMemoOn.isSelected();

        // Limpiar canvas antes de dibujar
        canvasFib.getGraphicsContext2D().clearRect(0, 0, canvasFib.getWidth(), canvasFib.getHeight());

        engine.computeFibonacci(n, useMemo);
        lastRoot = engine.getTreeRoot();

        // Mostramos cuántas llamadas tomó (aquí verás la magia de la memoización)
        lblFibCalls.setText(String.valueOf(engine.getCallCount()));

        // Dibujar
        List<RecursionEngine.CallNode> bfs = TreePainter.collectBFS(lastRoot);
        painter.paint(canvasFib, lastRoot, bfs.size(), bfs);
    }

    // Método para la pestaña Fact-Fib (Imagen 3)
    @javafx.fxml.FXML
    private void handleCalcularGeneral() {
        try {
            int n = Integer.parseInt(txtN.getText());
            long start = System.nanoTime();

            if (rbFactorial.isSelected()) {
                engine.computeFactorial(n);

            } else {
                // Fibonacci en la general suele ser con memo para no congelar la app
                engine.computeFibonacci(n, true);
                // ASIGNAR COMPLEJIDAD AQUÍ

            }
            long end = System.nanoTime();

            // Actualizar Resultado
            lblResultadoGeneral.setText(util.Utility.format(engine.getTreeRoot().result));

            // Actualizar Tiempo (Asegúrate de que lblTimeGeneral esté inyectado con @FXML)
            if (lblTimeGeneral != null) {
                lblTimeGeneral.setText(util.Utility.format(end - start) + " ns");
            }

            // Llenar el TreeView (Estructura de cascada)
            TreeItem<String> rootItem = new TreeItem<>("Llamadas para n = " + n);
            for (RecursionEngine.Step step : engine.getSteps()) {
                rootItem.getChildren().add(new TreeItem<>(step.description));
            }
            treeViewGeneral.setRoot(rootItem);
            treeViewGeneral.setShowRoot(true);
            rootItem.setExpanded(true); // Para que aparezca desplegado

        } catch (NumberFormatException e) {
            lblResultadoGeneral.setText("Error: N inválido");
        }
    }

    @javafx.fxml.FXML
    private void resetFibTab() {
        // 1. Limpiar textos
        lblFibResult.setText("-");
        lblFibCalls.setText("-");
        lblfibComplexity.setText("-");
        if (lblfibComplexity != null) lblfibComplexity.setText("-");

        // 2. Limpiar lista y dibujo
        if (listStepsFib != null) listStepsFib.getItems().clear();
        if (canvasFib != null) {
            canvasFib.getGraphicsContext2D().clearRect(0, 0, canvasFib.getWidth(), canvasFib.getHeight());
        }

        // 3. Resetear control
        sliderFibN.setValue(0);
    }

    // Método para la pestaña Fibonacci (Imagen 1)
    @javafx.fxml.FXML
    private void runFibonacciTab() {
        try {
            int n = (int) sliderFibN.getValue();
            boolean useMemo = rbMemoOn.isSelected();

            // 1. Limpiar el área de dibujo
            canvasFib.getGraphicsContext2D().clearRect(0, 0, canvasFib.getWidth(), canvasFib.getHeight());

            // 2. Calcular la lógica
            engine.computeFibonacci(n, useMemo);
            RecursionEngine.CallNode root = engine.getTreeRoot();

            // 3. Actualizar la Interfaz de Usuario
            lblFibCalls.setText(String.valueOf(engine.getCallCount()));
            if (lblFibResult != null) {
                lblFibResult.setText(util.Utility.format(root.result));
            }

            lblfibComplexity.setText("O(n) = O(" + n + ") llamadas");

            // 4. Llenar la lista de pasos (opcional pero recomendado)
            ObservableList<String> items = FXCollections.observableArrayList();
            for (RecursionEngine.Step step : engine.getSteps()) {
                items.add(step.description);
            }
            if (listStepsFib != null) listStepsFib.setItems(items);

            // 5. Dibujar el árbol visual
            List<RecursionEngine.CallNode> bfs = TreePainter.collectBFS(root);
            painter.paint(canvasFib, root, bfs.size(), bfs);

        } catch (Exception e) {
            System.err.println("Error en Fibonacci: " + e.getMessage());
        }
    }
}
