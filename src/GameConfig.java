/**
 * Configuración del juego Betweenle.
 * Encapsula dificultad, intentos e idioma.
 */
public class GameConfig {

    public enum Difficulty {
        EASY(5, "Fácil"),
        MEDIUM(6, "Intermedio"),
        HARD(7, "Difícil");

        private final int wordLength;
        private final String label;

        Difficulty(int wordLength, String label) {
            this.wordLength = wordLength;
            this.label = label;
        }

        public int getWordLength() { return wordLength; }
        public String getLabel() { return label; }
    }

    public enum Language {
        ENGLISH("en", "English"),
        SPANISH("es", "Español");

        private final String code;
        private final String label;

        Language(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() { return code; }
        public String getLabel() { return label; }
    }

    private Difficulty difficulty;
    private int maxAttempts;
    private Language language;

    public GameConfig(Difficulty difficulty, int maxAttempts, Language language) {
        this.difficulty = difficulty;
        this.maxAttempts = maxAttempts;
        this.language = language;
    }

    public Difficulty getDifficulty() { return difficulty; }
    public int getMaxAttempts() { return maxAttempts; }
    public Language getLanguage() { return language; }
    public int getWordLength() { return difficulty.getWordLength(); }

    @Override
    public String toString() {
        return String.format("Dificultad: %s (%d letras) | Intentos: %d | Idioma: %s",
                difficulty.getLabel(), difficulty.getWordLength(), maxAttempts, language.getLabel());
    }
}
