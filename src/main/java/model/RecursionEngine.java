package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Motor de visualización de recursividad para la interfaz JavaFX.
 *
 * Mientras que {@link Recursion} implementa los algoritmos de forma pura,
 * esta clase los reimplementa con estado interno para poder:
 *   1. Construir un árbol de llamadas ({@link CallNode}) que refleja la jerarquía de
 *      invocaciones recursivas.
 *   2. Registrar cada paso del algoritmo ({@link Step}) con descripción,
 *      expresión matemática y resultado parcial, para mostrarlos en la UI paso a paso.
 *   3. Contar el total de llamadas recursivas realizadas.
 *   4. Soportar memorización (memoización) en Fibonacci y marcar qué nodos
 *      fueron resueltos desde la caché.
 *
 * Uso típico:
 *   RecursionEngine engine = new RecursionEngine();
 *   long result = engine.computeFactorial(5);
 *   // Luego consultar engine.getSteps(), engine.getTreeRoot(), engine.getCallCount()
 */
public class RecursionEngine {

    // ── Clases internas ──────────────────────────────────────────────────────

    /**
     * Representa un nodo en el árbol de llamadas recursivas.
     *
     * Cada vez que el algoritmo invoca una función recursiva se crea un CallNode
     * con la etiqueta de esa llamada (p.ej. "fact(3)") y se enlaza como hijo
     * del nodo que la originó. Al retornar, se almacena el resultado calculado.
     */
    public static class CallNode {
        /** Etiqueta legible de la llamada, p.ej. "fact(3)" o "fib(5)". */
        public final String label;

        /** Valor de n para esta llamada (permite al TreePainter identificar el nodo). */
        public final int n;

        /** Lista de llamadas recursivas generadas por este nodo (hijos en el árbol). */
        public final List<CallNode> children = new ArrayList<>();

        /** Resultado calculado; -1 mientras no se haya retornado aún. */
        public long result = -1;

        /**
         * Indica si este nodo fue resuelto mediante memorización (caché).
         * El {@link TreePainter} lo usa para pintarlo con color diferente (COL_MEMO).
         */
        public boolean fromMemo = false;

        /** Profundidad del nodo en el árbol (raíz = 0). Usada para calcular posición Y. */
        public int depth;

        public CallNode(String label, int n, int depth) {
            this.label = label;
            this.n = n;
            this.depth = depth;
        }
    }

    /**
     * Captura el estado de un paso individual durante la ejecución del algoritmo.
     *
     * La lista de pasos ({@link #steps}) permite reproducir la ejecución en la UI
     * de forma animada: cada Step describe qué ocurrió en ese momento (una nueva
     * llamada, un caso base, un retorno, o un resultado recuperado de caché).
     */
    public static class Step {
        /** Descripción en lenguaje natural de lo que ocurre en este paso. */
        public final String description;

        /** Expresión matemática asociada, p.ej. "5 * 4 * 3 * 2 * 1". */
        public final String expression;

        /** Resultado parcial de este paso; -1 si aún no hay valor calculado. */
        public final long partialResult;

        /** true si este paso provino de la caché de memorización. */
        public final boolean isMemo;

        /** Número acumulado de llamadas recursivas hasta este paso. */
        public final int callCount;

        public Step(String description, String expression, long result, boolean isMemo, int callCount) {
            this.description = description;
            this.expression = expression;
            this.partialResult = result;
            this.isMemo = isMemo;
            this.callCount = callCount;
        }
    }

    // ── Estado interno del motor ─────────────────────────────────────────────

    /** Caché de resultados Fibonacci ya calculados (clave = n, valor = fib(n)). */
    private final Map<Integer, Long> memo = new HashMap<>();

    /** Número total de llamadas recursivas realizadas en el último cálculo. */
    private int callCount;

    /** Secuencia de pasos registrados durante el último cálculo. */
    private final List<Step> steps = new ArrayList<>();

    /** Raíz del árbol de llamadas generado durante el último cálculo. */
    private CallNode treeRoot;

