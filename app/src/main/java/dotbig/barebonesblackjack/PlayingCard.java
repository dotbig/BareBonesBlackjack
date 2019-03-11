package dotbig.barebonesblackjack;

class PlayingCard implements Card {
    String suit;
    String rank;
    boolean faceUp;

    //when we construct a card we need a suit and a rank
    //default visibility is face up
    PlayingCard(int suit, int rank){
        faceUp = true;
        switch(suit){
            case 0:
                this.suit = "Hearts";
                break;
            case 1:
                this.suit = "Diamonds";
                break;
            case 2:
                this.suit = "Clubs";
                break;
            case 3:
                this.suit = "Spades";
                break;
            default:
                this.suit = "Joker";
                break;
        }

        switch(rank){
            case 0:
                this.rank = "Ace";
                break;
            case 1:
                this.rank = "Two";
                break;
            case 2:
                this.rank = "Three";
                break;
            case 3:
                this.rank = "Four";
                break;
            case 4:
                this.rank = "Five";
                break;
            case 5:
                this.rank = "Six";
                break;
            case 6:
                this.rank = "Seven";
                break;
            case 7:
                this.rank = "Eight";
                break;
            case 8:
                this.rank = "Nine";
                break;
            case 9:
                this.rank = "Ten";
                break;
            case 10:
                this.rank = "Jack";
                break;
            case 11:
                this.rank = "Queen";
                break;
            case 12:
                this.rank = "King";
                break;
            default:
                this.suit = "Joker";
                break;
        }
    }

    public String getSuit(){
        return suit;
    }

    public String getRank(){
        return rank;
    }

    public boolean isFaceUp(){
        return faceUp;
    }

    public boolean flip(boolean visible){
        if (faceUp == visible){
            return faceUp;
        } else {
            faceUp = visible;
            return faceUp;
        }
    }

    public String toString(){
        return rank + " of " + suit;
    }

}
