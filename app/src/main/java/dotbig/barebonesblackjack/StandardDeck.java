package dotbig.barebonesblackjack;

import java.util.ArrayList;
import java.util.List;

class StandardDeck implements Deck {
    List<Card> cards;

    StandardDeck(){
        cards = new ArrayList<>();
        populateDeck();
    }

    public List<Card> getDeck(){
        return cards;
    }

    private void populateDeck(){
        for (int s=0; s<4; s++){
            for (int r=0; r<13; r++){
                Card c = new PlayingCard(s, r);
                cards.add(c);
            }
        }

    }

}
