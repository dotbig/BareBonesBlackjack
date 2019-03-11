package dotbig.barebonesblackjack;

interface Card {
    boolean isFaceUp();
    boolean flip(boolean face);
    String getSuit();
    String getRank();
    String toString();
}
