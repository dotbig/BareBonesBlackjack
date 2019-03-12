package dotbig.barebonesblackjack;

interface Hand {
    void hit(Card draw);
    void hit(Card draw, boolean faceUp);
<<<<<<< HEAD
    Card getCard(int index);
    int getBet();
    void increaseBet(int amount);
=======
    Card get(int index);
>>>>>>> bbc35aa727ec622c11115a9e362a92c0a1463894
    int count();
    int value();
    boolean natural();
    boolean softSeventeen();
    void clear();
    String toString();
}
