package dotbig.barebonesblackjack;

public interface BlackjackHandPlayer extends BlackjackHand {
    int getBet();
    void increaseBet(int amount);
    void stay();
    boolean stayed();
    BlackjackCard split();
    boolean isSplit();
}
