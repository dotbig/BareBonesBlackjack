package dotbig.barebonesblackjack;

public interface BlackjackShoe {
    BlackjackCard draw();
    BlackjackCard draw(boolean faceUp);
    void addDeck(Deck<BlackjackCard> deck);
    int count();
    void shuffle();
    void setPenetration(int pen);
    boolean penetrationCheck();
}
