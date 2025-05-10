package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Nodes.ListNode;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LinkedList<T> implements Iterable<T> {

    private ListNode<T> firstNode;
    private ListNode<T> lastNode;
    private int size;

    public LinkedList() {
        firstNode = null;
        lastNode = null;
        size = 0;
    }

    public void addBeginning (T amountNodo){
        ListNode<T> newNodo = new ListNode<>(amountNodo);

        if(isEmpty()){
            firstNode = newNodo;

        } else {
            newNodo.setNextNodo(firstNode);
            firstNode = newNodo;
        }

        size++;
    }
    public void add(T element) {
        addEnd(element);
    }


    public void addEnd (T amountNodo) {
        ListNode<T> nodo = new ListNode<>(amountNodo);

        if (isEmpty()){
            firstNode = lastNode = nodo;

        }else{
            lastNode.setNextNodo(nodo);
            lastNode = nodo;
        }

        size++;
    }

    public T getAmountNodo(int index){
        ListNode<T> temporaryNodo = null;
        int counter = 0;

        if (validIndex(index)){
            temporaryNodo = firstNode;
            while (counter < index){
                temporaryNodo = temporaryNodo.getNextNodo();
                counter++;
            }
        }
        if(temporaryNodo != null){
            return temporaryNodo.getAmountNodo();

        }else {
            return null;
        }
    }

    private boolean validIndex(int index){
        if (index >= 0 && index < size){
            return true;
        }
        throw new RuntimeException(("Indice no válido"));
    }


    public boolean isEmpty(){
        return (firstNode == null) ? true : false;
    }

    public void printList(){
        ListNode<T> aux = firstNode;

        while (aux != null){
            System.out.println(aux.getAmountNodo()+"\t");
            aux = aux.getNextNodo();
        }
        System.out.println();
    }

    public T delete(T fact){
        ListNode<T> nodo = firstNode;
        ListNode<T> previous = null;
        ListNode<T> next = null;
        boolean found = false;

        while (nodo != null){
            if (nodo.getAmountNodo() == fact){
                found = true;
                break;
            }
            previous = nodo;
            nodo = nodo.getNextNodo();
        }

        if (found) {
            next = nodo.getNextNodo();
            if (previous == null){
                firstNode = next;
            }else {
                previous.setNextNodo(next);
            }

            if (next == null){
                lastNode = previous;
            } else {
                nodo.setNextNodo(null);
            }

            nodo = null;
            size--;
            return fact;
        }

        throw new RuntimeException("Elemento no existe");
    }

    public T deleteFirst(){
        if(!isEmpty()){
            ListNode<T> n = firstNode;
            T amount = n.getAmountNodo();
            firstNode = n.getNextNodo();

            if (firstNode == null){
                lastNode = null;
            }

            size--;
            return amount;
        }

        throw new RuntimeException("Lista vacia");
    }

    private ListNode<T> getNodo(int index){
        if (index >= 0 && index < size){
            ListNode<T> nodo = firstNode;

            for (int i = 0; i < index; i++) {
                nodo = nodo.getNextNodo();
            }

            return nodo;
        }

        return null;
    }

    public void changeNodo(int index, T newN){
        if(validIndex(index)){
            ListNode<T> nodo = getNodo(index);
            nodo.setAmountNodo(newN);
        }
    }

    public int getNodoPosition(T fact){
        int i = 0;

        for (ListNode<T> aux = firstNode; aux != null; aux = aux.getNextNodo()){
            if (aux.getAmountNodo().equals(fact)){
                return i;
            }
            i++;
        }

        return -1;

    }

    public int countRepetitions(T amount) {
        ListNode<T> current = firstNode;
        int counter = 0;

        while (current != null) {
            if (current.getAmountNodo().equals(amount)) {
                counter++;
            }
            current = current.getNextNodo();
        }

        return counter;
    }

    public boolean contains(T element) {
        ListNode<T> current = firstNode;
        while (current != null) {
            if (current.getAmountNodo().equals(element)) {
                return true;
            }
            current = current.getNextNodo();
        }
        return false;
    }

    public void clear() {
        firstNode = null;
        lastNode = null;
        size = 0;
    }

    public Object[] toArray() {
        Object[] array = new Object[size];
        ListNode<T> current = firstNode;
        int index = 0;

        while (current != null) {
            array[index++] = current.getAmountNodo();
            current = current.getNextNodo();
        }

        return array;
    }





    @Override
    public Iterator<T> iterator() {
        return new IteradorListaSimple(firstNode) {
        };
    }

    protected class IteradorListaSimple implements Iterator<T>{
        private ListNode<T> nodo;
        private int position;

        public IteradorListaSimple(ListNode<T> nodo){
            this.nodo = nodo;
            this.position = 0;
        }

        @Override
        public boolean hasNext() {
            return nodo != null;
        }

        @Override
        public T next() {
            T amount = nodo.getAmountNodo();
            nodo = nodo.getNextNodo();
            position++;
            return amount;
        }

        public int getPosition(){ return position;}
    }

    public ListNode<T> getFirstNode() {
        return firstNode;
    }

    public void setFirstNode(ListNode<T> firstNode) {
        this.firstNode = firstNode;
    }

    public ListNode<T> getLastNode() {
        return lastNode;
    }

    public void setLastNode(ListNode<T> lastNode) {
        this.lastNode = lastNode;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
    @SuppressWarnings("unchecked")
    public void priority(T element) {
        try {
            int priority = (int) element.getClass().getMethod("getPriority").invoke(element);

            ListNode<T> newNode = new ListNode<>(element);

            if (isEmpty()) {
                firstNode = lastNode = newNode;
            } else if (getPriorityValue(firstNode.getAmountNodo()) > priority) {
                // Insertar al inicio
                newNode.setNextNodo(firstNode);
                firstNode = newNode;
            } else {
                ListNode<T> current = firstNode;
                while (current.getNextNodo() != null && getPriorityValue(current.getNextNodo().getAmountNodo()) <= priority) {
                    current = current.getNextNodo();
                }
                newNode.setNextNodo(current.getNextNodo());
                current.setNextNodo(newNode);

                if (newNode.getNextNodo() == null) {
                    lastNode = newNode;
                }
            }

            size++;
        } catch (Exception e) {
            throw new RuntimeException("El elemento debe tener un método getPriority() que retorne int");
        }
    }

    private int getPriorityValue(T element) throws Exception {
        return (int) element.getClass().getMethod("getPriority").invoke(element);
    }

    public void add(int index, T element) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Índice inválido: " + index);
        }

        ListNode<T> newNode = new ListNode<>(element);

        if (index == 0) {
            newNode.setNextNodo(firstNode);
            firstNode = newNode;
            if (size == 0) {
                lastNode = newNode;
            }
        } else {
            ListNode<T> current = firstNode;
            for (int i = 0; i < index - 1; i++) {
                current = current.getNextNodo();
            }
            newNode.setNextNodo(current.getNextNodo());
            current.setNextNodo(newNode);
            if (newNode.getNextNodo() == null) {
                lastNode = newNode;
            }
        }

        size++;
    }

    public T poll() {
        if (isEmpty()) {
            return null;
        }

        T element = firstNode.getAmountNodo();
        firstNode = firstNode.getNextNodo();
        size--;

        if (firstNode == null) {
            lastNode = null;
        }

        return element;
    }

    public Stream<T> stream() {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(this.iterator(), Spliterator.ORDERED),
                false
        );
    }


}