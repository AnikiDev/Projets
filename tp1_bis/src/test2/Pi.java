package test2;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Approximates PI using the Monte Carlo method.  Demonstrates
 * use of Callables, Futures, and thread pools.
 */
public class Pi 
{
    public static void main(String[] args) throws Exception 
    {
	long total;
	// 10 workers, 50000 iterations each
	total = new Master2().doRun(50000, 10);
	System.out.println("total from classes.Master = " + total);
    }
}

class Master2 {

    /**
     * Exécute une simulation Monte Carlo en parallèle.
     *
     * @param totalCount nombre total d'itérations (pour strong scaling)
     * @param numWorkers nombre de threads
     * @return somme des points dans le cercle
     */
    public long doRun(int totalCount, int numWorkers) throws InterruptedException, ExecutionException {
        // Diviser le travail pour strong scaling
        int workPerThread = totalCount / numWorkers;

        List<Callable<Long>> tasks = new ArrayList<>();
        for (int i = 0; i < numWorkers; ++i) {
            tasks.add(new Worker2(workPerThread));
        }

        ExecutorService exec = Executors.newFixedThreadPool(numWorkers);
        List<Future<Long>> results = exec.invokeAll(tasks);

        long total = 0;
        for (Future<Long> f : results) {
            total += f.get();
        }

        exec.shutdown();
        return total;
    }
}


class Worker2 implements Callable<Long> {
    /**
     * Représente une tâche Monte Carlo exécutée par un thread.
     * Cette tâche génère numIterations points aléatoires
     * et compte combien tombent dans le quart de cercle unité.
     */
    private int numIterations; // nombre de points à simuler

    public Worker2(int num) {
        this.numIterations = num;
    }

    @Override
    public Long call() {
        long circleCount = 0;        // nombre de points dans le cercle
        Random prng = new Random();  // générateur aléatoire propre à ce thread

        for (int j = 0; j < numIterations; j++) {
            double x = prng.nextDouble();
            double y = prng.nextDouble();
            if (x * x + y * y < 1)
                circleCount++;
        }

        return circleCount;
    }
}
