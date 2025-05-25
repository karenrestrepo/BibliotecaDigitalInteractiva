package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Nodes;

public class NodeTree<T> {
    private T data;
    private NodeTree<T> left, right;

    public NodeTree(T data) {
        this.data = data;
        this.left = null;
        this.right = null;
    }

    // Getters y setters
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public NodeTree<T> getLeft() {
        return left;
    }

    public void setLeft(NodeTree<T> left) {
        this.left = left;
    }

    public NodeTree<T> getRight() {
        return right;
    }

    public void setRight(NodeTree<T> right) {
        this.right = right;
    }
}

