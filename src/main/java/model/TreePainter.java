package model;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.*;

/**
 * Dibuja el árbol de llamadas recursivas en un Canvas de JavaFX.
 * Calcula posiciones (x,y) usando un recorrido post-order para asignar
 * espacios de forma que los subárboles no se solapen.
 */
public class TreePainter {

    // ── Colores ───────────────────────────────────────────────────────────────
    private static final Color COL_NORMAL   = Color.web("#1F3868");
    private static final Color COL_BASE     = Color.web("#1A8C7B");
    private static final Color COL_MEMO     = Color.web("#E8A020");
    private static final Color COL_RESULT   = Color.web("#2E5FAC");
    private static final Color COL_EDGE     = Color.web("#8896A5");
    private static final Color COL_TEXT     = Color.WHITE;
    private static final Color COL_BG       = Color.web("#F4F6FA");

    private static final double NODE_R  = 26;   // radio del nodo
    private static final double H_GAP   = 18;   // espacio horizontal mínimo entre nodos
    private static final double V_GAP   = 70;   // espacio vertical entre niveles

    // ── Posición calculada para cada nodo ─────────────────────────────────────
    private final Map<RecursionEngine.CallNode, double[]> positions = new HashMap<>();

    public void paint(Canvas canvas, RecursionEngine.CallNode root,
                      int highlightStep, List<RecursionEngine.CallNode> visitedOrder) {
        if (root == null) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        double W = canvas.getWidth();
        double H = canvas.getHeight();

        gc.setFill(COL_BG);
        gc.fillRect(0, 0, W, H);

        positions.clear();
        // ── Asignar posiciones ──────────────────────────────────────────────
        double[] counter = {0};
        assignX(root, counter);
        double totalWidth = counter[0];
        double offsetX = Math.max(0, (W - totalWidth) / 2.0) + NODE_R;
        shiftX(root, offsetX, 0);

        // ── Dibujar aristas primero ─────────────────────────────────────────
        gc.setStroke(COL_EDGE);
        gc.setLineWidth(1.5);
        drawEdges(gc, root);

        // ── Dibujar nodos ───────────────────────────────────────────────────
        Set<RecursionEngine.CallNode> visited = new HashSet<>(
                visitedOrder.subList(0, Math.min(highlightStep, visitedOrder.size())));
        drawNodes(gc, root, visited, highlightStep, visitedOrder);
    }

    // ── Asignar coordenada X con algoritmo de árbol simple ───────────────────
    private void assignX(RecursionEngine.CallNode node, double[] counter) {
        if (node.children.isEmpty()) {
            double x = counter[0] * (NODE_R * 2 + H_GAP) + NODE_R;
            positions.put(node, new double[]{x, 0});
            counter[0]++;
        } else {
            for (RecursionEngine.CallNode child : node.children)
                assignX(child, counter);
            double firstX = positions.get(node.children.get(0))[0];
            double lastX  = positions.get(node.children.get(node.children.size()-1))[0];
            positions.put(node, new double[]{(firstX + lastX) / 2.0, 0});
        }
    }

    private void shiftX(RecursionEngine.CallNode node, double ox, int depth) {
        double[] pos = positions.get(node);
        if (pos != null) {
            pos[0] += (depth == 0 ? 0 : 0);   // raíz ya centrada
            pos[1]  = depth * V_GAP + NODE_R + 10;
        }
        for (RecursionEngine.CallNode child : node.children)
            shiftX(child, ox, depth + 1);
    }

    // ── Dibujar aristas ───────────────────────────────────────────────────────
    private void drawEdges(GraphicsContext gc, RecursionEngine.CallNode node) {
        double[] pos = positions.get(node);
        if (pos == null) return;
        for (RecursionEngine.CallNode child : node.children) {
            double[] cp = positions.get(child);
            if (cp != null) {
                gc.setStroke(COL_EDGE);
                gc.strokeLine(pos[0], pos[1], cp[0], cp[1]);
            }
            drawEdges(gc, child);
        }
    }

    // ── Dibujar nodos ─────────────────────────────────────────────────────────
    private void drawNodes(GraphicsContext gc, RecursionEngine.CallNode node,
                           Set<RecursionEngine.CallNode> visited,
                           int highlightStep,
                           List<RecursionEngine.CallNode> order) {
        double[] pos = positions.get(node);
        if (pos == null) return;

        boolean isVisited   = visited.contains(node);
        boolean isHighlight = order.size() > 0 &&
                              highlightStep > 0 &&
                              highlightStep <= order.size() &&
                              order.get(highlightStep - 1) == node;

        // Elegir color
        Color fill;
        if (isHighlight)          fill = Color.web("#E74C3C");
        else if (node.fromMemo)   fill = COL_MEMO;
        else if (node.children.isEmpty() && isVisited) fill = COL_BASE;
        else if (isVisited)       fill = COL_RESULT;
        else                      fill = COL_NORMAL.deriveColor(0, 1, 1, 0.3);

        // Sombra
        gc.setFill(Color.color(0, 0, 0, 0.12));
        gc.fillOval(pos[0] - NODE_R + 3, pos[1] - NODE_R + 3, NODE_R * 2, NODE_R * 2);

        // Círculo
        gc.setFill(fill);
        gc.fillOval(pos[0] - NODE_R, pos[1] - NODE_R, NODE_R * 2, NODE_R * 2);

        // Borde
        gc.setStroke(isHighlight ? Color.web("#FFCDD2") : Color.WHITE);
        gc.setLineWidth(isHighlight ? 3 : 1.5);
        gc.strokeOval(pos[0] - NODE_R, pos[1] - NODE_R, NODE_R * 2, NODE_R * 2);

        // Texto (label encima, resultado abajo)
        gc.setFill(COL_TEXT);
        gc.setFont(Font.font("Calibri", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(node.label, pos[0], pos[1] + (node.result >= 0 ? -3 : 4));

        if (node.result >= 0 && isVisited) {
            gc.setFont(Font.font("Calibri", FontWeight.NORMAL, 10));
            gc.setFill(Color.web("#FFD700"));
            gc.fillText("= " + node.result, pos[0], pos[1] + 10);
        }

        for (RecursionEngine.CallNode child : node.children)
            drawNodes(gc, child, visited, highlightStep, order);
    }

    // ── Recopilar nodos en orden BFS para la animación ────────────────────────
    public static List<RecursionEngine.CallNode> collectBFS(RecursionEngine.CallNode root) {
        List<RecursionEngine.CallNode> list = new ArrayList<>();
        if (root == null) return list;
        Queue<RecursionEngine.CallNode> q = new LinkedList<>();
        q.add(root);
        while (!q.isEmpty()) {
            RecursionEngine.CallNode n = q.poll();
            list.add(n);
            q.addAll(n.children);
        }
        return list;
    }
}
