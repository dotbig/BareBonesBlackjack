package dotbig.barebonesblackjack;

interface Hand {
    void hit(Card draw);
    void hit(Card draw, boolean faceUp);
    Card get(int index);
    int count();
    int value();
    boolean natural();
    boolean softSeventeen();
    void clear();
    String toString();
}
