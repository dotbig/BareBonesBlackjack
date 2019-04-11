package dotbig.barebonesblackjack;

public class HandDealer extends HandBase implements DealerSpecific {

    private final int UP = 0;
    private final int HOLE = 1;

    HandDealer(){

    }

    public boolean natural(){
        if (count() == 2 && value() == 21) {
            return true;
        } else return false;
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

    public boolean softSeventeen(){
        int aces = numberOfAces();
        int sansAces = valueWithoutAces();
        int hard = valueHard();
        //TODO: review this logic
        if (aces == 1){
            return sansAces == 6;
        } else if (aces > 1) {
            if ((hard - 1) == 6){
                return true;
            }
        }
        return false;
    }

    private int valueHard(){
        return numberOfAces() + valueWithoutAces();
    }
}