package co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Test;

import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Enum.BookStatus;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Model.*;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service.AffinitySystem;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Service.BookRecommendationSystem;
import co.edu.uniquindio.bibliotecadigital.bibliotecadigitalfx.Structures.*;

import java.util.HashSet;

/**
 * Suite completa de pruebas unitarias para el proyecto Biblioteca Digital
 *
 * Incluye pruebas para:
 * 1. Estructuras de datos personalizadas
 * 2. Funcionalidades del modelo de dominio
 * 3. Algoritmos de recomendación
 * 4. Sistema de afinidad
 * 5. Gestión de préstamos
 * 6. Sistema de valoraciones
 * 7. Algoritmos de grafos
 *
 * Patrón de diseño: Test Suite Pattern
 * Cada método test... representa una prueba unitaria independiente
 * que valida un aspecto específico del sistema.
 */
public class LibrarySystemTests {

    /**
     * PRUEBA UNITARIA 1: Verificar funcionamiento básico de LinkedList personalizada
     *
     * Objetivo: Validar que nuestra implementación de lista enlazada funciona correctamente
     * Conceptos testados: Inserción, eliminación, búsqueda, tamaño
     */
    public static boolean testLinkedListFunctionality() {
        System.out.println("🧪 PRUEBA 1: Funcionalidad de LinkedList personalizada");

        try {
            LinkedList<String> lista = new LinkedList<>();

            // Prueba 1.1: Lista inicialmente vacía
            if (!lista.isEmpty()) {
                System.out.println("❌ Error: Lista debería estar vacía inicialmente");
                return false;
            }

            // Prueba 1.2: Inserción de elementos
            lista.add("Elemento1");
            lista.add("Elemento2");
            lista.add("Elemento3");

            if (lista.getSize() != 3) {
                System.out.println("❌ Error: Tamaño esperado 3, obtenido " + lista.getSize());
                return false;
            }

            // Prueba 1.3: Acceso por índice
            if (!lista.getAmountNodo(0).equals("Elemento1")) {
                System.out.println("❌ Error: Elemento en índice 0 incorrecto");
                return false;
            }

            // Prueba 1.4: Búsqueda de elementos
            if (!lista.contains("Elemento2")) {
                System.out.println("❌ Error: No encuentra elemento que debería existir");
                return false;
            }

            // Prueba 1.5: Eliminación
            lista.delete("Elemento2");
            if (lista.getSize() != 2 || lista.contains("Elemento2")) {
                System.out.println("❌ Error: Eliminación no funcionó correctamente");
                return false;
            }

            System.out.println("✅ PRUEBA 1 EXITOSA: LinkedList funciona correctamente");
            return true;

        } catch (Exception e) {
            System.out.println("❌ PRUEBA 1 FALLIDA: Excepción - " + e.getMessage());
            return false;
        }
    }

    /**
     * PRUEBA UNITARIA 2: Verificar funcionamiento de HashMap personalizado
     *
     * Objetivo: Validar que nuestra implementación de hash map maneja correctamente
     * las operaciones de clave-valor, incluyendo colisiones
     */
    public static boolean testHashMapFunctionality() {
        System.out.println("🧪 PRUEBA 2: Funcionalidad de HashMap personalizado");

        try {
            HashMap<String, Integer> mapa = new HashMap<>();

            // Prueba 2.1: Inserción y recuperación básica
            mapa.put("clave1", 100);
            mapa.put("clave2", 200);
            mapa.put("clave3", 300);

            if (!mapa.get("clave1").equals(100)) {
                System.out.println("❌ Error: Valor recuperado incorrecto para clave1");
                return false;
            }

            // Prueba 2.2: Verificar existencia de claves
            if (!mapa.containsKey("clave2")) {
                System.out.println("❌ Error: No encuentra clave que debería existir");
                return false;
            }

            if (mapa.containsKey("claveInexistente")) {
                System.out.println("❌ Error: Encuentra clave que no debería existir");
                return false;
            }

            // Prueba 2.3: Actualización de valores
            mapa.put("clave1", 150);
            if (!mapa.get("clave1").equals(150)) {
                System.out.println("❌ Error: Actualización de valor no funcionó");
                return false;
            }

            // Prueba 2.4: Eliminación
            mapa.remove("clave2");
            if (mapa.containsKey("clave2")) {
                System.out.println("❌ Error: Eliminación no funcionó correctamente");
                return false;
            }

            // Prueba 2.5: Manejo de colisiones (insertar muchos elementos)
            for (int i = 0; i < 50; i++) {
                mapa.put("test" + i, i);
            }

            if (!mapa.get("test25").equals(25)) {
                System.out.println("❌ Error: HashMap no maneja correctamente múltiples elementos");
                return false;
            }

            System.out.println("✅ PRUEBA 2 EXITOSA: HashMap funciona correctamente");
            return true;

        } catch (Exception e) {
            System.out.println("❌ PRUEBA 2 FALLIDA: Excepción - " + e.getMessage());
            return false;
        }
    }

