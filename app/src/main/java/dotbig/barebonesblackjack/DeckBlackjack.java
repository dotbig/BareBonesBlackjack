package dotbig.barebonesblackjack;

import java.util.ArrayList;
import java.util.List;

public class DeckBlackjack implements Deck {
    private List<BlackjackCard> cards = new ArrayList<>();

    DeckBlackjack(){
        populateDeck();
    }

    public List<BlackjackCard> getDeck(){
        return cards;
    }

    private void populateDeck(){
        for (int s=0; s<4; s++){
            for (int r=0; r<13; r++){
                BlackjackCard c = new CardBlackjack(s, r);
                cards.add(c);
            }
        }
    }

    public int count(){
        return cards.size();
    }
}
