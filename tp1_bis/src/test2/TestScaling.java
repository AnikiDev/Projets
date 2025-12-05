package test2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class TestScaling {

    public static double median(ArrayList<Long> list) {
        Collections.sort(list);
        int size = list.size();
        if (size % 2 == 1) return list.get(size / 2);
        return (list.get(size / 2 - 1) + list.get(size / 2)) / 2.0;
    }

    public static void main(String[] args) {

        int[] processors = {1, 2, 3, 4, 5, 6, 7, 8};
        int[] strongProblems = {(int) 1e7}; // taille fixe pour strong scaling
        int repetitions = 10;

        String csvFile = "scaling_results2.csv";

        try (FileWriter writer = new FileWriter(csvFile)) {
            writer.append("testType,nTot,nbP,median_ms\n");

            // STRONG SCALING
            System.out.println("--------------------------------------------------");
            System.out.println("               Strong Scaling Tests");
            System.out.println("--------------------------------------------------");

            for (int nThrows : strongProblems) {
                for (int p : processors) {
                    ArrayList<Long> durations = new ArrayList<>();

                    for (int r = 0; r < repetitions; r++) {
                        long start = System.currentTimeMillis();
                        new Master2().doRun(nThrows, p);
                        long stop = System.currentTimeMillis();
                        durations.add(stop - start);
                    }

                    double medianDuration = median(durations);
                    System.out.printf("nTot = %d | nbP = %d | median = %.1f ms%n", nThrows, p, medianDuration);
                    writer.append(String.format("strong,%d,%d,%.1f\n", nThrows, p, medianDuration));
                }
            }

            // WEAK SCALING
            System.out.println("--------------------------------------------------");
            System.out.println("               Weak Scaling Tests");
            System.out.println("--------------------------------------------------");

            int baseThrows = 1_000_000;

            for (int p : processors) {
                int nThrows = baseThrows * p; // charge augmente avec nbP
                ArrayList<Long> durations = new ArrayList<>();

                for (int r = 0; r < repetitions; r++) {
                    long start = System.currentTimeMillis();
                    new Master2().doRun(nThrows, p);
                    long stop = System.currentTimeMillis();
                    durations.add(stop - start);
                }

                double medianDuration = median(durations);
                System.out.printf("nTot = %d | nbP = %d | median = %.1f ms%n", nThrows, p, medianDuration);
                writer.append(String.format("weak,%d,%d,%.1f\n", nThrows, p, medianDuration));
            }

            System.out.println("\nRésultats enregistrés dans " + csvFile);

        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
