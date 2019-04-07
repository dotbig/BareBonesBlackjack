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
import java.util.Locale;

//TODO: replace setText(Integer.toString...) with setText(String.format("%d",value));

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
    private TextView playerHandDisplay2;
    private TextView playerHandDisplay3;
    private TextView playerHandDisplay4;

    private TextView playerValueDisplay;
    private TextView dealerHandDisplay;
    private TextView dealerValueDisplay;
    private TextView gameResult;
    private TextView betDisplay;
    private TextView bankDisplay;

    private TextView eventLog;
    private StringBuilder events;

    private ShoeBlackjack shoe;
    private int shoeSize;
    private int penetration;
    //TODO: cards remaining counter

    private int bank;
    private int bet;
    private int insurance;

    private int betValue1;
    private int betValue2;
    private int betValue3;

    private List<BlackjackHandPlayer> playerHands;
    private BlackjackHandPlayer currentPlayerHand;
    private BlackjackHandDealer dealerHand;

    private List<TextView> handDisplays;

    private enum Result{
        LOSS, PUSH, WIN
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        Bundle gameBundle = getIntent().getExtras();
        if (gameBundle != null){
            shoeSize = gameBundle.getInt("shoeSize");
            penetration = gameBundle.getInt("penetration");
        } else {
            shoeSize = 4;
            penetration = 90;
        }

        System.out.println(shoeSize +"|"+penetration);

        initialiseBetValues(20, 50, 100);
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

        initialiseHandDisplays();

        //result of the game
        gameResult = findViewById(R.id.textviewResult);

        eventLog = findViewById(R.id.textviewEventLog);

        updateDealerInformation();
        updateBetValueDisplays();
    }

    private void initialiseBetValues(int betValue1, int betValue2, int betValue3) {
        //TODO: validate betValue1<betValue2<betValue3
        this.betValue1 = betValue1;
        this.betValue2 = betValue2;
        this.betValue3 = betValue3;
    }

    private void updateBetValueDisplays(){
        betButton1.setText(String.format(Locale.ENGLISH, "%d", betValue1));
        betButton2.setText(String.format(Locale.ENGLISH, "%d", betValue1));
        betButton3.setText(String.format(Locale.ENGLISH, "%d", betValue1));
    }


    private void initialiseHandDisplays(){
        playerHandDisplay = findViewById(R.id.textviewHandPlayer);
        playerHandDisplay2 = findViewById(R.id.textviewHandPlayer2);
        playerHandDisplay3 = findViewById(R.id.textviewHandPlayer3);
        playerHandDisplay4 = findViewById(R.id.textviewHandPlayer4);

        handDisplays = new ArrayList<>();
        handDisplays.add(playerHandDisplay);
        handDisplays.add(playerHandDisplay2);
        handDisplays.add(playerHandDisplay3);
        handDisplays.add(playerHandDisplay4);
        clearHandDisplays();
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
                disableOptions();
                doubleDown(currentPlayerHand);
                break;
            case (R.id.buttonSplit):
                disableOptions();
                split(currentPlayerHand);
                break;
            case (R.id.buttonInsurance):
                toggleInsuranceButton(false);
                insurance(currentPlayerHand);
                if (allowEvenMoney(currentPlayerHand)){
                    stay(currentPlayerHand);
                }
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

    private void updatePlayerInformation(BlackjackHand hand){
        int numberOfHands = playerHands.size();
        for(int i=0; i<numberOfHands; i++){
            TextView currentDisplay = handDisplays.get(i);
            BlackjackHand currentHand = playerHands.get(i);

            currentDisplay.setText(currentHand.toString());
            currentDisplay.setVisibility(View.VISIBLE);
        }

        if (hand != null) {
            int currentHandIndex = playerHands.indexOf(hand);
            for (int i=0; i<handDisplays.size(); i++){
                TextView currentDisplay = handDisplays.get(i);
                if (currentHandIndex == i){
                    maximiseHandDisplay(currentDisplay);
                } else {
                    minimiseHandDisplay(currentDisplay);
                }
            }
        } else {
            minimiseAllHandDisplays();
        }
    }

    private void clearHandDisplays(){
        for (int i=0; i<handDisplays.size(); i++){
            TextView currentDisplay = handDisplays.get(i);
            currentDisplay.setText("");
            currentDisplay.setVisibility(View.GONE);
        }
    }

    private void minimiseHandDisplay(TextView handDisplay){
        handDisplay.setTextSize(22);
    }

    private void maximiseHandDisplay(TextView handDisplay){
        handDisplay.setTextSize(28);
    }

    private void minimiseAllHandDisplays(){
        for (int i=0; i<handDisplays.size(); i++){
            minimiseHandDisplay(handDisplays.get(i));
        }
    }

    private void updateDealerInformation(){
        if (dealerHand != null){
            dealerHandDisplay.setText(dealerHand.toString());
            dealerValueDisplay.setText(Integer.toString(dealerHand.value()));
        } else {
            dealerValueDisplay.setText("");
            dealerHandDisplay.setText("");
        }
    }

    private BlackjackHandPlayer getLastHand(List<BlackjackHandPlayer> hands, boolean active){
        BlackjackHandPlayer current;
        current = hands.get(hands.size()-1);
        if (active){
            if (current.busted() ||  current.stayed()){
                current = getNextHand(current, hands, true);
            }
        }
        return current;
    }

    private BlackjackHandPlayer getNextHand(BlackjackHandPlayer current, List<BlackjackHandPlayer> hands, boolean active){
        if (current == null){
            return null;
        }
        int currentIndex = hands.indexOf(current);
        if (active){
            return nextHand(hands, currentIndex-1, true);
        }
        return nextHand(hands, currentIndex-1, false);
    }

    private BlackjackHandPlayer nextHand(List<BlackjackHandPlayer> hands, int indexToCheck, boolean active){
        System.out.println("nextHand, index: " +Integer.toString(indexToCheck));
        if (indexToCheck < 0){
            return null;
        }
        BlackjackHandPlayer hand = hands.get(indexToCheck);
        if (active){
            if (hand.busted() || hand.stayed()){
                return nextHand(hands, indexToCheck-1, true);
            }
        }
        return hand;
    }

    //TODO: previous will be needed once we implement the ability to select which hand we're viewing
    private BlackjackHand previousHand(BlackjackHand current, List<BlackjackHand> hands){
        return null;
    }
    private BlackjackHand getPreviousHand(List<BlackjackHand> hands, int indexToCheck){
        return null;
    }

    private void stay(BlackjackHandPlayer hand){
        hand.stay();
        tryNextPlayerHand(hand);
    }

    private void bust(BlackjackHandPlayer hand){
        log("Bust!\nLost your "+hand.getBet() +"\n");
        hand.bust();
        tryNextPlayerHand(hand);
    }

    private boolean justBusted(BlackjackHand hand){
        return hand.value() == -1;
    }

    private boolean justHitTwentyOne(BlackjackHand hand){ return hand.value() == 21;}

    //use it whenever we need another hand from stay bust etc
    private void tryNextPlayerHand(BlackjackHandPlayer hand){
        BlackjackHandPlayer nextHand = getNextHand(hand, playerHands, true);
        updatePlayerInformation(nextHand);
        if (nextHand == null){
            dealerTurn(natural(hand));
        } else {
            currentPlayerHand = nextHand;
            toggleSplitButton(allowSplit(currentPlayerHand));
        }
    }

    private boolean natural(BlackjackHand hand){
        return hand.natural();
    }

    private boolean possibleDealerNatural(){
        return dealerUpCardIsAce() || dealerUpCardIsTenValued();
    }

    private boolean dealerUpCardIsAce(){
        return getUpCard().getValue() == 1;
    }

    private boolean dealerUpCardIsTenValued(){
        return getUpCard().getValue() == 10;
    }

    private boolean insuranceTaken(){
        return insurance > 0;
    }

    private boolean shouldReveal(boolean playerNatural){
        //if player has natural and the dealer can't have natural, don't bother revealing
        //if dealer can't have natural then it rules out the possibility that player took insurance
        if (playerNatural && !possibleDealerNatural()){
            return false;
        } else if (!insuranceTaken() && !playerHasLiveHands()){
        //card should be revealed if player took insurance so they can see if they should get paid
        //card should be revealed if player has hands left in play
            return false;
        } else {
            return true;
        }
    }

    private void completeDealerHand(){
        while ((dealerHand.value() < 17 && dealerHand.value() != -1) || dealerHand.softSeventeen()) {
            hitDealer(dealerHand);
        }
    }

    private void resolveInsurance(){
        if (natural(dealerHand)) {
            payInsurance();
        } else {
            log("lost your insurance");
        }
    }

    private void dealerTurn(boolean playerNatural){
        //decide whether or not to reveal the face down card
        if (shouldReveal(playerNatural)){
            revealHoleCard();
        }
        //if the player has hands still in play and doesn't have natural, dealer draws til 17
        if (playerHasLiveHands()) {
            if (!playerNatural) {
                completeDealerHand();
            }
        }
        //if player has taken insurance, resolve the outcome of that bet
        if (insuranceTaken()) {
            resolveInsurance();
        }
        evaluateAllResults();
    }

    private boolean playerHasLiveHands(){
        int living = 0;
        for (BlackjackHand hand : playerHands){
            if (!hand.busted()){
                living++;
            }
        }
        return living > 0;
    }

    private void revealHoleCard(){
        dealerHand.getCard(1).flip(true);
        updateDealerInformation();
    }

    private BlackjackCard getUpCard(){
        return dealerHand.getCard(0);
    }

    private void evaluateAllResults() {
        currentPlayerHand = getLastHand(playerHands, false);
        while (currentPlayerHand != null){
            if (!currentPlayerHand.busted()) {
                evaluateResult(currentPlayerHand);
            }
            currentPlayerHand = getNextHand(currentPlayerHand, playerHands, false);
        }
        endRound();
    }

    private void evaluateResult(BlackjackHandPlayer hand){
        int dealerValue = dealerHand.value();
        int playerValue = hand.value();
        boolean dealerNatural = dealerHand.natural();
        boolean playerNatural = hand.natural();

        if (dealerValue == -1){
            log(Integer.toString(playerValue)+" vs bust");
        } else {
            log(Integer.toString(playerValue)+" vs "+Integer.toString(dealerValue));
        }

        if (playerNatural) {
            if (dealerNatural) {
                gameResult.setText("Push");
                log("Push");
                resolveHand(hand, Result.PUSH);
            } else {
                gameResult.setText("Player natural");
                log("Player natural");
                resolveHand(hand, Result.WIN);
            }
        } else if (dealerNatural) {
            gameResult.setText("Dealer natural");
            log("Dealer natural");
            resolveHand(hand, Result.LOSS);
        } else if (dealerValue == -1) {
            gameResult.setText("Dealer bust!");
            log("Dealer bust!");
            resolveHand(hand, Result.WIN);
        } else if (dealerValue == playerValue){
            gameResult.setText("Push");
            log("Push");
            resolveHand(hand, Result.PUSH);
        } else if (dealerValue < playerValue){
            gameResult.setText("Player win");
            log("Player wins");
            resolveHand(hand, Result.WIN);
        } else {
            gameResult.setText("Dealer win");
            log("Dealer wins");
            resolveHand(hand, Result.LOSS);
        }
    }

    private void resolveHand(BlackjackHandPlayer hand, Result result){
        boolean blackjack = hand.natural();
        int bet = hand.getBet();
        int roi = 0;
        String message;

        switch (result){
            case WIN:
                int winnings;
                if (blackjack){
                    winnings = (int) (bet*1.5);
                    message = "Won "+Integer.toString(winnings) +"\n";
                } else {
                    winnings = bet;
                    message = "Won "+Integer.toString(winnings)+"\n";
                }
                roi = bet + winnings;
                break;
            case PUSH:
                roi = bet;
                message = "Returning your "+Integer.toString(bet)+"\n";
                break;
            case LOSS:
                message = "Lost your "+Integer.toString(bet)+"\n";
                break;
            default:
                message = "\n";
        }
        increaseBank(roi);
        log(message);
    }

    private void initialiseShoe(){
        shoe = new ShoeBlackjack();
        for (int i = 0; i < shoeSize; i++){
            shoe.addDeck(new DeckBlackjack());
        }
        shoe.setPenetration(penetration);
        shoe.shuffle();
    }

    private void initialiseHands(){
        dealerHand = new HandDealer();
        playerHands = new ArrayList<>();
        BlackjackHandPlayer newHand = new HandPlayer(bet);
        playerHands.add(newHand);
        currentPlayerHand = playerHands.get(0);
    }

    private void deal(){
        hit(dealerHand);
        hit(currentPlayerHand);
        hitFaceDown(dealerHand);
        hit(currentPlayerHand);
    }

    //deal predetermined cards for testing
    private void dealTest(){
        BlackjackCard dealerCard1 = new CardBlackjack(0,0);
        BlackjackCard dealerCard2 = new CardBlackjack(0,10);
        dealerCard2.flip(false);

        BlackjackCard playerCard1 = new CardBlackjack(0,0);
        BlackjackCard playerCard2 = new CardBlackjack(0,10);

        dealerHand.add(dealerCard1);
        currentPlayerHand.add(playerCard1);
        dealerHand.add(dealerCard2);
        currentPlayerHand.add(playerCard2);
    }

    private void hit(BlackjackHand hand){
        hand.add(shoe.draw());
    }

    private void hitFaceDown(BlackjackHand hand){
        hand.add(shoe.draw(false));
    }

    private void hitPlayer(BlackjackHandPlayer hand) {
        hit(hand);
        updatePlayerInformation(hand);
        if (justBusted(hand)){
            bust(hand);
        } else if (justHitTwentyOne(hand)){
            stay(hand);
        }
    }

    private void hitDealer(BlackjackHandDealer hand){
        hit(hand);
        updateDealerInformation();
    }

    private void play(){
        initialiseHands();
        updateBankDisplay();
        toggleBetButtons(false);

        clearHandDisplays();
        //clear events
        events.setLength(0);
        updateEventLog();

        deal();
        //dealTest();

        showGameButtons(true);
        updateDealerInformation();
        updatePlayerInformation(currentPlayerHand);

        firstTurnOptionsCheck();
        //TODO: if natural(player), allow take even money
        if (natural(currentPlayerHand)){
            if (allowEvenMoney(currentPlayerHand)) {
                activateEvenMoney(currentPlayerHand);
            } else {
                dealerTurn(true);
            }
        } else {
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

    private void endRound(){
        resetBet();
        insurance = 0;
        if (shoe.penetrationCheck()) {
            initialiseShoe();
        }

        minimiseAllHandDisplays();

        showGameButtons(false);
        togglePlayButton(false);
        toggleBetButtons(true);
    }

    private void split(BlackjackHandPlayer hand){
        BlackjackHandPlayer master = hand;
        BlackjackHandPlayer branch = splitHand(master);
        decreaseBank(branch.getBet());
        playerHands.add(branch);

        hit(branch);
        hit(master);

        if (justBusted(branch)) {
            branch.bust();
        } else if (justHitTwentyOne(branch)){
            branch.stay();
        }
        if (justBusted(master)){
            master.bust();
        } else if (justHitTwentyOne(master)){
            master.stay();
        }

        updateEventLog();
        currentPlayerHand = getLastHand(playerHands, true);
        updatePlayerInformation(currentPlayerHand);
        if (currentPlayerHand == null){
            dealerTurn(false);
        } else {
            toggleSplitButton(allowSplit(currentPlayerHand));
        }
    }
    //only to be used by split()
    private BlackjackHandPlayer splitHand(BlackjackHandPlayer hand){
        int bet = hand.getBet();
        BlackjackCard splitCard = hand.split();
        return new HandPlayer(bet, splitCard);
    }

    private void doubleDown(BlackjackHandPlayer hand){
        int bet = hand.getBet();
        bank -= bet;
        hand.increaseBet(bet);
        log("Doubled down");
        hit(hand);
        updatePlayerInformation(hand);
        if (justBusted(hand)){
            bust(hand);
        } else {
            stay(hand);
        }
    }

    //TODO: make sure hand font size is reduced upon surrender
    private void surrender(BlackjackHandPlayer hand){
        int recoup;
        //hand.stayed();
        recoup = (int)(hand.getBet()/2);
        increaseBank(recoup);
        log("Returned "+Integer.toString(recoup) +"\n");
        if (insuranceTaken()){
            revealHoleCard();
            resolveInsurance();
        }
        endRound();
    }

    private void firstTurnOptionsCheck(){
        toggleSplitButton(allowSplit(currentPlayerHand));
        toggleInsuranceButton(allowInsurance(currentPlayerHand));
        toggleDoubleDownButton(true);
        toggleSurrenderButton(true);
    }

    private void toggleDoubleDownButton(boolean enabled){
        doubleDownButton.setEnabled(enabled);
    }

    private void disableOptions(){
        toggleSplitButton(false);
        toggleInsuranceButton(false);
        toggleSurrenderButton(false);
        toggleDoubleDownButton(false);
    }

    private void toggleSurrenderButton(boolean enabled){
        surrenderButton.setEnabled(enabled);
    }

    private void activateEvenMoney(BlackjackHandPlayer hand){
        toggleHitButton(false);
        toggleDoubleDownButton(false);
        toggleSurrenderButton(false);
        toggleInsuranceButton(allowInsurance(hand));
    }

    private boolean allowEvenMoney(BlackjackHandPlayer hand){
        return natural(hand) && dealerUpCardIsAce();
    }

    private boolean allowInsurance(BlackjackHandPlayer hand){
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

    private void insurance(BlackjackHandPlayer hand){
        int max = (int)hand.getBet()/2;
        decreaseBank(max);
        insurance = max;
        toggleInsuranceDisplay(true);
        log("Insured "+Integer.toString(insurance));
    }

    private void toggleInsuranceDisplay(boolean enabled){

    }

    private void payInsurance(){
        int stake = insurance;
        int winnings = insurance*2;
        int payout = stake + winnings;
        increaseBank(payout);
        toggleInsuranceDisplay(false);
        log("Insurance paid "+Integer.toString(payout));
    }

    private void toggleSplitButton(boolean enabled){
        splitButton.setEnabled(enabled);
    }

    private boolean allowSplit(BlackjackHandPlayer hand){
        if (hand.count() > 2) {
            return false;
        }
        if (!canBet(hand.getBet())) {
            return false;
        }
        if (playerHands.size() >= 4){
            return false;
        }
        return (cardValue(hand.getCard(0)) == cardValue(hand.getCard(1)));
    }

    private boolean canBet(int bet){
        return bank >= bet;
    }

    private int cardValue(BlackjackCard card){
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

    private void toggleHitButton(boolean enabled){
        hitButton.setEnabled(enabled);
    }

    private void toggleBetButtons(boolean enabled){
        if (!canBet(betValue1)){
            betButton1.setEnabled(false);
            betButton2.setEnabled(false);
            betButton3.setEnabled(false);
        } else {
            if (canBet(betValue1)) {
                betButton1.setEnabled(enabled);
            } else betButton1.setEnabled(false);
            if (canBet(betValue2)) {
                betButton2.setEnabled(enabled);
            } else betButton2.setEnabled(false);
            if (canBet(betValue3)) {
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
