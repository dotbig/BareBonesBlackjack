package dotbig.barebonesblackjack;

public class HandDealer extends HandBase implements BlackjackHandDealer {

    HandDealer(){

    }

    public boolean natural(){
        if (count() == 2 && value() == 21) {
            return true;
        } else return false;
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
