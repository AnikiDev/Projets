package test;

import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Représente une tâche Monte Carlo exécutée par un thread.
 * Cette tâche génère numIterations points aléatoires
 * et compte combien tombent dans le quart de cercle unité.
 */
public class Worker2 implements Callable<Long> {

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