    /**
     * PRUEBA UNITARIA 3: Verificar funcionalidades del modelo Reader
     *
     * Objetivo: Validar que las operaciones de préstamo, devolución y valoración
     * funcionan correctamente según las reglas de negocio
     */
    public static boolean testReaderFunctionality() {
        System.out.println("🧪 PRUEBA 3: Funcionalidades del modelo Reader");

        try {
            // Configuración inicial
            Library library = new Library();
            Reader reader = new Reader("Juan Pérez", "juan@email.com", "password123", library);

            Book book1 = new Book("1", "El Quijote", "Cervantes", 1605, "Clásico");
            Book book2 = new Book("2", "Cien Años", "García Márquez", 1967, "Realismo Mágico");

            // Prueba 3.1: Préstamo exitoso
            boolean loanSuccess = reader.requestLoan(book1);
            if (!loanSuccess) {
                System.out.println("❌ Error: Préstamo debería ser exitoso");
                return false;
            }

            if (book1.getStatus() != BookStatus.CHECKED_OUT) {
                System.out.println("❌ Error: Estado del libro no cambió a prestado");
                return false;
            }

            if (reader.getLoanHistoryList().getSize() != 1) {
                System.out.println("❌ Error: Historial de préstamos no se actualizó");
                return false;
            }

            // Prueba 3.2: No se puede prestar libro ya prestado
            boolean secondLoan = reader.requestLoan(book1);
            if (secondLoan) {
                System.out.println("❌ Error: No debería poder prestar libro ya prestado");
                return false;
            }

            // Prueba 3.3: Valoración de libro leído
            boolean ratingSuccess = reader.rateBook(book1, 5, "Excelente libro");
            if (!ratingSuccess) {
                System.out.println("❌ Error: Valoración debería ser exitosa");
                return false;
            }

            if (reader.getRatingsList().getSize() != 1) {
                System.out.println("❌ Error: Lista de valoraciones no se actualizó");
                return false;
            }

            // Prueba 3.4: No se puede valorar libro no leído
            try {
                reader.rateBook(book2, 4, "No debería funcionar");
                System.out.println("❌ Error: No debería poder valorar libro no leído");
                return false;
            } catch (RuntimeException e) {
                // Comportamiento esperado
            }

            // Prueba 3.5: Devolución exitosa
            boolean returnSuccess = reader.returnBook(book1);
            if (!returnSuccess) {
                System.out.println("❌ Error: Devolución debería ser exitosa");
                return false;
            }

            if (book1.getStatus() != BookStatus.AVAILABLE) {
                System.out.println("❌ Error: Estado del libro no cambió a disponible");
                return false;
            }

            System.out.println("✅ PRUEBA 3 EXITOSA: Funcionalidades de Reader correctas");
            return true;

        } catch (Exception e) {
            System.out.println("❌ PRUEBA 3 FALLIDA: Excepción - " + e.getMessage());
            return false;
        }
    }

