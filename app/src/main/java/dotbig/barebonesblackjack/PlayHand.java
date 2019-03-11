package dotbig.barebonesblackjack;

import java.util.List;
import java.util.ArrayList;

class PlayHand implements Hand {
    List<Card> cards;
    int bet;

    PlayHand(){
        cards = new ArrayList<>();
    }

    PlayHand(int stake){
        cards = new ArrayList<>();
        bet = stake;
    }

    public void hit(Card draw){
        cards.add(draw);
    }

    public void hit(Card draw, boolean faceUp){
        Card toAdd = draw;
        if (toAdd.isFaceUp() != faceUp){
            toAdd.flip(faceUp);
        }
        cards.add(draw);
    }

    public int value(){
        return -1;
    }

    public boolean softSeventeen(){
        return false;
    }

    public void clear(){
        cards.clear();
    }

    public String toString(){
        return "";
    }
}