    // ── Ciclo de vida ────────────────────────────────────────────────────────

    /**
     * Reinicia todos los datos del motor antes de un nuevo cálculo.
     * Debe llamarse al inicio de cada método {@code compute*} para evitar
     * que resultados anteriores contaminen los nuevos.
     */
    public void reset() {
        memo.clear();
        callCount = 0;
        treeRoot = null;
        steps.clear();
    }

    // ── Factorial ────────────────────────────────────────────────────────────

    /**
     * Punto de entrada público para calcular el factorial de n.
     *
     * Resetea el estado interno, crea la raíz del árbol de llamadas,
     * delega el cálculo al método privado recursivo y agrega un paso
     * final con el resultado completo.
     *
     * @param n número entero no negativo
     * @return  n!
     */
    public long computeFactorial(int n) {
        reset();
        treeRoot = new CallNode("fact(" + n + ")", n, 0);
        long result = factorial(n, treeRoot, 0);
        // Paso final: muestra el resultado completo con notación factorial
        steps.add(new Step("Resultado final", n + "!= " + result, result, false, callCount));
        return result;
    }

    /**
     * Implementación recursiva del factorial con instrumentación para la UI.
     *
     * En cada llamada:
     *   1. Incrementa el contador global de llamadas.
     *   2. Registra un Step de "llamada" con la expresión expandida.
     *   3. Evalúa el caso base (n <= 1) o crea un hijo en el árbol y recursa.
     *   4. Al retornar, registra un Step de "retorno" con el resultado parcial.
     *
     * @param n      valor actual de la recursión
     * @param parent nodo del árbol correspondiente a esta llamada
     * @param depth  profundidad actual (para construir el árbol)
     * @return       n!
     */
    private long factorial(int n, CallNode parent, int depth) {
        callCount++;
        String label = " fact(" + n + ") ";

        // Registra la entrada a esta llamada con la expresión completa (p.ej. "5 * 4 * 3 * 2 * 1")
        steps.add(new Step("LLamada No #" + callCount + ":" + label,
                buildFactExpression(n), -1, false, callCount));

        if (n <= 1) {
            // Caso base: factorial(1) = factorial(0) = 1
            parent.result = 1;
            steps.add(new Step("Caso Base: " + label + " = 1", "factorial(1) = 1", 1, false, callCount));
            return 1;
        }

        // Caso recursivo: crea un nodo hijo para la llamada factorial(n-1)
        CallNode child = new CallNode("factorial(" + (n - 1) + ")", n - 1, depth + 1);
        parent.children.add(child);
        long sub = factorial(n - 1, child, depth + 1);

        long result = (long) n * sub;
        parent.result = result;

        // Registra el retorno: muestra la multiplicación y su resultado
        steps.add(new Step("Retorno: " + label + " = " + n + " * " + sub + " = " + result,
                label + " = " + result, result, false, callCount));
        return result;
    }

