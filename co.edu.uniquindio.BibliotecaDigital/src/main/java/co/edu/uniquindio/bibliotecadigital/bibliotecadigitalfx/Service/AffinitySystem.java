package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Rating;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Graph;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Sistema mejorado para gestionar la afinidad entre lectores
 * CORRECCIÓN: Ahora incluye conexiones explícitas desde archivos
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
     * MÉTODO CORREGIDO: Construye el grafo considerando tanto valoraciones como conexiones explícitas
     */
    public void buildAffinityGraph() {
        LinkedList<Reader> readers = library.getReadersList();

        // Añadir todos los lectores como vértices
        for (Reader reader : readers) {
            affinityGraph.addVertex(reader);
        }

        System.out.println("🔄 Construyendo grafo de afinidad...");

        // PASO 1: Añadir conexiones basadas en valoraciones similares
        int affinityConnections = addAffinityBasedConnections(readers);

        // PASO 2: NUEVO - Añadir conexiones explícitas desde archivo
        int explicitConnections = addExplicitConnections();

        System.out.println("✅ Grafo construido: " + affinityConnections + " conexiones por afinidad + " +
                explicitConnections + " conexiones explícitas");
    }

    /**
     * NUEVO: Añade conexiones explícitas desde el archivo de conexiones
     */
    private int addExplicitConnections() {
        int count = 0;

        try {
            BufferedReader reader = getConnectionsFileReader();
            if (reader == null) {
                System.out.println("📄 No hay archivo de conexiones o está vacío");
                return 0;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        String username1 = parts[0].trim();
                        String username2 = parts[1].trim();

                        Reader reader1 = library.getReaderByUsername(username1);
                        Reader reader2 = library.getReaderByUsername(username2);

                        if (reader1 != null && reader2 != null && !reader1.equals(reader2)) {
                            // Verificar si la conexión ya existe (para evitar duplicados)
                            HashSet<Reader> connections1 = affinityGraph.getAdjacentVertices(reader1);
                            if (connections1 == null || !connections1.contains(reader2)) {
                                affinityGraph.addEdge(reader1, reader2);
                                count++;
                                System.out.println("🤝 Conexión explícita añadida: " + username1 + " <-> " + username2);
                            }
                        } else {
                            System.err.println("⚠️ No se pudo crear conexión: " + username1 + " <-> " + username2);
                        }
                    }
                }
            }
            reader.close();

        } catch (IOException e) {
            System.err.println("⚠️ Error leyendo conexiones explícitas: " + e.getMessage());
        }

        return count;
    }

    /**
     * NUEVO: Obtiene reader para el archivo de conexiones
     */
    private BufferedReader getConnectionsFileReader() throws IOException {
        // Intentar leer desde filesystem primero
        String basePath = "src/main/resources/Archivos/";
        String connectionsFile = "Connections/Connections.txt";

        Path filesystemPath = Paths.get(basePath + connectionsFile);
        if (Files.exists(filesystemPath)) {
            System.out.println("📄 Leyendo conexiones desde filesystem");
            return Files.newBufferedReader(filesystemPath);
        }

        // Intentar leer desde classpath
        InputStream classPathStream = getClass().getClassLoader()
                .getResourceAsStream("Archivos/" + connectionsFile);
        if (classPathStream != null) {
            System.out.println("📄 Leyendo conexiones desde classpath");
            return new BufferedReader(new InputStreamReader(classPathStream));
        }

        return null;
    }

    /**
     * MÉTODO SEPARADO: Añade conexiones basadas en afinidad de valoraciones
     */
    private int addAffinityBasedConnections(LinkedList<Reader> readers) {
        int count = 0;

        // Comparar cada par de lectores para determinar afinidad
        for (int i = 0; i < readers.getSize(); i++) {
            for (int j = i + 1; j < readers.getSize(); j++) {
                Reader reader1 = readers.getAmountNodo(i);
                Reader reader2 = readers.getAmountNodo(j);

                if (haveAffinity(reader1, reader2)) {
                    affinityGraph.addEdge(reader1, reader2);
                    count++;
                    System.out.println("⭐ Conexión por afinidad: " + reader1.getName() + " <-> " + reader2.getName());
                }
            }
        }

        return count;
    }

    /**
     * Determina si dos lectores tienen afinidad basándose en valoraciones similares
     * (método sin cambios)
     */
    private boolean haveAffinity(Reader reader1, Reader reader2) {
        int commonBooksWithSimilarRatings = 0;

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

    // RESTO DE MÉTODOS SIN CAMBIOS...
    public LinkedList<Reader> getSuggestedFriends(Reader reader) {
        LinkedList<Reader> suggestions = new LinkedList<>();
        Set<Reader> visited = new HashSet<>();

        HashSet<Reader> directFriends = affinityGraph.getAdjacentVertices(reader);
        if (directFriends == null) {
            return suggestions;
        }

        visited.add(reader);
        visited.addAll(directFriends);

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

    public LinkedList<Reader> getShortestPath(Reader start, Reader end) {
        return affinityGraph.getShortestPath(start, end);
    }

    public LinkedList<HashSet<Reader>> detectAffinityClusters() {
        return affinityGraph.getConnectedComponents();
    }

    public LinkedList<Reader> getMostConnectedReaders() {
        LinkedList<Reader> allReaders = affinityGraph.getVertices();
        LinkedList<Reader> sortedReaders = new LinkedList<>();

        for (Reader reader : allReaders) {
            HashSet<Reader> connections = affinityGraph.getAdjacentVertices(reader);
            int connectionCount = connections != null ? connections.size() : 0;

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
     * MÉTODO MEJORADO: Actualiza el grafo cuando se añaden nuevos datos
     */
    public void updateAffinityGraph() {
        System.out.println("🔄 Actualizando grafo de afinidad...");
        affinityGraph = new Graph<>();
        buildAffinityGraph();
    }
}