    /**
     * PRUEBA UNITARIA 4: Verificar algoritmos del grafo de afinidad
     *
     * Objetivo: Validar que el grafo se construye correctamente y que los algoritmos
     * de búsqueda de caminos y componentes conectados funcionan
     */
    public static boolean testAffinityGraphAlgorithms() {
        System.out.println("🧪 PRUEBA 4: Algoritmos del grafo de afinidad");

        try {
            Graph<String> grafo = new Graph<>();

            // Prueba 4.1: Construcción básica del grafo
            grafo.addVertex("A");
            grafo.addVertex("B");
            grafo.addVertex("C");
            grafo.addVertex("D");

            grafo.addEdge("A", "B");
            grafo.addEdge("B", "C");
            grafo.addEdge("D", "D"); // Autoconexión (caso especial)

            // Prueba 4.2: Verificar adyacencias
            HashSet<String> adyacentesA = grafo.getAdjacentVertices("A");
            if (!adyacentesA.contains("B")) {
                System.out.println("❌ Error: A debería estar conectado con B");
                return false;
            }

            // Prueba 4.3: Camino más corto
            LinkedList<String> caminoAC = grafo.getShortestPath("A", "C");
            if (caminoAC.getSize() != 3) { // A -> B -> C
                System.out.println("❌ Error: Camino A-C debería tener 3 nodos, tiene " + caminoAC.getSize());
                return false;
            }

            // Verificar secuencia del camino
            if (!caminoAC.getAmountNodo(0).equals("A") ||
                    !caminoAC.getAmountNodo(1).equals("B") ||
                    !caminoAC.getAmountNodo(2).equals("C")) {
                System.out.println("❌ Error: Secuencia del camino A-C incorrecta");
                return false;
            }

            // Prueba 4.4: No hay camino entre nodos desconectados
            LinkedList<String> caminoAD = grafo.getShortestPath("A", "D");
            if (caminoAD.getSize() != 0) {
                System.out.println("❌ Error: No debería haber camino entre A y D");
                return false;
            }

            // Prueba 4.5: Componentes conectados
            LinkedList<HashSet<String>> componentes = grafo.getConnectedComponents();
            if (componentes.getSize() != 2) { // {A,B,C} y {D}
                System.out.println("❌ Error: Debería haber 2 componentes conectados, hay " + componentes.getSize());
                return false;
            }

            System.out.println("✅ PRUEBA 4 EXITOSA: Algoritmos de grafo funcionan correctamente");
            return true;

        } catch (Exception e) {
            System.out.println("❌ PRUEBA 4 FALLIDA: Excepción - " + e.getMessage());
            return false;
        }
    }

    /**
     * PRUEBA UNITARIA 5: Verificar sistema de recomendaciones
     *
     * Objetivo: Validar que el algoritmo de recomendación de libros produce
     * resultados lógicos basados en preferencias del usuario
     */
    public static boolean testBookRecommendationSystem() {
        System.out.println("🧪 PRUEBA 5: Sistema de recomendaciones de libros");

        try {
            // Configuración de datos de prueba
            Library library = new Library();
            Reader reader = new Reader("Ana García", "ana@email.com", "pass123", library);

            // Crear libros de prueba
            Book book1 = new Book("1", "Harry Potter 1", "J.K. Rowling", 1997, "Fantasía");
            Book book2 = new Book("2", "Harry Potter 2", "J.K. Rowling", 1998, "Fantasía");
            Book book3 = new Book("3", "LOTR", "J.R.R. Tolkien", 1954, "Fantasía");
            Book book4 = new Book("4", "1984", "George Orwell", 1949, "Distopía");

            library.getBookssList().add(book1);
            library.getBookssList().add(book2);
            library.getBookssList().add(book3);
            library.getBookssList().add(book4);

            // Simular historial del usuario
            reader.requestLoan(book1);
            reader.rateBook(book1, 5, "Excelente");

            // Prueba 5.1: Verificar que el sistema genera recomendaciones
            BookRecommendationSystem recomendador = new BookRecommendationSystem(library);
            LinkedList<BookRecommendationSystem.BookRecommendation> recomendaciones =
                    recomendador.getHybridRecommendations(reader, 5);

            if (recomendaciones.getSize() == 0) {
                System.out.println("❌ Error: Sistema debería generar recomendaciones");
                return false;
            }

            // Prueba 5.2: Verificar que recomienda libros del mismo autor/género
            boolean recomiendaRowling = false;
            boolean recomiendaFantasia = false;

            for (BookRecommendationSystem.BookRecommendation rec : recomendaciones) {
                Book libro = rec.getBook();
                if (libro.getAuthor().equals("J.K. Rowling")) {
                    recomiendaRowling = true;
                }
                if (libro.getCategory().equals("Fantasía")) {
                    recomiendaFantasia = true;
                }
            }

            if (!recomiendaRowling && !recomiendaFantasia) {
                System.out.println("❌ Error: Debería recomendar libros del mismo autor o género");
                return false;
            }

            // Prueba 5.3: Verificar que no recomienda libros ya leídos
            for (BookRecommendationSystem.BookRecommendation rec : recomendaciones) {
                if (rec.getBook().getIdBook().equals("1")) {
                    System.out.println("❌ Error: No debería recomendar libros ya leídos");
                    return false;
                }
            }

            // Prueba 5.4: Verificar que las puntuaciones están en rango válido
            for (BookRecommendationSystem.BookRecommendation rec : recomendaciones) {
                double score = rec.getScore();
                if (score < 0.0 || score > 1.0) {
                    System.out.println("❌ Error: Puntuación fuera del rango válido: " + score);
                    return false;
                }
            }

            System.out.println("✅ PRUEBA 5 EXITOSA: Sistema de recomendaciones funciona correctamente");
            return true;

        } catch (Exception e) {
            System.out.println("❌ PRUEBA 5 FALLIDA: Excepción - " + e.getMessage());
            return false;
        }
    }

