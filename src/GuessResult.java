/**
 * Resultado de un intento (guess) en el juego.
 */
public class GuessResult {

    public enum Direction {
        ABOVE,  // La palabra secreta está DESPUÉS del intento (subir en la UI)
        BELOW,  // La palabra secreta está ANTES del intento (bajar en la UI)
        FOUND   // ¡Palabra encontrada!
    }

    private String guessedWord;
    private int attemptNumber;
    private Direction direction;
    private double distanceToTop;    // % de distancia desde tope superior
    private double distanceToBottom; // % de distancia desde tope inferior
    private boolean closerToTop;     // El punto naranja indica si está más cerca del tope

    public GuessResult(String guessedWord, int attemptNumber, Direction direction,
                       double distanceToTop, double distanceToBottom, boolean closerToTop) {
        this.guessedWord = guessedWord;
        this.attemptNumber = attemptNumber;
        this.direction = direction;
        this.distanceToTop = distanceToTop;
        this.distanceToBottom = distanceToBottom;
        this.closerToTop = closerToTop;
    }

    public String getGuessedWord() { return guessedWord; }
    public int getAttemptNumber() { return attemptNumber; }
    public Direction getDirection() { return direction; }
    public double getDistanceToTop() { return distanceToTop; }
    public double getDistanceToBottom() { return distanceToBottom; }
    public boolean isCloserToTop() { return closerToTop; }
    public boolean isFound() { return direction == Direction.FOUND; }
}
