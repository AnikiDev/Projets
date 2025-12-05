package classes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

class PiMonteCarlo {

    // AtomicInteger permet de gérer une variable partagée entre plusieurs threads
    // sans devoir utiliser de verrous explicites (synchronisation). Les opérations
    // d’incrémentation sont atomiques → évite les conditions de course.
    AtomicInteger nAtomSuccess; // nombre de points tombés dans le quart de cercle
    int nThrows;                // nombre total de lancers Monte-Carlo
    double value;               // valeur finale approximée de π

    // Classe interne représentant une tâche Monte-Carlo exécutée par un thread
    class MonteCarlo implements Runnable {
        @Override
        // La méthode run() correspond à une seule itération du lancer de points
        public void run() {
            // Génération aléatoire d'un point (x, y) dans le carré unité [0,1] × [0,1]
            double x = Math.random();
            double y = Math.random();

            // Si le point est dans le cercle unité : x² + y² ≤ 1, alors il est compté
            if (x * x + y * y <= 1)
                // Incrément atomique du compteur → thread-safe
                nAtomSuccess.incrementAndGet();
        }
    }

    // Constructeur : initialise les compteurs
    public PiMonteCarlo(int i) { // i = nombre total de lancers (ntot)
        this.nAtomSuccess = new AtomicInteger(0); // compteur initialisé à zéro
        this.nThrows = i;                         // on stocke le nombre de lancers
        this.value = 0;                           // la valeur de π sera calculée plus tard
    }

    // Méthode principale : exécute le Monte-Carlo en parallèle et renvoie π estimé
    public double getPi() {

        // Récupère le nombre de cœurs disponibles pour optimiser le parallélisme
        int nProcessors = Runtime.getRuntime().availableProcessors();

        // Pool de threads utilisant un mécanisme de "work stealing"
        // → les threads récupèrent des tâches aux autres quand ils ont fini,
        // améliorant la charge et les performances sur CPU multicœur.
        ExecutorService executor = Executors.newWorkStealingPool(nProcessors);

        // Lancer nThrows tâches en parallèle, chacune étant une simulation Monte-Carlo
        for (int i = 1; i <= nThrows; i++) {
            Runnable worker = new MonteCarlo(); // tâche représentant un lancer
            executor.execute(worker);           // soumission au pool
        }

        // Plus aucune tâche ne sera soumise
        executor.shutdown();

        // On attend que toutes les tâches soient terminées
        while (!executor.isTerminated()) {
            // boucle vide : on attend la fin de toutes les exécutions
        }

        // Formule Monte-Carlo pour π : Pi ≈ 4 * (nombre de succès) / (nombre total de tirs)
        value = 4.0 * nAtomSuccess.get() / nThrows;

        return value; // renvoie l'estimation finale de π
    }
}