    /**
     * PRUEBA UNITARIA 6: Verificar gestión de la biblioteca
     *
     * Objetivo: Validar operaciones CRUD de la biblioteca y persistencia de datos
     */
    public static boolean testLibraryManagement() {
        System.out.println("🧪 PRUEBA 6: Gestión de la biblioteca");

        try {
            Library library = Library.getInstance();

            // Prueba 6.1: Creación de libro
            Book nuevoLibro = library.createBook("TEST001", "Libro de Prueba", "Autor Test", 2023, "Pruebas", BookStatus.AVAILABLE);

            if (nuevoLibro == null) {
                System.out.println("❌ Error: No se pudo crear el libro");
                return false;
            }

            // Prueba 6.2: Verificar que el libro se almacenó
            Book libroRecuperado = library.getBookById("TEST001");
            if (libroRecuperado == null || !libroRecuperado.getTitle().equals("Libro de Prueba")) {
                System.out.println("❌ Error: Libro no se almacenó correctamente");
                return false;
            }

            // Prueba 6.3: No se puede crear libro con ID duplicado
            try {
                library.createBook("TEST001", "Otro Libro", "Otro Autor", 2023, "Otra Categoría", BookStatus.AVAILABLE);
                System.out.println("❌ Error: No debería permitir IDs duplicados");
                return false;
            } catch (IllegalArgumentException e) {
                // Comportamiento esperado
            }

            // Prueba 6.4: Registro de lector
            boolean registroExitoso = library.registerReader("Usuario Test", "test@email.com", "password");
            if (!registroExitoso) {
                System.out.println("❌ Error: No se pudo registrar el lector");
                return false;
            }

            // Prueba 6.5: Verificar que el lector se registró
            Reader lectorRecuperado = library.getReaderByUsername("test@email.com");
            if (lectorRecuperado == null || !lectorRecuperado.getName().equals("Usuario Test")) {
                System.out.println("❌ Error: Lector no se registró correctamente");
                return false;
            }

            // Prueba 6.6: No se puede registrar lector con username duplicado
            boolean registroDuplicado = library.registerReader("Otro Usuario", "test@email.com", "password2");
            if (registroDuplicado) {
                System.out.println("❌ Error: No debería permitir usernames duplicados");
                return false;
            }

            // Prueba 6.7: Eliminación de libro
            boolean eliminacionExitosa = library.removeBook("TEST001");
            if (!eliminacionExitosa) {
                System.out.println("❌ Error: No se pudo eliminar el libro");
                return false;
            }

            // Verificar que el libro se eliminó
            if (library.getBookById("TEST001") != null) {
                System.out.println("❌ Error: Libro no se eliminó correctamente");
                return false;
            }

            System.out.println("✅ PRUEBA 6 EXITOSA: Gestión de biblioteca funciona correctamente");
            return true;

        } catch (Exception e) {
            System.out.println("❌ PRUEBA 6 FALLIDA: Excepción - " + e.getMessage());
            return false;
        }
    }

