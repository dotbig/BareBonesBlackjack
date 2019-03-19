package dotbig.barebonesblackjack;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.ArrayList;

public class GameActivity extends AppCompatActivity implements OnClickListener {

    //butons
    private Button returnButton;
    private Button hitButton;
    private Button stayButton;
    private Button playButton;
    private Button betButton1;
    private Button betButton2;
    private Button betButton3;

    private Button doubleButton;
    private Button splitButton;

    //button containers
    private LinearLayout betBar;
    private LinearLayout contextBar;
    private LinearLayout playBar;
    private LinearLayout hitStayBar;
    private LinearLayout currencyBar;
    //information displays
    private TextView playerHandDisplay;
    private TextView playerValueDisplay;
    private TextView dealerHandDisplay;
    private TextView dealerValueDisplay;
    private TextView gameResult;
    private TextView betDisplay;
    private TextView bankDisplay;

    private Shoe shoe;
    private int shoeSize = 4;

    private int bank;
    private int bet;

    private List<Hand> playerHands;
    private Hand currentPlayerHand;
    private Hand dealerHand;

    private enum Result{
        LOSS, PUSH, WIN
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        initialiseUI();

        initialiseGameState();
    }

    private void initialiseUI(){
        //individual buttons
        returnButton = findViewById(R.id.buttonReturnToMain);
        hitButton = findViewById(R.id.buttonHit);
        stayButton = findViewById(R.id.buttonStay);
        playButton = findViewById(R.id.buttonPlay);
        betButton1 = findViewById(R.id.buttonBet1);
        betButton2 = findViewById(R.id.buttonBet2);
        betButton3 = findViewById(R.id.buttonBet3);

        splitButton = findViewById(R.id.buttonSplit);

        returnButton.setOnClickListener(this);
        hitButton.setOnClickListener(this);
        stayButton.setOnClickListener(this);
        playButton.setOnClickListener(this);
        betButton1.setOnClickListener(this);
        betButton2.setOnClickListener(this);
        betButton3.setOnClickListener(this);

        splitButton.setOnClickListener(this);
        //button bar containers
        betBar = findViewById(R.id.barBets);
        contextBar = findViewById(R.id.barContextuals);
        playBar = findViewById(R.id.barPlay);
        hitStayBar = findViewById(R.id.barHitStay);
        //displays for current bet and player funds
        betDisplay = findViewById(R.id.textviewBet);
        bankDisplay = findViewById(R.id.textviewBank);
        //text displays
        //dealer hand information
        dealerValueDisplay = findViewById(R.id.textviewValueDealer);
        dealerHandDisplay = findViewById(R.id.textviewHandDealer);
        //player hand information
        playerValueDisplay = findViewById(R.id.textviewValuePlayer);
        playerHandDisplay = findViewById(R.id.textviewHandPlayer);
        //result of the game
        gameResult = findViewById(R.id.textviewResult);

    }

    public void onClick(View v){
        switch(v.getId()){
            case (R.id.buttonReturnToMain):
                finish();
                break;
            case (R.id.buttonHit):
                hitPlayer(currentPlayerHand);
                break;
            case (R.id.buttonStay):
                stay2();
                break;
            case (R.id.buttonPlay):
                play();
                break;
            case (R.id.buttonBet1):
                increaseBet(20);
                break;
            case (R.id.buttonBet2):
                increaseBet(50);
                break;
            case (R.id.buttonBet3):
                increaseBet(100);
                break;
            case (R.id.buttonDoubleDown):

                break;
            case (R.id.buttonSplit):
                split();
                break;
            case (R.id.buttonInsurance):

                break;
            case (R.id.buttonSurrender):

                break;
        }
    }

    private void initialiseGameState(){
        bank = 300;
        bet = 0;

        updateBankDisplay();
        updateBetDisplay();

        clickablePlayButton(false);
        clickableBetButtons(true);

        initialiseShoe();
    }

    private void updatePlayerInformation(Hand currentHand){
        if (currentHand != null) {
            playerHandDisplay.setText(currentHand.toString());
            playerValueDisplay.setText(Integer.toString(currentHand.value()));
        } else {
            //just leave the previous information there
        }
    }
    private void updateDealerInformation(Hand hand){
        dealerHandDisplay.setText(hand.toString());
        dealerValueDisplay.setText(Integer.toString(hand.value()));
    }

