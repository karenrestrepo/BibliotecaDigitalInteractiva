package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Nodes.NodeTree;

import java.util.*;
import java.util.LinkedList;
import java.util.function.Function;

public class BinarySearchTree<T> {

    private NodeTree<T> root;
    private Comparator<T> comparator;

    public BinarySearchTree(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public NodeTree<T> getRoot() {
        return root;
    }

    // Insertar un valor en el árbol
    public void insert(T value) {
        root = insertRecursive(root, value);
    }

    private NodeTree<T> insertRecursive(NodeTree<T> current, T value) {
        if (current == null) {
            return new NodeTree<>(value);
        }

        int cmp = comparator.compare(value, current.getData());

        if (cmp < 0) {
            current.setLeft(insertRecursive(current.getLeft(), value));
        } else if (cmp > 0) {
            current.setRight(insertRecursive(current.getRight(), value));
        }
        // Si cmp == 0 no insertamos (o manejar duplicados si quieres)

        return current;
    }

    // Buscar un valor
    public boolean search(T value) {
        return searchRecursive(root, value);
    }

    private boolean searchRecursive(NodeTree<T> current, T value) {
        if (current == null) {
            return false;
        }

        int cmp = comparator.compare(value, current.getData());

        if (cmp == 0) return true;
        if (cmp < 0) return searchRecursive(current.getLeft(), value);
        else return searchRecursive(current.getRight(), value);
    }

    public T searchObject(T value) {
        return searchRecursiveobject(root, value);
    }

    private T searchRecursiveobject(NodeTree<T> node, T value) {
        if (node == null) {
            return null; // No encontrado
        }

        int cmp = comparator.compare(value, node.getData());

        if (cmp == 0) {
            return node.getData(); // ¡Encontrado!
        } else if (cmp < 0) {
            return searchRecursiveobject(node.getLeft(), value);
        } else {
            return searchRecursiveobject(node.getRight(), value);
        }
    }


    // Eliminar un valor
    public void delete(T value) {
        root = deleteRecursive(root, value);
    }

    private NodeTree<T> deleteRecursive(NodeTree<T> current, T value) {
        if (current == null) return null;

        int cmp = comparator.compare(value, current.getData());

        if (cmp < 0) {
            current.setLeft(deleteRecursive(current.getLeft(), value));
        } else if (cmp > 0) {
            current.setRight(deleteRecursive(current.getRight(), value));
        } else {
            // Nodo con un solo hijo o sin hijos
            if (current.getLeft() == null) return current.getRight();
            if (current.getRight() == null) return current.getLeft();

            // Nodo con dos hijos: buscar el sucesor mínimo
            T smallest = findMin(current.getRight());
            current.setData(smallest);
            current.setRight(deleteRecursive(current.getRight(), smallest));
        }

        return current;
    }

    private T findMin(NodeTree<T> node) {
        while (node.getLeft() != null) {
            node = node.getLeft();
        }
        return node.getData();
    }

    // Recorridos
    public void inOrder() {
        inOrderRecursive(root);
        System.out.println();
    }

    private void inOrderRecursive(NodeTree<T> node) {
        if (node != null) {
            inOrderRecursive(node.getLeft());
            System.out.print(node.getData() + " ");
            inOrderRecursive(node.getRight());
        }
    }

    public void preOrder() {
        preOrderRecursive(root);
        System.out.println();
    }

    private void preOrderRecursive(NodeTree<T> node) {
        if (node != null) {
            System.out.print(node.getData() + " ");
            preOrderRecursive(node.getLeft());
            preOrderRecursive(node.getRight());
        }
    }

    public void postOrder() {
        postOrderRecursive(root);
        System.out.println();
    }

    private void postOrderRecursive(NodeTree<T> node) {
        if (node != null) {
            postOrderRecursive(node.getLeft());
            postOrderRecursive(node.getRight());
            System.out.print(node.getData() + " ");
        }
    }

    public void imprimir() {
        if (root == null) return;

        Queue<NodeTree<T>> cola = new LinkedList<>();
        cola.add(root);

        while (!cola.isEmpty()) {
            NodeTree<T> actual = cola.poll();
            System.out.print(actual.getData() + " ");

            if (actual.getLeft() != null) cola.add(actual.getLeft());
            if (actual.getRight() != null) cola.add(actual.getRight());
        }
        System.out.println();
    }

    public int size() {
        return sizeRecursive(root);
    }

    private int sizeRecursive(NodeTree<T> node) {
        if (node == null) {
            return 0;
        }
        return 1 + sizeRecursive(node.getLeft()) + sizeRecursive(node.getRight());
    }

    // Altura del árbol
    public int height() {
        return heightRecursive(root);
    }

    private int heightRecursive(NodeTree<T> node) {
        if (node == null) return -1;
        return 1 + Math.max(heightRecursive(node.getLeft()), heightRecursive(node.getRight()));
    }
    public void clear() {
        root = null;
    }


    // Verificar si está vacío
    public boolean isEmpty() {
        return root == null;
    }

    public List<T> obtenerListainOrder() {
        List<T> list = new ArrayList<>();
        inOrderRecursive(root, list);
        return list;
    }

    private void inOrderRecursive(NodeTree<T> node, List<T> list) {
        if (node != null) {
            inOrderRecursive(node.getLeft(), list);
            list.add(node.getData());  // aquí agregamos el dato a la lista
            inOrderRecursive(node.getRight(), list);
        }
    }

    // Método genérico para buscar coincidencias parciales usando un extractor de clave
    public List<T> searchPartialMatches(String text, Function<T, String> keyExtractor) {
        List<T> result = new ArrayList<>();
        searchPartialRecursive(root, text.toLowerCase(), result, keyExtractor);
        return result;
    }


    private void searchPartialRecursive(NodeTree<T> node, String text, List<T> result, Function<T, String> keyExtractor) {
        if (node == null) return;

        searchPartialRecursive(node.getLeft(), text, result, keyExtractor);

        String key = keyExtractor.apply(node.getData());
        if (key != null && key.toLowerCase().contains(text)) {
            result.add(node.getData());
        }

        searchPartialRecursive(node.getRight(), text, result, keyExtractor);
    }

}





