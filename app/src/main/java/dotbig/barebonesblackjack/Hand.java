package dotbig.barebonesblackjack;

interface Hand {
    void hit(Card draw);
    void hit(Card draw, boolean faceUp);
    int value();
    boolean softSeventeen();
    void clear();
    String toString();
}
