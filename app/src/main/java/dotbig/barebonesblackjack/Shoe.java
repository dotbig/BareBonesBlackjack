package dotbig.barebonesblackjack;

interface Shoe {
    Card draw();
    Card draw(boolean faceUp);
    void addDeck(Deck deck);
    void shuffle();
    void setPenetration(int pen);
    boolean penetrationCheck();
}
