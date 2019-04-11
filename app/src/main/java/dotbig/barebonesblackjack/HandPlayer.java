package dotbig.barebonesblackjack;

public class HandPlayer extends HandBase implements PlayerSpecific {
    private int bet;
    private boolean stay;
    private boolean split;

    HandPlayer(){

    }

    HandPlayer(int stake){
        bet = stake;
    }

    //the only time a new hand is created with an existing card is during a split
    HandPlayer(int stake, BlackjackCard card){
        split = true;
        bet = stake;
        cards.add(card);
    }

    //first two cards adding to 21 is a blackjack; split hands don't count
    public boolean natural() {
        return (count() == 2 && value() == 21 && !split);
    }

    public int getBet(){
        return bet;
    }

    public void increaseBet(int amount){
        bet += amount;
    }

    public void stay(){
        stay = true;
    }

    public boolean stayed(){
        return stay;
    }

    /*
    remove second card and return it to be used in the creation of a new hand
    both hands are counted as split hands
     */
    public BlackjackCard split(){
        split = true;
        return cards.remove(1);
    }
    public boolean isSplit(){
        return split;
    }
}
