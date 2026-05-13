package model;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.*;

/**
 * Dibuja el árbol de llamadas recursivas en un {@link Canvas} de JavaFX.
 *
 * Responsabilidades:
 *   1. Calcular la posición (x, y) de cada nodo usando un recorrido post-order,
 *      de modo que los subárboles no se solapen horizontalmente.
 *   2. Dibujar las aristas (líneas) entre nodos padre e hijo.
 *   3. Dibujar cada nodo como un círculo coloreado según su estado:
 *      - Azul oscuro  (COL_NORMAL)  : nodo aún no visitado en la animación.
 *      - Azul claro   (COL_RESULT)  : nodo ya visitado (retornó un valor).
 *      - Verde        (COL_BASE)    : nodo hoja visitado (caso base).
 *      - Naranja      (COL_MEMO)    : nodo resuelto desde la caché de memorización.
 *      - Rojo         (#E74C3C)     : nodo destacado en el paso actual de la animación.
 *   4. Mostrar dentro de cada nodo su etiqueta (p.ej. "fib(3)") y, si ya fue
 *      visitado, su resultado calculado.
 *   5. Proveer el método estático {@link #collectBFS} para obtener el orden de
 *      recorrido BFS que la UI usa para animar el árbol paso a paso.
 */
public class TreePainter {

    // ── Paleta de colores ─────────────────────────────────────────────────────
    /** Nodo no visitado (estado inicial). */
    private static final Color COL_NORMAL = Color.web("#1F3868");

    /** Nodo hoja visitado (caso base alcanzado). */
    private static final Color COL_BASE   = Color.web("#1A8C7B");

    /** Nodo resuelto desde la caché de memorización. */
    private static final Color COL_MEMO   = Color.web("#E8A020");

    /** Nodo interno ya visitado (retornó un resultado). */
    private static final Color COL_RESULT = Color.web("#2E5FAC");

    /** Color de las aristas (líneas entre nodos). */
    private static final Color COL_EDGE   = Color.web("#8896A5");

    /** Color del texto dentro de los nodos. */
    private static final Color COL_TEXT   = Color.WHITE;

    /** Color de fondo del Canvas. */
    private static final Color COL_BG     = Color.web("#F4F6FA");

    // ── Parámetros de geometría ───────────────────────────────────────────────
    /** Radio de cada nodo circular en píxeles. */
    private static final double NODE_R = 26;

    /** Espacio horizontal mínimo entre nodos hermanos (en píxeles). */
    private static final double H_GAP  = 18;

    /** Distancia vertical entre niveles del árbol (en píxeles). */
    private static final double V_GAP  = 70;

    // ── Estado interno ────────────────────────────────────────────────────────
    /**
     * Mapa de posiciones calculadas para cada nodo.
     * Clave: nodo; Valor: arreglo [x, y] en coordenadas del Canvas.
     * Se resetea al inicio de cada llamada a {@link #paint}.
     */
    private final Map<RecursionEngine.CallNode, double[]> positions = new HashMap<>();

    // ── Método principal de dibujo ────────────────────────────────────────────

    /**
     * Renderiza el árbol completo de llamadas recursivas en el Canvas dado.
     *
     * Pasos internos:
     *   1. Limpia el Canvas con el color de fondo.
     *   2. Calcula las coordenadas X de cada nodo con {@link #assignX} (post-order).
     *   3. Centra el árbol horizontalmente y asigna las coordenadas Y con {@link #shiftX}.
     *   4. Dibuja las aristas con {@link #drawEdges}.
     *   5. Dibuja los nodos con {@link #drawNodes}, coloreando según el estado de animación.
     *
     * @param canvas        Canvas de JavaFX donde se dibuja
     * @param root          raíz del árbol de llamadas (puede ser null si no hay cálculo)
     * @param highlightStep índice 1-based del paso actual en la animación; el nodo en esa
     *                      posición del orden {@code visitedOrder} se pinta de rojo
     * @param visitedOrder  lista de nodos en el orden en que serán visitados durante la
     *                      animación (generada por {@link #collectBFS})
     */
    public void paint(Canvas canvas, RecursionEngine.CallNode root,
                      int highlightStep, List<RecursionEngine.CallNode> visitedOrder) {
        if (root == null) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        double W = canvas.getWidth();
        double H = canvas.getHeight();

        // Limpia el lienzo con el color de fondo antes de redibujar
        gc.setFill(COL_BG);
        gc.fillRect(0, 0, W, H);

        positions.clear();

        // ── Calcular posiciones ───────────────────────────────────────────────
        // counter[0] actúa como contador de hojas vistas; cada hoja ocupa una "columna"
        double[] counter = {0};
        assignX(root, counter);
        double totalWidth = counter[0];

        // Desplazamiento horizontal para centrar el árbol en el Canvas
        double offsetX = Math.max(0, (W - totalWidth) / 2.0) + NODE_R;
        shiftX(root, offsetX, 0);

        // ── Dibujar aristas primero (quedan detrás de los nodos) ─────────────
        gc.setStroke(COL_EDGE);
        gc.setLineWidth(1.5);
        drawEdges(gc, root);

        // ── Dibujar nodos encima de las aristas ──────────────────────────────
        // El conjunto "visited" contiene los nodos que ya se mostraron en la animación
        Set<RecursionEngine.CallNode> visited = new HashSet<>(
                visitedOrder.subList(0, Math.min(highlightStep, visitedOrder.size())));
        drawNodes(gc, root, visited, highlightStep, visitedOrder);
    }

