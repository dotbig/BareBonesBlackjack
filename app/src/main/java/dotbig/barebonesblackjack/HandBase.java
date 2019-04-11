package dotbig.barebonesblackjack;

import java.util.ArrayList;
import java.util.List;

public abstract class HandBase implements Hand {
    List<BlackjackCard> cards = new ArrayList<>();
    private boolean bust;

    public void add(BlackjackCard card){
        cards.add(card);
    }

    //returns null if we reach out of bounds
    public BlackjackCard getCard(int index){
        if (index >= 0 && index <= cards.size()-1){
            return cards.get(index);
        } else return null;
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

    public void clear(){
        cards.clear();
    }

    public int value(){
        return valueOptimal(numberOfAces(), valueWithoutAces());
    }

    int numberOfAces() {
        int aces = 0;
        for (BlackjackCard c : cards){
            if (c.isAce()){
                aces++;
            }
        }
        return aces;
    }

    int valueWithoutAces() {
        int runningTotal = 0;
        for (BlackjackCard c : cards) {
            if (c.value() != 1){
                runningTotal += c.value();
            }
        }
        return runningTotal;
    }

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
    private int valueOptimal(int numAces, int valWithoutAces){
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

    public String string(){
        StringBuilder builder = new StringBuilder();
        for (BlackjackCard c : cards){
            if (!c.isFaceUp()){
                builder.append("??\n");
            } else {
                builder.append(c.string()+"\n");
            }
        }
        return builder.toString();
    }
}
