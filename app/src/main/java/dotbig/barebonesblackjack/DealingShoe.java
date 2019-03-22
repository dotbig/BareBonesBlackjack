package dotbig.barebonesblackjack;

import java.util.Collections;
import java.util.Stack;


class DealingShoe implements Shoe {
    private Stack<Card> stack = new Stack<>();
    private int capacity;
    private int penetration;
    private int penetrated;

    DealingShoe(){

    }

    public void addDeck(Deck deck){
        capacity += deck.count();
        stack.addAll(deck.getDeck());
    }

    public void shuffle(){
        Collections.shuffle(stack);
    }

    public Card draw(){
        penetrated++;
        return stack.pop();

    }

    public Card draw(boolean faceUp){
        Card top = stack.pop();
        if (top.isFaceUp() != faceUp){
            top.flip(faceUp);
        }
        penetrated++;
        return top;
    }

    public void setPenetration(int pen){
        if (pen > 90){
            penetration = 90;
        } else if (pen < 40){
            penetration = 40;
        } else {
            penetration = pen;
        }
    }

    public boolean penetrationCheck(){
        float ratio = (float)penetration/100;
        int maxPen = (int)(capacity * ratio);
        if (penetrated > maxPen){
            return true;
        } else return false;
    }


}
