package test3;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Master3 {

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
            tasks.add(new Worker3(workPerThread));
        }

        ExecutorService exec = Executors.newFixedThreadPool(numWorkers);
        List<Future<Long>> results = exec.invokeAll(tasks);

        long total = 0;
        for (Future<Long> f : results) {
            total += f.get();
        }

        double pi = 4.0 * total / totalCount / numWorkers;

        long stopTime = System.currentTimeMillis();

        exec.shutdown();
        return total;
    }
}
