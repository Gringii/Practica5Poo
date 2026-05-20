import java.util.*;

/**
 * ============================================================
 * BetweenleGame - API del juego (sin interfaz de usuario)
 * ============================================================
 * 
 * Esta clase provee únicamente la API del juego Betweenle.
 * NO contiene ninguna lógica de interfaz de usuario (UI).
 * La UI está completamente separada en BetweenleConsole.java
 * 
 * USO DE HashSet: Se utiliza para llevar control de las letras
 * usadas en cada ronda, con acceso O(1) para agregar/consultar.
 * 
 * USO DE HashMap: El Dictionary interno usa HashMap<String, WordMetadata>
 * para almacenar y consultar palabras eficientemente.
 * 
 * USO DE Iterator: Usado en varios métodos para recorrer colecciones.
 * 
 * LAMBDAS: Usadas en sort, stream y filter de colecciones.
 */
public class BetweenleGame {

    // =========================================================
    // Estado del juego
    // =========================================================
    private Dictionary dictionary;
    private GameConfig config;
    private String secretWord;
    private String topWord;       // Límite superior actual
    private String bottomWord;    // Límite inferior actual
    private int currentAttempt;
    private List<GuessResult> guessHistory;
    private boolean gameOver;
    private boolean playerWon;

    // USO DE HashSet: control de letras usadas en esta ronda
    // Se eligió HashSet porque evita duplicados automáticamente
    // y permite verificar existencia en O(1)
    private HashSet<Character> usedLetters;

    // =========================================================
    // Constructor y configuración
    // =========================================================

    public BetweenleGame() {
        this.guessHistory = new ArrayList<>();
        this.usedLetters = new HashSet<>();
    }

    /**
     * Inicializa el juego con la configuración dada.
     * Carga el diccionario, selecciona la palabra secreta
     * y establece los límites inicial (AAAAA / ZZZZZ).
     */
    public void initialize(GameConfig config) {
        this.config = config;
        this.dictionary = new Dictionary(config.getLanguage().getCode());
        this.dictionary.loadWords(config.getWordLength());

        // Seleccionar palabra secreta aleatoria
        this.secretWord = dictionary.getRandomWord();
        if (secretWord == null) {
            throw new IllegalStateException("No se pudo obtener una palabra secreta del diccionario.");
        }

        // Inicializar límites
        this.topWord = "a".repeat(config.getWordLength());
        this.bottomWord = "z".repeat(config.getWordLength());

        // Reset estado
        this.currentAttempt = 0;
        this.gameOver = false;
        this.playerWon = false;
        this.guessHistory.clear();
        this.usedLetters.clear();
    }

    // =========================================================
    // Método principal: procesar intento del jugador
    // =========================================================

    /**
     * Procesa la palabra ingresada por el jugador.
     * 
     * @param word Palabra ingresada
     * @return GuessResult con el resultado del intento, o null si la palabra no es válida
     * @throws IllegalArgumentException si la palabra tiene longitud incorrecta
     * @throws IllegalStateException si el juego ya terminó
     */
    public GuessResult guess(String word) {
        if (gameOver) {
            throw new IllegalStateException("El juego ya terminó.");
        }

        word = word.toLowerCase().trim();

        // Validar longitud
        if (word.length() != config.getWordLength()) {
            throw new IllegalArgumentException("La palabra debe tener " + config.getWordLength() + " letras.");
        }

        // Validar que exista en el diccionario
        if (!dictionary.contains(word)) {
            return null; // Palabra no válida, el caller puede pedir definición
        }

        currentAttempt++;

        // Registrar letras usadas en HashSet
        // LAMBDA #2: forEach con lambda para agregar cada letra al HashSet
        word.chars().forEach(c -> usedLetters.add((char) c));

        // Determinar dirección
        GuessResult.Direction direction;
        if (word.equals(secretWord)) {
            direction = GuessResult.Direction.FOUND;
            gameOver = true;
            playerWon = true;
        } else {
            int cmp = word.compareTo(secretWord);
            if (cmp < 0) {
                // La palabra ingresada es MENOR que la secreta → secreta está DESPUÉS
                direction = GuessResult.Direction.ABOVE;
                // Actualizar límite superior
                if (word.compareTo(topWord) > 0) {
                    topWord = word;
                }
            } else {
                // La palabra ingresada es MAYOR que la secreta → secreta está ANTES
                direction = GuessResult.Direction.BELOW;
                // Actualizar límite inferior
                if (word.compareTo(bottomWord) < 0) {
                    bottomWord = word;
                }
            }
        }

        // Calcular distancias porcentuales
        double distToTop    = dictionary.getDistancePercent(topWord, secretWord);
        double distToBottom = dictionary.getDistancePercent(secretWord, bottomWord);

        // Si no se pudo calcular (palabra fuera del dict ordenado), usar estimación
        if (distToTop < 0) distToTop = 0;
        if (distToBottom < 0) distToBottom = 0;

        boolean closerToTop = distToTop <= distToBottom;

        GuessResult result = new GuessResult(word, currentAttempt, direction,
                distToTop, distToBottom, closerToTop);
        guessHistory.add(result);

        // Verificar si se agotaron intentos
        if (currentAttempt >= config.getMaxAttempts() && !playerWon) {
            gameOver = true;
        }

        return result;
    }

