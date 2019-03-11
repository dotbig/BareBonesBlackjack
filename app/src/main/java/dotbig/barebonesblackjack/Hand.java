package dotbig.barebonesblackjack;

interface Hand {
    Card hit();
    Card hit(boolean faceUp);
    int value();
    boolean softSeventeen();
    void clear();
    String toString();
}
