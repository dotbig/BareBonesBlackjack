package dotbig.barebonesblackjack;

import java.util.Collections;
import java.util.Stack;


class DealingShoe implements Shoe {
    Stack<Card> stack = new Stack<Card>();

    DealingShoe(){
        stack.addAll(new StandardDeck().getDeck());
        Collections.shuffle(stack);
    }

    DealingShoe(int numberOfDecks){
        if (numberOfDecks < 1) {
            stack.addAll(new StandardDeck().getDeck());
        } else {
            for (int i=0; i < numberOfDecks; i++){
                stack.addAll(new StandardDeck().getDeck());
            }
        }
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
