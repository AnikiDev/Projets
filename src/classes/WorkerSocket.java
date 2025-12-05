package classes;

import java.io.*;
import java.net.*;

/**
 * Worker is a server. It computes PI by Monte Carlo method and sends
 * the result to Master.
 *
 * Le Worker agit comme un serveur : il attend qu’un Master se connecte,
 * reçoit une demande de calcul (un entier = nombre de tirages Monte-Carlo)
 * puis renvoie le résultat au Master.
 */
public class WorkerSocket {

    // Port d'écoute du worker (modifiable par argument)
    static int port = 25545; // default port

    // Flag servant à garder le worker actif
    private static boolean isRunning = true;

    /**
     * compute PI locally by MC and sends the number of points
     * inside the disk to Master.
     */
    public static void main(String[] args) throws Exception {

        // Si un port est passé en argument, on l'utilise
        if (!("".equals(args[0]))) port = Integer.parseInt(args[0]);
        System.out.println(port);

        // Le worker crée un serveur socket pour écouter sur le port choisi
        ServerSocket s = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        // Le worker attend qu'un Master se connecte
        Socket soc = s.accept();

        // Reader pour recevoir les messages du Master
        BufferedReader bRead = new BufferedReader(
                new InputStreamReader(soc.getInputStream()));

        // Writer pour envoyer les messages au Master
        PrintWriter pWrite = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(soc.getOutputStream())), true);

        String str;

        // Boucle principale : le worker attend des instructions du Master
        while (isRunning) {

            // Lecture du message envoyé par le Master
            str = bRead.readLine();

            // Si le Master ne demande pas d'arrêter
            if (!(str.equals("END"))) {

                // Affiche le nombre de simulations à effectuer
                System.out.println("Server receives totalCount = " + str);

                // Conversion de la demande (string -> int)
                int totalCount = Integer.parseInt(str);

                // -----------------------------
                //      MONTE CARLO PI
                // -----------------------------
                // On estime PI en comptant le nombre de points dans le quart de cercle.
                // Le Worker renvoie SEULEMENT ce nombre, pas PI.
                int inside = 0;
                for (int i = 0; i < totalCount; i++) {
                    // Tirage aléatoire uniforme entre 0 et 1
                    double x = Math.random();
                    double y = Math.random();

                    // Vérifie si le point est dans le quart de disque
                    if (x*x + y*y <= 1.0) {
                        inside++;
                    }
                }
                // -----------------------------

                // Envoie au Master le nombre de points "inside"
                pWrite.println(inside);

            } else {
                // Si le Master envoie "END", on arrête le worker
                isRunning = false;
            }
        }

        // Fermeture des flux et socket
        bRead.close();
        pWrite.close();
        soc.close();
    }
}
