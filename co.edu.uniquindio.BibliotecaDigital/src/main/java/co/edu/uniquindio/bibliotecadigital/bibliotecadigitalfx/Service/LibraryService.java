package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;

import java.util.HashMap;

public class LibraryService {
    private static LibraryService instance;
    private HashMap<String, Reader> readers;

    private LibraryService() {
        readers = new HashMap<>();
    }

    public static LibraryService getInstance() {
        if (instance == null) {
            instance = new LibraryService();
        }
        return instance;
    }

    public boolean registerReader(Reader reader) {
        if (readers.containsKey(reader.getUsername())) {
            return false; // Already exists
        }
        readers.put(reader.getUsername(), reader);
        return true;
    }

    public Reader getReader(String username) {
        return readers.get(username);
    }

    public boolean exists(String username) {
        return readers.containsKey(username);
    }
}

