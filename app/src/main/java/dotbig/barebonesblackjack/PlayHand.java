package dotbig.barebonesblackjack;

import java.util.List;
import java.util.ArrayList;

class PlayHand implements Hand {
    private List<Card> cards = new ArrayList<>();
    private int bet;
    private boolean stay;
    private boolean bust;
    private boolean split;

    PlayHand(){
        
    }

    PlayHand(int stake){
        bet = stake;
    }

    PlayHand(int stake, Card card){
        bet = stake;
        split = true;
        cards.add(card);
    }

    public void hit(Card draw){
        cards.add(draw);
    }

    public Card getCard(int index){
        return cards.get(index);
    }

    public int getBet(){
        return bet;
    }

    public void increaseBet(int amount){
        bet += amount;
    }

    public boolean isSplit(){
        return split;
    }

    public Card split(){
        split = true;
        return cards.remove(1);
    }

    public void stay(){
        stay = true;
    }

    public boolean stayed(){
        return stay;
    }

    public void bust(){
        bust = true;
    }

    public boolean busted(){
        return bust;
    }

    public int count(){
        return cards.size();
    }

    public int value(){
        int aces = numberOfAces();
        int sansAces = valueWithoutAces();
        int value = valueOptimal(aces, sansAces);

        return value;
    }

    public boolean natural(){
        if (count() == 2 && value() == 21 && !split) {
            return true;
        } else return false;
    }

    public boolean softSeventeen(){
        int aces = numberOfAces();
        int sansAces = valueWithoutAces();
        int hard = valueHard();
        if (aces == 1){
            if (sansAces == 6){
                return true;
            } else return false;
        } else if (aces > 1) {
            if ((hard - 1) == 6){
                return true;
            }
        }
        return false;
    }

    public void clear(){
        cards.clear();
    }

    public String toString(){
        StringBuilder currentHand = new StringBuilder();
        for (Card c : cards){
            if (!c.isFaceUp()){
                currentHand.append("??\n");
            } else {
                currentHand.append(c.toString()+"\n");
            }
        }
        return currentHand.toString();
    }

    private int numberOfAces() {
        int aces = 0;
        for (Card c : cards){
            if (c.isAce()){
                aces++;
            }
        }
        return aces;
    }

    private int valueWithoutAces() {
        int runningTotal = 0;
        for (Card c : cards) {
            if (c.getValue() != 1){
                runningTotal += c.getValue();
            }
        }
        return runningTotal;
    }

    private int valueHard(){
        return numberOfAces() + valueWithoutAces();
    }

    private int valueOptimal(int numAces, int valWithoutAces){
        /*
        returns the highest value of the hand without busting, accounting for aces
        if bust is unavoidable, returns -1

        since aces can be worth 1 or 11, if we have any aces then we need to determine which
          combination of ace values added to our running total will give us the best hand

          we'll need to remember the value of each unique combination of ace values.
          for n aces there's n+1 possible unique combinations of ace values
              1 ace:  {(1),
                       (11)}

              2 aces: {(1, 1),
                       (1, 11),
                       (11, 11)}

              3 aces: {(1, 1, 1),
                       (1, 1, 11),
                       (1, 11, 11),
                       (11, 11, 11)}

              to find the value of a combination we can use
                value = A*1 + B*11
                    where A is the number of aces with value 1 and B is the number of aces with value 11

                start with A = numberOfAces and B = 0
                decrement A and increment B; total number of aces stays the same
                when A = 0 and B = numberOfAces we've gone through all unique combinations
        */
        int result = -1;
        if (numAces > 0){
            int[] combinations = new int[numAces + 1];
            int ones = numAces;
            int elevens = 0;
            for (int i = 0; i <= numAces; i++) {
                int total = ones + elevens*11;
                combinations[i] = total;
                ones -= 1;
                elevens += 1;
            }
            //add each combination to our running total, keeping the best one
            int currentBest = -1;
            for (int i = 0; i < combinations.length; i++) {
                int candidate = valWithoutAces + combinations[i];
                if ((currentBest < candidate) && (candidate <= 21)) {
                    currentBest = candidate;
                }
            }
            result = currentBest;
        } else
            //if we don't have any aces, then our total is just the raw values of our cards
            if (valWithoutAces <= 21){
                result = valWithoutAces;
            }
        return result;
    }
}
