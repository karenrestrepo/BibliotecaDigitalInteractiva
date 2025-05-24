package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Enum.BookStatus;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Administrator;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;

import java.io.IOException;

public class LibraryUtil {
    public static Library initializeData() throws IOException {
        Library library = Library.getInstance();

        // Crear lectores de ejemplo
        Reader reader1 = new Reader("Ana García", "ana@gmail.com", "Ana123");
        reader1.setLibrary(library);
        library.getReadersList().add(reader1);

        Reader reader2 = new Reader("Tomás López", "tomas@gmail.com", "Tomas123");
        reader2.setLibrary(library);
        library.getReadersList().add(reader2);

        // Crear administrador
        Administrator administrator = new Administrator("Manuela Admin", "admin@biblioteca.com", "admin123");
        library.getAdministrators().add(administrator);

        // Crear libros de ejemplo
        Book book1 = new Book("001", "If I Stay", "Gayle Forman", 2009, "Ficción adulto joven");
        library.getBookssList().add(book1);

        Book book2 = new Book("002", "Apología de Sócrates", "Platón", -399, "Filosofía");
        library.getBookssList().add(book2);

        Book book3 = new Book("003", "El Quijote", "Miguel de Cervantes", 1605, "Clásico");
        library.getBookssList().add(book3);

        return library;
    }
}