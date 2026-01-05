import java.io.*;   // Import des classes pour gérer les entrées/sorties (flux, lecteurs, écrivains)
import java.net.*;  // Import des classes réseau pour gérer les sockets
/**
 * MasterSocket agit comme un **client** dans le modèle maître-esclave.
 * Il contacte plusieurs Workers (qui sont des serveurs) via des sockets.
 * Le Master envoie une requête (nombre de lancers Monte-Carlo)
 * et reçoit les résultats pour calculer PI.
 */
public class MasterSocket {

    // Nombre maximal de Workers que le Master peut gérer
    static int maxServer = 8;

    // Ports prédéfinis pour les Workers
    static final int[] tab_port = {25545,25546,25547,25548,25549,25550,25551,25552};

    // Tableau pour stocker les résultats reçus de chaque Worker
    static String[] tab_total_workers = new String[maxServer];

    // Adresse IP des Workers ici la machine de mon binome ex 192.168.24.227
    static final String ip = "192.168.24.32"; // localhost

    // BufferedReader pour chaque Worker : permet de lire les messages que le Worker envoie
    static BufferedReader[] reader = new BufferedReader[maxServer];

    // PrintWriter pour chaque Worker : permet d'envoyer des messages au Worker
    static PrintWriter[] writer = new PrintWriter[maxServer];

    // Tableau de sockets, une socket par Worker
    static Socket[] sockets = new Socket[maxServer];

    public static void main(String[] args) throws Exception {

        // Paramètres Monte-Carlo
        int totalCount = 16000000;  // nombre de lancers par Worker
        int total = 0;              // compteur global de points dans le quart de disque
        double pi;

        int numWorkers = maxServer; // nombre de Workers que l’utilisateur veut utiliser

        // BufferedReader pour lire les entrées clavier
        // InputStreamReader : transforme le flux d'octets System.in en caractères
        // BufferedReader : permet de lire une ligne complète avec readLine()
        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        String s;

        System.out.println("#########################################");
        System.out.println("# Calcul de PI par la méthode Monte Carlo #");
        System.out.println("#########################################");

        // DEMANDE DU NOMBRE DE WORKERS
        System.out.println("\n Combien de workers (< maxServer) : ");
        try {
            s = bufferRead.readLine();          // lire la saisie clavier
            numWorkers = Integer.parseInt(s);  // convertir la chaîne en entier
            System.out.println(numWorkers);
        }
        catch(IOException ioE){
            ioE.printStackTrace();
        }

        // DEMANDE DES PORTS DES WORKERS
        for (int i=0; i<numWorkers; i++){
            System.out.println("Entrez le port du worker " + i + " : ");
            try{
                s = bufferRead.readLine();   // lecture clavier
                System.out.println("Vous avez choisi " + s);
            }
            catch(IOException ioE){
                ioE.printStackTrace();
            }
        }

        //---------------------------------------------------------
        // CREATION DES SOCKETS VERS LES WORKERS
        //---------------------------------------------------------
        for(int i = 0 ; i < numWorkers ; i++) {
            try {
                // Création d'une socket TCP vers le Worker
                // Socket(ip, port) : ouvre une connexion avec le serveur Worker
                // Si le serveur n'est pas en écoute → Connection refused
                sockets[i] = new Socket(ip, tab_port[i]);
                System.out.println("SOCKET = " + sockets[i]);

                // Initialisation du lecteur pour lire les messages envoyés par le Worker
                // InputStreamReader : transforme le flux d'octets en caractères
                // BufferedReader : permet de lire des lignes entières avec readLine()
                reader[i] = new BufferedReader(new InputStreamReader(sockets[i].getInputStream()));

                // Initialisation du writer pour envoyer des messages au Worker
                // OutputStreamWriter : transforme les caractères en octets
                // BufferedWriter : optimise l'écriture
                // PrintWriter : permet d'écrire facilement des lignes avec println()
                // true → autoFlush, envoie immédiatement le message sans attendre
                writer[i] = new PrintWriter(new BufferedWriter(new OutputStreamWriter(sockets[i].getOutputStream())), true);
            }
            catch(IOException ioE){
                ioE.printStackTrace();
            }
        }

        //---------------------------------------------------------
        // BOUCLE PRINCIPALE : COMMUNICATION MASTER ↔ WORKERS
        //---------------------------------------------------------
        String message_to_send = String.valueOf(totalCount); // nombre de lancers à envoyer aux Workers
        String message_repeat = "y"; // variable pour relancer le calcul

        long stopTime, startTime;

        while (message_repeat.equals("y")) {
            total = 0;
            startTime = System.currentTimeMillis();

            // ENVOI DU MESSAGE À TOUS LES WORKERS
            for(int i = 0 ; i < numWorkers ; i++) {
                // writer[i].println() : envoie une ligne de texte au Worker
                writer[i].println(message_to_send);
            }

            // RECEPTION DES RÉSULTATS DES WORKERS
            for(int i = 0 ; i < numWorkers ; i++) {
                // reader[i].readLine() : lit la réponse envoyée par le Worker
                // Chaque Worker renvoie le nombre de points dans le quart de disque
                tab_total_workers[i] = reader[i].readLine();
                System.out.println("Worker a envoyé : " + tab_total_workers[i]);
            }

            // CALCUL DU PI GLOBAL À PARTIR DES RÉSULTATS DES WORKERS
            for(int i = 0 ; i < numWorkers ; i++) {
                total += Integer.parseInt(tab_total_workers[i]);
            }

            // Formule Monte-Carlo pour PI
            pi = 4.0 * total / totalCount / numWorkers;

            stopTime = System.currentTimeMillis();

            //-------------------------------------------------
            // AFFICHAGE DES RÉSULTATS
            //-------------------------------------------------
            System.out.println("\nPi : " + pi );
            System.out.println("Erreur relative : " + (Math.abs((pi - Math.PI)) / Math.PI) +"\n");

            System.out.println("Total points : " + totalCount*numWorkers);
            System.out.println("Workers utilisés : " + numWorkers);
            System.out.println("Temps (ms) : " + (stopTime - startTime) + "\n");

            //-------------------------------------------------
            // DEMANDE POUR RELANCER
            //-------------------------------------------------
            System.out.println("\n Refaire un calcul ? (y/N) : ");
            try{
                message_repeat = bufferRead.readLine();
            }
            catch(IOException ioE){
                ioE.printStackTrace();
            }
        }

        //---------------------------------------------------------
        // FERMETURE DES SOCKETS ET DES FLUX
        //---------------------------------------------------------
        for(int i = 0 ; i < numWorkers ; i++) {
            writer[i].println("END");         // message de fin envoyé au Worker
            reader[i].close();                 // fermeture du flux d'entrée
            writer[i].close();                 // fermeture du flux de sortie
            sockets[i].close();                // fermeture de la socket
        }

        System.out.println("Master terminé.");
    }
}