package model;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clase utilitaria con implementaciones puras (sin estado) de algoritmos recursivos clásicos.
 * Cada método recibe un contador atómico para registrar cuántas llamadas recursivas se realizan,
 * lo que permite comparar el costo computacional entre variantes.
 */
public class Recursion {

    /**
     * Calcula el factorial de n de forma recursiva.
     *
     * Caso base : n <= 1  →  retorna 1  (0! = 1! = 1)
     * Caso recursivo : n * factorial(n-1)
     *
     * Complejidad temporal: O(n) — se hacen exactamente n llamadas recursivas.
     * Complejidad espacial: O(n) — se apilan n marcos en el call stack.
     *
     * @param n       número entero no negativo del que se calcula el factorial
     * @param counter contador compartido que se incrementa en cada llamada recursiva
     * @return        n!
     */
    public static long factorial(int n, AtomicInteger counter) {
        counter.incrementAndGet(); // Registra esta llamada recursiva
        if (n <= 1) return 1;     // Caso base: factorial de 0 o 1 es 1
        return n * factorial(n - 1, counter); // Caso recursivo: n * (n-1)!
    }

    /**
     * Simula la apertura de muñecas Matryoshka de forma recursiva, imprimiendo
     * en consola el proceso nivel a nivel.
     *
     * Caso base : n <= 1  →  anuncia la muñeca más pequeña y se detiene.
     * Caso recursivo : anuncia la muñeca actual y abre la siguiente (n-1).
     *
     * Este método es un ejemplo didáctico de recursión lineal de cola
     * (no acumula valor de retorno).
     *
     * @param n número de muñecas (nivel actual)
     */
    public static void matryoshka(int n) {
        if (n <= 1) {
            // Caso base: llegamos a la muñeca más pequeña
            System.out.println("abriendo la muñeca mas pequeña: " + n);
        } else {
            // Caso recursivo: anuncia la muñeca actual antes de abrir la siguiente
            System.out.println("abriendo la muñeca número: " + n);
            matryoshka(n - 1);
        }
    }

    /**
     * Versión de {@link #matryoshka(int)} que devuelve el resultado como String
     * en lugar de imprimir directamente en consola.
     *
     * Útil para integración con la interfaz gráfica (JavaFX), donde no se puede
     * usar System.out para mostrar texto al usuario.
     *
     * La cadena se construye acumulando el mensaje de cada nivel de forma recursiva:
     * el nivel actual se concatena con el resultado del nivel inferior (n-1).
     *
     * @param n número de muñecas (nivel actual)
     * @return  cadena con el recorrido completo de apertura de muñecas
     */
    public static String matryoshkaS(int n) {
        if (n <= 1) {
            // Caso base: retorna el mensaje de la muñeca más pequeña
            return ("\n Abriendo la muñeca mas pequeña: " + n);
        } else {
            // Caso recursivo: prepende el mensaje del nivel actual al resultado del nivel inferior
            return ("\n Abriendo la muñeca número: " + n + matryoshkaS(n - 1));
        }
    }

    /**
     * Calcula el n-ésimo número de Fibonacci de forma recursiva naive (sin memorización).
     *
     * Caso base : n <= 1  →  retorna n  (fib(0)=0, fib(1)=1)
     * Caso recursivo : fib(n-1) + fib(n-2)
     *
     * ADVERTENCIA: su árbol de llamadas es binario, por lo que el número de llamadas
     * crece exponencialmente (~2^n). Para valores grandes de n esto se vuelve muy lento.
     *
     * Complejidad temporal: O(2^n)
     * Complejidad espacial: O(n) — profundidad máxima del call stack
     *
     * @param n       posición en la secuencia Fibonacci (0-indexado)
     * @param counter contador compartido que se incrementa en cada llamada recursiva
     * @return        fib(n)
     */
    public static long fibonacci(int n, AtomicInteger counter) {
        counter.incrementAndGet(); // Registra esta llamada (permite medir el costo real)
        if (n <= 1) return n;     // Caso base: fib(0)=0, fib(1)=1
        return fibonacci(n - 1, counter) + fibonacci(n - 2, counter); // Suma las dos ramas
    }

    /**
     * Calcula el n-ésimo número de Fibonacci con memorización usando un HashMap.
     *
     * Antes de calcular, verifica si el resultado ya existe en el mapa {@code memo}.
     * Si existe, lo devuelve inmediatamente evitando el recálculo (O(1) extra por consulta).
     * Si no existe, lo calcula recursivamente y lo almacena en el mapa antes de retornar.
     *
     * Esto reduce drásticamente el número de llamadas: cada valor de n se calcula una
     * sola vez, bajando la complejidad de O(2^n) a O(n).
     *
     * Complejidad temporal: O(n)
     * Complejidad espacial: O(n) — para el mapa y el call stack
     *
     * @param n       posición en la secuencia Fibonacci (0-indexado)
     * @param memo    mapa compartido que actúa como caché de resultados ya calculados
     * @param counter contador compartido que se incrementa en cada llamada recursiva
     * @return        fib(n)
     */
    public static long fibMemo(int n, Map<Integer, Long> memo, AtomicInteger counter) {
        counter.incrementAndGet(); // Cuenta la llamada aunque venga de caché
        if (n <= 1) return n;     // Caso base

        // Si el valor ya fue calculado anteriormente, se recupera directamente del mapa
        if (memo.containsKey(n)) return memo.get(n);

        // Calcula recursivamente y guarda en el mapa antes de retornar
        long result = fibMemo(n - 1, memo, counter) + fibMemo(n - 2, memo, counter);
        memo.put(n, result); // Almacena en caché para evitar recalcular en el futuro
        return result;
    }

    /**
     * Calcula el n-ésimo número de Fibonacci con memorización usando un arreglo.
     *
     * Funciona igual que {@link #fibMemo(int, Map, AtomicInteger)}, pero usa un
     * arreglo de long en lugar de un HashMap. Esto mejora el rendimiento porque:
     * - El acceso por índice es O(1) sin hashing.
     * - Menos overhead de memoria al evitar objetos Integer/Long en el heap.
     *
     * El arreglo debe tener tamaño n+1 e inicializarse completamente con -1 antes
     * de la primera llamada, para distinguir "no calculado" de un resultado real (0 o más).
     *
     * Complejidad temporal: O(n)
     * Complejidad espacial: O(n)
     *
     * @param n       posición en la secuencia Fibonacci (0-indexado)
     * @param memo    arreglo de caché inicializado en -1; memo[i] guarda fib(i) una vez calculado
     * @param counter contador compartido que se incrementa en cada llamada recursiva
     * @return        fib(n)
     */
    public static long fibMemoArray(int n, long[] memo, AtomicInteger counter) {
        counter.incrementAndGet(); // Cuenta la llamada incluso si viene de caché
        if (n <= 1) return n;     // Caso base

        // Si memo[n] != -1 significa que ya fue calculado; se retorna directamente
        if (memo[n] != -1) return memo[n];

        // Calcula y almacena en el arreglo antes de retornar
        memo[n] = fibMemoArray(n - 1, memo, counter) + fibMemoArray(n - 2, memo, counter);
        return memo[n];
    }
}