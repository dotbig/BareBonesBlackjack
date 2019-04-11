package dotbig.barebonesblackjack;

public interface Shoe extends Countable {

    BlackjackCard draw();
    BlackjackCard draw(boolean faceUp);
    void addDeck(Deck<BlackjackCard> deck);
    void shuffle();
    void setPenetration(int pen);
    boolean penetrationCheck();
}
