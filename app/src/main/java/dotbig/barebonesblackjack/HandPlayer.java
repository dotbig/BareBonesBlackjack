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

    HandPlayer(int stake, BlackjackCard card){
        bet = stake;
        split = true;
        cards.add(card);
    }

    public boolean natural(){
        if (count() == 2 && value() == 21 && !split) {
            return true;
        } else return false;
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

    public boolean isSplit(){
        return split;
    }

    public BlackjackCard split(){
        split = true;
        return cards.remove(1);
    }

}
