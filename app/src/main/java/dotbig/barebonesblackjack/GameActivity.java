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

    private TextView eventLog;
    private StringBuilder events;

    private Shoe shoe;
    private int shoeSize = 4;
    private int penetration = 90;

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

        events = new StringBuilder();

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

        eventLog = findViewById(R.id.textviewEventLog);
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
                //stay2();
                stay(currentPlayerHand);
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
                //split()
                split(currentPlayerHand);
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

    private void updatePlayerInformation(Hand hand){
        if (hand != null) {
            playerHandDisplay.setText(hand.toString());
            playerValueDisplay.setText(Integer.toString(hand.value()));
        } else {
            //just leave the previous information there
        }
    }
    private void updateDealerInformation(Hand hand){
        dealerHandDisplay.setText(hand.toString());
        dealerValueDisplay.setText(Integer.toString(hand.value()));
    }

    //TODO: like stay(), maybe make this return a Hand and do currentPlayerHand = bust(hand) when we use it
    private void bust(Hand hand){
        System.out.println("bust!");
        gameResult.setText("Bust!");
        log("Bust!");
        hand.bust();

        //TODO: remove currentPlayerHand usages
        currentPlayerHand = getNextActiveHand(currentPlayerHand, playerHands);
        if (currentPlayerHand == null){
            //TODO: make sure we aren't evaluating hands that have already busted
            dealerTurn();
        } else {
            updatePlayerInformation(currentPlayerHand);
        }
    }

    private void hitPlayer(Hand hand) {
        hand.hit(shoe.draw());
        updatePlayerInformation(hand);
        int newValue = hand.value();
        if (newValue == -1){
            bust(hand);
        } else if (newValue == 21){
            if (hand.natural()) {
                dealerTurn();
            }
            log("hitPlayer 21");
            stay(hand);
        }
    }
    private void hitDealer(Hand hand){
        if (hand.count() == 1){
            hand.hit(shoe.draw(false));
        } else {
            hand.hit(shoe.draw(true));
        }
        updateDealerInformation(hand);
    }

    //TODO: consolidate getHand methods, maybe use parameter boolean "active" to distinguish functionality
    private Hand getLastHand(List<Hand> hands){
        Hand current;
        current = playerHands.get(hands.size()-1);
        return current;
    }

    private Hand getLastActiveHand(List<Hand> hands){
        Hand current;
        current = playerHands.get(hands.size()-1);
        if (current.busted() || current.value() == 21){
            current = getNextActiveHand(current, hands);
        }
        return current;
    }

    private Hand getNextActiveHand(Hand current, List<Hand> hands){
        if (current == null){
            return null;
        }
        int currentIndex = hands.indexOf(current);
        return nextActiveHand(hands, currentIndex-1);
    }

    private Hand nextActiveHand(List<Hand> hands, int indexToCheck){
        if (indexToCheck < 0){
            System.out.println("getNextActiveHand returning null");
            return null;
        } else if (hands.get(indexToCheck).busted()) {
            System.out.println("getNextActiveHand busted, getting next");
            return nextActiveHand(hands, indexToCheck-1);
        } else if (hands.get(indexToCheck).value() == 21) {
            System.out.println("getNextActiveHand 21, getting next");
            return nextActiveHand(hands, indexToCheck-1);
        } else {
            System.out.println("getNextActiveHand: "+indexToCheck);
            return hands.get(indexToCheck);
        }
    }

    private Hand getNextHand(Hand current, List<Hand> hands){
        if (current == null){
            return null;
        }
        int currentIndex = hands.indexOf(current);
        return nextHand(hands, currentIndex-1);
    }

    private Hand nextHand(List<Hand> hands, int indexToCheck){
        if (indexToCheck < 0){
            System.out.println("getNextHand returning null");
            return null;
        } else {
            System.out.println("getNextHand: "+indexToCheck);
            return hands.get(indexToCheck);
        }
    }

    //TODO: previous will be needed once we implement the ability to select which hand we're viewing
    private Hand previousHand(Hand current, List<Hand> hands){
        return null;
    }

    private Hand getPreviousHand(List<Hand> hands, int indexToCheck){
        return null;
    }


    //if things start messing up, change hand back to currentPlayerHand
    //TODO: maybe make this return a Hand and do currentPlayerHand = stay(hand) when we use it
    private void stay(Hand hand){
        hand = getNextActiveHand(hand, playerHands);
        if (hand == null){
            dealerTurn();
        } else {
            currentPlayerHand = hand;
            updatePlayerInformation(currentPlayerHand);
        }
    }

    private void dealerTurn(){
        //TODO: if player has natural and dealer doesn't, don't bother drawing cards for dealer; just flip and win
        //TODO: maybe add parameter boolean playerNatural
        /*maybe something like
            if (playerNatural){
                if (!dealerNatural){
                    flip
                    updateDealerInformation
                    evaluateAllResults
                }
            }
         */
        dealerHand.getCard(1).flip(true);
        updateDealerInformation(dealerHand);

        while ((dealerHand.value() < 17 && dealerHand.value() != -1) || dealerHand.softSeventeen()){
            hitDealer(dealerHand);
            updateDealerInformation(dealerHand);
        }
        evaluateAllResults();
    }

    //TODO: make sure we aren't evaluating hands that have already busted
    private void evaluateResult2(Hand hand){
        int dealerValue = dealerHand.value();
        int playerValue = hand.value();
        boolean dealerNatural = dealerHand.natural();
        boolean playerNatural = hand.natural();

        if (playerNatural) {
            if (dealerNatural) {
                gameResult.setText("Natural push");
                log("Natural push");
                resolveHand(hand, Result.PUSH);
            } else {
                gameResult.setText("Player natural wins");
                log("Player natural wins");
                resolveHand(hand, Result.WIN);
            }
        } else if (dealerNatural) {
            gameResult.setText("Dealer natural wins");
            log("Dealer natural wins");
            resolveHand(hand, Result.LOSS);
        } else if (dealerValue == -1) {
            gameResult.setText("Dealer bust!");
            log("Dealer bust!");
            resolveHand(hand, Result.WIN);
        } else if (dealerValue == playerValue){
            gameResult.setText("Push");
            log("Push!");
            resolveHand(hand, Result.PUSH);
        } else if (dealerValue < playerValue){
            gameResult.setText("Player wins!");
            log("Player wins!");
            resolveHand(hand, Result.WIN);
        } else {
            gameResult.setText("Dealer wins!");
            log("Dealer wins!");
            resolveHand(hand, Result.LOSS);
        }
    }

    private void resolveHand(Hand hand, Result result){
        //clickableGameButtons(false);
        boolean blackjack = hand.natural();
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

        log("won "+ roi);
    }


    private void evaluateAllResults() {
        //testing stuff
        currentPlayerHand = playerHands.get(playerHands.size()-1);
        while (currentPlayerHand != null) {
            log(currentPlayerHand.toString());
            currentPlayerHand = getNextHand(currentPlayerHand, playerHands);
        }
        //end testing stuff

        currentPlayerHand = getLastHand(playerHands);
        int i = 0;
        while (currentPlayerHand != null){
            if (!currentPlayerHand.busted()) {
                System.out.println(i);
                evaluateResult2(currentPlayerHand);
            }
            currentPlayerHand = getNextHand(currentPlayerHand, playerHands);
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
        shoe.setPenetration(penetration);
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
            if (dealerHand.natural()){
                hitPlayer(currentPlayerHand);
                dealerTurn();
            }
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

        //updatePlayerInformation(currentPlayerHand);
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
        if (shoe.penetrationCheck()) {
            initialiseShoe();
        }
        log("round over");
        showGameButtons(false);
        clickablePlayButton(false);

    }

    private Hand splitHand(Hand hand){
        int bet = hand.getBet();
        Card splitCard = hand.split();
        Hand newHand = new PlayHand(bet, splitCard);
        return newHand;
    }

    private void split(Hand hand){
        Hand master = hand;
        Hand branch = splitHand(master);

        playerHands.add(branch);

        branch.hit(shoe.draw());
        master.hit(shoe.draw());
        int branchValue = branch.value();
        int masterValue = master.value();

        if (branchValue == -1) {
            branch.bust();
        } else if (branchValue == 21){
            log("split 21 branch");
        }
        if (masterValue == -1){
            master.bust();
        } else if (masterValue == 21){
            log("split 21 master");
        }

        currentPlayerHand = getLastActiveHand(playerHands);
        updatePlayerInformation(currentPlayerHand);
        if (currentPlayerHand == null){
            dealerTurn();
        }
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

    private void updateEventLog(){
        eventLog.setText(events);
    }

    private void log(String s){
        StringBuilder builder = new StringBuilder(s + "\n");
        events.append(builder);
        updateEventLog();
    }

}
