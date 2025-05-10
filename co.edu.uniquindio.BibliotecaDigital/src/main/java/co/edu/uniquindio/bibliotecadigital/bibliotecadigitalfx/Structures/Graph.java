package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures;

import java.util.HashSet;

public class Graph<T> {

    private final HashMap<T, HashSet<T>> adjacencyList = new HashMap<>();

    public void addVertex(T vertex) {
        if (!adjacencyList.containsKey(vertex)) {
            adjacencyList.put(vertex, new HashSet<>());
        }
    }

    public void removeVertex(T vertex) {
        adjacencyList.remove(vertex);
        for (T v : adjacencyList.keySet()) {
            adjacencyList.get(v).remove(vertex);
        }
    }

    public void addEdge(T v1, T v2) {
        addVertex(v1);
        addVertex(v2);
        adjacencyList.get(v1).add(v2);
        adjacencyList.get(v2).add(v1);
    }

    public void removeEdge(T v1, T v2) {
        if (adjacencyList.containsKey(v1)) {
            adjacencyList.get(v1).remove(v2);
        }
        if (adjacencyList.containsKey(v2)) {
            adjacencyList.get(v2).remove(v1);
        }
    }

    public HashSet<T> getAdjacentVertices(T vertex) {
        return adjacencyList.get(vertex);
    }
////////////////////////////
    public LinkedList<T> getVertices() {
        return adjacencyList.keySet();
    }
////////////////////////////

    // BFS para camino más corto
    public LinkedList<T> getShortestPath(T start, T end) {
        HashMap<T, T> prev = new HashMap<>();
        HashSet<T> visited = new HashSet<>();
        LinkedList<T> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);

        while (!queue.isEmpty()) {
            T current = queue.poll();
            if (current.equals(end)) break;

            for (T neighbor : getAdjacentVertices(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    prev.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        // reconstruir camino
        LinkedList<T> path = new LinkedList<>();
        T at = end;
        while (prev.containsKey(at)) {
            path.addBeginning(at);
            at = prev.get(at);
        }

        if (at.equals(start)) {
            path.addBeginning(start);
            return path;
        }

        return new LinkedList<>(); // vacío si no hay camino
    }

    // Componentes conectados (DFS)
    public LinkedList<HashSet<T>> getConnectedComponents() {
        HashSet<T> visited = new HashSet<>();
        LinkedList<HashSet<T>> components = new LinkedList<>();

        for (T vertex : getVertices()) {
            if (!visited.contains(vertex)) {
                HashSet<T> component = new HashSet<>();
                dfs(vertex, visited, component);
                components.add(component);
            }
        }

        return components;
    }

    private void dfs(T current, HashSet<T> visited, HashSet<T> component) {
        visited.add(current);
        component.add(current);
        for (T neighbor : getAdjacentVertices(current)) {
            if (!visited.contains(neighbor)) {
                dfs(neighbor, visited, component);
            }
        }
    }

    @Override
    public String toString() {
        return adjacencyList.toString();
    }
}