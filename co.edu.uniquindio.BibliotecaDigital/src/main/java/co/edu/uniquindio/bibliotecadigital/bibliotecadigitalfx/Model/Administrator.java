package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service.LibrarySystem;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Graph;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.LibraryUtil;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;

import java.util.*;
import java.util.stream.Collectors;

public class Administrator extends Person {
    Library library;


    public Administrator(String name, String username, String password, Library library) {
        super(name, username, password);
        this.library = library;
    }
    public Administrator(String name, String username, String password) {
        super(name, username, password);
    }
    public Administrator() {}

    // Añadir y eliminar libros.
    public boolean addBook(Book book){
        if (book != null){
            library.getBookssList().add(book);
            return true;
        }
        return false;
    }

    public boolean removeBook(Book book){
        if (book != null){
            library.getBookssList().delete(book);
            return true;
        }
        return false;
    }

    // Gestionar usuarios.
    public boolean addUser(Reader person){
        if (person != null && !library.getReadersList().contains(person)){
            library.getReadersList().add(person);
            return true;
        }
        return false;
    }

    public boolean removeUser(Reader person){
        if (person != null){
            library.getReadersList().delete(person);
            return true;
        }
        return false;
    }

    //Visualizar el grafo de afinidad entre lectores.

    public Graph<Reader> getAffinityGraph(){
        LibrarySystem fx = new LibrarySystem();//Hay que corregir
        return fx.getAffinityGraph();
    }

    // Cantidad de préstamos por lector.
    public Map<Person, Integer> getLoansPerReader(){
        Map<Person, Integer> loansMap = new HashMap<>();
        for (int i = 0; i < library.getReadersList().getSize(); i++) {
            Reader reader = library.getReadersList().getAmountNodo(i);
            int loans = reader.getLoanHistoryList().getSize(); // método getter necesario
            loansMap.put(reader, loans);
        }
        return loansMap;
    }


    // Libros más valorados.
    public List<Book> getTopRatedBooks(int topN){
        return library.getBookssList().stream()
                .sorted((b1, b2) -> Double.compare(b2.getAverageRating(), b1.getAverageRating()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    // Lectores con más conexiones.
    public List<Reader> getMostConnectedReaders(int topN){
        Graph<Reader> graph = getAffinityGraph();
        return graph.getVertices().stream()
                .sorted((p1, p2) -> Integer.compare(
                        graph.getAdjacentVertices(p2).size(),
                        graph.getAdjacentVertices(p1).size()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    // Caminos más cortos entre dos lectores.
    public LinkedList<Reader> getShortestPathBetweenReaders(Reader a, Reader b){
        Graph<Reader> graph = getAffinityGraph();
        co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList<Reader> customPath = graph.getShortestPath(a, b);

        java.util.LinkedList<Reader> standardPath = new java.util.LinkedList<>();
        for (Reader person : customPath) {
            standardPath.add(person);
        }

        return standardPath;
    }


    // Detectar clústeres de afinidad (grupos).
    public List<Set<Reader>> detectAffinityClusters(){
        Graph<Reader> graph = getAffinityGraph();
        co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList<HashSet<Reader>> customComponents = graph.getConnectedComponents();

        List<Set<Reader>> standardComponents = new java.util.ArrayList<>();
        for (HashSet<Reader> component : customComponents) {
            standardComponents.add(component);
        }

        return standardComponents;
    }

}

