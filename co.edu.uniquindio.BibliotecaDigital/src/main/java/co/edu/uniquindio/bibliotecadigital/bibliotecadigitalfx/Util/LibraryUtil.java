package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Enum.BookStatus;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Administrator;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;

public class LibraryUtil {
    public static Library initializeData() {
        Library library = new Library();

        Reader reader = new Reader();
        reader.setName("Ana");
        reader.setUsername("Ana@gmail.com");
        reader.setPassword("Ana123");

        library.getReadersList().addBeginning(reader);

        Reader reader1 = new Reader();
        reader.setName("Tomas");
        reader.setUsername("Tomas@gmail.com");
        reader.setPassword("Tomas123");

        library.getReadersList().addBeginning(reader1);

        Administrator administrator = new Administrator();
        administrator.setName("Manuela");
        administrator.setUsername("Administrador1@gmail.com");
        administrator.setPassword("1234");

        Book book = new Book();
        book.setTitle("If I stay");
        book.setAuthor("Gayle Forman");
        book.setCategory("Ficción adulto joven");
        book.setYear(2009);
        book.setStatus(BookStatus.AVAILABLE);
        book.setAverageRating(0);

        library.getBookssList().addBeginning(book);

        Book book1 = new Book();
        book.setTitle("If I stay");
        book.setAuthor("Socrates");
        book.setCategory("Apologia de Socrates");
        book.setYear(2013);
        book.setStatus(BookStatus.AVAILABLE);
        book.setAverageRating(5);

        library.getBookssList().addBeginning(book1);


        return library;
    }
}
