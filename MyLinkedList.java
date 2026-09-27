package com.aitu.ds;

public class MyLinkedList<T> {

    private static class Node<T> {
        T value;
        Node<T> prev;
        Node<T> next;

        Node(T value) {
            this.value = value;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;
    private long movements;
    private long comparisons;

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
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

    public void add(T x) {
        Node<T> node = new Node<>(x);
        if (tail == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
        size++;
    }

    private Node<T> nodeAt(int index) {
        Node<T> cur;
        if (index <= size / 2) {
            cur = head;
            for (int i = 0; i < index; i++) {
                cur = cur.next;
                movements++;
            }
        } else {
            cur = tail;
            for (int i = size - 1; i > index; i--) {
                cur = cur.prev;
                movements++;
            }
        }
        return cur;
    }

    public void add(int index, T x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        if (index == size) {
            add(x);
            return;
        }
        Node<T> at = nodeAt(index);
        Node<T> node = new Node<>(x);
        node.next = at;
        node.prev = at.prev;
        if (at.prev == null) {
            head = node;
        } else {
            at.prev.next = node;
        }
        at.prev = node;
        size++;
    }

    public T remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        Node<T> target = nodeAt(index);
        if (target.prev == null) {
            head = target.next;
        } else {
            target.prev.next = target.next;
        }
        if (target.next == null) {
            tail = target.prev;
        } else {
            target.next.prev = target.prev;
        }
        size--;
        return target.value;
    }

    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        return nodeAt(index).value;
    }

    public boolean contains(T x) {
        Node<T> cur = head;
        while (cur != null) {
            comparisons++;
            if (cur.value == null ? x == null : cur.value.equals(x)) {
                return true;
            }
            cur = cur.next;
        }
        return false;
    }
}
