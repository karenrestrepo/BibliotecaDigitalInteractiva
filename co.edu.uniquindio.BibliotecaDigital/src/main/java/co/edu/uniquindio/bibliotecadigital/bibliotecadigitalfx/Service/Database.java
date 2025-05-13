package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service;


import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.HashMap;

public class Database {
    private static HashMap<String, Reader> readers = new HashMap<>();

    public static boolean registerReader(Reader reader) {
        if (readers.containsKey(reader.getUsername())) {
            return false;
        }
        readers.put(reader.getUsername(), reader);
        return true;
    }

    public static Reader getReader(String username) {
        return readers.get(username);
    }
}
