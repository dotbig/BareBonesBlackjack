package dotbig.barebonesblackjack;

public class CardBlackjack implements BlackjackCard {
    private Suit suit;
    private Rank rank;
    private boolean faceUp;

    CardBlackjack(int suit, int rank){
        faceUp = true;
        switch(suit){
            case 0:
                this.suit = Suit.HEARTS;
                break;
            case 1:
                this.suit = Suit.DIAMONDS;
                break;
            case 2:
                this.suit = Suit.CLUBS;
                break;
            case 3:
                this.suit = Suit.SPADES;
                break;
            default:
                this.suit = Suit.JOKER;
                break;
        }

        switch(rank){
            case 0:
                this.rank = Rank.ACE;
                break;
            case 1:
                this.rank = Rank.TWO;
                break;
            case 2:
                this.rank = Rank.THREE;
                break;
            case 3:
                this.rank = Rank.FOUR;
                break;
            case 4:
                this.rank = Rank.FIVE;
                break;
            case 5:
                this.rank = Rank.SIX;
                break;
            case 6:
                this.rank = Rank.SEVEN;
                break;
            case 7:
                this.rank = Rank.EIGHT;
                break;
            case 8:
                this.rank = Rank.NINE;
                break;
            case 9:
                this.rank = Rank.TEN;
                break;
            case 10:
                this.rank = Rank.JACK;
                break;
            case 11:
                this.rank = Rank.QUEEN;
                break;
            case 12:
                this.rank = Rank.KING;
                break;
            default:
                this.rank = Rank.JOKER;
                break;
        }
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank(){
        return rank;
    }

    public int value(){
        return rank.getValue();
    }

    public boolean isAce(){
        return rank.isAce();
    }

    public boolean isFaceUp(){
        return faceUp;
    }

    public void flip(boolean faceUp){
        this.faceUp = faceUp;
    }

    public String string(){
        return rank.getRank()+suit.getSuit();
    }
}