    private void hitPlayer(Hand hand) {
        hand.hit(shoe.draw());
        updatePlayerInformation(hand);
        int newValue = hand.value();
        if (newValue == -1){
            gameResult.setText("Bust!");
            resolveHand(currentPlayerHand, Result.LOSS, false);
        } else if (newValue == 21){
            stay2();
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

    private void resolveHand(Hand hand, Result result, boolean blackjack){
        //clickableGameButtons(false);

        int bet = hand.getBet();
        int roi = 0;

        switch (result){
            case WIN:
                int winnings = 0;
                if (blackjack){
                    winnings = (int) (bet*1.5);
                } else winnings = bet;
                roi = bet + winnings;
                break;

            case PUSH:
                roi = bet;
                break;

            case LOSS:
                break;
        }
        increaseBank(roi);
        updateBankDisplay();

        currentPlayerHand = nextHand(currentPlayerHand, playerHands);
        updatePlayerInformation(currentPlayerHand);

        if (currentPlayerHand == null) {
            finishGame();
        }
    }

    private Hand nextHand(Hand current, List<Hand> hands){
        int currentIndex = hands.indexOf(current);
        return getNextHand(hands, currentIndex-1);
    }


    private Hand getNextHand(List<Hand> hands, int indexToCheck){
        if (indexToCheck < 0){
            return null;
        } else if (hands.get(indexToCheck) == null) {
            return getNextHand(hands, indexToCheck-1);
        } else return hands.get(indexToCheck);
    }

    private Hand previousHand(Hand current, List<Hand> hands){
        int currentIndex = hands.indexOf(current);
        return getNextHand(hands, currentIndex+1);
    }

    private Hand getPreviousHand(List<Hand> hands, int indexToCheck){
        if (indexToCheck >= hands.size()){
            return null;
        } else if (hands.get(indexToCheck) == null) {
            return getNextHand(hands, indexToCheck+1);
        } else return hands.get(indexToCheck);
    }

    private void stay2(){
        currentPlayerHand = nextHand(currentPlayerHand, playerHands);
        if (currentPlayerHand == null){
            dealerTurn();
        } else {
            updatePlayerInformation(currentPlayerHand);
        }
    }

    private void dealerTurn(){
        dealerHand.getCard(0).flip(true);
        dealerHandDisplay.setText(dealerHand.toString());

        while ((dealerHand.value() < 17 && dealerHand.value() != -1) || dealerHand.softSeventeen()){
            hitDealer(dealerHand);
            dealerHandDisplay.setText(dealerHand.toString());
            dealerValueDisplay.setText(Integer.toString(dealerHand.value()));
        }

        evaluateAllResults();
    }

    private void evaluateResult2(Hand hand){
        int dealerValue = dealerHand.value();
        int playerValue = hand.value();
        boolean dealerNatural = dealerHand.natural();
        boolean playerNatural = hand.natural();

        if (playerNatural) {
            if (dealerNatural) {
                gameResult.setText("Natural push");
                //push(currentPlayerHand);
                resolveHand(hand, Result.PUSH, true);
            } else {
                gameResult.setText("Player natural wins");
                //win(currentPlayerHand, true);
                resolveHand(hand, Result.WIN, true);
            }
        } else if (dealerNatural) {
            gameResult.setText("Dealer natural wins");
            //lose(currentPlayerHand);
            resolveHand(hand, Result.LOSS, false);
        } else if (dealerValue == -1) {
            gameResult.setText("Dealer bust!");
            //win(currentPlayerHand, false);
            resolveHand(hand, Result.WIN, false);
        } else if (dealerValue == playerValue){
            gameResult.setText("Push");
            //push(currentPlayerHand);
            resolveHand(hand, Result.PUSH, false);
        } else if (dealerValue < playerValue){
            gameResult.setText("Player wins!");
            //win(currentPlayerHand, false);
            resolveHand(hand, Result.WIN, false);
        } else {
            gameResult.setText("Dealer wins!");
            //lose(currentPlayerHand);
            resolveHand(hand, Result.LOSS, false);
        }
    }


    private void evaluateAllResults() {
        int i = 0;
        currentPlayerHand = playerHands.get(playerHands.size()-1);
        while (currentPlayerHand != null){
            System.out.println(i);
            evaluateResult2(currentPlayerHand);
            currentPlayerHand = nextHand(currentPlayerHand, playerHands);
            i++;
        }
        //updateBetDisplay();
        //updateBankDisplay();

        finishGame();

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
        //playerHands = new LinkedList<>();
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

    private void play(){
        gameResult.setText("");
        initialiseHands();

        updateBankDisplay();

        clickablePlayButton(false);
        disableBetButtons();
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
        enableBetButtons();
    }

    private void finishGame(){
        resetBet();
        showGameButtons(false);
        clickablePlayButton(false);

    }

    private Hand splitHand(Hand hand){
        int bet = hand.getBet();
        Card splitCard = hand.split();
        Hand newHand = new PlayHand(bet, splitCard);
        return newHand;
    }

    private void split(){
        Hand toAdd = splitHand(currentPlayerHand);
        playerHands.add(toAdd);
        hitPlayer(currentPlayerHand);
        currentPlayerHand = toAdd;
        hitPlayer(currentPlayerHand);
        updatePlayerInformation(currentPlayerHand);
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
                betButton1.setEnabled(enabled);
            }
            if (bank >= 50) {
                betButton2.setEnabled(enabled);
            }
            if (bank >= 100) {
                betButton3.setEnabled(enabled);
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

}
