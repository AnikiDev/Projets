package test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class PiMonte_Carlo {

    AtomicInteger nAtomSuccess; // Nombre de points dans le quart de cercle
    int nThrows;                // Nombre total de lancers Monte-Carlo
    double value;               // Valeur finale approximée de π

    // Classe interne représentant un lancer Monte-Carlo
    class MonteCarlo implements Runnable {
        @Override
        public void run() {
            double x = Math.random();
            double y = Math.random();

            if (x * x + y * y <= 1)
                nAtomSuccess.incrementAndGet(); // Incrément atomique
        }
    }

    // Constructeur
    public PiMonte_Carlo(int nThrows) {
        this.nAtomSuccess = new AtomicInteger(0);
        this.nThrows = nThrows;
        this.value = 0;
    }

    // Méthode pour calculer π en parallèle
    public double getPi(int nbThreads) {

        // Créer un pool de threads avec le nombre de threads donné
        ExecutorService executor = Executors.newWorkStealingPool(nbThreads);

        // Lancer nThrows tâches
        for (int i = 0; i < nThrows; i++) {
            executor.execute(new MonteCarlo());
        }

        // Fin de soumission des tâches
        executor.shutdown();

        // Attendre que toutes les tâches soient terminées
        while (!executor.isTerminated()) {
            // boucle vide
        }

        // Calcul Monte-Carlo de π
        value = 4.0 * nAtomSuccess.get() / nThrows;

        return value;
    }
}

