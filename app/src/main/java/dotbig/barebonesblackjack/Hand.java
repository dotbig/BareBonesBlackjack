package dotbig.barebonesblackjack;

interface Hand {
    void hit(Card draw);
    Card getCard(int index);
    int getBet();
    void increaseBet(int amount);
    int count();
    int value();
    void bust();
    boolean busted();
    Card split();
    boolean isSplit();
    boolean natural();
    boolean softSeventeen();
    void clear();
    String toString();
}
