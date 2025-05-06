package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.LibraryUtil;

public class Library {
    LinkedList<Reader> readersList = new LinkedList<>();
    LinkedList<Book> bookssList = new LinkedList<>();

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
}
