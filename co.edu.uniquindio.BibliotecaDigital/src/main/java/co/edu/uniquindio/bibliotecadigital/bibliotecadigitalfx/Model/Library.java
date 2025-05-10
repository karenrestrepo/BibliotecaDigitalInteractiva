package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Graph;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.HashMap;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.LibraryUtil;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.collections.ObservableList;

public class Library {
    Persistence persistence = new Persistence();

    private HashMap<String, Book> books = new HashMap<>();

    LinkedList<Reader> readersList = new LinkedList<>();
    LinkedList<Book> bookssList = new LinkedList<>();
    LinkedList<Administrator> administrators = new LinkedList<>();


    public Book  createBook(String id, String title, String author, int year, String category){
        Book newbook = null;
        verifyBookDoesNotExist(id);

            newbook = new Book();
            newbook.setIdBook(id);
            newbook.setTitle(title);
            newbook.setAuthor(author);
            newbook.setYear(year);
            newbook.setCategory(category);
            books.put(id, newbook);
            persistence.saveBookToFile(newbook);

            return newbook;
    }

    public boolean removeBook(String id) {
        Book book = getBookById(id);
        if (book == null) {
            throw new RuntimeException("The book to delete does not exist");
        }
        books.remove(id);
        persistence.saveAllBooks();

        return true;
    }

    private Book getBookById(String id) {
        return books.get(id);

    }

    private void verifyBookDoesNotExist(String id) {
        if (bookExists(id)) {
            throw new RuntimeException("The book with ID: " + id + " already exists");
        }
    }

    public boolean bookExists(String id) {
        return books.containsKey(id);

    }

    public LinkedList<Reader> getReadersList() {
        return readersList;
    }

    public void setReadersList(LinkedList<Reader> readersList) {
        this.readersList = readersList;
    }

    public LinkedList<Book> getBookssList() {
        return bookssList;
    }

    public void setBookssList(LinkedList<Book> bookssList) {
        this.bookssList = bookssList;
    }

    public LinkedList<Administrator> getAdministrators() {
        return administrators;
    }

    public HashMap<String, Book> getBooks() {
        return books;
    }
    public void setAdministrators(LinkedList<Administrator> administrators) {
        this.administrators = administrators;
    }


    /**
     *

    public Graph<Person> getAffinityGraph() {
        Graph<Person> affinityGraph = new Graph<>();

        // Supongamos que tienes una lista de personas en la biblioteca
        for (Person person : personList) {
            affinityGraph.addVertex(person);
        }

        // Ahora agregamos las aristas basadas en afinidad (ej: libros comunes)
        for (int i = 0; i < personList.size(); i++) {
            for (int j = i + 1; j < personList.size(); j++) {
                Person p1 = personList.get(i);
                Person p2 = personList.get(j);

                if (haveAffinity(p1, p2)) {
                    affinityGraph.addEdge(p1, p2);
                }
            }
        }

        return affinityGraph;
    }
    private boolean haveAffinity(Reader p1, Reader p2) {
        // Ejemplo: tienen al menos un libro en común prestado
        for (Book book : p1.get) {
            if (p2.getBorrowedBooks().contains(book)) {
                return true;
            }
        }
        return false;
    }

     * @return
     */
}
