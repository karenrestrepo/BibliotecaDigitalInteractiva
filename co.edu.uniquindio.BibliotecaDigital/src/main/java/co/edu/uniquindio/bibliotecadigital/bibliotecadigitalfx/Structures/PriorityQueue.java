package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Nodes.Node;

public class PriorityQueue<T> {

    private LinkedList<Node<T>> queue;

    public PriorityQueue() {
        this.queue = new LinkedList<>();
    }

    // Agregar un elemento con prioridad
    public void enqueue(T element, int priority) {
        Node<T> newNode = new Node<>(element, priority);

        if (queue.isEmpty()) {
            queue.add(newNode);
            return;
        }

        int i = 0;

        while (i < queue.getSize() && queue.getAmountNodo(i).priority <= priority) {
            i++;
        }

        queue.add(i, newNode); // insertar en la posición adecuada
    }

    // Eliminar y devolver el elemento de mayor prioridad
    public T dequeue() {
        if (queue.isEmpty()) {
            return null;
        }

        Node<T> node = queue.getAmountNodo(0);
        queue.delete(node);
        return node.element;
    }

    // Ver el elemento de mayor prioridad sin eliminarlo
    public T peek() {
        if (queue.isEmpty()) {
            return null;
        }

        return queue.getAmountNodo(0).element;
    }

    // Verificar si está vacía
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    // Obtener tamaño
    public int size() {
        return queue.getSize();
    }

    // Mostrar la cola (para depuración)
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PriorityQueue: ");
        for (int i = 0; i < queue.getSize(); i++) {
            Node<T> node = queue.getAmountNodo(i);
            sb.append("[").append(node.element).append(", p=").append(node.priority).append("] ");
        }
        return sb.toString();
    }
}