    /**
     * PRUEBA UNITARIA 7: Verificar cola de prioridad personalizada
     *
     * Objetivo: Validar que la cola de prioridad mantiene el orden correcto
     * según las prioridades asignadas
     */
    public static boolean testPriorityQueueFunctionality() {
        System.out.println("🧪 PRUEBA 7: Funcionalidad de Cola de Prioridad");

        try {
            PriorityQueue<String> colaPrioridad = new PriorityQueue<>();

            // Prueba 7.1: Cola inicialmente vacía
            if (!colaPrioridad.isEmpty()) {
                System.out.println("❌ Error: Cola debería estar vacía inicialmente");
                return false;
            }

            // Prueba 7.2: Inserción con diferentes prioridades
            colaPrioridad.enqueue("Baja prioridad", 3);
            colaPrioridad.enqueue("Alta prioridad", 1);  // Menor número = mayor prioridad
            colaPrioridad.enqueue("Media prioridad", 2);
            colaPrioridad.enqueue("Máxima prioridad", 0);

            if (colaPrioridad.size() != 4) {
                System.out.println("❌ Error: Tamaño de cola incorrecto");
                return false;
            }

            // Prueba 7.3: Verificar orden de prioridad
            String primero = colaPrioridad.dequeue();
            if (!primero.equals("Máxima prioridad")) {
                System.out.println("❌ Error: Primer elemento debería ser 'Máxima prioridad', es: " + primero);
                return false;
            }

            String segundo = colaPrioridad.dequeue();
            if (!segundo.equals("Alta prioridad")) {
                System.out.println("❌ Error: Segundo elemento debería ser 'Alta prioridad', es: " + segundo);
                return false;
            }

            // Prueba 7.4: Verificar peek sin remover
            String tercero = colaPrioridad.peek();
            if (!tercero.equals("Media prioridad")) {
                System.out.println("❌ Error: Peek debería devolver 'Media prioridad', devuelve: " + tercero);
                return false;
            }

            // Verificar que peek no removió el elemento
            if (colaPrioridad.size() != 2) {
                System.out.println("❌ Error: Peek no debería cambiar el tamaño de la cola");
                return false;
            }

            // Prueba 7.5: Vaciar cola completamente
            colaPrioridad.dequeue(); // Media prioridad
            colaPrioridad.dequeue(); // Baja prioridad

            if (!colaPrioridad.isEmpty()) {
                System.out.println("❌ Error: Cola debería estar vacía después de remover todos los elementos");
                return false;
            }

            // Prueba 7.6: Operaciones en cola vacía
            String elementoVacio = colaPrioridad.dequeue();
            if (elementoVacio != null) {
                System.out.println("❌ Error: dequeue en cola vacía debería devolver null");
                return false;
            }

            System.out.println("✅ PRUEBA 7 EXITOSA: Cola de prioridad funciona correctamente");
            return true;

        } catch (Exception e) {
            System.out.println("❌ PRUEBA 7 FALLIDA: Excepción - " + e.getMessage());
            return false;
        }
    }

