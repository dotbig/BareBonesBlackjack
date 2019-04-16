package dotbig.barebonesblackjack;

import android.content.Context;
import android.content.res.Resources;

final class CardImage {
    //returns the resource id of the image file associated with a given card
    private CardImage(){ }

    static int findImage(Card card, Context context){
        Resources res = context.getResources();
        String file = getFileName(card);
        int id = res.getIdentifier(file, "drawable", context.getPackageName());
        return id;
    }

    //filenames will be ace_hearts, jack_spades etc
    //this function takes a card and returns a string corresponding to the filename required
    static private String getFileName(Card card){
        String rank;
        String suit;
        switch(card.getRank()){
            case ACE:
                rank = "ace";
                break;
            case TWO:
                rank = "two";
                break;
            case THREE:
                rank = "three";
                break;
            case FOUR:
                rank = "four";
                break;
            case FIVE:
                rank = "five";
                break;
            case SIX:
                rank = "six";
                break;
            case SEVEN:
                rank = "seven";
                break;
            case EIGHT:
                rank = "eight";
                break;
            case NINE:
                rank = "nine";
                break;
            case TEN:
                rank = "ten";
                break;
            case JACK:
                rank = "jack";
                break;
            case QUEEN:
                rank = "queen";
                break;
            case KING:
                rank = "king";
                break;
            default:
                rank = "joker";
                break;
        }

        switch(card.getSuit()){
            case HEARTS:
                suit = "hearts";
                break;
            case DIAMONDS:
                suit = "diamonds";
                break;
            case SPADES:
                suit = "spades";
                break;
            case CLUBS:
                suit = "clubs";
                break;
            default:
                suit = "joker";
                break;
        }

        String fileName = rank + "_" + suit;
        return fileName;
    }
}
