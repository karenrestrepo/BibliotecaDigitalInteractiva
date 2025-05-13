package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.*;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.HashMap;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.LinkedList;

import java.io.*;

public class Persistence {

    private static Person currentUser;
    private static final String READERS_FILE = "src/main/resources/Archivos/Readers/Readers.txt";
    private static final String ADMINS_FILE = "src/main/resources/Archivos/Administrators/Administrators.txt";
    private static final String BOOKS_FILE = "src/main/resources/Archivos/Books/Books.txt";


    private HashMap<String, Reader> readers; // Key: username
    private HashMap<String, Administrator> administrators;

    private HashMap<String, Book> books;

    public Persistence() {
        readers = new HashMap<>();
        administrators = new HashMap<>();
        books = new HashMap<>();
        loadReadersFromFile();
        loadAdmin(); // always ensures admin exists
        loadBooksFromFile();
    }

    private void loadAdmin() {
        try (BufferedReader reader = new BufferedReader(new FileReader(ADMINS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String name = parts[0].trim();
                    String username = parts[1].trim();
                    String password = parts[2].trim();
                    administrators.put(username, new Administrator(name, username, password));
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading administrators: " + e.getMessage());
        }
    }

    private void loadReadersFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(READERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String name = parts[0].trim();
                    String username = parts[1].trim();
                    String password = parts[2].trim();

                    Reader r = new Reader(name, username, password);
                    readers.put(username, r);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading readers: " + e.getMessage());
        }
    }

    private void saveReaderToFile(Reader reader) {
        if (readers.containsKey(reader.getUsername())) {
            System.out.println("El lector ya está registrado.");
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(READERS_FILE, true))) {

            writer.write(reader.getName() + "," + reader.getUsername() + "," + reader.getPassword());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving reader: " + e.getMessage());
        }
    }

    public boolean registerReader(String name, String username, String password) {
        if (readers.get(username) == null) {
            Reader reader = new Reader(name, username, password);
            readers.put(username, reader);
            saveReaderToFile(reader);
            return true;
        }
        return false;
    }

    public Person login(String username, String password) {
        Reader user = readers.get(username);
        Administrator administrator = administrators.get(username);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            return user;
        }
        if (administrator != null && administrator.getPassword().equals(password)) {
            currentUser = administrator;
            return administrator;
        }
        return null;
    }

    public void loadBooksFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(BOOKS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String id = parts[0].trim();
                    String title = parts[1].trim();
                    String author = parts[2].trim();
                    int year = Integer.parseInt(parts[3].trim());
                    String category = parts[4].trim();
                    Book book = new Book(id, title, author, year, category);
                    books.put(id, book); // insertamos en el mapa por ID
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading books: " + e.getMessage());
        }
    }

    public void saveBookToFile(Book book) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKS_FILE, true))) {
            writer.write(book.getIdBook() + "," + book.getTitle() + "," + book.getAuthor() + "," + book.getYear() + "," + book.getCategory());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error saving book: " + e.getMessage());
        }
    }

    public void saveAllBooks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKS_FILE))) {
            for (Book book : books.values()) {
                writer.write(book.getIdBook() + "," + book.getTitle() + "," + book.getAuthor() + "," + book.getYear() + "," + book.getCategory());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing books: " + e.getMessage());
        }
    }


    public LinkedList<Reader> getAllReaders() {
        LinkedList<Reader> list = new LinkedList<>();
        LinkedList<String> keys = readers.keySet();
        for (int i = 0; i < keys.getSize(); i++) {
            Reader u = readers.get(keys.getAmountNodo(i));
            if (u instanceof Reader) {
                list.addEnd((Reader) u);
            }
        }
        return list;
    }
    public static Person getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(Person user) {
        currentUser = user;
    }

    public HashMap<String, Book> getBooks() {
        return books;
    }

    public HashMap<String, Reader> getReaders() {
        return readers;
    }

}
