package dotbig.barebonesblackjack;

enum Suit {

    HEARTS ("♥"),
    DIAMONDS ("♦"),
    CLUBS ("♣"),
    SPADES ("♠"),
    JOKER ("Joker");

    private final String suit;

    Suit(String suit){
        this.suit = suit;
    }

    public String getSuit(){
        return suit;
    }
}
