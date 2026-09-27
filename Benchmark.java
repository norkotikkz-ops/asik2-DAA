package com.aitu.ds;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class Benchmark {

    static final int[] SIZES = {100, 1_000, 10_000, 100_000};
    static final int REPEATS = 5;
    static final long SEED = 42;
    static final String OUT_DIR = "results/tables/";

    public static void main(String[] args) throws IOException {
        Files.createDirectories(Paths.get(OUT_DIR));
        workload1RandomAccess();
        workload2Search();
        workload3InsertRemove();
        workload4Heap();
        System.out.println("All benchmarks complete. CSVs written to " + OUT_DIR);
    }

    static int[] randomArray(int n, Random rnd) {
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = rnd.nextInt(n * 10 + 1);
        }
        return arr;
    }

    static void workload1RandomAccess() throws IOException {
        try (PrintWriter w = new PrintWriter(new FileWriter(OUT_DIR + "workload1_random_access.csv"))) {
            w.println("structure,n,avg_time_ns,accesses");
            for (int n : SIZES) {
                Random rnd = new Random(SEED);
                int[] seed = randomArray(n, rnd);
                int numOps = 10_000;
                int[] indices = new int[numOps];
                for (int i = 0; i < numOps; i++) {
                    indices[i] = rnd.nextInt(n);
                }

                long totalArray = 0;
                long accessesArray = 0;
                for (int r = 0; r < REPEATS; r++) {
                    DynamicArray<Integer> da = new DynamicArray<>();
                    for (int v : seed) da.add(v);
                    da.resetCounters();
                    long start = System.nanoTime();
                    for (int idx : indices) {
                        da.get(idx);
                    }
                    totalArray += System.nanoTime() - start;
                    accessesArray = da.getMovements();
                }
                w.println("DynamicArray," + n + "," + (totalArray / REPEATS) + "," + accessesArray);

                long totalList = 0;
                long accessesList = 0;
                for (int r = 0; r < REPEATS; r++) {
                    MyLinkedList<Integer> ll = new MyLinkedList<>();
                    for (int v : seed) ll.add(v);
                    ll.resetCounters();
                    long start = System.nanoTime();
                    for (int idx : indices) {
                        ll.get(idx);
                    }
                    totalList += System.nanoTime() - start;
                    accessesList = ll.getMovements();
                }
                w.println("LinkedList," + n + "," + (totalList / REPEATS) + "," + accessesList);
            }
        }
    }

    static void workload2Search() throws IOException {
        try (PrintWriter w = new PrintWriter(new FileWriter(OUT_DIR + "workload2_search.csv"))) {
            w.println("structure,n,avg_time_ns,comparisons");
            for (int n : SIZES) {
                Random rnd = new Random(SEED);
                int[] seed = randomArray(n, rnd);
                int numOps = 1_000;
                int[] queries = new int[numOps];
                for (int i = 0; i < numOps; i++) {
                    queries[i] = rnd.nextInt(n * 10 + 1);
                }

                long totalArray = 0;
                long compArray = 0;
                for (int r = 0; r < REPEATS; r++) {
                    DynamicArray<Integer> da = new DynamicArray<>();
                    for (int v : seed) da.add(v);
                    da.resetCounters();
                    long start = System.nanoTime();
                    for (int q : queries) {
                        da.contains(q);
                    }
                    totalArray += System.nanoTime() - start;
                    compArray = da.getComparisons();
                }
                w.println("DynamicArray," + n + "," + (totalArray / REPEATS) + "," + compArray);

                long totalList = 0;
                long compList = 0;
                for (int r = 0; r < REPEATS; r++) {
                    MyLinkedList<Integer> ll = new MyLinkedList<>();
                    for (int v : seed) ll.add(v);
                    ll.resetCounters();
                    long start = System.nanoTime();
                    for (int q : queries) {
                        ll.contains(q);
                    }
                    totalList += System.nanoTime() - start;
                    compList = ll.getComparisons();
                }
                w.println("LinkedList," + n + "," + (totalList / REPEATS) + "," + compList);
            }
        }
    }

    static void workload3InsertRemove() throws IOException {
        try (PrintWriter w = new PrintWriter(new FileWriter(OUT_DIR + "workload3_insert_remove.csv"))) {
            w.println("structure,operation,position,n,avg_time_ns,movements");
            int numOps = 1_000;
            for (int n : SIZES) {
                Random rnd = new Random(SEED);
                int[] seed = randomArray(n, rnd);

                runInsertRemove(w, "DynamicArray", n, seed, numOps, true, 0);
                runInsertRemove(w, "DynamicArray", n, seed, numOps, true, n / 2);
                runInsertRemove(w, "LinkedList", n, seed, numOps, false, 0);
                runInsertRemove(w, "LinkedList", n, seed, numOps, false, n / 2);
            }
        }
    }

    static void runInsertRemove(PrintWriter w, String label, int n, int[] seed, int numOps,
                                 boolean isArray, int position) {
        String posLabel = position == 0 ? "begin" : "middle";

        long totalInsert = 0;
        long movInsert = 0;
        for (int r = 0; r < REPEATS; r++) {
            if (isArray) {
                DynamicArray<Integer> da = new DynamicArray<>();
                for (int v : seed) da.add(v);
                da.resetCounters();
                long start = System.nanoTime();
                for (int i = 0; i < numOps; i++) {
                    da.add(position, i);
                }
                totalInsert += System.nanoTime() - start;
                movInsert = da.getMovements();
            } else {
                MyLinkedList<Integer> ll = new MyLinkedList<>();
                for (int v : seed) ll.add(v);
                ll.resetCounters();
                long start = System.nanoTime();
                for (int i = 0; i < numOps; i++) {
                    ll.add(position, i);
                }
                totalInsert += System.nanoTime() - start;
                movInsert = ll.getMovements();
            }
        }
        w.println(label + ",insert," + posLabel + "," + n + "," + (totalInsert / REPEATS) + "," + movInsert);

        long totalRemove = 0;
        long movRemove = 0;
        for (int r = 0; r < REPEATS; r++) {
            if (isArray) {
                DynamicArray<Integer> da = new DynamicArray<>();
                for (int v : seed) da.add(v);
                for (int i = 0; i < numOps; i++) da.add(position, i);
                da.resetCounters();
                long start = System.nanoTime();
                for (int i = 0; i < numOps; i++) {
                    da.remove(position);
                }
                totalRemove += System.nanoTime() - start;
                movRemove = da.getMovements();
            } else {
                MyLinkedList<Integer> ll = new MyLinkedList<>();
                for (int v : seed) ll.add(v);
                for (int i = 0; i < numOps; i++) ll.add(position, i);
                ll.resetCounters();
                long start = System.nanoTime();
                for (int i = 0; i < numOps; i++) {
                    ll.remove(position);
                }
                totalRemove += System.nanoTime() - start;
                movRemove = ll.getMovements();
            }
        }
        w.println(label + ",remove," + posLabel + "," + n + "," + (totalRemove / REPEATS) + "," + movRemove);
    }

    static void workload4Heap() throws IOException {
        try (PrintWriter w = new PrintWriter(new FileWriter(OUT_DIR + "workload4_heap.csv"))) {
            w.println("n,avg_insert_time_ns,avg_extract_time_ns,comparisons,sorted_correctly");
            for (int n : SIZES) {
                Random rnd = new Random(SEED);
                int[] values = randomArray(n, rnd);

                long totalInsert = 0;
                long totalExtract = 0;
                long comparisons = 0;
                boolean sortedOk = true;

                for (int r = 0; r < REPEATS; r++) {
                    MinHeap heap = new MinHeap(n);
                    long start = System.nanoTime();
                    for (int v : values) {
                        heap.insert(v);
                    }
                    totalInsert += System.nanoTime() - start;

                    int prev = Integer.MIN_VALUE;
                    boolean ok = true;
                    start = System.nanoTime();
                    for (int i = 0; i < n; i++) {
                        int min = heap.extractMin();
                        if (min < prev) ok = false;
                        prev = min;
                    }
                    totalExtract += System.nanoTime() - start;

                    comparisons = heap.getComparisons();
                    sortedOk = sortedOk && ok;
                }
                w.println(n + "," + (totalInsert / REPEATS) + "," + (totalExtract / REPEATS)
                        + "," + comparisons + "," + sortedOk);
            }
        }
    }
}
