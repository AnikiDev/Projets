import java.io.*;
import java.net.Socket;

public class MasterStrong {

    static final int MAX_WORKERS = 8;
    static final int[] PORTS = {25545, 25546, 25547, 25548, 25549, 25550, 25551, 25552};
    static final String IP = "192.168.24.19";
    static final int TOTAL_POINTS = 16000000;  // Total de lancers à répartir entre les workers

    public static void appendCSV(String filename, String line) {
        try (FileWriter fw = new FileWriter(filename, true)) {
            fw.write(line + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

        System.out.println("#########################################");
        System.out.println("#     SCALABILITÉ FORTE      #");
        System.out.println("#########################################\n");

        String csvFile = "scalabilite_strong.csv";
        appendCSV(csvFile, "ntot,nth,tps_ms");

        for (int numWorkers = 1; numWorkers <= MAX_WORKERS; numWorkers++) {

            // Calculer le nombre de lancers par worker
            int pointsPerWorker = TOTAL_POINTS / numWorkers;

            System.out.println("\n===== TEST AVEC " + numWorkers + " WORKER(S) =====");

            Socket[] sockets = new Socket[numWorkers];
            BufferedReader[] readers = new BufferedReader[numWorkers];
            PrintWriter[] writers = new PrintWriter[numWorkers];

            long start = System.currentTimeMillis();

            // Connexion aux workers avec un timeout
            for (int i = 0; i < numWorkers; i++) {
                try {
                    sockets[i] = new Socket(IP, PORTS[i]);
                    sockets[i].setSoTimeout(5000);  // Timeout de 5 secondes
                    readers[i] = new BufferedReader(new InputStreamReader(sockets[i].getInputStream()));
                    writers[i] = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sockets[i].getOutputStream())), true);
                } catch (IOException e) {
                    System.out.println("Erreur lors de la connexion au Worker " + i + ": " + e.getMessage());
                    e.printStackTrace();
                    return;
                }
            }

            // Envoi des points à chaque worker
            for (int i = 0; i < numWorkers; i++) {
                writers[i].println(pointsPerWorker); // envoyer le nombre de lancers par worker
            }

            // Lecture des résultats et libération des workers
            for (int i = 0; i < numWorkers; i++) {
                try {
                    String result = readers[i].readLine(); // récupérer la réponse du worker
                    System.out.println("Received from worker " + i + ": " + result);
                    writers[i].println("END"); // libérer le worker
                } catch (IOException e) {
                    System.out.println("Erreur lors de la lecture du Worker " + i + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            long stop = System.currentTimeMillis();
            long duration = stop - start;

            System.out.println("Total points = " + TOTAL_POINTS + ", Durée = " + duration + " ms");

            // Écriture CSV
            appendCSV(csvFile, TOTAL_POINTS + "," + numWorkers + "," + duration);

            // Fermeture des connexions
            for (int i = 0; i < numWorkers; i++) {
                writers[i].close();
                readers[i].close();
                sockets[i].close();
            }
        }

        System.out.println("\n✔ Benchmark terminé !");
        System.out.println("→ Résultats enregistrés dans : " + csvFile);
    }
}
