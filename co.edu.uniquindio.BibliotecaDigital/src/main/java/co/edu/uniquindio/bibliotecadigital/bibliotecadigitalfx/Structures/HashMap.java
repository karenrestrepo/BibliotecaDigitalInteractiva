package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Nodes.MapNode;


public class HashMap<K, V> {
    private static final int SIZE = 100;
    private MapNode<K, V>[] table;
    private int size;

    @SuppressWarnings("unchecked")
    public HashMap() {
        table = new MapNode[SIZE];
        size = 0;
    }

    private int hash(K key) {
        return Math.abs(key.hashCode()) % SIZE;
    }

    public void put(K key, V value) {
        int index = hash(key);
        MapNode<K, V> current = table[index];

        while (current != null) {
            if (current.getKey().equals(key)) {
                current.setValue(value);
                return;
            }
            current = current.getNext();
        }

        MapNode<K, V> newNode = new MapNode<>(key, value);
        newNode.setNext(table[index]);
        table[index] = newNode;
        size++;
    }

    public V get(K key) {
        int index = hash(key);
        MapNode<K, V> current = table[index];

        while (current != null) {
            if (current.getKey().equals(key)) {
                return current.getValue();
            }
            current = current.getNext();
        }

        return null;
    }

    public boolean containsKey(K key) {
        return get(key) != null;
    }

    public void remove(K key) {
        int index = hash(key);
        MapNode<K, V> current = table[index];
        MapNode<K, V> previous = null;

        while (current != null) {
            if (current.getKey().equals(key)) {
                if (previous == null) {
                    table[index] = current.getNext();
                } else {
                    previous.setNext(current.getNext());
                }
                size--;
                return;
            }
            previous = current;
            current = current.getNext();
        }
    }

    public LinkedList<V> values() {
        LinkedList<V> result = new LinkedList<>();

        for (MapNode<K, V> node : table) {
            while (node != null) {
                result.add(node.getValue());
                node = node.getNext();
            }
        }

        return result;
    }

    public void clear(){
        table = new MapNode[SIZE];
        size = 0;
    }


    public LinkedList<K> keySet() {
        LinkedList<K> keys = new LinkedList<>();

        for (MapNode<K, V> node : table) {
            while (node != null) {
                keys.add(node.getKey());
                node = node.getNext();
            }
        }

        return keys;
    }

    public int size() {
        return size;
    }

    // Method to get the key at a specific index
    public K getKey(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        int currentIndex = 0;
        for (int i = 0; i < table.length; i++) {
            MapNode<K, V> current = table[i];
            while (current != null) {
                if (currentIndex == index) {
                    return current.getKey();
                }
                currentIndex++;
                current = current.getNext();
            }
        }

        return null; // Should not reach here if index is valid
    }


}



