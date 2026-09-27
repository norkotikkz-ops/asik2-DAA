package com.aitu.ds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

public class Tests {

    static int passed = 0;
    static int failed = 0;

    public static void main(String[] args) {
        testDynamicArrayEmpty();
        testDynamicArraySingle();
        testDynamicArrayMultiple();
        testDynamicArrayDuplicates();
        testDynamicArrayBoundaryIndices();
        testDynamicArrayInvalidIndices();
        testDynamicArrayLargeInput();
        testDynamicArrayAgainstArrayList();

        testLinkedListEmpty();
        testLinkedListSingle();
        testLinkedListMultiple();
        testLinkedListDuplicates();
        testLinkedListBoundaryIndices();
        testLinkedListInvalidIndices();
        testLinkedListLargeInput();
        testLinkedListAgainstJavaLinkedList();

        testHeapEmpty();
        testHeapSingle();
        testHeapMultiple();
        testHeapDuplicates();
        testHeapLargeInputAgainstPriorityQueue();
        testHeapPropertyAfterOps();

        System.out.println();
        System.out.println("Passed: " + passed + ", Failed: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS: " + name);
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }

    static void testDynamicArrayEmpty() {
        DynamicArray<Integer> da = new DynamicArray<>();
        check("DynamicArray empty size", da.size() == 0);
        check("DynamicArray empty isEmpty", da.isEmpty());
        check("DynamicArray empty contains", !da.contains(1));
    }

    static void testDynamicArraySingle() {
        DynamicArray<Integer> da = new DynamicArray<>();
        da.add(5);
        check("DynamicArray single size", da.size() == 1);
        check("DynamicArray single get", da.get(0) == 5);
        check("DynamicArray single contains", da.contains(5));
    }

    static void testDynamicArrayMultiple() {
        DynamicArray<Integer> da = new DynamicArray<>();
        for (int i = 0; i < 20; i++) da.add(i);
        boolean ok = true;
        for (int i = 0; i < 20; i++) if (da.get(i) != i) ok = false;
        check("DynamicArray multiple add/get", ok);
        da.add(10, 999);
        check("DynamicArray insert at index", da.get(10) == 999 && da.get(11) == 10);
        int removed = da.remove(10);
        check("DynamicArray remove at index", removed == 999 && da.get(10) == 10);
    }

    static void testDynamicArrayDuplicates() {
        DynamicArray<Integer> da = new DynamicArray<>();
        da.add(7); da.add(7); da.add(7);
        check("DynamicArray duplicates size", da.size() == 3);
        check("DynamicArray duplicates contains", da.contains(7));
        da.remove(0);
        check("DynamicArray duplicates remove one", da.size() == 2 && da.get(0) == 7);
    }

    static void testDynamicArrayBoundaryIndices() {
        DynamicArray<Integer> da = new DynamicArray<>();
        da.add(1); da.add(2); da.add(3);
        da.add(0, 0);
        da.add(da.size(), 4);
        check("DynamicArray boundary insert", da.get(0) == 0 && da.get(da.size() - 1) == 4);
        int firstRemoved = da.remove(0);
        int lastRemoved = da.remove(da.size() - 1);
        check("DynamicArray boundary remove", firstRemoved == 0 && lastRemoved == 4);
    }

    static void testDynamicArrayInvalidIndices() {
        DynamicArray<Integer> da = new DynamicArray<>();
        da.add(1);
        boolean threwGet = false;
        try { da.get(5); } catch (IndexOutOfBoundsException e) { threwGet = true; }
        boolean threwRemove = false;
        try { da.remove(-1); } catch (IndexOutOfBoundsException e) { threwRemove = true; }
        boolean threwAdd = false;
        try { da.add(10, 1); } catch (IndexOutOfBoundsException e) { threwAdd = true; }
        check("DynamicArray invalid indices throw", threwGet && threwRemove && threwAdd);
    }

    static void testDynamicArrayLargeInput() {
        DynamicArray<Integer> da = new DynamicArray<>();
        int n = 50_000;
        for (int i = 0; i < n; i++) da.add(i);
        check("DynamicArray large input size", da.size() == n);
        check("DynamicArray large input get", da.get(n - 1) == n - 1);
    }

