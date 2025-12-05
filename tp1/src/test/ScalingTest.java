package test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class ScalingTest {

    // Fonction pour calculer la médiane
    public static double median(ArrayList<Long> list) {
        Collections.sort(list);
        int size = list.size();
        if (size % 2 == 1) {
            return list.get(size / 2);
        } else {
            return (list.get(size / 2 - 1) + list.get(size / 2)) / 2.0;
        }
    }

    public static void main(String[] args) {

        int[] processors = {1, 2, 3, 4}; // Nombre de threads
        int[] tests = {(int) 1e6, (int) 1e7, (int) 1e8}; // Problèmes pour Strong Scaling
        int repetitions = 5; // Nombre de répétitions

        String csvFile = "scaling_results.csv";

        try (FileWriter writer = new FileWriter(csvFile)) {

            // Entête CSV
            writer.append("testType,nTot,nbP,median_ms\n");

            System.out.println("--------------------------------------------------");
            System.out.println("               Strong Scaling Tests");
            System.out.println("--------------------------------------------------");

            // ------------------ Strong Scaling ------------------
            for (int nThrows : tests) {

                for (int p : processors) {

                    ArrayList<Long> durations = new ArrayList<>();

                    for (int r = 1; r <= repetitions; r++) {
                        PiMonte_Carlo pi = new PiMonte_Carlo(nThrows);

                        long startTime = System.currentTimeMillis();
                        pi.getPi(p); // nbThreads = p
                        long stopTime = System.currentTimeMillis();

                        durations.add(stopTime - startTime);
                    }

                    double medianDuration = median(durations);

                    System.out.printf("nTot = %d | nbP = %d | median = %.1f ms%n",
                            nThrows, p, medianDuration);

                    writer.append(String.format("strong,%d,%d,%.1f\n", nThrows, p, medianDuration));
                }
            }

            System.out.println("\n--------------------------------------------------");
            System.out.println("                  Weak Scaling Tests");
            System.out.println("--------------------------------------------------");

            // ------------------ Weak Scaling ------------------
            int baseThrows = 1_000_000;

            for (int p : processors) {

                int nThrows = baseThrows * p;

                ArrayList<Long> durations = new ArrayList<>();

                for (int r = 1; r <= repetitions; r++) {
                    PiMonte_Carlo pi = new PiMonte_Carlo(nThrows);

                    long startTime = System.currentTimeMillis();
                    pi.getPi(p);
                    long stopTime = System.currentTimeMillis();

                    durations.add(stopTime - startTime);
                }

                double medianDuration = median(durations);

                System.out.printf("nTot = %d | nbP = %d | median = %.1f ms%n",
                        nThrows, p, medianDuration);

                writer.append(String.format("weak,%d,%d,%.1f\n", nThrows, p, medianDuration));
            }

            System.out.println("\nRésultats enregistrés dans " + csvFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
