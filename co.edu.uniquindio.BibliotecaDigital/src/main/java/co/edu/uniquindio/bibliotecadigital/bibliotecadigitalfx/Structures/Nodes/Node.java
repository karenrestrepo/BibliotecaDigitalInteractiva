package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Nodes;

public class Node<T> {
    public T element;
    public int priority;

    public Node(T element, int priority) {
        this.element = element;
        this.priority = priority;
    }
}
