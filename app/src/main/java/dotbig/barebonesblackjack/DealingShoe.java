package dotbig.barebonesblackjack;

import java.util.Collections;
import java.util.Stack;


class DealingShoe implements Shoe {
    private Stack<Card> stack = new Stack<>();

    DealingShoe(){

    }

    public void addDeck(Deck deck){
        stack.addAll(deck.getDeck());
    }

    public void shuffle(){
        Collections.shuffle(stack);
    }

    public Card draw(){
        return stack.pop();
    }

    public Card draw(boolean faceUp){
        Card top = stack.pop();
        if (top.isFaceUp() != faceUp){
            top.flip(faceUp);
        }
        return top;
    }
}
