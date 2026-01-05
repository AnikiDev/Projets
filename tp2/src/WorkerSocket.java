import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutionException;
import test3.*;

/**
 * WorkerSocket agit comme un **serveur**.
 * Il reçoit des requêtes du Master (nombre de lancers Monte-Carlo)
 * calcule PI localement et renvoie le résultat au Master.
 */
public class WorkerSocket {

    // Port par défaut si aucun argument n'est fourni
    static int port = 25545;

    /**
     * Point d'entrée du Worker
     */
    public static void main(String[] args) throws Exception {

        // Si un argument est fourni, on l'utilise comme port
        if (args.length > 0 && !args[0].equals("")) {
            port = Integer.parseInt(args[0]);
        }
        System.out.println("Port du serveur : " + port);

        // 1. CREATION DU SERVEUR
        ServerSocket server = new ServerSocket(port);
        System.out.println("Worker démarré sur le port " + port);

        // 🔥 Le worker reste toujours actif
        while (true) {

            // 2. ATTENTE D'UN MASTER
            Socket soc = server.accept();
            System.out.println("Master connecté : " + soc);

            BufferedReader bRead = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            PrintWriter pWrite = new PrintWriter(new BufferedWriter(new OutputStreamWriter(soc.getOutputStream())), true);

            String msg;

            // 3. BOUCLE DE COMMUNICATION AVEC CE MASTER
            while ((msg = bRead.readLine()) != null) {

                if (msg.equals("END")) {
                    System.out.println("Master a terminé, fermeture de la connexion.");
                    break; // On ferme juste la connexion, pas le serveur
                }

                int totalCount = Integer.parseInt(msg);
                System.out.println("Reçu totalCount = " + totalCount);

                long inside = new Master3().doRun(totalCount, 1);

                // Renvoi du résultat
                pWrite.println(inside);
            }

            // 4. FERMETURE DE LA CONNEXION AVEC CE MASTER
            soc.close();
            System.out.println("Connexion fermée. Worker prêt pour un nouveau master.");
        }

        // ⚠ Ce code ne sera jamais atteint, mais on le laisse pour la forme
        // server.close();
    }
}
