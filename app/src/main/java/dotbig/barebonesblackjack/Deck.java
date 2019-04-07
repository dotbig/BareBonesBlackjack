package dotbig.barebonesblackjack;

import java.util.List;

public interface Deck<T extends Card> {
    List<T> getDeck();
    int count();
}
