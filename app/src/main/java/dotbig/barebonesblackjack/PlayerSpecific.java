package dotbig.barebonesblackjack;

public interface PlayerSpecific extends Hand {
    int getBet();
    void increaseBet(int amount);
    void stay();
    boolean stayed();
    BlackjackCard split();
    boolean isSplit();
}
