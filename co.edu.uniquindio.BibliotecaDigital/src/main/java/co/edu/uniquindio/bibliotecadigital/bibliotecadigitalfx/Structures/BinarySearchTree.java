package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures;

import java.util.LinkedList;
import java.util.Queue;

public class BinarySearchTree<T extends Comparable<T>> {

    private class Node {
        public int value;
        T data;
        Node left, right;

        Node(T data) {
            this.data = data;
        }
        public Node(int value) {
            this.value = value;
            this.left = null;
            this.right = null;
        }
    }

    private Node root;

    // Insertar un valor en el árbol
    public void insert(T value) {
        root = insertRecursive(root, value);
    }

    private Node insertRecursive(Node current, T value) {
        if (current == null) {
            return new Node(value);
        }

        if (value.compareTo(current.data) < 0) {
            current.left = insertRecursive(current.left, value);
        } else if (value.compareTo(current.data) > 0) {
            current.right = insertRecursive(current.right, value);
        }

        return current;
    }

    // Buscar un valor
    public boolean search(T value) {
        return searchRecursive(root, value);
    }

    private boolean searchRecursive(Node current, T value) {
        if (current == null) {
            return false;
        }

        int cmp = value.compareTo(current.data);
        if (cmp == 0) return true;
        if (cmp < 0) return searchRecursive(current.left, value);
        else return searchRecursive(current.right, value);
    }

    // Eliminar un valor
    public void delete(T value) {
        root = deleteRecursive(root, value);
    }

    private Node deleteRecursive(Node current, T value) {
        if (current == null) return null;

        int cmp = value.compareTo(current.data);

        if (cmp < 0) {
            current.left = deleteRecursive(current.left, value);
        } else if (cmp > 0) {
            current.right = deleteRecursive(current.right, value);
        } else {
            // Nodo con un solo hijo o sin hijos
            if (current.left == null) return current.right;
            if (current.right == null) return current.left;

            // Nodo con dos hijos: buscar el sucesor
            T smallest = findMin(current.right);
            current.data = smallest;
            current.right = deleteRecursive(current.right, smallest);
        }

        return current;
    }

    private T findMin(Node node) {
        while (node.left != null) node = node.left;
        return node.data;
    }

    // Recorridos
    public void inOrder() {
        inOrderRecursive(root);
        System.out.println();
    }

    private void inOrderRecursive(Node node) {
        if (node != null) {
            inOrderRecursive(node.left);
            System.out.print(node.data + " ");
            inOrderRecursive(node.right);
        }
    }

    public void preOrder() {
        preOrderRecursive(root);
        System.out.println();
    }

    private void preOrderRecursive(Node node) {
        if (node != null) {
            System.out.print(node.data + " ");
            preOrderRecursive(node.left);
            preOrderRecursive(node.right);
        }
    }

    public void postOrder() {
        postOrderRecursive(root);
        System.out.println();
    }

    private void postOrderRecursive(Node node) {
        if (node != null) {
            postOrderRecursive(node.left);
            postOrderRecursive(node.right);
            System.out.print(node.data + " ");
        }
    }

    public void imprimir() {
        if (root == null) return;

        Queue<Node> cola = new LinkedList<>();
        cola.add(root);

        while (!cola.isEmpty()) {
            Node actual = cola.poll();
            System.out.print(actual.value + " ");

            if (actual.left != null) cola.add(actual.left);
            if (actual.right != null) cola.add(actual.right);


        }}

    // Altura del árbol
    public int height() {
        return heightRecursive(root);
    }

    private int heightRecursive(Node node) {
        if (node == null) return -1;
        return 1 + Math.max(heightRecursive(node.left), heightRecursive(node.right));
    }

    // Verificar si está vacío
    public boolean isEmpty() {
        return root == null;
    }
}

