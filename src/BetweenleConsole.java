import java.util.*;

/**
 * ============================================================
 * BetweenleConsole - Interfaz de usuario en consola
 * ============================================================
 * 
 * Esta clase ÚNICAMENTE maneja la interfaz de usuario.
 * Toda la lógica del juego se delega a BetweenleGame (la API).
 * 
 * Separación de responsabilidades:
 *   - BetweenleGame  → lógica del juego (API pura)
 *   - BetweenleConsole → entrada/salida con el usuario
 */
public class BetweenleConsole {

    private BetweenleGame game;
    private Scanner scanner;

    // Colores ANSI para la consola
    private static final String RESET  = "\u001B[0m";
    private static final String BLUE   = "\u001B[34m";
    private static final String ORANGE = "\u001B[33m";
    private static final String GREEN  = "\u001B[32m";
    private static final String RED    = "\u001B[31m";
    private static final String BOLD   = "\u001B[1m";
    private static final String CYAN   = "\u001B[36m";
    private static final String GRAY   = "\u001B[90m";

    public BetweenleConsole() {
        this.game = new BetweenleGame();
        this.scanner = new Scanner(System.in);
    }

    // =========================================================
    // Flujo principal
    // =========================================================

    public void start() {
        printWelcome();

        boolean playAgain = true;
        while (playAgain) {
            GameConfig config = askGameConfig();
            game.initialize(config);
            playRound();
            playAgain = askPlayAgain();
        }

        System.out.println("\n" + CYAN + "¡Gracias por jugar Betweenle! 👋" + RESET);
    }

    // =========================================================
    // Configuración inicial
    // =========================================================

    private GameConfig askGameConfig() {
        System.out.println("\n" + BOLD + "═══════════════════════════════════════" + RESET);
        System.out.println(BOLD + "          CONFIGURAR NUEVA PARTIDA" + RESET);
        System.out.println(BOLD + "═══════════════════════════════════════" + RESET);

        // Idioma
        GameConfig.Language language = askLanguage();

        // Dificultad
        GameConfig.Difficulty difficulty = askDifficulty(language);

        // Intentos
        int attempts = askAttempts(language);

        GameConfig config = new GameConfig(difficulty, attempts, language);
        System.out.println("\n" + GREEN + "✓ " + config + RESET);
        return config;
    }

    private GameConfig.Language askLanguage() {
        System.out.println("\n" + BOLD + "Selecciona el idioma / Select language:" + RESET);
        System.out.println("  1. Español");
        System.out.println("  2. English");
        System.out.print("→ ");
        int opt = readInt(1, 2);
        return opt == 1 ? GameConfig.Language.SPANISH : GameConfig.Language.ENGLISH;
    }

    private GameConfig.Difficulty askDifficulty(GameConfig.Language lang) {
        boolean es = lang == GameConfig.Language.SPANISH;
        System.out.println("\n" + BOLD + (es ? "Selecciona la dificultad:" : "Select difficulty:") + RESET);
        System.out.println("  1. " + (es ? "Fácil      " : "Easy       ") + GRAY + "(5 " + (es ? "letras" : "letters") + ")" + RESET);
        System.out.println("  2. " + (es ? "Intermedio " : "Medium     ") + GRAY + "(6 " + (es ? "letras" : "letters") + ")" + RESET);
        System.out.println("  3. " + (es ? "Difícil    " : "Hard       ") + GRAY + "(7 " + (es ? "letras" : "letters") + ")" + RESET);
        System.out.print("→ ");
        int opt = readInt(1, 3);
        return switch (opt) {
            case 1 -> GameConfig.Difficulty.EASY;
            case 2 -> GameConfig.Difficulty.MEDIUM;
            default -> GameConfig.Difficulty.HARD;
        };
    }

    private int askAttempts(GameConfig.Language lang) {
        boolean es = lang == GameConfig.Language.SPANISH;
        System.out.println("\n" + BOLD + (es ? "Número de intentos:" : "Number of attempts:") + RESET);
        System.out.println("  1. 10");
        System.out.println("  2. 12");
        System.out.println("  3. 14");
        System.out.print("→ ");
        int opt = readInt(1, 3);
        return switch (opt) {
            case 1 -> 10;
            case 2 -> 12;
            default -> 14;
        };
    }

    // =========================================================
    // Ronda de juego
    // =========================================================

