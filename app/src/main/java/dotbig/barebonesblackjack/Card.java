package dotbig.barebonesblackjack;

interface Card {
    String getSuit();
    String getRank();
    int getValue();
    boolean isAce();
    boolean isFaceUp();
    void flip(boolean faceUp);
    String toString();
}
