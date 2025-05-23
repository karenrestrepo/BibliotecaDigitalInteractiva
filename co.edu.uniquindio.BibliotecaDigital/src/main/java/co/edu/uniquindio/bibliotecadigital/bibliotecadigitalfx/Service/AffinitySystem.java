package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Rating;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Graph;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;

import java.util.HashSet;
import java.util.Set;

/**
 * Sistema mejorado para gestionar la afinidad entre lectores
 * basado en valoraciones similares de libros comunes
 */
public class AffinitySystem {
    private Library library;
    private Graph<Reader> affinityGraph;

    public AffinitySystem(Library library) {
        this.library = library;
        this.affinityGraph = new Graph<>();
        buildAffinityGraph();
    }

    /**
     * Construye el grafo de afinidad analizando las valoraciones
     * de todos los lectores en la biblioteca
     */
    public void buildAffinityGraph() {
        LinkedList<Reader> readers = library.getReadersList();

        // Añadir todos los lectores como vértices
        for (Reader reader : readers) {
            affinityGraph.addVertex(reader);
        }

        // Comparar cada par de lectores para determinar afinidad
        for (int i = 0; i < readers.getSize(); i++) {
            for (int j = i + 1; j < readers.getSize(); j++) {
                Reader reader1 = readers.getAmountNodo(i);
                Reader reader2 = readers.getAmountNodo(j);

                if (haveAffinity(reader1, reader2)) {
                    affinityGraph.addEdge(reader1, reader2);
                }
            }
        }
    }

    /**
     * Determina si dos lectores tienen afinidad basándose en:
     * 1. Han valorado al menos 3 libros en común
     * 2. Sus valoraciones son similares (diferencia máxima de 1 estrella)
     */
    private boolean haveAffinity(Reader reader1, Reader reader2) {
        int commonBooksWithSimilarRatings = 0;

        // Necesitamos acceso a las valoraciones de cada lector
        // Esto requiere que implementemos un sistema de valoraciones
        LinkedList<Rating> ratings1 = reader1.getRatingsList();
        LinkedList<Rating> ratings2 = reader2.getRatingsList();

        // Comparar valoraciones de libros comunes
        for (Rating rating1 : ratings1) {
            for (Rating rating2 : ratings2) {
                // Si valoraron el mismo libro
                if (rating1.getBook().getIdBook().equals(rating2.getBook().getIdBook())) {
                    // Si las valoraciones son similares (diferencia ≤ 1)
                    if (Math.abs(rating1.getStars() - rating2.getStars()) <= 1) {
                        commonBooksWithSimilarRatings++;
                    }
                }
            }
        }

        // Requieren al menos 3 libros en común con valoraciones similares
        return commonBooksWithSimilarRatings >= 3;
    }

    /**
     * Obtiene sugerencias de amigos para un lector basándose en
     * "amigos de amigos" (tránsito en el grafo)
     */
    public LinkedList<Reader> getSuggestedFriends(Reader reader) {
        LinkedList<Reader> suggestions = new LinkedList<>();
        Set<Reader> visited = new HashSet<>();

        // Obtener amigos directos
        HashSet<Reader> directFriends = affinityGraph.getAdjacentVertices(reader);
        if (directFriends == null) {
            return suggestions; // No tiene amigos directos
        }

        visited.add(reader);
        visited.addAll(directFriends);

        // Buscar amigos de amigos
        for (Reader friend : directFriends) {
            HashSet<Reader> friendsOfFriend = affinityGraph.getAdjacentVertices(friend);
            if (friendsOfFriend != null) {
                for (Reader candidate : friendsOfFriend) {
                    if (!visited.contains(candidate)) {
                        suggestions.add(candidate);
                        visited.add(candidate);
                    }
                }
            }
        }

        return suggestions;
    }

    /**
     * Encuentra el camino más corto entre dos lectores
     */
    public LinkedList<Reader> getShortestPath(Reader start, Reader end) {
        return affinityGraph.getShortestPath(start, end);
    }

    /**
     * Detecta grupos o clústeres de afinidad
     */
    public LinkedList<HashSet<Reader>> detectAffinityClusters() {
        return affinityGraph.getConnectedComponents();
    }

    /**
     * Obtiene estadísticas de conexiones por lector
     */
    public LinkedList<Reader> getMostConnectedReaders() {
        LinkedList<Reader> allReaders = affinityGraph.getVertices();
        LinkedList<Reader> sortedReaders = new LinkedList<>();

        // Ordenar lectores por número de conexiones (implementación simple)
        for (Reader reader : allReaders) {
            HashSet<Reader> connections = affinityGraph.getAdjacentVertices(reader);
            int connectionCount = connections != null ? connections.size() : 0;

            // Insertar en posición correcta (ordenamiento por inserción)
            boolean inserted = false;
            for (int i = 0; i < sortedReaders.getSize(); i++) {
                Reader other = sortedReaders.getAmountNodo(i);
                HashSet<Reader> otherConnections = affinityGraph.getAdjacentVertices(other);
                int otherCount = otherConnections != null ? otherConnections.size() : 0;

                if (connectionCount > otherCount) {
                    sortedReaders.add(i, reader);
                    inserted = true;
                    break;
                }
            }

            if (!inserted) {
                sortedReaders.add(reader);
            }
        }

        return sortedReaders;
    }

    public Graph<Reader> getAffinityGraph() {
        return affinityGraph;
    }

    /**
     * Actualiza el grafo cuando se añaden nuevas valoraciones
     */
    public void updateAffinityGraph() {
        affinityGraph = new Graph<>();
        buildAffinityGraph();
    }
}