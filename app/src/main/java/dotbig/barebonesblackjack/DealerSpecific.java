package dotbig.barebonesblackjack;

public interface DealerSpecific extends Hand {
    BlackjackCard getUpCard();
    BlackjackCard getHoleCard();
    void reveal();
    boolean softSeventeen();
}
