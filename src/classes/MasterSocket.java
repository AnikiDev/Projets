package classes;

import java.io.*;
import java.net.*;

/** Master is a client. It makes requests to numWorkers.
 *   Ce programme joue le rôle de "master" dans un calcul parallèle de PI
 *   basé sur la méthode de Monte-Carlo. Il envoie des tâches à plusieurs
 *   workers via des sockets TCP et récupère leurs résultats.
 */
public class MasterSocket {

    // Nombre maximum de serveurs (workers) possibles
    static int maxServer = 8;

    // Tableau des ports utilisés pour les workers
    static final int[] tab_port = {25545,25546,25547,25548,25549,25550,25551,25552};

    // Tableau qui contiendra les réponses de chaque worker
    static String[] tab_total_workers = new String[maxServer];

    // Adresse IP des workers (ici local)
    static final String ip = "127.0.0.1";

    // Tableaux pour les flux de communication
    static BufferedReader[] reader = new BufferedReader[maxServer]; // liste où on va lire ce que le client reçoit
    static PrintWriter[] writer = new PrintWriter[maxServer]; // permet au client d'écrire et de répondre aux serveurs
    static Socket[] sockets = new Socket[maxServer];


    public static void main(String[] args) throws Exception {

        // Paramètres du Monte Carlo
        int totalCount = 16000000; // Nombre total de lancers par worker
        int total = 0;             // Nombre de points dans le quart de disque (sommes des workers)
        double pi;                // Résultat final calculé

        int numWorkers = maxServer; // Nombre de workers utilisés

        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
        String s; // Pour lire les entrées utilisateur

        System.out.println("#########################################");
        System.out.println("# Computation of PI by MC method        #");
        System.out.println("#########################################");

        // Choix du nombre de workers par l'utilisateur
        System.out.println("\n How many workers for computing PI (< maxServer): ");
        try{
            s = bufferRead.readLine();
            numWorkers = Integer.parseInt(s);
            System.out.println(numWorkers);
        }
        catch(IOException ioE){
            ioE.printStackTrace();
        }

        // Demande des ports à utiliser pour chaque worker (même si dans le code ils ne sont pas utilisés)
        for (int i=0; i<numWorkers; i++){
            System.out.println("Enter worker"+ i +" port : ");
            try{
                s = bufferRead.readLine();
                System.out.println("You select " + s);
            }
            catch(IOException ioE){
                ioE.printStackTrace();
            }
        }

        // Création des sockets pour chaque worker
        for(int i = 0 ; i < numWorkers ; i++) {

            // Connexion au worker i sur l'IP et le port correspondants
            sockets[i] = new Socket(ip, tab_port[i]);
            System.out.println("SOCKET = " + sockets[i]);

            // Reader pour recevoir les réponses du worker
            reader[i] = new BufferedReader(
                    new InputStreamReader(sockets[i].getInputStream()));

            // Writer pour envoyer des messages au worker
            writer[i] = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(sockets[i].getOutputStream())),true);
        }

        String message_to_send;
        message_to_send = String.valueOf(totalCount); // On envoie le nombre de simulations à faire

        String message_repeat = "y"; // Permet de relancer plusieurs fois le calcul

        long stopTime, startTime;

        // Boucle principale
        while (message_repeat.equals("y")){

            startTime = System.currentTimeMillis(); // Chronometric du temps

            // Envoi de l’ordre aux workers : exécute totalCount simulations
            for(int i = 0 ; i < numWorkers ; i++) {
                writer[i].println(message_to_send);
            }

            // Lecture des résultats retournés par chaque worker
            for(int i = 0 ; i < numWorkers ; i++) {
                tab_total_workers[i] = reader[i].readLine();
                System.out.println("Client sent: " + tab_total_workers[i]);
            }

            // Addition des résultats envoyés par les workers
            for(int i = 0 ; i < numWorkers ; i++) {
                total += Integer.parseInt(tab_total_workers[i]);
            }

            // Calcul final de PI via Monte Carlo (aire estimée)
            pi = 4.0 * total / totalCount / numWorkers;

            stopTime = System.currentTimeMillis();

            // Affichage des résultats
            System.out.println("\nPi : " + pi );
            System.out.println("Error: " + (Math.abs((pi - Math.PI)) / Math.PI) +"\n");

            System.out.println("Ntot: " + totalCount*numWorkers);
            System.out.println("Available processors: " + numWorkers);
            System.out.println("Time Duration (ms): " + (stopTime - startTime) + "\n");

            System.out.println( (Math.abs((pi - Math.PI)) / Math.PI) +" "+ totalCount*numWorkers +" "+ numWorkers +" "+ (stopTime - startTime));

            // Demande si l'utilisateur souhaite recommencer
            System.out.println("\n Repeat computation (y/N): ");
            try{
                message_repeat = bufferRead.readLine();
                System.out.println(message_repeat);
            }
            catch(IOException ioE){
                ioE.printStackTrace();
            }
        }

        // Envoi du message "END" à chaque worker et fermeture des sockets
        for(int i = 0 ; i < numWorkers ; i++) {
            System.out.println("END");
            writer[i].println("END");  // Indique aux workers d'arrêter
            reader[i].close();
            writer[i].close();
            sockets[i].close();
        }
    }
}
