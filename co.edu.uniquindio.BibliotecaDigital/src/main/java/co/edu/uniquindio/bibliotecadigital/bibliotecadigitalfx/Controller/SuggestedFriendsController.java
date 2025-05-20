package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service.LibrarySystem;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Graph;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.util.*;

public class SuggestedFriendsController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ListView<Reader> lvSuggestedFriends;

    private Persistence librarySystem;
    private LibrarySystem library;

    // Métodos setter para inyectar las dependencias desde otro controlador
    public void setLibrarySystem(Persistence librarySystem) {
        this.librarySystem = librarySystem;
    }

    public void setLibrary(LibrarySystem library) {
        this.library = library;
    }

    @FXML
    void initialize() {
        assert lvSuggestedFriends != null : "fx:id=\"lvSuggestedFriends\" was not injected: check your FXML file 'SuggestedFriends.fxml'.";

        if (librarySystem == null || library == null) {
            System.err.println("Error: librarySystem o library no han sido inicializados.");
            return;
        }

        Reader currentReader = (Reader) librarySystem.getCurrentUser();

        if (currentReader == null) {
            System.err.println("Error: No hay un lector autenticado.");
            return;
        }

        Graph<Reader> affinityGraph = library.getAffinityGraph();

        if (affinityGraph == null) {
            System.err.println("Error: El grafo de afinidades es nulo.");
            return;
        }

        Map<Reader, Set<Reader>> graphMap = affinityGraph.getAdjacencyList();

        if (graphMap == null) {
            System.err.println("Error: El mapa de adyacencias es nulo.");
            return;
        }

        List<Reader> suggestedFriends = getSuggestedFriends(currentReader, graphMap);
        ObservableList<Reader> observableSuggestions = FXCollections.observableArrayList(suggestedFriends);
        lvSuggestedFriends.setItems(observableSuggestions);
    }

    private List<Reader> getSuggestedFriends(Reader currentReader, Map<Reader, Set<Reader>> affinityGraph) {
        Set<Reader> directFriends = affinityGraph.getOrDefault(currentReader, new HashSet<>());
        Set<Reader> suggestions = new HashSet<>();

        for (Reader friend : directFriends) {
            Set<Reader> friendsOfFriend = affinityGraph.getOrDefault(friend, new HashSet<>());
            for (Reader candidate : friendsOfFriend) {
                if (!candidate.equals(currentReader) && !directFriends.contains(candidate)) {
                    suggestions.add(candidate);
                }
            }
        }

        // Ordenar alfabéticamente por nombre (si tienes getName())
        List<Reader> orderedSuggestions = new ArrayList<>(suggestions);
        orderedSuggestions.sort(Comparator.comparing(Reader::getName));
        return orderedSuggestions;
    }
}
