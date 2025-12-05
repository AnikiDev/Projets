package classes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


/**
 * Classe classes.Master
 * -------------
 * Cette classe gère la création et la coordination de plusieurs threads (workers)
 * pour exécuter une simulation Monte Carlo en parallèle.
 * Elle se charge aussi d’agréger les résultats et d’afficher différentes métriques.
 */
class Master {

    /**
     * Lance une simulation Monte Carlo en utilisant plusieurs workers (threads)
     *
     * @param totalCount nombre total d’itérations par worker
     * @param numWorkers nombre de threads à utiliser
     *
     * @return la somme totale des points tombés dans le cercle
     *
     * @throws InterruptedException si l’attente des threads est interrompue
     * @throws ExecutionException si une erreur survient dans un worker
     */
    public long doRun(int totalCount, int numWorkers) throws InterruptedException, ExecutionException {

        // Enregistre le temps de départ pour calculer la durée de la simulation
        long startTime = System.currentTimeMillis();

        // ============================
        // 1) Création des tâches
        // ============================

        // Liste de tâches de type Callable<Long>
        // Chaque classes.Worker renverra un nombre (points dans le cercle)
        List<Callable<Long>> tasks = new ArrayList<Callable<Long>>();

        for (int i = 0; i < numWorkers; ++i) {
            // Chaque worker reçoit totalCount comme nombre d'essais à réaliser
            tasks.add(new Worker(totalCount));
        }

        // ============================
        // 2) Création du pool de threads
        // ============================

        // Création d’un pool fixe de threads contenant numWorkers threads.
        // => permet l’exécution en parallèle des workers.
        ExecutorService exec = Executors.newFixedThreadPool(numWorkers);

        // invokeAll() exécute *toutes* les tâches et retourne une liste de Future
        // Chaque Future représente le résultat d’un worker (non encore disponible).
        List<Future<Long>> results = exec.invokeAll(tasks);

        long total = 0;

        // ============================
        // 3) Récupération des résultats
        // ============================

        for (Future<Long> f : results) {

            // f.get() est un point de synchronisation :
            // - attend que le worker ait fini son calcul
            // - récupère la valeur renvoyée par le worker
            //
            // Cette instruction bloque jusqu’à ce que la tâche associée soit terminée.
            total += f.get();
        }

        // ============================
        // 4) Calcul de π (approximation)
        // ============================

        // total = somme des points tombés dans le cercle
        // totalCount = nombre d’essais par worker
        // numWorkers = nombre de workers
        //
        // -> Monte Carlo : π ≈ 4 × (points_dans_cercle / points_totaux)
        double pi = 4.0 * total / totalCount / numWorkers;

        long stopTime = System.currentTimeMillis();

        // ============================
        // 5) Affichage des résultats
        // ============================

        System.out.println("\ntest.Pi : " + pi);

        // Erreur relative entre la valeur calculée et la valeur réelle de π
        System.out.println("Error: " + (Math.abs((pi - Math.PI)) / Math.PI) + "\n");

        // Total d’essais réalisés
        System.out.println("Ntot: " + totalCount * numWorkers);

        // Affiche le nombre de threads utilisés
        System.out.println("Available processors: " + numWorkers);

        // Durée totale d’exécution
        System.out.println("Time Duration (ms): " + (stopTime - startTime) + "\n");

        // Ligne supplémentaire compacte (souvent utilisée pour exporter en CSV)
        System.out.println(
                (Math.abs((pi - Math.PI)) / Math.PI)
                        + " " + totalCount * numWorkers
                        + " " + numWorkers
                        + " " + (stopTime - startTime)
        );

        // ============================
        // 6) Arrêt du pool de threads
        // ============================

        exec.shutdown(); // Arrête proprement les threads après l'exécution

        return total;
    }
}
