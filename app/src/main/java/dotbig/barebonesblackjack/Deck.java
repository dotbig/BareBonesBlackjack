package dotbig.barebonesblackjack;

import java.util.List;

public interface Deck<T extends Card> extends Countable {
    List<T> getDeck();
}
