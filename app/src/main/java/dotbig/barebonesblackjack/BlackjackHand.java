package dotbig.barebonesblackjack;

public interface BlackjackHand {
    void add(BlackjackCard card);
    BlackjackCard getCard(int index);
    int value();
    void bust();
    boolean busted();
    boolean natural();
    int count();
    String toString();
}