    // ── Cálculo de posiciones ─────────────────────────────────────────────────

    /**
     * Asigna la coordenada X a cada nodo usando un recorrido post-order.
     *
     * Algoritmo:
     *   - Las hojas reciben una X proporcional a su posición de llegada (counter[0]).
     *     Cada hoja ocupa exactamente un "slot" horizontal de ancho (NODE_R*2 + H_GAP).
     *   - Los nodos internos se centran entre el X de su hijo más a la izquierda
     *     y el X de su hijo más a la derecha.
     *
     * Esto garantiza que subárboles no se solapen, independientemente de su forma.
     *
     * @param node    nodo actual
     * @param counter contador de hojas procesadas (se modifica in-place)
     */
    private void assignX(RecursionEngine.CallNode node, double[] counter) {
        if (node.children.isEmpty()) {
            // Hoja: ocupa el slot actual y avanza el contador
            double x = counter[0] * (NODE_R * 2 + H_GAP) + NODE_R;
            positions.put(node, new double[]{x, 0});
            counter[0]++;
        } else {
            // Nodo interno: primero asigna X a todos los hijos (post-order)
            for (RecursionEngine.CallNode child : node.children)
                assignX(child, counter);
            // Luego se centra entre el primer y último hijo
            double firstX = positions.get(node.children.get(0))[0];
            double lastX  = positions.get(node.children.get(node.children.size() - 1))[0];
            positions.put(node, new double[]{(firstX + lastX) / 2.0, 0});
        }
    }

    /**
     * Desplaza el árbol horizontalmente (agrega {@code ox} a todos los X)
     * y asigna la coordenada Y a cada nodo según su profundidad en el árbol.
     *
     * Y = depth * V_GAP + NODE_R + 10  →  deja un margen superior de 10px.
     *
     * @param node  nodo actual
     * @param ox    desplazamiento horizontal (para centrar el árbol en el Canvas)
     * @param depth profundidad del nodo actual (raíz = 0)
     */
    private void shiftX(RecursionEngine.CallNode node, double ox, int depth) {
        double[] pos = positions.get(node);
        if (pos != null) {
            // La coordenada X ya fue correctamente centrada en assignX; solo se fija Y
            pos[1] = depth * V_GAP + NODE_R + 10;
        }
        for (RecursionEngine.CallNode child : node.children)
            shiftX(child, ox, depth + 1);
    }

    // ── Dibujo de aristas ─────────────────────────────────────────────────────

    /**
     * Dibuja recursivamente una línea desde cada nodo hacia cada uno de sus hijos.
     * Se llama antes de {@link #drawNodes} para que las aristas queden por debajo
     * de los círculos de los nodos.
     *
     * @param gc   contexto gráfico del Canvas
     * @param node nodo actual (origen de las aristas)
     */
    private void drawEdges(GraphicsContext gc, RecursionEngine.CallNode node) {
        double[] pos = positions.get(node);
        if (pos == null) return;
        for (RecursionEngine.CallNode child : node.children) {
            double[] cp = positions.get(child);
            if (cp != null) {
                // Línea del centro del nodo padre al centro del nodo hijo
                gc.setStroke(COL_EDGE);
                gc.strokeLine(pos[0], pos[1], cp[0], cp[1]);
            }
            drawEdges(gc, child); // Continúa recursivamente hacia los nietos
        }
    }

    // ── Dibujo de nodos ───────────────────────────────────────────────────────