    /**
     * Construye la expresión matemática expandida del factorial de n.
     * Por ejemplo, para n=4 devuelve "4 * 3 * 2 * 1".
     * Se usa en los Steps para mostrar la fórmula completa antes de calcularla.
     *
     * @param n número del cual se expande el factorial
     * @return  cadena con la multiplicación encadenada, o "1" si n <= 1
     */
    private String buildFactExpression(int n) {
        if (n <= 1) return "1";
        StringBuilder sb = new StringBuilder();
        for (int i = n; i >= 1; i--) {
            sb.append(i);
            if (i > 1) sb.append(" * ");
        }
        return sb.toString();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    /** @return caché de memorización actual (solo relevante para Fibonacci con memo). */
    public Map<Integer, Long> getMemo() {
        return memo;
    }

    /** @return número total de llamadas recursivas del último cálculo. */
    public int getCallCount() {
        return callCount;
    }

    /** @return lista de pasos registrados, en orden cronológico de ejecución. */
    public List<Step> getSteps() {
        return steps;
    }

    /** @return raíz del árbol de llamadas construido durante el último cálculo. */
    public CallNode getTreeRoot() {
        return treeRoot;
    }

    // ── Fibonacci ────────────────────────────────────────────────────────────

    /**
     * Punto de entrada público para calcular el n-ésimo número de Fibonacci.
     *
     * @param n        posición en la secuencia (0-indexado)
     * @param useMemo  si true, activa la memorización para evitar recalcular subproblemas
     * @return         fib(n)
     */
    public long computeFibonacci(int n, boolean useMemo) {
        reset(); // Limpia estados previos para no contaminar el nuevo cálculo
        treeRoot = new CallNode("fib(" + n + ")", n, 0);
        long result = fibonacci(n, treeRoot, 0, useMemo);
        steps.add(new Step("Resultado final", "fib(" + n + ") = " + result, result, useMemo, callCount));
        return result;
    }

    /**
     * Implementación recursiva de Fibonacci con instrumentación para la UI.
     *
     * El flujo de cada llamada es:
     *   1. Incrementa el contador de llamadas.
     *   2. Si la memorización está activa y el valor ya fue calculado, lo devuelve
     *      de la caché y marca el nodo como {@code fromMemo = true}.
     *   3. Registra un Step de "llamada".
     *   4. Evalúa el caso base (n <= 1).
     *   5. En el caso recursivo crea dos hijos (izquierdo para n-1, derecho para n-2),
     *      recursa en ambos y suma sus resultados.
     *   6. Si la memorización está activa, almacena el resultado en el mapa.
     *   7. Registra un Step de "retorno" con la suma.
     *
     * @param n        valor actual de la recursión
     * @param parent   nodo del árbol correspondiente a esta llamada
     * @param depth    profundidad actual en el árbol
     * @param useMemo  si true, consulta y actualiza la caché de memorización
     * @return         fib(n)
     */
    private long fibonacci(int n, CallNode parent, int depth, boolean useMemo) {
        callCount++;
        String label = "fib(" + n + ")";

        // ── 1. Consulta de memorización ──────────────────────────────────────
        // Si ya calculamos fib(n) antes, lo recuperamos del mapa en O(1)
        if (useMemo && memo.containsKey(n)) {
            long mRes = memo.get(n);
            parent.result = mRes;
            parent.fromMemo = true; // El TreePainter pintará este nodo con COL_MEMO (naranja)
            steps.add(new Step("Memo: " + label, "Recuperado de caché: " + mRes, mRes, true, callCount));
            return mRes;
        }

        // Registra la entrada a esta llamada antes de evaluar el caso base
        steps.add(new Step("Llamada #" + callCount + ": " + label, "Calculando " + label, -1, false, callCount));

        // ── 2. Caso base ─────────────────────────────────────────────────────
        // fib(0) = 0, fib(1) = 1
        if (n <= 1) {
            parent.result = n;
            steps.add(new Step("Caso Base: " + label + " = " + n, label + " = " + n, n, false, callCount));
            return n;
        }

        // ── 3. Llamadas recursivas (árbol binario) ───────────────────────────
        // Hijo izquierdo: calcula fib(n-1)
        CallNode leftChild = new CallNode("fib(" + (n - 1) + ")", n - 1, depth + 1);
        parent.children.add(leftChild);
        long a = fibonacci(n - 1, leftChild, depth + 1, useMemo);

        // Hijo derecho: calcula fib(n-2)
        CallNode rightChild = new CallNode("fib(" + (n - 2) + ")", n - 2, depth + 1);
        parent.children.add(rightChild);
        long b = fibonacci(n - 2, rightChild, depth + 1, useMemo);

        // ── 4. Retorno y almacenamiento ──────────────────────────────────────
        long result = a + b;
        parent.result = result;

        // Guarda en caché para que llamadas futuras con el mismo n sean O(1)
        if (useMemo) {
            memo.put(n, result);
        }

        steps.add(new Step("Retorno: " + label + " = " + a + " + " + b + " = " + result,
                label + " = " + result, result, false, callCount));
        return result;
    }
}