    static void testDynamicArrayAgainstArrayList() {
        Random rnd = new Random(1);
        DynamicArray<Integer> da = new DynamicArray<>();
        ArrayList<Integer> ref = new ArrayList<>();
        for (int i = 0; i < 2000; i++) {
            int op = rnd.nextInt(4);
            if (op == 0 || ref.isEmpty()) {
                int v = rnd.nextInt(1000);
                da.add(v); ref.add(v);
            } else if (op == 1) {
                int idx = rnd.nextInt(ref.size() + 1);
                int v = rnd.nextInt(1000);
                da.add(idx, v); ref.add(idx, v);
            } else if (op == 2 && !ref.isEmpty()) {
                int idx = rnd.nextInt(ref.size());
                da.remove(idx); ref.remove(idx);
            } else if (!ref.isEmpty()) {
                int idx = rnd.nextInt(ref.size());
                if (!(da.get(idx).equals(ref.get(idx)))) {
                    check("DynamicArray vs ArrayList mismatch at get", false);
                    return;
                }
            }
        }
        boolean same = ref.size() == da.size();
        for (int i = 0; same && i < ref.size(); i++) {
            same = ref.get(i).equals(da.get(i));
        }
        check("DynamicArray matches ArrayList after random ops", same);
    }

    static void testLinkedListEmpty() {
        MyLinkedList<Integer> ll = new MyLinkedList<>();
        check("LinkedList empty size", ll.size() == 0);
        check("LinkedList empty isEmpty", ll.isEmpty());
        check("LinkedList empty contains", !ll.contains(1));
    }

    static void testLinkedListSingle() {
        MyLinkedList<Integer> ll = new MyLinkedList<>();
        ll.add(5);
        check("LinkedList single get", ll.get(0) == 5);
        check("LinkedList single contains", ll.contains(5));
    }

    static void testLinkedListMultiple() {
        MyLinkedList<Integer> ll = new MyLinkedList<>();
        for (int i = 0; i < 20; i++) ll.add(i);
        boolean ok = true;
        for (int i = 0; i < 20; i++) if (ll.get(i) != i) ok = false;
        check("LinkedList multiple add/get", ok);
        ll.add(10, 999);
        check("LinkedList insert at index", ll.get(10) == 999 && ll.get(11) == 10);
        int removed = ll.remove(10);
        check("LinkedList remove at index", removed == 999 && ll.get(10) == 10);
    }

    static void testLinkedListDuplicates() {
        MyLinkedList<Integer> ll = new MyLinkedList<>();
        ll.add(7); ll.add(7); ll.add(7);
        check("LinkedList duplicates size", ll.size() == 3);
        ll.remove(0);
        check("LinkedList duplicates remove one", ll.size() == 2 && ll.get(0) == 7);
    }

    static void testLinkedListBoundaryIndices() {
        MyLinkedList<Integer> ll = new MyLinkedList<>();
        ll.add(1); ll.add(2); ll.add(3);
        ll.add(0, 0);
        ll.add(ll.size(), 4);
        check("LinkedList boundary insert", ll.get(0) == 0 && ll.get(ll.size() - 1) == 4);
        int firstRemoved = ll.remove(0);
        int lastRemoved = ll.remove(ll.size() - 1);
        check("LinkedList boundary remove", firstRemoved == 0 && lastRemoved == 4);
    }

    static void testLinkedListInvalidIndices() {
        MyLinkedList<Integer> ll = new MyLinkedList<>();
        ll.add(1);
        boolean threwGet = false;
        try { ll.get(5); } catch (IndexOutOfBoundsException e) { threwGet = true; }
        boolean threwRemove = false;
        try { ll.remove(-1); } catch (IndexOutOfBoundsException e) { threwRemove = true; }
        boolean threwAdd = false;
        try { ll.add(10, 1); } catch (IndexOutOfBoundsException e) { threwAdd = true; }
        check("LinkedList invalid indices throw", threwGet && threwRemove && threwAdd);
    }

    static void testLinkedListLargeInput() {
        MyLinkedList<Integer> ll = new MyLinkedList<>();
        int n = 50_000;
        for (int i = 0; i < n; i++) ll.add(i);
        check("LinkedList large input size", ll.size() == n);
        check("LinkedList large input get", ll.get(n - 1) == n - 1);
    }

