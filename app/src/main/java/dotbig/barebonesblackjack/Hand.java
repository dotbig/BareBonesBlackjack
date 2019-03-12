package dotbig.barebonesblackjack;

interface Hand {
    void hit(Card draw);
    void hit(Card draw, boolean faceUp);
    Card getCard(int index);
    int getBet();
    void increaseBet(int amount);
    int count();
    int value();
    boolean natural();
    boolean softSeventeen();
    void clear();
    String toString();
}