    private void playRound() {
        boolean es = game.getConfig().getLanguage() == GameConfig.Language.SPANISH;
        System.out.println("\n" + BOLD + "═══════════════════════════════════════" + RESET);
        System.out.println(BOLD + "               ¡A JUGAR!" + RESET);
        System.out.println(BOLD + "═══════════════════════════════════════" + RESET);
        System.out.println(GRAY + "Diccionario: " + game.getDictionarySize() + " palabras" + RESET);
        System.out.println();

        while (!game.isGameOver()) {
            printGameBoard();
            String input = askInput();

            if (input.equalsIgnoreCase("PISTA") || input.equalsIgnoreCase("HINT")) {
                handleHint(es);
                continue;
            }

            // Validar longitud
            if (input.length() != game.getConfig().getWordLength()) {
                System.out.println(RED + "✗ La palabra debe tener " + game.getConfig().getWordLength() + " letras." + RESET);
                continue;
            }

            GuessResult result;
            try {
                result = game.guess(input);
            } catch (IllegalArgumentException e) {
                System.out.println(RED + "✗ " + e.getMessage() + RESET);
                continue;
            }

            // Palabra no en diccionario
            if (result == null) {
                System.out.println(ORANGE + "⚠ La palabra '" + input + "' no está en el diccionario." + RESET);
                System.out.print(es ? "¿Deseas agregarla con su definición? (s/n): " : "Add it with a definition? (y/n): ");
                String resp = scanner.nextLine().trim().toLowerCase();
                if (resp.equals("s") || resp.equals("y")) {
                    System.out.print(es ? "Escribe la definición: " : "Enter definition: ");
                    String def = scanner.nextLine().trim();
                    game.addNewWord(input, def);
                    System.out.println(GREEN + "✓ Palabra agregada al diccionario." + RESET);
                    // Reintentar con la palabra recién agregada
                    result = game.guess(input);
                    if (result == null) continue;
                } else {
                    continue;
                }
            }

            printGuessResult(result);

            if (result.isFound()) {
                printWin();
                return;
            }

            if (game.isGameOver()) {
                printLose();
                return;
            }
        }
    }

    // =========================================================
    // Visualización del tablero
    // =========================================================

    private void printGameBoard() {
        boolean es = game.getConfig().getLanguage() == GameConfig.Language.SPANISH;
        int wordLen = game.getConfig().getWordLength();

        System.out.println();
        System.out.println(BOLD + "  Intento " + (game.getCurrentAttempt() + 1) + "/" + game.getMaxAttempts() + RESET +
                GRAY + "   Rango actual:" + RESET);

        // Tope superior
        printWordRow(game.getTopWord().toUpperCase(), BLUE, "▲");

        // Historial de intentos (usando Iterator)
        Iterator<GuessResult> it = game.getGuessHistoryIterator();
        while (it.hasNext()) {
            GuessResult gr = it.next();
            String color = gr.isFound() ? GREEN : ORANGE;
            String distInfo = String.format("(%.1f%% ↑  %.1f%% ↓)",
                    gr.getDistanceToTop(), gr.getDistanceToBottom());
            System.out.println("  " + color + BOLD + formatWord(gr.getGuessedWord().toUpperCase(), wordLen) + RESET +
                    " " + GRAY + "#" + gr.getAttemptNumber() + " " + distInfo + RESET);
        }

        // Si no hay intentos aún, mostrar fila vacía
        if (game.getCurrentAttempt() == 0) {
            System.out.println("  " + GRAY + formatWord("?????", wordLen) + RESET + " ← " + (es ? "tu próxima palabra" : "your next word"));
        }

        // Tope inferior
        printWordRow(game.getBottomWord().toUpperCase(), BLUE, "▼");

        // Mostrar letras usadas
        printUsedLetters();

        System.out.println();
        System.out.println(GRAY + "  Comandos: " + (es ? "PISTA" : "HINT") + " para pedir una pista" + RESET);
    }

    private void printWordRow(String word, String color, String arrow) {
        int wl = game.getConfig().getWordLength();
        System.out.println("  " + color + BOLD + arrow + " " + formatWord(word, wl) + RESET);
    }

    private String formatWord(String word, int len) {
        StringBuilder sb = new StringBuilder();
        for (char c : word.toCharArray()) {
            sb.append("[").append(c).append("]");
        }
        return sb.toString();
    }

    private void printUsedLetters() {
        Set<Character> used = game.getUsedLetters();
        if (used.isEmpty()) return;

        // LAMBDA: ordenar letras usando stream con lambda
        String letters = used.stream()
                .sorted()
                .map(String::valueOf)
                .reduce("", (a, b) -> a + b.toUpperCase() + " ");

        System.out.println(GRAY + "  Letras usadas: " + letters.trim() + RESET);
    }

    // =========================================================
    // Mostrar resultado de intento
    // =========================================================

    private void printGuessResult(GuessResult result) {
        System.out.println();
        if (result.isFound()) return; // win se imprime aparte

        String arrow = result.getDirection() == GuessResult.Direction.ABOVE ? "⬆ ARRIBA" : "⬇ ABAJO";
        String msg = result.getDirection() == GuessResult.Direction.ABOVE
                ? "La palabra secreta está DESPUÉS (más abajo en el diccionario)"
                : "La palabra secreta está ANTES (más arriba en el diccionario)";

        System.out.println(ORANGE + "  " + arrow + " — " + msg + RESET);
        System.out.printf(CYAN + "  Distancia: %.1f%% desde arriba  |  %.1f%% desde abajo%n" + RESET,
                result.getDistanceToTop(), result.getDistanceToBottom());

        String dotPos = result.isCloserToTop() ? "● más cerca del TOPE SUPERIOR" : "● más cerca del TOPE INFERIOR";
        System.out.println(ORANGE + "  " + dotPos + RESET);
    }

    // =========================================================
    // Sistema de pistas
    // =========================================================

