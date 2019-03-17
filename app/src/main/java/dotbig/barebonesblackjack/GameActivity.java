package dotbig.barebonesblackjack;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.ArrayList;

public class GameActivity extends AppCompatActivity {

    //textviews that will display game information
    private TextView playerHandDisplay;
    private TextView playerValueDisplay;
    private TextView dealerHandDisplay;
    private TextView dealerValueDisplay;
    private TextView gameResult;

    private Button hitButton;
    private Button stayButton;
    private Button playButton;

    private TextView betDisplay;
    private TextView bankDisplay;
    private Button bet20Button;
    private Button bet50Button;
    private Button bet100Button;

    private LinearLayout betBar;
    private LinearLayout contextBar;
    private LinearLayout playBar;
    private LinearLayout hitStayBar;
    private LinearLayout currencyBar;


    private Shoe shoe;
    private int shoeSize = 4;

    private int bank;
    private int bet;
    private boolean split;


    private List<Hand> playerHands;
    private Hand currentPlayerHand;
    private Hand dealerHand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        //initialise textviews
        initialiseTextViews();
        assignLinearLayouts();
        //make sure our buttons do stuff
        configureReturnButton();

        bank = 300;
        bet = 0;

        configurePlayButton();
        clickablePlayButton(false);

        configureHitButton();
        configureStayButton();
        clickableGameButtons(false);

        configureBetButtons();
        clickableBetButtons(true);

        initialiseShoe();

