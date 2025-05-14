package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Nodes.MapNode;


public class HashMap<K, V> {
    private static final int SIZE = 100;
    private MapNode<K, V>[] table;

    @SuppressWarnings("unchecked")
    public HashMap() {
        table = new MapNode[SIZE];
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


}



