package dotbig.barebonesblackjack;

public interface Card {
    String getSuit();
    String getRank();
    boolean isFaceUp();
    void flip(boolean faceUp);
    String toString();
}
