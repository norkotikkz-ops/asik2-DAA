package com.aitu.ds;

import java.util.NoSuchElementException;

public class MinHeap {

    private int[] data;
    private int size;
    private long comparisons;

    public MinHeap() {
        data = new int[16];
        size = 0;
    }

    public MinHeap(int capacityHint) {
        data = new int[Math.max(4, capacityHint)];
        size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public long getComparisons() {
        return comparisons;
    }

    public void resetComparisons() {
        comparisons = 0;
    }

    private void grow() {
        int[] next = new int[data.length * 2];
        System.arraycopy(data, 0, next, 0, size);
        data = next;
    }

    private void swap(int i, int j) {
        int tmp = data[i];
        data[i] = data[j];
        data[j] = tmp;
    }

    public void insert(int x) {
        if (size == data.length) {
            grow();
        }
        data[size] = x;
        size++;
        siftUp(size - 1);
    }

    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            comparisons++;
            if (data[i] < data[parent]) {
                swap(i, parent);
                i = parent;
            } else {
                break;
            }
        }
    }

    public int peekMin() {
        if (size == 0) {
            throw new NoSuchElementException("heap is empty");
        }
        return data[0];
    }

    public int extractMin() {
        if (size == 0) {
            throw new NoSuchElementException("heap is empty");
        }
        int min = data[0];
        size--;
        data[0] = data[size];
        data[size] = 0;
        if (size > 0) {
            siftDown(0);
        }
        return min;
    }

    private void siftDown(int i) {
        while (true) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            int smallest = i;
            if (left < size) {
                comparisons++;
                if (data[left] < data[smallest]) {
                    smallest = left;
                }
            }
            if (right < size) {
                comparisons++;
                if (data[right] < data[smallest]) {
                    smallest = right;
                }
            }
            if (smallest == i) {
                break;
            }
            swap(i, smallest);
            i = smallest;
        }
    }

    public boolean isValidMinHeap() {
        for (int i = 0; i < size; i++) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            if (left < size && data[left] < data[i]) {
                return false;
            }
            if (right < size && data[right] < data[i]) {
                return false;
            }
        }
        return true;
    }
}
