package dotbig.barebonesblackjack;

public interface Card extends Stringable {
    String getSuit();
    String getRank();
    boolean isFaceUp();
    void flip(boolean faceUp);
}
