package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Book;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Library;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.Reader;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.Graph;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Util.Persistence;

public class LibrarySystem {
    private Persistence persistence;

    public LibrarySystem(Persistence persistence) {
        this.persistence = persistence;
    }

    public LibrarySystem() {
    }

    public Graph<Reader> getAffinityGraph() {
        Graph<Reader> affinityGraph = new Graph<>();

        for (Reader person : persistence.getAllReaders()) {
            affinityGraph.addVertex(person);
        }

        for (int i = 0; i < persistence.getAllReaders().getSize(); i++) {
            for (int j = i + 1; j < persistence.getAllReaders().getSize(); j++) {
                Reader p1 = persistence.getAllReaders().getAmountNodo(i);
                Reader p2 = persistence.getAllReaders().getAmountNodo(j);

                if (haveAffinity(p1, p2)) {
                    affinityGraph.addEdge(p1, p2);
                }
            }
        }

        return affinityGraph;
    }
    private boolean haveAffinity(Reader p1, Reader p2) {
        for (Book book : p1.getLoanHistoryList()) {
            if (p2.getLoanHistoryList().contains(book)) {
                return true;
            }
        }
        return false;
    }
}
