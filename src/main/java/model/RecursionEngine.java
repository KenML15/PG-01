package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Motor de recursividad: implementa factorial y fibonacci en java FX
 */

public class RecursionEngine {

    //nodo para un árbol de llamadas recursivas

    public static class CallNode{
        public final String label; //fact(3
        public final int n;
        public final List<CallNode> children= new ArrayList<>();
        public long result= -1;
        public boolean fromMemo= false; //para fib con memorizacion
        public int depth;

        public CallNode(String label, int n, int depth) {
            this.label = label;
            this.n = n;
            this.depth=depth;
        }

    }
    //datos de cada llamada recursiva

    public static class Step{
        public final String description;
        public final String expression;
        public final long partialResult;
        public final boolean isMemo;
        public final int callCount; //contador de llamadas recursivas

        public Step(String description, String expression, long result, boolean isMemo, int callCount) {
            this.description = description;
            this.expression = expression;
            this.partialResult = result;
            this.isMemo = isMemo;
            this.callCount = callCount;
        }
    }

    //atributos del estado interno
    private final Map<Integer,Long> memo= new HashMap<>(); //long resultado
    private int callCount;
    private final List<Step> steps= new ArrayList<>();
    private CallNode treeRoot; //raiz del arbol de llamadas

    public void reset(){
        memo.clear();
        callCount=0;
        treeRoot=null;
        steps.clear();
    }

    /**
     * factorial
     */

    public long computeFactorial(int n){
        reset();
        treeRoot =new CallNode("fact("+n+")",n,0);
        long result= factorial(n,treeRoot,0);
        steps.add(new Step("Resultado final",n+"!= "+result, result, false, callCount)); //simbolo factorial= !

        return result;

    }

    private long factorial(int n, CallNode parent, int depth) {
        callCount++;
        String label= " fact("+n+") ";
        steps.add(new Step("LLamada No #"+callCount+ ":"+label,
                buildFactExpression(n),-1,false,callCount));
        if(n<=1){
            parent.result= 1;
            steps.add(new Step("Caso Base: "+label+" = 1","factorial(1) = 1",1,false,callCount));
            return 1;
        }
        CallNode child = new CallNode("factorial("+(n-1)+")",n-1,depth+1);
        parent.children.add(child);
        long sub= factorial(n-1,child,depth+1);
        long result= (long) n*sub;
        parent.result= result;

        steps.add(new Step("Retorno: "+label+" = "+ n+ " * "+sub+" = "+result, label+ " = "+result,result,false,callCount));
        return result;

    }

    private String buildFactExpression(int n) {
        if(n<=1) return "1";
        StringBuilder sb= new StringBuilder();

        for(int i=n; i>=1; i--){
            sb.append(i);

            if(i>1) sb.append(" * ");
        }
        return sb.toString();
    }

    public Map<Integer, Long> getMemo() {
        return memo;
    }

    public int getCallCount() {
        return callCount;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public CallNode getTreeRoot() {
        return treeRoot;
    }

    public long computeFibonacci(int n, boolean useMemo) {
        reset(); // Limpia estados previos
        treeRoot = new CallNode("fib(" + n + ")", n, 0);
        long result = fibonacci(n, treeRoot, 0, useMemo);
        steps.add(new Step("Resultado final", "fib(" + n + ") = " + result, result, useMemo, callCount));
        return result;
    }

    private long fibonacci(int n, CallNode parent, int depth, boolean useMemo) {
        callCount++;
        String label = "fib(" + n + ")";

        // 1. Verificar Memoización (si está activa)
        if (useMemo && memo.containsKey(n)) {
            long mRes = memo.get(n);
            parent.result = mRes;
            parent.fromMemo = true; // El TreePainter usará COL_MEMO
            steps.add(new Step("Memo: " + label, "Recuperado de caché: " + mRes, mRes, true, callCount));
            return mRes;
        }

        steps.add(new Step("Llamada #" + callCount + ": " + label, "Calculando " + label, -1, false, callCount));

        // 2. Casos Base
        if (n <= 1) {
            parent.result = n;
            steps.add(new Step("Caso Base: " + label + " = " + n, label + " = " + n, n, false, callCount));
            return n;
        }

        // 3. Llamadas Recursivas (Árbol Binario)
        // Hijo Izquierdo: n-1
        CallNode leftChild = new CallNode("fib(" + (n - 1) + ")", n - 1, depth + 1);
        parent.children.add(leftChild);
        long a = fibonacci(n - 1, leftChild, depth + 1, useMemo);

        // Hijo Derecho: n-2
        CallNode rightChild = new CallNode("fib(" + (n - 2) + ")", n - 2, depth + 1);
        parent.children.add(rightChild);
        long b = fibonacci(n - 2, rightChild, depth + 1, useMemo);

        // 4. Retorno y Almacenamiento
        long result = a + b;
        parent.result = result;

        if (useMemo) {
            memo.put(n, result);
        }

        steps.add(new Step("Retorno: " + label + " = " + a + " + " + b + " = " + result, label + " = " + result, result, false, callCount));
        return result;
    }
}