        updateBankDisplay();
        updateBetDisplay();

    }

    private void initialiseTextViews(){
        //dealer hand information
        dealerValueDisplay = findViewById(R.id.textviewValueDealer);
        dealerHandDisplay = findViewById(R.id.textviewHandDealer);
        //player hand information
        playerValueDisplay = findViewById(R.id.textviewValuePlayer);
        playerHandDisplay = findViewById(R.id.textviewHandPlayer);
        //result of the game
        gameResult = findViewById(R.id.textviewResult);
        //displays for current bet and player funds
        betDisplay = findViewById(R.id.textviewBet);
        bankDisplay = findViewById(R.id.textviewBank);
    }

    private void configureReturnButton() {
        //back to main menu button
        Button returnButton = findViewById(R.id.buttonReturnToMain);
        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void updatePlayerInformation(Hand currentHand){
        playerHandDisplay.setText(currentHand.toString());
        playerValueDisplay.setText(Integer.toString(currentHand.value()));
    }
    private void updateDealerInformation(Hand hand){
        dealerHandDisplay.setText(hand.toString());
        dealerValueDisplay.setText(Integer.toString(hand.value()));
    }

    private void configureHitButton() {
        hitButton = findViewById(R.id.buttonHit);
        hitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hitPlayer(currentPlayerHand);
            }
        });
    }
    private void hitPlayer(Hand hand) {
        hand.hit(shoe.draw());
        updatePlayerInformation(hand);
        int newValue = hand.value();
        if (newValue == -1){
            gameResult.setText("Bust!");
            lose(hand);
        } else if (newValue == 21){
            stay();
        }
    }

    private void hitDealer(Hand hand){
        if (dealerHand.count() < 1){
            dealerHand.hit(shoe.draw(false));
        } else {
            dealerHand.hit(shoe.draw(true));
        }
        updateDealerInformation(hand);
    }

    //lose, push and win are called by evaluateResult() when the player stays
    private void lose(Hand currentHand){
        clickableGameButtons(false);
        //TODO: remove currentHand from playerHands
        finishGame();
    }
    private void push(Hand currentHand){
        clickableGameButtons(false);
        int bet = currentHand.getBet();
        increaseBank(bet);
        updateBankDisplay();
        finishGame();
    }
    private void win(Hand currentHand, boolean blackjack){
        clickableGameButtons(false);
        int bet = currentHand.getBet();
        int winnings;
        if (blackjack){
            winnings = (int) (currentHand.getBet()*1.5);
        } else winnings = currentHand.getBet();
        int total = bet + winnings;
        increaseBank(total);
        updateBankDisplay();
        finishGame();
    }

    private void configureStayButton(){
        stayButton = findViewById(R.id.buttonStay);
        stayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stay();
            }
        });
    }
    private void stay(){
        clickableGameButtons(false);

        dealerHand.getCard(0).flip(true);
        dealerHandDisplay.setText(dealerHand.toString());

        while ((dealerHand.value() < 17 && dealerHand.value() != -1) || dealerHand.softSeventeen()){
            hitDealer(dealerHand);
            dealerHandDisplay.setText(dealerHand.toString());
            dealerValueDisplay.setText(Integer.toString(dealerHand.value()));
        }
        evaluateResult();
    }

    private void evaluateResult(){
        int dealerValue = dealerHand.value();
        int playerValue = currentPlayerHand.value();
        boolean dealerNatural = dealerHand.natural();
        boolean playerNatural = currentPlayerHand.natural();

        if (playerNatural) {
            if (dealerNatural) {
                gameResult.setText("Natural push");
                push(currentPlayerHand);
            } else {
                gameResult.setText("Player natural wins");
                win(currentPlayerHand, true);
            }
        } else if (dealerNatural) {
            gameResult.setText("Dealer natural wins");
            lose(currentPlayerHand);
        } else if (dealerValue == -1) {
            gameResult.setText("Dealer bust!");
            win(currentPlayerHand, false);
        } else if (dealerValue == playerValue){
            gameResult.setText("Push");
            push(currentPlayerHand);
        } else if (dealerValue < playerValue){
            gameResult.setText("Player wins!");
            win(currentPlayerHand, false);
        } else {
            gameResult.setText("Dealer wins!");
            lose(currentPlayerHand);
        }
    }

    private void initialiseShoe(){
        shoe = new DealingShoe();
        for (int i = 0; i < shoeSize; i++){
            shoe.addDeck(new StandardDeck());
        }
        shoe.shuffle();
    }
    private void initialiseHands(){
        //maybe pass in bet value, taken from user input before calling this method
        dealerHand = new PlayHand();

        playerHands = new ArrayList<>();
        Hand newHand = new PlayHand(bet);
        playerHands.add(newHand);
        currentPlayerHand = playerHands.get(0);
    }
    private void deal(){
        hitDealer(dealerHand);
        hitPlayer(currentPlayerHand);
        hitDealer(dealerHand);
        hitPlayer(currentPlayerHand);
    }

    private void configurePlayButton(){
        playButton = findViewById(R.id.buttonPlay);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });
    }
    private void play(){
        gameResult.setText("");
        initialiseHands();

        updateBankDisplay();

        clickablePlayButton(false);
        clickableBetButtons(false);
        clickableGameButtons(true);

        showGameButtons(true);
        deal();

        updatePlayerInformation(currentPlayerHand);
        updateDealerInformation(dealerHand);
    }

    private void increaseBet(int amount){
        bet += amount;
        decreaseBank(amount);
        updateBetDisplay();
        updateBankDisplay();
        clickablePlayButton(true);
    }

    private void decreaseBet(int amount){

    }

    private void resetBet(){
        bet = 0;
        updateBetDisplay();
        clickableBetButtons(true);
    }

    private void finishGame(){
        resetBet();
        showGameButtons(false);
        clickablePlayButton(false);

    }

    private void increaseBank(int amount){
        bank += amount;
    }

    private void decreaseBank(int amount){
        bank -= amount;
    }

    private void updateBetDisplay(){
        betDisplay.setText(Integer.toString(bet));
    }

    private void updateBankDisplay(){
        bankDisplay.setText(Integer.toString(bank));
    }

    private void clickableGameButtons(boolean enabled){
        hitButton.setEnabled(enabled);
        stayButton.setEnabled(enabled);
    }

    private void clickableBetButtons(boolean enabled){
        if (bank < 20){
            gameResult.setText("You broke, son. Go home.");
        } else {
            if (bank >= 20) {
                bet20Button.setEnabled(enabled);
            }
            if (bank >= 50) {
                bet50Button.setEnabled(enabled);
            }
            if (bank >= 100) {
                bet100Button.setEnabled(enabled);
            }
        }
    }

    private void clickablePlayButton(boolean enabled){
        if (bet > 0 && enabled){
            playButton.setEnabled(true);
        } else {
            playButton.setEnabled(false);
        }
    }

    private void showGameButtons(boolean play){
        if (play){
            playBar.setVisibility(View.GONE);
            betBar.setVisibility(View.GONE);
            hitStayBar.setVisibility(View.VISIBLE);
            contextBar.setVisibility(View.VISIBLE);
        } else {
            playBar.setVisibility(View.VISIBLE);
            betBar.setVisibility(View.VISIBLE);
            hitStayBar.setVisibility(View.GONE);
            contextBar.setVisibility(View.GONE);
        }

    }

    private void configureBetButtons(){
        bet20Button = findViewById(R.id.buttonBet20);
        bet50Button = findViewById(R.id.buttonBet50);
        bet100Button = findViewById(R.id.buttonBet100);

        bet20Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseBet(20);
            }
        });
        bet50Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseBet(50);
            }
        });
        bet100Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseBet(100);
            }
        });
    }

    private void assignLinearLayouts(){
        betBar = findViewById(R.id.horizontalMiddleBets);
        contextBar = findViewById(R.id.horizontalMiddleContextuals);

        playBar = findViewById(R.id.horizontalTopPlay);
        hitStayBar = findViewById(R.id.horizontalTopHitStay);
    }



}
