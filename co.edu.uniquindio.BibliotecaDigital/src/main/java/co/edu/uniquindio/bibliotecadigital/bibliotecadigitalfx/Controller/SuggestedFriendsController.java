package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Controller;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Person;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service.LibrarySystem;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Graph;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.util.*;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;


public class SuggestedFriendsController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private ListView<Reader> lvSuggestedFriends;

    private  Persistence librarySystem;
    private  LibrarySystem library;

    /**
     *

    @FXML
    void initialize() {
        assert lvSuggestedFriends != null : "fx:id=\"lvSuggestedFriends\" was not injected: check your FXML file 'SuggestedFriends.fxml'.";

        Reader currentReader = (Reader) librarySystem.getCurrentUser(); // Obtener el lector autenticado
        Graph<Reader> affinityGraph = library.getAffinityGraph();
        Map<Reader, Set<Reader>> graphMap = affinityGraph.getAdjacencyList(); // Cambiar a Map<Reader, Set<Reader>>

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

        return new ArrayList<>(suggestions);
    }

     */
}
