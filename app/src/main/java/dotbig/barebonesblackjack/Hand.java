package dotbig.barebonesblackjack;

public interface Hand extends Countable,
                              Valuable,
                              Stringable {
    void add(BlackjackCard card);
    BlackjackCard getCard(int index);
    void bust();
    boolean busted();
    boolean natural();
}
