package dotbig.barebonesblackjack;

import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
    //private TextView playerHandDisplay;
    //private TextView playerHandDisplay2;
    //private TextView playerHandDisplay3;
    //private TextView playerHandDisplay4;

    private TextView dealerHandDisplay;
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

    List<HandDisplay> handDisplays;
    private ConstraintLayout handDisplayGroup1;
    private ConstraintLayout handDisplayGroup2;
    private ConstraintLayout handDisplayGroup3;
    private ConstraintLayout handDisplayGroup4;

    private enum Result{
        LOSS, PUSH, WIN, NATURAL
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
        bankDisplay = findViewById(R.id.textviewBank);
        //text displays
        //dealer hand information
        dealerHandDisplay = findViewById(R.id.textviewHandDealer);

        //player hand information
        initialiseHandDisplays();


        eventLog = findViewById(R.id.textviewEventLog);

        updateDealerInformation();
        updateBetButtonText();

    }

    private void initialiseBetValues(int betValue1, int betValue2, int betValue3) {
        //TODO: validate betValue1<betValue2<betValue3
        this.betValue1 = betValue1;
        this.betValue2 = betValue2;
        this.betValue3 = betValue3;
    }

    private void updateBetButtonText(){
        if (bet <= 0){
            betButton1.setText(String.format(Locale.ENGLISH, "Bet %d", betValue1));
            betButton2.setText(String.format(Locale.ENGLISH, "Bet %d", betValue2));
            betButton3.setText(String.format(Locale.ENGLISH, "Bet %d", betValue3));
        } else {
            betButton1.setText(String.format(Locale.ENGLISH, "Add %d", betValue1));
            betButton2.setText(String.format(Locale.ENGLISH, "Add %d", betValue2));
            betButton3.setText(String.format(Locale.ENGLISH, "Add %d", betValue3));
        }

    }

    private void initialiseHandDisplays(){
        handDisplayGroup1 = findViewById(R.id.handDisplayGroup1);
        handDisplayGroup2 = findViewById(R.id.handDisplayGroup2);
        handDisplayGroup3 = findViewById(R.id.handDisplayGroup3);
        handDisplayGroup4 = findViewById(R.id.handDisplayGroup4);

        handDisplays = new ArrayList<>();
        handDisplays.add(new HandDisplayGroup(handDisplayGroup1));
        handDisplays.add(new HandDisplayGroup(handDisplayGroup2));
        handDisplays.add(new HandDisplayGroup(handDisplayGroup3));
        handDisplays.add(new HandDisplayGroup(handDisplayGroup4));

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

        initialiseShoe();

        updatePlayButton();
        toggleBetButtons(true);
    }

    private void updatePlayerInformation(BlackjackHandPlayer hand){
        int numberOfHands = playerHands.size();
        for(int i=0; i<numberOfHands; i++){
            HandDisplay display = handDisplays.get(i);
            BlackjackHand currentHand = playerHands.get(i);

            display.setVisibility(View.VISIBLE);
            display.setCards(currentHand.toString());
        }

        if (hand != null) {
            int currentHandIndex = playerHands.indexOf(hand);
            for (int i = 0; i< handDisplays.size(); i++){
                if (currentHandIndex == i){
                    focusHandDisplay(handDisplays.get(i));
                } else {
                    unfocusHandDisplay(handDisplays.get(i));
                }
            }
        } else {
            focusAllHandDisplays();
        }
    }

    private void updateHandStatus(BlackjackHandPlayer hand){
        if (hand == null){
            return;
        }
        int currentIndex = playerHands.indexOf(hand);
        HandDisplay currentDisplay = handDisplays.get(currentIndex);

        currentDisplay.setStatus(String.format(Locale.ENGLISH, "%d", hand.getBet()));
    }

    private void updateHandStatus(BlackjackHandPlayer hand, Result result){
        if (hand == null){
            return;
        }
        int currentIndex = playerHands.indexOf(hand);
        HandDisplay currentDisplay = handDisplays.get(currentIndex);

        int net = calculateNetProfit(hand, result);
        String profit;
        if (net < 0) {
            profit = "-" + Integer.toString(Math.abs(net));
        } else if (net == 0){
            profit = Integer.toString(net);
        } else {
            profit = "+"+Integer.toString(net);
        }
        currentDisplay.setStatus(profit);
    }

    private int calculateNetProfit(BlackjackHandPlayer hand, Result result){
        int bet = hand.getBet();
        int net = 0;
        switch(result){
            case NATURAL:
                net = (int)(bet*1.5);
                break;
            case WIN:
                net = bet;
                break;
            case PUSH:
                break;
            case LOSS:
                net -= bet;
        }
        return net;
    }

    private int getHandIndex(BlackjackHand hand){
        return playerHands.indexOf(hand);
    }

    private void clearHandDisplays(){
        for (int i = 0; i< handDisplays.size(); i++){
            HandDisplay currentDisplay = handDisplays.get(i);
            currentDisplay.setCards("");
            currentDisplay.setStatus("");
            currentDisplay.setVisibility(View.GONE);
        }
    }

    private void unfocusHandDisplay(HandDisplay display){
        display.setAlpha((float)0.5);
    }

    private void focusHandDisplay(HandDisplay display){
        display.setAlpha((float)1.0);
    }

    private void unfocusAllHandDisplays(){
        for (HandDisplay h : handDisplays){
            h.setAlpha((float)0.5);
        }
    }

    private void focusAllHandDisplays(){
        for (HandDisplay h : handDisplays){
            h.setAlpha((float)1.0);
        }
    }

    private void updateDealerInformation(){
        if (dealerHand != null){
            dealerHandDisplay.setText(dealerHand.toString());
        } else {
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
        //log("Bust!\nLost your "+hand.getBet() +"\n");
        hand.bust();
        updateHandStatus(hand, Result.LOSS);
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
            log("Lost your insurance");
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

        //if (dealerValue == -1){
        //    log(Integer.toString(playerValue)+" vs bust");
        //} else {
        //    log(Integer.toString(playerValue)+" vs "+Integer.toString(dealerValue));
        //}

        if (playerNatural) {
            if (dealerNatural) {
                //log("Push");
                resolveHand(hand, Result.PUSH);
            } else {
                //log("Player natural");
                resolveHand(hand, Result.NATURAL);
            }
        } else if (dealerNatural) {
            //log("Dealer natural");
            resolveHand(hand, Result.LOSS);
        } else if (dealerValue == -1) {
            //log("Dealer bust!");
            resolveHand(hand, Result.WIN);
        } else if (dealerValue == playerValue){
            //log("Push");
            resolveHand(hand, Result.PUSH);
        } else if (dealerValue < playerValue){
            //log("Player wins");
            resolveHand(hand, Result.WIN);
        } else {
            //log("Dealer wins");
            resolveHand(hand, Result.LOSS);
        }
    }

    private void resolveHand(BlackjackHandPlayer hand, Result result){
        int bet = hand.getBet();
        int profit = calculateNetProfit(hand, result);
        int roi = 0;

        switch (result){
            case NATURAL:
                updateHandStatus(hand, result);
                roi = bet + profit;
                break;
            case WIN:
                updateHandStatus(hand, result);
                roi = bet + profit;
                break;
            case PUSH:
                updateHandStatus(hand, result);
                roi = bet;
                break;
            case LOSS:
                updateHandStatus(hand, result);
                break;
        }
        increaseBank(roi);
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
        updateHandStatus(currentPlayerHand);

        firstTurnOptionsCheck();
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
        updateBankDisplay();

        updatePlayButton();
        updateBetButtonText();
        toggleBetButtons(true);
    }

    private void updatePlayButton(){
        if (bet <= 0){
            playButton.setText("Place a bet!");
            togglePlayButton(false);
        } else {
            playButton.setText(String.format(Locale.ENGLISH, "Bet %d", bet));
            togglePlayButton(true);
        }
    }

    private void decreaseBet(int amount){

    }

    private void resetBet(){
        bet = 0;
        toggleBetButtons(true);
    }

    private void endRound(){
        resetBet();
        insurance = 0;
        if (shoe.penetrationCheck()) {
            initialiseShoe();
        }
        showGameButtons(false);

        updatePlayButton();
        updateBetButtonText();
        toggleBetButtons(true);
    }

    private void split(BlackjackHandPlayer hand){
        BlackjackHandPlayer master = hand;
        BlackjackHandPlayer branch = splitHand(master);
        decreaseBank(branch.getBet());
        playerHands.add(branch);

        hit(branch);
        hit(master);

        if (justHitTwentyOne(branch)){
            branch.stay();
        }
        if (justHitTwentyOne(master)){
            master.stay();
        }

        updateHandStatus(branch);
        updateHandStatus(master);

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
        updateBankDisplay();
        log("Doubled down\nAdded "+Integer.toString(bet)+" to bet");
        hit(hand);
        updatePlayerInformation(hand);
        updateHandStatus(hand);
        if (justBusted(hand)){
            bust(hand);
        } else {
            stay(hand);
        }
    }

    //TODO: make sure hand font size is reduced upon surrender
    private void surrender(BlackjackHandPlayer hand){
        int recoup;
        recoup = (int)(hand.getBet()/2);
        increaseBank(recoup);
        log("Surrendered\nReturned "+Integer.toString(recoup) +"\n");
        if (insuranceTaken()){
            revealHoleCard();
            resolveInsurance();
        }
        endRound();
    }

    private void firstTurnOptionsCheck(){
        toggleSplitButton(allowSplit(currentPlayerHand));
        toggleInsuranceButton(allowInsurance(currentPlayerHand));
        toggleDoubleDownButton(allowDoubleDown(currentPlayerHand));
        toggleSurrenderButton(true);
    }

    private boolean allowDoubleDown(BlackjackHandPlayer hand){
        return bank >= hand.getBet();
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
