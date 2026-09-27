package com.aitu.ds;

public class DynamicArray<T> {

    private Object[] data;
    private int size;
    private long movements;
    private long comparisons;

    public DynamicArray() {
        data = new Object[4];
        size = 0;
    }

    public int size() {
        return size;
    }

    public long getMovements() {
        return movements;
    }

    public long getComparisons() {
        return comparisons;
    }

    public void resetCounters() {
        movements = 0;
        comparisons = 0;
    }

    private void grow() {
        Object[] next = new Object[data.length * 2];
        for (int i = 0; i < size; i++) {
            next[i] = data[i];
        }
        data = next;
    }

    public void add(T x) {
        if (size == data.length) {
            grow();
        }
        data[size] = x;
        size++;
    }

    public void add(int index, T x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        if (size == data.length) {
            grow();
        }
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
            movements++;
        }
        data[index] = x;
        size++;
    }

    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        @SuppressWarnings("unchecked")
        T removed = (T) data[index];
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
            movements++;
        }
        data[size - 1] = null;
        size--;
        return removed;
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        movements++;
        return (T) data[index];
    }

    public boolean contains(T x) {
        for (int i = 0; i < size; i++) {
            comparisons++;
            if (data[i] == null ? x == null : data[i].equals(x)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