    static void testLinkedListAgainstJavaLinkedList() {
        Random rnd = new Random(2);
        MyLinkedList<Integer> ll = new MyLinkedList<>();
        LinkedList<Integer> ref = new LinkedList<>();
        for (int i = 0; i < 2000; i++) {
            int op = rnd.nextInt(4);
            if (op == 0 || ref.isEmpty()) {
                int v = rnd.nextInt(1000);
                ll.add(v); ref.add(v);
            } else if (op == 1) {
                int idx = rnd.nextInt(ref.size() + 1);
                int v = rnd.nextInt(1000);
                ll.add(idx, v); ref.add(idx, v);
            } else if (op == 2 && !ref.isEmpty()) {
                int idx = rnd.nextInt(ref.size());
                ll.remove(idx); ref.remove(idx);
            } else if (!ref.isEmpty()) {
                int idx = rnd.nextInt(ref.size());
                if (!ll.get(idx).equals(ref.get(idx))) {
                    check("LinkedList vs java.util.LinkedList mismatch", false);
                    return;
                }
            }
        }
        boolean same = ref.size() == ll.size();
        for (int i = 0; same && i < ref.size(); i++) {
            same = ref.get(i).equals(ll.get(i));
        }
        check("LinkedList matches java.util.LinkedList after random ops", same);
    }

    static void testHeapEmpty() {
        MinHeap heap = new MinHeap();
        check("Heap empty isEmpty", heap.isEmpty());
        boolean threw = false;
        try { heap.extractMin(); } catch (Exception e) { threw = true; }
        check("Heap empty extractMin throws", threw);
    }

    static void testHeapSingle() {
        MinHeap heap = new MinHeap();
        heap.insert(42);
        check("Heap single peekMin", heap.peekMin() == 42);
        check("Heap single extractMin", heap.extractMin() == 42);
        check("Heap single now empty", heap.isEmpty());
    }

    static void testHeapMultiple() {
        MinHeap heap = new MinHeap();
        int[] values = {5, 3, 8, 1, 9, 2, 7};
        for (int v : values) heap.insert(v);
        List<Integer> extracted = new ArrayList<>();
        while (!heap.isEmpty()) extracted.add(heap.extractMin());
        List<Integer> sorted = new ArrayList<>(extracted);
        Collections.sort(sorted);
        check("Heap extracts in non-decreasing order", extracted.equals(sorted));
    }

    static void testHeapDuplicates() {
        MinHeap heap = new MinHeap();
        int[] values = {4, 4, 4, 1, 1, 9};
        for (int v : values) heap.insert(v);
        List<Integer> extracted = new ArrayList<>();
        while (!heap.isEmpty()) extracted.add(heap.extractMin());
        List<Integer> sorted = new ArrayList<>(extracted);
        Collections.sort(sorted);
        check("Heap handles duplicates correctly", extracted.equals(sorted));
    }

    static void testHeapLargeInputAgainstPriorityQueue() {
        Random rnd = new Random(3);
        MinHeap heap = new MinHeap();
        PriorityQueue<Integer> ref = new PriorityQueue<>();
        int n = 20_000;
        for (int i = 0; i < n; i++) {
            int v = rnd.nextInt(100_000);
            heap.insert(v);
            ref.add(v);
        }
        boolean same = true;
        while (!ref.isEmpty()) {
            int expected = ref.poll();
            int actual = heap.extractMin();
            if (expected != actual) {
                same = false;
                break;
            }
        }
        check("Heap matches PriorityQueue extraction order", same);
    }

    static void testHeapPropertyAfterOps() {
        Random rnd = new Random(4);
        MinHeap heap = new MinHeap();
        boolean ok = true;
        for (int i = 0; i < 5000; i++) {
            if (rnd.nextBoolean() || heap.isEmpty()) {
                heap.insert(rnd.nextInt(10000));
            } else {
                heap.extractMin();
            }
            if (!heap.isValidMinHeap()) {
                ok = false;
                break;
            }
        }
        check("Heap property maintained after random insert/extract sequence", ok);
    }
}