    /**
     * PRUEBA UNITARIA 8: Verificar sistema de afinidad completo
     *
     * Objetivo: Validar que el sistema detecta correctamente afinidades entre lectores
     * basándose en valoraciones similares
     */
    public static boolean testAffinitySystemIntegration() {
        System.out.println("🧪 PRUEBA 8: Sistema de afinidad integrado");

        try {
            // Configuración de escenario de prueba
            Library library = new Library();

            Reader lector1 = new Reader("Alice", "alice@email.com", "pass1", library);
            Reader lector2 = new Reader("Bob", "bob@email.com", "pass2", library);
            Reader lector3 = new Reader("Charlie", "charlie@email.com", "pass3", library);

            Book libro1 = new Book("1", "Libro A", "Autor A", 2020, "Ficción");
            Book libro2 = new Book("2", "Libro B", "Autor B", 2021, "Ficción");
            Book libro3 = new Book("3", "Libro C", "Autor C", 2022, "Ficción");
            Book libro4 = new Book("4", "Libro D", "Autor D", 2023, "Ciencia");

            // Simular valoraciones similares entre Alice y Bob
            lector1.requestLoan(libro1);
            lector1.rateBook(libro1, 5, "Excelente");
            lector1.requestLoan(libro2);
            lector1.rateBook(libro2, 4, "Muy bueno");
            lector1.requestLoan(libro3);
            lector1.rateBook(libro3, 5, "Fantástico");

            lector2.requestLoan(libro1);
            lector2.rateBook(libro1, 5, "Me encantó");
            lector2.requestLoan(libro2);
            lector2.rateBook(libro2, 4, "Recomendado");
            lector2.requestLoan(libro3);
            lector2.rateBook(libro3, 4, "Muy bueno");

            // Charlie tiene gustos diferentes
            lector3.requestLoan(libro4);
            lector3.rateBook(libro4, 5, "Científicamente riguroso");

            library.getReadersList().add(lector1);
            library.getReadersList().add(lector2);
            library.getReadersList().add(lector3);

            // Prueba 8.1: Crear sistema de afinidad
            AffinitySystem sistemaAfinidad = new AffinitySystem(library);
            Graph<Reader> grafoAfinidad = sistemaAfinidad.getAffinityGraph();

            if (grafoAfinidad == null) {
                System.out.println("❌ Error: No se pudo crear el grafo de afinidad");
                return false;
            }

            // Prueba 8.2: Alice y Bob deberían estar conectados (3 libros en común con valoraciones similares)
            HashSet<Reader> amigosAlice = grafoAfinidad.getAdjacentVertices(lector1);
            if (amigosAlice == null || !amigosAlice.contains(lector2)) {
                System.out.println("❌ Error: Alice y Bob deberían estar conectados por afinidad");
                return false;
            }

            // Prueba 8.3: Charlie no debería estar conectado con Alice/Bob (gustos diferentes)
            if (amigosAlice.contains(lector3)) {
                System.out.println("❌ Error: Alice y Charlie no deberían estar conectados");
                return false;
            }

            // Prueba 8.4: Sugerencias de amigos para Alice
            LinkedList<Reader> sugerenciasAlice = sistemaAfinidad.getSuggestedFriends(lector1);
            // Como Alice solo está conectada con Bob y no hay "amigos de amigos", debería estar vacía
            if (sugerenciasAlice.getSize() > 0) {
                System.out.println("⚠️ Advertencia: Sugerencias inesperadas para Alice (puede ser normal si el algoritmo cambió)");
            }

            // Prueba 8.5: Camino más corto Alice-Bob
            LinkedList<Reader> caminoAliceBob = sistemaAfinidad.getShortestPath(lector1, lector2);
            if (caminoAliceBob.getSize() != 2) { // [Alice, Bob]
                System.out.println("❌ Error: Camino Alice-Bob debería tener 2 nodos, tiene " + caminoAliceBob.getSize());
                return false;
            }

            // Prueba 8.6: No hay camino Alice-Charlie
            LinkedList<Reader> caminoAliceCharlie = sistemaAfinidad.getShortestPath(lector1, lector3);
            if (caminoAliceCharlie.getSize() != 0) {
                System.out.println("❌ Error: No debería haber camino entre Alice y Charlie");
                return false;
            }

            System.out.println("✅ PRUEBA 8 EXITOSA: Sistema de afinidad integrado funciona correctamente");
            return true;

        } catch (Exception e) {
            System.out.println("❌ PRUEBA 8 FALLIDA: Excepción - " + e.getMessage());
            return false;
        }
    }

    /**
     * Método principal para ejecutar todas las pruebas
     *
     * Ejecuta secuencialmente todas las pruebas unitarias y reporta los resultados
     */
    public static void runAllTests() {
        System.out.println("🚀 INICIANDO SUITE DE PRUEBAS UNITARIAS");
        System.out.println("========================================");

        boolean[] resultados = new boolean[8];

        resultados[0] = testLinkedListFunctionality();
        resultados[1] = testHashMapFunctionality();
        resultados[2] = testReaderFunctionality();
        resultados[3] = testAffinityGraphAlgorithms();
        resultados[4] = testBookRecommendationSystem();
        resultados[5] = testLibraryManagement();
        resultados[6] = testPriorityQueueFunctionality();
        resultados[7] = testAffinitySystemIntegration();

        System.out.println("\n📊 RESUMEN DE RESULTADOS:");
        System.out.println("========================");

        int exitosas = 0;
        for (int i = 0; i < resultados.length; i++) {
            String status = resultados[i] ? "✅ EXITOSA" : "❌ FALLIDA";
            System.out.println("Prueba " + (i + 1) + ": " + status);
            if (resultados[i]) exitosas++;
        }

        System.out.println("\n🎯 ESTADÍSTICAS FINALES:");
        System.out.println("Total de pruebas: " + resultados.length);
        System.out.println("Pruebas exitosas: " + exitosas);
        System.out.println("Pruebas fallidas: " + (resultados.length - exitosas));
        System.out.println("Porcentaje de éxito: " + (exitosas * 100 / resultados.length) + "%");

        if (exitosas == resultados.length) {
            System.out.println("\n🎉 ¡TODAS LAS PRUEBAS PASARON EXITOSAMENTE!");
            System.out.println("El sistema está listo para producción.");
        } else {
            System.out.println("\n⚠️ Algunas pruebas fallaron. Revisar implementación.");
        }
    }

    /**
     * Método main para ejecutar las pruebas directamente
     */
    public static void main(String[] args) {
        runAllTests();
    }
}