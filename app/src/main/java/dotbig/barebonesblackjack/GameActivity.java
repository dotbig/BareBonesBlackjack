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

    private Button doubleDownButton;
    private Button splitButton;
    private Button insuranceButton;
    private Button surrenderButton;

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
    private int insurance;

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
        doubleDownButton = findViewById(R.id.buttonDoubleDown);
        insuranceButton = findViewById(R.id.buttonInsurance);
        surrenderButton = findViewById(R.id.buttonSurrender);

        returnButton.setOnClickListener(this);
        hitButton.setOnClickListener(this);
        stayButton.setOnClickListener(this);
        playButton.setOnClickListener(this);
        betButton1.setOnClickListener(this);
        betButton2.setOnClickListener(this);
        betButton3.setOnClickListener(this);

        splitButton.setOnClickListener(this);
        doubleDownButton.setOnClickListener(this);
        insuranceButton.setOnClickListener(this);
        surrenderButton.setOnClickListener(this);

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
                disableOptions();
                hitPlayer(currentPlayerHand);
                break;
            case (R.id.buttonStay):
                disableOptions();
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
                doubleDown(currentPlayerHand);
                break;
            case (R.id.buttonSplit):
                disableOptions();
                split(currentPlayerHand);
                break;
            case (R.id.buttonInsurance):
                disableOptions();
                insurance(currentPlayerHand);
                break;
            case (R.id.buttonSurrender):
                surrender(currentPlayerHand);
                break;
        }
    }

    private void initialiseGameState(){
        bank = 300;
        bet = 0;

        updateBankDisplay();
        updateBetDisplay();

        initialiseShoe();

        togglePlayButton(false);
        toggleBetButtons(true);
    }

    private void updatePlayerInformation(Hand hand){
        if (hand != null) {
            playerHandDisplay.setText(hand.toString());
            playerValueDisplay.setText(Integer.toString(hand.value()));
        }
    }
    private void updateDealerInformation(Hand hand){
        dealerHandDisplay.setText(hand.toString());
        dealerValueDisplay.setText(Integer.toString(hand.value()));
    }

    //TODO: consolidate getHand methods, maybe use parameter boolean "active" to distinguish functionality
    private Hand getLastHand(List<Hand> hands, boolean active){
        Hand current;
        current = hands.get(hands.size()-1);
        if (active){
            if (current.busted() || current.value() == 21){
                current = getNextHand(current, hands, true);
            }
        }
        return current;
    }

    private Hand getNextHand(Hand current, List<Hand> hands, boolean active){
        if (current == null){
            return null;
        }
        int currentIndex = hands.indexOf(current);
        if (active){
            return nextHand(hands, currentIndex-1, true);
        }
        return nextHand(hands, currentIndex-1, false);
    }

    private Hand nextHand(List<Hand> hands, int indexToCheck, boolean active){
        System.out.println("nextHand, index: " +Integer.toString(indexToCheck));
        if (indexToCheck < 0){
            return null;
        }
        if (active){
            System.out.println("nextHand, active: "+active);
            if (hands.get(indexToCheck).busted() || hands.get(indexToCheck).value() == 21){
                System.out.println("nextHand, not good enough");
                return nextHand(hands, indexToCheck-1, true);
            }
        }
        return hands.get(indexToCheck);
    }

    //TODO: previous will be needed once we implement the ability to select which hand we're viewing
    private Hand previousHand(Hand current, List<Hand> hands){
        return null;
    }
    private Hand getPreviousHand(List<Hand> hands, int indexToCheck){
        return null;
    }

    //TODO: maybe make this return a Hand and do currentPlayerHand = stay(hand) when we use it
    private void stay(Hand hand){
        tryNextPlayerHand(hand);
    }

    //TODO: like stay(), maybe make this return a Hand and do currentPlayerHand = bust(hand) when we use it
    private void bust(Hand hand){
        log("Bust!");
        hand.bust();

        tryNextPlayerHand(hand);
    }

    private boolean bustCheck(Hand hand){
        return hand.value() == -1;
    }

    //use it whenever we need another hand from stay bust etc
    private void tryNextPlayerHand(Hand hand){
        Hand nextHand = getNextHand(hand, playerHands, true);
        if (nextHand == null){
            dealerTurn(false);
        } else {
            currentPlayerHand = nextHand;
            updatePlayerInformation(currentPlayerHand);
            toggleSplitButton(allowSplit(currentPlayerHand));
        }
    }

    private void dealerTurn(boolean playerNatural){
        //TODO: consolidate revealFaceDown() calls
        if (numberOfLiveHands() > 0) {
            revealFaceDown(dealerHand);
            updateDealerInformation(dealerHand);
            if (!playerNatural) {
                while ((dealerHand.value() < 17 && dealerHand.value() != -1) || dealerHand.softSeventeen()) {
                    hitDealer(dealerHand);
                    updateDealerInformation(dealerHand);
                }
            }
        }
        //TODO: consolidate revealFaceDown() calls
        if (insurance > 0){
            revealFaceDown(dealerHand);
            updateDealerInformation(dealerHand);
            if (dealerHand.natural()){
                payInsurance();
            } else {
                log("lost your insurance");
            }
        }
        evaluateAllResults();
    }

    private int numberOfLiveHands(){
        int living = 0;
        for (Hand hand : playerHands){
            if (!hand.busted()){
                living++;
            }
        }
        return living;
    }

    private void revealFaceDown(Hand hand){
        hand.getCard(1).flip(true);
    }

    private void evaluateAllResults() {
        currentPlayerHand = getLastHand(playerHands, false);
        while (currentPlayerHand != null){
            if (!currentPlayerHand.busted()) {
                evaluateResult(currentPlayerHand);
            }
            currentPlayerHand = getNextHand(currentPlayerHand, playerHands, false);
        }
        finishGame();
    }

    private void evaluateResult(Hand hand){
        int dealerValue = dealerHand.value();
        int playerValue = hand.value();
        boolean dealerNatural = dealerHand.natural();
        boolean playerNatural = hand.natural();

        log(Integer.toString(playerValue)+" vs "+Integer.toString(dealerValue));

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
        boolean blackjack = hand.natural();
        int bet = hand.getBet();
        int roi = 0;
        String message;

        switch (result){
            case WIN:
                int winnings;
                if (blackjack){
                    winnings = (int) (bet*1.5);
                    message = "NATURAL; won "+Integer.toString(winnings);
                } else {
                    winnings = bet;
                    message = "WIN; won "+Integer.toString(winnings);
                }
                roi = bet + winnings;
                break;
            case PUSH:
                roi = bet;
                message = "PUSH; returning your "+Integer.toString(bet);
                break;
            case LOSS:
                message = "LOSS; lost your "+Integer.toString(bet);
                break;
            default:
                message = "";
        }
        increaseBank(roi);
        updateBankDisplay();
        log(message);
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
        dealerHand.hit(shoe.draw());
        currentPlayerHand.hit(shoe.draw());
        dealerHand.hit(shoe.draw(false));
        currentPlayerHand.hit(shoe.draw());
    }

    private Card draw(){
        return shoe.draw();
    }

    private Card drawFaceDown(){
        return shoe.draw(false);
    }

    private void hit(Hand hand){
        hand.hit(draw());
    }

    private void hitFaceDown(Hand hand){
        hand.hit(drawFaceDown());
    }

    private void hitPlayer(Hand hand) {
        hand.hit(shoe.draw());
        updatePlayerInformation(hand);
        int newValue = hand.value();
        if (newValue == -1){
            bust(hand);
        } else if (newValue == 21){
            log("hit 21");
            if (hand.natural()) {
                dealerTurn(true);
            } else {
                stay(hand);
            }
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

    private void play(){
        initialiseHands();
        updateBankDisplay();
        toggleBetButtons(false);

        deal();

        showGameButtons(true);
        updateDealerInformation(dealerHand);
        updatePlayerInformation(currentPlayerHand);

        if (currentPlayerHand.natural()){
            dealerTurn(true);
        } else {
            firstTurnOptionsCheck();
            toggleGameButtons(true);
        }
        //dont put things here
    }

    private void increaseBet(int amount){
        bet += amount;
        decreaseBank(amount);
        updateBetDisplay();
        updateBankDisplay();
        toggleBetButtons(true);
        togglePlayButton(true);
    }

    private void decreaseBet(int amount){

    }

    private void resetBet(){
        bet = 0;
        updateBetDisplay();
        toggleBetButtons(true);
    }

    private void finishGame(){
        resetBet();
        insurance = 0;
        if (shoe.penetrationCheck()) {
            initialiseShoe();
        }
        log("round over \n");
        showGameButtons(false);
        togglePlayButton(false);
        toggleBetButtons(true);
    }

    private void split(Hand hand){
        Hand master = hand;
        Hand branch = splitHand(master);

        decreaseBank(branch.getBet());
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

        updateEventLog();

        currentPlayerHand = getLastHand(playerHands, true);

        if (currentPlayerHand == null){
            dealerTurn(false);
        } else {
            updatePlayerInformation(currentPlayerHand);
            toggleSplitButton(allowSplit(currentPlayerHand));
        }
    }
    //only to be used by split()
    private Hand splitHand(Hand hand){
        int bet = hand.getBet();
        Card splitCard = hand.split();
        return new PlayHand(bet, splitCard);
    }

    private void doubleDown(Hand hand){
        int bet = hand.getBet();
        bank -= bet;
        hand.increaseBet(bet);
        log("doubled down, hand's bet is now "+2*bet);
        hand.hit(shoe.draw());
        updatePlayerInformation(hand);
        if (hand.value() == -1){
            bust(hand);
        } else {
            stay(hand);
        }
    }

    private void surrender(Hand hand){
        int recoup;
        recoup = (int)(hand.getBet()/2);
        increaseBank(recoup);
        finishGame();
    }

    private void firstTurnOptionsCheck(){
        toggleSplitButton(allowSplit(currentPlayerHand));
        toggleInsuranceButton(allowInsurance(currentPlayerHand));
        toggleSurrenderButton(true);
    }

    private void disableOptions(){
        toggleSplitButton(false);
        toggleInsuranceButton(false);
        toggleSurrenderButton(false);
    }

    private void toggleSurrenderButton(boolean enabled){
        surrenderButton.setEnabled(enabled);
    }

    private boolean allowInsurance(Hand hand){
        int max = (int)hand.getBet()/2;
        if (hand.count() > 2) {
            return false;
        }
        if (!canBet(max)) {
            return false;
        }
        return dealerHand.getCard(0).isAce();
    }

    private void toggleInsuranceButton(boolean enabled){
        insuranceButton.setEnabled(enabled);
    }

    private void insurance(Hand hand){
        int max = (int)hand.getBet()/2;
        decreaseBank(max);
        insurance = max;
        toggleInsuranceDisplay(true);
        log("took insurance of "+Integer.toString(insurance));
    }

    private void toggleInsuranceDisplay(boolean enabled){

    }

    private void payInsurance(){
        int payout = insurance*2;
        increaseBank(payout);
        toggleInsuranceDisplay(false);
        log("paid insurance of "+Integer.toString(payout));
    }

    private void toggleSplitButton(boolean enabled){
        splitButton.setEnabled(enabled);
    }

    private boolean allowSplit(Hand hand){
        if (hand.count() > 2) {
            return false;
        }
        if (!canBet(hand.getBet())) {
            return false;
        }
        return (cardValue(hand.getCard(0)) == cardValue(hand.getCard(1)));
    }

    private boolean canBet(int bet){
        return bank >= bet;
    }

    private int cardValue(Card card){
        return card.getValue();
    }

    private void increaseBank(int amount){
        bank += amount;
        updateBankDisplay();
    }

    private void decreaseBank(int amount){
        bank -= amount;
        updateBankDisplay();
    }

    private void updateBetDisplay(){
        betDisplay.setText(Integer.toString(bet));
    }

    private void updateBankDisplay(){
        bankDisplay.setText(Integer.toString(bank));
    }

    private void toggleGameButtons(boolean enabled){
        hitButton.setEnabled(enabled);
        stayButton.setEnabled(enabled);
    }

    private void toggleBetButtons(boolean enabled){
        if (!canBet(20)){
            betButton1.setEnabled(false);
            betButton2.setEnabled(false);
            betButton3.setEnabled(false);
        } else {
            if (canBet(20)) {
                betButton1.setEnabled(enabled);
            } else betButton1.setEnabled(false);
            if (canBet(50)) {
                betButton2.setEnabled(enabled);
            } else betButton2.setEnabled(false);
            if (canBet(100)) {
                betButton3.setEnabled(enabled);
            } else betButton3.setEnabled(false);
        }
    }

    private void togglePlayButton(boolean enabled){
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
