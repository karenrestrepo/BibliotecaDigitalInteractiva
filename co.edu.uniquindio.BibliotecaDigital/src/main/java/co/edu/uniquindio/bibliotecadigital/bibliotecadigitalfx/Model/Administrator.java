package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Graph;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.LibraryUtil;

import java.util.*;
import java.util.stream.Collectors;

public class Administrator extends Person {
    Library library = LibraryUtil.initializeData();

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
            library.getBookssList().remove(book);
            return true;
        }
        return false;
    }

    // Gestionar usuarios.
    public boolean addUser(Person person){
        if (person != null && !library.getUserList().contains(person)){
            library.getUserList().add(person);
            return true;
        }
        return false;
    }

    public boolean removeUser(Person person){
        if (person != null){
            return library.getUserList().remove(person);
        }
        return false;
    }

    // Visualizar el grafo de afinidad entre lectores.
    public Graph<Person> getAffinityGraph(){
        return library.getAffinityGraph();
    }

    // Cantidad de préstamos por lector.
    public Map<Person, Integer> getLoansPerReader(){
        Map<Person, Integer> loansMap = new HashMap<>();
        for (Loan loan : library.getLoanList()) {
            Person reader = loan.getReader();
            loansMap.put(reader, loansMap.getOrDefault(reader, 0) + 1);
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
    public List<Person> getMostConnectedReaders(int topN){
        Graph<Person> graph = getAffinityGraph();
        return graph.getVertices().stream()
                .sorted((p1, p2) -> Integer.compare(
                        graph.getAdjacentVertices(p2).size(),
                        graph.getAdjacentVertices(p1).size()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    // Caminos más cortos entre dos lectores.
    public List<Person> getShortestPathBetweenReaders(Person a, Person b){
        Graph<Person> graph = getAffinityGraph();
        return graph.getShortestPath(a, b); // Suponiendo método en Graph
    }

    // Detectar clústeres de afinidad (grupos).
    public List<Set<Person>> detectAffinityClusters(){
        Graph<Person> graph = getAffinityGraph();
        return graph.getConnectedComponents(); // Suponiendo método en Graph
    }
}

