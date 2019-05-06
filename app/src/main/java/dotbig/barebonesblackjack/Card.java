package dotbig.barebonesblackjack;

public interface Card extends Stringable {
    Suit getSuit();
    Rank getRank();
    boolean isFaceUp();
    void flip(boolean faceUp);
}
