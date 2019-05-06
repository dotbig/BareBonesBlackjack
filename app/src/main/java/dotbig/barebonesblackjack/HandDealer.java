package dotbig.barebonesblackjack;

public class HandDealer extends HandBase implements DealerSpecific {

    //indexes of the hole card and up card
    private final int UP = 1;
    private final int HOLE = 0;

    HandDealer(){

    }

    //21 on the first two cards is a blackjack
    public boolean natural(){
        return (count() == 2 && value() == 21);
    }

    public BlackjackCard getUpCard(){
        return getCard(UP);
    }

    public BlackjackCard getHoleCard(){
        return getCard(HOLE);
    }

    public void reveal(){
        getHoleCard().flip(true);
    }

    /*
    a soft seventeen is any hand that adds up to 17 while counting an ace as 11-valued
    only need to know if, excluding an 11-counted ace, the rest of the hand adds to 6

    with just one ace, when the rest of the hand's value adds up to 6 it's a soft seventeen

    with multiple aces, one will be counted as 11-valued; the rest 1-valued
        valueHard() counts all aces as 1-valued, so subtract one to account for an 11-valued ace
        if the result is 6, it's a soft seventeen
     */
    public boolean softSeventeen(){
        int aces = numberOfAces();
        if (aces == 1){
            return valueWithoutAces() == 6;
        } else if (aces > 1) {
            return (valueHard() - 1) == 6;
        }
        return false;
    }

    //value of the hand counting all aces as 1-valued
    private int valueHard(){
        return numberOfAces() + valueWithoutAces();
    }
}