    // =========================================================
    // Sistema de pistas
    // =========================================================

    /**
     * Pista A: Mover el límite superior 1% hacia la palabra secreta.
     * @return nueva palabra límite superior
     */
    public String hintMoveTopDown() {
        topWord = dictionary.getWordBelowByPercent(topWord, 1.0);
        if (topWord == null) topWord = "a".repeat(config.getWordLength());
        return topWord;
    }

    /**
     * Pista B: Mover el límite inferior 1% hacia la palabra secreta.
     * @return nueva palabra límite inferior
     */
    public String hintMoveBottomUp() {
        bottomWord = dictionary.getWordAboveByPercent(bottomWord, 1.0);
        if (bottomWord == null) bottomWord = "z".repeat(config.getWordLength());
        return bottomWord;
    }

    /**
     * Pista C: Revelar la primera letra de la palabra secreta.
     * @return primera letra
     */
    public char hintFirstLetter() {
        return dictionary.getFirstLetter(secretWord);
    }

    // =========================================================
    // Manejo de palabra nueva (no en diccionario)
    // =========================================================

    /**
     * Agrega una palabra nueva al diccionario con su definición.
     * El jugador puede demostrar que es una palabra válida.
     */
    public void addNewWord(String word, String definition) {
        dictionary.addWord(word, definition);
    }

    /**
     * Verifica si una palabra está en el diccionario.
     */
    public boolean isValidWord(String word) {
        return dictionary.contains(word.toLowerCase().trim());
    }

    // =========================================================
    // Getters del estado del juego
    // =========================================================

    public String getTopWord()         { return topWord; }
    public String getBottomWord()      { return bottomWord; }
    public int getCurrentAttempt()     { return currentAttempt; }
    public int getMaxAttempts()        { return config.getMaxAttempts(); }
    public boolean isGameOver()        { return gameOver; }
    public boolean isPlayerWon()       { return playerWon; }
    public String getSecretWord()      { return secretWord; }
    public GameConfig getConfig()      { return config; }
    public List<GuessResult> getGuessHistory() { return Collections.unmodifiableList(guessHistory); }

    /**
     * Retorna el conjunto de letras usadas (HashSet).
     * USO DE HashSet: retornamos copia no modificable.
     */
    public Set<Character> getUsedLetters() {
        return Collections.unmodifiableSet(usedLetters);
    }

    /**
     * USO DE Iterator: itera sobre el historial de intentos.
     * Útil para mostrar el historial sin exponer la lista directamente.
     */
    public Iterator<GuessResult> getGuessHistoryIterator() {
        return guessHistory.iterator();
    }

    public int getRemainingAttempts() {
        return config.getMaxAttempts() - currentAttempt;
    }

    public int getDictionarySize() {
        return dictionary.size();
    }
}
