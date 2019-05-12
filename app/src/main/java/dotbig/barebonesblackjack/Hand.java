package dotbig.barebonesblackjack;

import java.util.List;

public interface Hand extends Countable,
                              Valuable,
                              Stringable {
    void add(BlackjackCard card);
    BlackjackCard getCard(int index);
    List<BlackjackCard> getCards();
    void bust();
    boolean busted();
    boolean natural();
}
