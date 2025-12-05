package classe2;

import java.util.Random;
import java.util.concurrent.Callable;

/**
 * Représente une tâche Monte Carlo exécutée par un thread.
 * Cette tâche génère numIterations points aléatoires
 * et compte combien tombent dans le quart de cercle unité.
 */
public class Worker implements Callable<Long> {

    private final int numIterations; // nombre de points à simuler

    public Worker(int num) {
        this.numIterations = num;
    }

    @Override
    public Long call() {

        long circleCount = 0;         // nombre de points dans le cercle
        Random prng = new Random();   // générateur aléatoire propre à ce thread

        // Boucle Monte-Carlo
        for (int j = 0; j < numIterations; j++) {

            double x = prng.nextDouble(); // tirage entre 0 et 1
            double y = prng.nextDouble();

            // Vérifie si le point est dans le quart de cercle : x² + y² < 1
            if (x * x + y * y < 1)
                circleCount++;
        }

        // Callable impose de renvoyer un Long (objet)
        return circleCount;
    }
}