    /**
     * Dibuja cada nodo como un círculo con sombra, borde, etiqueta y resultado.
     *
     * El color del círculo depende del estado del nodo en la animación:
     *   - Rojo        : nodo actualmente destacado (paso {@code highlightStep}).
     *   - Naranja     : nodo resuelto desde caché (fromMemo = true).
     *   - Verde       : hoja visitada (caso base alcanzado).
     *   - Azul claro  : nodo interno ya visitado.
     *   - Azul oscuro semitransparente : nodo aún no visitado.
     *
     * Dentro del círculo se muestra:
     *   - Siempre: la etiqueta del nodo (p.ej. "fib(3)"), en blanco.
     *   - Si fue visitado y tiene resultado: el valor calculado en dorado.
     *
     * @param gc            contexto gráfico del Canvas
     * @param node          nodo actual a dibujar
     * @param visited       conjunto de nodos ya mostrados en la animación
     * @param highlightStep índice 1-based del paso actual
     * @param order         lista BFS de nodos en orden de animación
     */
    private void drawNodes(GraphicsContext gc, RecursionEngine.CallNode node,
                           Set<RecursionEngine.CallNode> visited,
                           int highlightStep,
                           List<RecursionEngine.CallNode> order) {
        double[] pos = positions.get(node);
        if (pos == null) return;

        boolean isVisited   = visited.contains(node);
        // isHighlight: true solo para el nodo en la posición exacta de highlightStep
        boolean isHighlight = order.size() > 0 &&
                highlightStep > 0 &&
                highlightStep <= order.size() &&
                order.get(highlightStep - 1) == node;

        // ── Elegir color de relleno según estado ──────────────────────────────
        Color fill;
        if (isHighlight)                           fill = Color.web("#E74C3C");  // Rojo: paso actual
        else if (node.fromMemo)                    fill = COL_MEMO;              // Naranja: desde caché
        else if (node.children.isEmpty() && isVisited) fill = COL_BASE;         // Verde: caso base visitado
        else if (isVisited)                        fill = COL_RESULT;            // Azul: interno visitado
        else                                       fill = COL_NORMAL.deriveColor(0, 1, 1, 0.3); // Semitransparente

        // ── Sombra suave (círculo negro desplazado 3px) ───────────────────────
        gc.setFill(Color.color(0, 0, 0, 0.12));
        gc.fillOval(pos[0] - NODE_R + 3, pos[1] - NODE_R + 3, NODE_R * 2, NODE_R * 2);

        // ── Círculo principal ─────────────────────────────────────────────────
        gc.setFill(fill);
        gc.fillOval(pos[0] - NODE_R, pos[1] - NODE_R, NODE_R * 2, NODE_R * 2);

        // ── Borde del círculo (más grueso y claro si está destacado) ──────────
        gc.setStroke(isHighlight ? Color.web("#FFCDD2") : Color.WHITE);
        gc.setLineWidth(isHighlight ? 3 : 1.5);
        gc.strokeOval(pos[0] - NODE_R, pos[1] - NODE_R, NODE_R * 2, NODE_R * 2);

        // ── Etiqueta del nodo (p.ej. "fib(3)") en texto blanco ───────────────
        gc.setFill(COL_TEXT);
        gc.setFont(Font.font("Calibri", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.CENTER);
        // Si hay resultado que mostrar, sube la etiqueta 3px para hacerle espacio
        gc.fillText(node.label, pos[0], pos[1] + (node.result >= 0 ? -3 : 4));

        // ── Resultado calculado (en dorado, debajo de la etiqueta) ───────────
        // Solo se muestra si el nodo ya fue visitado y tiene un resultado válido
        if (node.result >= 0 && isVisited) {
            gc.setFont(Font.font("Calibri", FontWeight.NORMAL, 10));
            gc.setFill(Color.web("#FFD700")); // Dorado para resaltar el valor
            gc.fillText("= " + node.result, pos[0], pos[1] + 10);
        }

        // Dibuja recursivamente todos los nodos hijos
        for (RecursionEngine.CallNode child : node.children)
            drawNodes(gc, child, visited, highlightStep, order);
    }

    // ── Utilidad de recorrido ─────────────────────────────────────────────────

    /**
     * Recorre el árbol de llamadas en orden BFS (anchura) y devuelve la lista
     * de nodos en ese orden.
     *
     * La UI usa esta lista para animar el árbol nivel por nivel: en el paso i
     * se colorea el nodo {@code list.get(i-1)} como "activo" (rojo) y todos los
     * anteriores como "visitados".
     *
     * BFS garantiza que los nodos de niveles superiores aparezcan antes que los
     * de niveles inferiores, lo que produce una animación descendente natural.
     *
     * @param root raíz del árbol; si es null se devuelve lista vacía
     * @return     lista de nodos en orden BFS
     */
    public static List<RecursionEngine.CallNode> collectBFS(RecursionEngine.CallNode root) {
        List<RecursionEngine.CallNode> list = new ArrayList<>();
        if (root == null) return list;

        Queue<RecursionEngine.CallNode> q = new LinkedList<>();
        q.add(root);
        while (!q.isEmpty()) {
            RecursionEngine.CallNode n = q.poll();
            list.add(n);
            q.addAll(n.children); // Encola todos los hijos para procesarlos en el siguiente nivel
        }
        return list;
    }
}