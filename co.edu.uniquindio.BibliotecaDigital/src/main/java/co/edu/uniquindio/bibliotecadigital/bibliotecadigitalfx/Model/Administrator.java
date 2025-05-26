package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model;

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

}

