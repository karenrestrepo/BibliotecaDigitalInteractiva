package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Nodes;

public class ListNode <T> {
    private ListNode<T> nextNodo;
    private T amountNodo;

    public ListNode(T amountNodo) {this.amountNodo = amountNodo;}

    public ListNode(T fact, ListNode<T> next) {
        super();
        this.amountNodo = fact;
        this.nextNodo = next;
    }

    public ListNode<T> getNextNodo() {
        return nextNodo;
    }

    public void setNextNodo(ListNode<T> nextNodo) {
        this.nextNodo = nextNodo;
    }

    public T getAmountNodo() {
        return amountNodo;
    }

    public void setAmountNodo(T amountNodo) {
        this.amountNodo = amountNodo;
    }
}