    private void handleHint(boolean es) {
        System.out.println("\n" + BOLD + (es ? "  Elige una pista:" : "  Choose a hint:") + RESET);
        System.out.println("  a) " + (es ? "Mover 1% el límite superior hacia la palabra secreta" : "Move top limit 1% closer to secret word"));
        System.out.println("  b) " + (es ? "Mover 1% el límite inferior hacia la palabra secreta" : "Move bottom limit 1% closer to secret word"));
        System.out.println("  c) " + (es ? "Revelar la primera letra de la palabra secreta" : "Reveal first letter of secret word"));
        System.out.print("→ ");

        String opt = scanner.nextLine().trim().toLowerCase();
        switch (opt) {
            case "a" -> {
                String newTop = game.hintMoveTopDown();
                System.out.println(GREEN + "  Pista: el nuevo límite superior es → " + newTop.toUpperCase() + RESET);
            }
            case "b" -> {
                String newBottom = game.hintMoveBottomUp();
                System.out.println(GREEN + "  Pista: el nuevo límite inferior es → " + newBottom.toUpperCase() + RESET);
            }
            case "c" -> {
                char first = game.hintFirstLetter();
                System.out.println(GREEN + "  Pista: la palabra empieza con → " + BOLD + Character.toUpperCase(first) + RESET);
            }
            default -> System.out.println(RED + "  Opción no válida." + RESET);
        }
    }

    // =========================================================
    // Input del jugador
    // =========================================================

    private String askInput() {
        boolean es = game.getConfig().getLanguage() == GameConfig.Language.SPANISH;
        System.out.print(BOLD + (es ? "  Tu palabra: " : "  Your word: ") + RESET);
        return scanner.nextLine().trim().toLowerCase();
    }

    // =========================================================
    // Mensajes finales
    // =========================================================

    private void printWin() {
        System.out.println();
        System.out.println(GREEN + BOLD + "  ╔══════════════════════════════╗" + RESET);
        System.out.println(GREEN + BOLD + "  ║   🎉 ¡GANASTE! ¡CORRECTO!   ║" + RESET);
        System.out.println(GREEN + BOLD + "  ╚══════════════════════════════╝" + RESET);
        System.out.println(GREEN + "  La palabra era: " + BOLD + game.getSecretWord().toUpperCase() + RESET);
        System.out.println(GREEN + "  Intentos usados: " + game.getCurrentAttempt() + "/" + game.getMaxAttempts() + RESET);
    }

    private void printLose() {
        System.out.println();
        System.out.println(RED + BOLD + "  ╔══════════════════════════════╗" + RESET);
        System.out.println(RED + BOLD + "  ║   😔 ¡Sin más intentos!     ║" + RESET);
        System.out.println(RED + BOLD + "  ╚══════════════════════════════╝" + RESET);
        System.out.println(RED + "  La palabra secreta era: " + BOLD + game.getSecretWord().toUpperCase() + RESET);
    }

    private void printWelcome() {
        System.out.println(BLUE + BOLD);
        System.out.println("  ██████╗ ███████╗████████╗██╗    ██╗███████╗███████╗███╗  ██╗██╗     ███████╗");
        System.out.println("  ██╔══██╗██╔════╝╚══██╔══╝██║    ██║██╔════╝██╔════╝████╗ ██║██║     ██╔════╝");
        System.out.println("  ██████╔╝█████╗     ██║   ██║ █╗ ██║█████╗  █████╗  ██╔██╗██║██║     █████╗  ");
        System.out.println("  ██╔══██╗██╔══╝     ██║   ██║███╗██║██╔══╝  ██╔══╝  ██║╚████║██║     ██╔══╝  ");
        System.out.println("  ██████╔╝███████╗   ██║   ╚███╔███╔╝███████╗███████╗██║ ╚███║███████╗███████╗");
        System.out.println("  ╚═════╝ ╚══════╝   ╚═╝    ╚══╝╚══╝ ╚══════╝╚══════╝╚═╝  ╚══╝╚══════╝╚══════╝");
        System.out.println(RESET);
        System.out.println(CYAN + "  Adivina la palabra secreta oculta entre otras palabras del diccionario." + RESET);
        System.out.println(GRAY + "  Cada intento te indica si la secreta está ANTES o DESPUÉS en el diccionario." + RESET);
        System.out.println();
    }

    private boolean askPlayAgain() {
        boolean es = game.getConfig().getLanguage() == GameConfig.Language.SPANISH;
        System.out.print("\n" + BOLD + (es ? "  ¿Jugar de nuevo? (s/n): " : "  Play again? (y/n): ") + RESET);
        String resp = scanner.nextLine().trim().toLowerCase();
        return resp.equals("s") || resp.equals("y");
    }

    // =========================================================
    // Utilidades de lectura
    // =========================================================

    private int readInt(int min, int max) {
        while (true) {
            try {
                String line = scanner.nextLine().trim();
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) return val;
                System.out.print("  Elige entre " + min + " y " + max + ": ");
            } catch (NumberFormatException e) {
                System.out.print("  Ingresa un número (" + min + "-" + max + "): ");
            }
        }
    }
}
