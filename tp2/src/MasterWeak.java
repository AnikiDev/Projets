import java.io.*;
import java.net.Socket;

public class MasterWeak {

    static final int MAX_WORKERS = 8;
    static final int[] PORTS = {25545,25546,25547,25548,25549,25550,25551,25552};
    static final String IP = "192.168.24.19";

    public static void appendCSV(String filename, String line) {
        try (FileWriter fw = new FileWriter(filename, true)) {
            fw.write(line + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {

        System.out.println("#########################################");
        System.out.println("#     SCALABILITÉ FAIBLE      #");
        System.out.println("#########################################\n");

        String csvFile = "scalabilite_weak.csv";
        appendCSV(csvFile, "ntot,nth,tps_ms");

        int pointsPerWorker = 16000000;

        for (int numWorkers = 1; numWorkers <= MAX_WORKERS; numWorkers++) {

            System.out.println("\n===== TEST AVEC " + numWorkers + " WORKER(S) =====");

            Socket[] sockets = new Socket[numWorkers];
            BufferedReader[] readers = new BufferedReader[numWorkers];
            PrintWriter[] writers = new PrintWriter[numWorkers];

            long start = System.currentTimeMillis();

            // Connexion aux workers
            for (int i = 0; i < numWorkers; i++) {
                sockets[i] = new Socket(IP, PORTS[i]);
                readers[i] = new BufferedReader(new InputStreamReader(sockets[i].getInputStream()));
                writers[i] = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sockets[i].getOutputStream())), true);
            }

            // Envoi des points à chaque worker
            for (int i = 0; i < numWorkers; i++) {
                writers[i].println(pointsPerWorker);
            }

            // Lecture des résultats et libération des workers
            for (int i = 0; i < numWorkers; i++) {
                readers[i].readLine(); // synchronisation
                writers[i].println("END"); // libérer le worker
            }

            long stop = System.currentTimeMillis();
            long duration = stop - start;

            long totalPoints = pointsPerWorker * numWorkers;
            System.out.println("Total points = " + totalPoints + ", Durée = " + duration + " ms");

            // Écriture CSV
            appendCSV(csvFile, totalPoints + "," + numWorkers + "," + duration);

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
