package test3;

import java.util.Random;
import java.util.concurrent.Callable;

public class Worker3 implements Callable<Long> {
    /**
     * Représente une tâche Monte Carlo exécutée par un thread.
     * Cette tâche génère numIterations points aléatoires
     * et compte combien tombent dans le quart de cercle unité.
     */
    private int numIterations; // nombre de points à simuler

    public Worker3(int num) {
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
