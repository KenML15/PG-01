package model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Suite de pruebas para los algoritmos recursivos de la clase {@link Recursion}.
 *
 * Cada test ejecuta el algoritmo correspondiente sobre una lista de entradas
 * predefinidas e imprime en consola:
 *   - El resultado calculado.
 *   - La cantidad total de llamadas recursivas realizadas.
 *   - El tiempo de ejecución en nanosegundos.
 *
 * El objetivo principal es comparar el comportamiento (número de llamadas y
 * tiempo) entre las variantes naive y con memorización de Fibonacci, y verificar
 * que factorial y matryoshka funcionan correctamente.
 *
 * Nota: estos tests no usan aserciones (assertions) formales; su salida es
 * informativa y debe revisarse manualmente en la consola de JUnit.
 */
class RecursionTest {

    /**
     * Prueba el cálculo de factorial para los valores {5, 10, 12, 15, 20}.
     *
     * Para cada n se crea un contador nuevo (AtomicInteger) que registra cuántas
     * veces se invocó el método recursivo. Se mide el tiempo con System.nanoTime()
     * antes y después de la llamada.
     *
     * Salida esperada en consola (ejemplo para n=5):
     *   "Factorial 5 es: 120 | Total de llamadas recursivas: 5 | T(n): ... ns"
     *
     * El número de llamadas debe ser exactamente n (complejidad O(n)).
     */
    @Test
    void factorialTest() {
        int[] list = {5, 10, 12, 15, 20};

        for (int n : list) {
            AtomicInteger counter = new AtomicInteger(0); // Contador reiniciado para cada n
            long t1 = System.nanoTime();
            long result = Recursion.factorial(n, counter);
            long t2 = System.nanoTime();

            System.out.println("Factorial " + n + " es: " + util.Utility.format(result) +
                    "| Total de llamadas recursivas: " + util.Utility.format(counter.get()) +
                    "| T(n): " + util.Utility.format(t2 - t1) + " ns");
        }
    }

    /**
     * Prueba la versión de matryoshka que imprime directamente en consola.
     *
     * Llama a {@link Recursion#matryoshka(int)} con n=5 y verifica visualmente
     * que se impriman 5 mensajes en orden descendente, comenzando por "muñeca 5"
     * y terminando con "muñeca más pequeña: 1".
     *
     * No hay valor de retorno que verificar; la corrección se evalúa leyendo la salida.
     */
    @Test
    void matryoshkaTest() {
        Recursion.matryoshka(5);
    }

    /**
     * Prueba la versión de matryoshka que retorna la secuencia como String.
     *
     * Llama a {@link Recursion#matryoshkaS(int)} con n=5 e imprime el resultado.
     * El String devuelto debe contener los mismos mensajes que {@link #matryoshkaTest},
     * pero concatenados en una sola cadena con saltos de línea.
     *
     * Útil para verificar que la versión sin System.out produce la misma secuencia
     * que la versión con impresión directa.
     */
    @Test
    void matryoshkaSTest() {
        System.out.println(Recursion.matryoshkaS(5));
    }

    /**
     * Prueba el cálculo de Fibonacci naive (sin memorización) para {5, 10, 12, 15, 20}.
     *
     * Al no usar caché, el árbol de llamadas es binario completo: cada llamada
     * genera dos subproblemas, lo que resulta en ~2^n llamadas totales.
     * Esto se vuelve notablemente lento a partir de n=20 y se agudiza para n>30.
     *
     * Comparar el contador de llamadas de este test con {@link #fibMemoTest} y
     * {@link #fibMemoArrayTest} ilustra claramente la ventaja de la memorización.
     *
     * Complejidad esperada: O(2^n) llamadas.
     */
    @Test
    void fibonacciTest() {
        int[] list = {5, 10, 12, 15, 20};

        for (int n : list) {
            AtomicInteger counter = new AtomicInteger(0); // Contador reiniciado para cada n
            long t1 = System.nanoTime();
            long result = Recursion.fibonacci(n, counter);
            long t2 = System.nanoTime();

            System.out.println("Fibonacci " + n + " es: " + util.Utility.format(result) +
                    "| Total de llamadas recursivas: " + util.Utility.format(counter.get()) +
                    "| T(n): " + util.Utility.format(t2 - t1) + " ns");
        }
    }

    /**
     * Prueba el cálculo de Fibonacci con memorización usando HashMap para {5, 10, 12, 15, 20}.
     *
     * Se crea un Map<Integer, Long> nuevo por cada valor de n para que la caché no
     * se comparta entre iteraciones (cada prueba arranca sin resultados previos).
     *
     * La memorización evita recalcular subproblemas: cada fib(k) se calcula una sola
     * vez y se guarda en el mapa. Esto reduce las llamadas de O(2^n) a O(n).
     *
     * Al comparar con {@link #fibonacciTest}, se observa una reducción drástica tanto
     * en el número de llamadas como en el tiempo de ejecución.
     *
     * Complejidad esperada: O(n) llamadas.
     */
    @Test
    void fibMemoTest() {
        int[] list = {5, 10, 12, 15, 20};

        for (int n : list) {
            AtomicInteger counter = new AtomicInteger(0);
            Map<Integer, Long> memo = new HashMap<>(); // Caché vacía por cada n
            long t1 = System.nanoTime();
            long result = Recursion.fibMemo(n, memo, counter);
            long t2 = System.nanoTime();

            System.out.println("Fibonacci Memoria " + n + " es: " + util.Utility.format(result) +
                    "| Total de llamadas recursivas: " + util.Utility.format(counter.get()) +
                    "| T(n): " + util.Utility.format(t2 - t1) + " ns");
        }
    }

    /**
     * Prueba el cálculo de Fibonacci con memorización usando arreglo para {5, 10, 12, 15, 20}.
     *
     * Equivalente a {@link #fibMemoTest} pero usa un arreglo de long en lugar de un HashMap.
     * Antes de cada llamada, el arreglo se inicializa completamente en -1 (valor centinela
     * que indica "no calculado aún"), ya que 0 y 1 son resultados válidos de Fibonacci.
     *
     * El arreglo tiene tamaño n+1 para poder indexar desde memo[0] hasta memo[n].
     *
     * Ventajas frente al HashMap:
     *   - Acceso por índice en O(1) sin overhead de hashing.
     *   - Menor uso de memoria al evitar boxing de Integer/Long.
     *
     * El número de llamadas debería ser igual al de {@link #fibMemoTest},
     * pero el tiempo puede ser menor por las optimizaciones de acceso al arreglo.
     *
     * Complejidad esperada: O(n) llamadas.
     */
    @Test
    void fibMemoArrayTest() {
        int[] list = {5, 10, 12, 15, 20};

        for (int n : list) {
            AtomicInteger counter = new AtomicInteger(0);
            long[] memo = new long[n + 1]; // Tamaño n+1 para índices 0..n

            // Inicializa todo en -1 para distinguir "no calculado" de fib(0)=0
            for (int i = 0; i < n + 1; i++) {
                memo[i] = -1;
            }

            long t1 = System.nanoTime();
            long result = Recursion.fibMemoArray(n, memo, counter);
            long t2 = System.nanoTime();

            System.out.println("Fibonacci Memoria Array" + n + " es: " + util.Utility.format(result) +
                    "| Total de llamadas recursivas: " + util.Utility.format(counter.get()) +
                    "| T(n): " + util.Utility.format(t2 - t1) + " ns");
        }
    }
}