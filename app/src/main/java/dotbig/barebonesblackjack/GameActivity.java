package dotbig.barebonesblackjack;

import android.support.constraint.ConstraintLayout;
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

//TODO: replace setText(Integer.string...) with setText(String.format("%d",value));

public class GameActivity extends AppCompatActivity implements OnClickListener {

    final boolean ACTIVE = true;
    final boolean INACTIVE = false;
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

    private TextView dealerHandDisplay;


    private TextView eventLog;
    private StringBuilder events;

    private ShoeBlackjack shoe;
    private int shoeSize;
    private int penetration;
    //TODO: cards remaining counter

    private int bank;
    private int bet;
    private int insurance;

    private ViewGroupWrapper bankDisplay;
    private ViewGroupWrapper insuranceDisplay;
    private ViewGroupWrapper cardsRemainingDisplay;

    private int betValue1;
    private int betValue2;
    private int betValue3;

    private List<PlayerSpecific> playerHands;
    private PlayerSpecific currentPlayerHand;
    private DealerSpecific dealerHand;

    List<ViewGroupWrapper> handGroups;

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

        events = new StringBuilder();
        initialiseBetValues(20, 50, 100);
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

        //dealer hand information
        dealerHandDisplay = findViewById(R.id.textviewHandDealer);

        bankDisplay = new ViewGroupWrapper((LinearLayout)findViewById(R.id.bankLayout));
        cardsRemainingDisplay = new ViewGroupWrapper((LinearLayout)findViewById(R.id.remainingLayout));
        insuranceDisplay = new ViewGroupWrapper((LinearLayout)findViewById(R.id.insuranceLayout));

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
            betButton1.setText(String.format(Locale.ENGLISH, "Bet $%d", betValue1));
            betButton2.setText(String.format(Locale.ENGLISH, "Bet $%d", betValue2));
            betButton3.setText(String.format(Locale.ENGLISH, "Bet $%d", betValue3));
        } else {
            betButton1.setText(String.format(Locale.ENGLISH, "Add $%d", betValue1));
            betButton2.setText(String.format(Locale.ENGLISH, "Add $%d", betValue2));
            betButton3.setText(String.format(Locale.ENGLISH, "Add $%d", betValue3));
        }
    }

    private void initialiseHandDisplays(){
        ConstraintLayout handGroup1 = findViewById(R.id.handDisplayGroup1);
        ConstraintLayout handGroup2 = findViewById(R.id.handDisplayGroup2);
        ConstraintLayout handGroup3 = findViewById(R.id.handDisplayGroup3);
        ConstraintLayout handGroup4 = findViewById(R.id.handDisplayGroup4);

        handGroups = new ArrayList<>();
        handGroups.add(new ViewGroupWrapper(handGroup1));
        handGroups.add(new ViewGroupWrapper(handGroup2));
        handGroups.add(new ViewGroupWrapper(handGroup3));
        handGroups.add(new ViewGroupWrapper(handGroup4));

        clearHandDisplays();
    }

    public void onClick(View v){
        switch(v.getId()){
            case (R.id.buttonReturnToMain):
                finish();
                break;
            case (R.id.buttonHit):
                disableFirstTurnOptions();
                hitPlayer(currentPlayerHand);
                break;
            case (R.id.buttonStay):
                disableFirstTurnOptions();
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
                disableFirstTurnOptions();
                doubleDown(currentPlayerHand);
                break;
            case (R.id.buttonSplit):
                disableFirstTurnOptions();
                split(currentPlayerHand);
                break;
            case (R.id.buttonInsurance):
                toggleButton(insuranceButton, false);
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
        tryToggleBetButtons(true);
        toggleInsuranceDisplay(false);
    }

    private void updatePlayerInformation(PlayerSpecific hand){
        int numberOfHands = playerHands.size();
        for(int i=0; i<numberOfHands; i++){
            ViewGroupWrapper currentGroup = handGroups.get(i);
            Hand currentHand = playerHands.get(i);

            show(currentGroup);
            setText(currentGroup, "cards", currentHand.string());
        }

        if (hand != null) {
            int currentHandIndex = playerHands.indexOf(hand);
            for (int i = 0; i< handGroups.size(); i++){
                if (currentHandIndex == i){
                    colourIn(handGroups.get(i));
                } else {
                    greyOut(handGroups.get(i));
                }
            }
        } else {
            focusAllHandDisplays();
        }
    }

    private void updateHandStatus(PlayerSpecific hand){
        if (hand == null){
            return;
        }
        int currentIndex = playerHands.indexOf(hand);
        TextSettable currentGroup = handGroups.get(currentIndex);

        String status = String.format(Locale.ENGLISH, "$%d", hand.getBet());
        currentGroup.setText("status", status);
    }

    private void updateHandStatus(PlayerSpecific hand, Result result){
        if (hand == null){
            return;
        }
        int currentIndex = playerHands.indexOf(hand);
        TextSettable group = handGroups.get(currentIndex);

        int net = calculateNetProfit(hand, result);
        String profit;
        if (net < 0) {
            profit = "-$" + Integer.toString(Math.abs(net));
        } else if (net == 0){
            //profit = Integer.toString(net);
            profit = "Push";
        } else {
            profit = "+$"+Integer.toString(net);
        }
        group.setText("status", profit);
    }

    private int calculateNetProfit(PlayerSpecific hand, Result result){
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

    private int getHandIndex(PlayerSpecific hand){
        return playerHands.indexOf(hand);
    }

    private void clearHandDisplays(){
        for (int i = 0; i< handGroups.size(); i++){
            ViewGroupWrapper currentGroup = handGroups.get(i);
            setText(currentGroup, "cards", "");
            setText(currentGroup, "status", "");
            hide(currentGroup);
        }
    }

    private void focusAllHandDisplays(){
        for (AlphaSettable h : handGroups){
            h.setAlpha((float)1.0);
        }
    }

    private void updateDealerInformation(){
        if (dealerHand != null){
            dealerHandDisplay.setText(dealerHand.string());
        } else {
            dealerHandDisplay.setText("");
        }
    }

    private PlayerSpecific getLastHand(boolean active){
        PlayerSpecific current;
        current = playerHands.get(playerHands.size()-1);
        if (active){
            if (current.busted() ||  current.stayed()){
                current = getNextHand(current, ACTIVE);
            }
        }
        return current;
    }

    private PlayerSpecific getNextHand(PlayerSpecific current, boolean active){
        if (current == null){
            return null;
        }
        int currentIndex = getHandIndex(current);
        if (active){
            return nextHand(currentIndex-1, ACTIVE);
        }
        return nextHand(currentIndex-1, INACTIVE);
    }

    private PlayerSpecific nextHand(int indexToCheck, boolean active){
        System.out.println("nextHand, index: " +Integer.toString(indexToCheck));
        if (indexToCheck < 0){
            return null;
        }
        PlayerSpecific hand = playerHands.get(indexToCheck);
        if (active){
            if (hand.busted() || hand.stayed()){
                return nextHand(indexToCheck-1, ACTIVE);
            }
        }
        return hand;
    }

    //TODO: previous will be needed once we implement the ability to select which hand we're viewing
    private Hand previousHand(Hand current, List<Hand> hands){
        return null;
    }
    private Hand getPreviousHand(List<Hand> hands, int indexToCheck){
        return null;
    }

    private void stay(PlayerSpecific hand){
        hand.stay();
        tryNextPlayerHand(hand);
    }

    private void bust(PlayerSpecific hand){
        hand.bust();
        updateHandStatus(hand, Result.LOSS);
        tryNextPlayerHand(hand);
    }

    //use it whenever we need another hand from stay bust etc
    private void tryNextPlayerHand(PlayerSpecific hand){
        PlayerSpecific nextHand = getNextHand(hand, ACTIVE);
        updatePlayerInformation(nextHand);
        if (nextHand == null){
            dealerTurn(natural(hand));
        } else {
            currentPlayerHand = nextHand;
            toggleButton(splitButton, allowSplit(currentPlayerHand));
        }
    }

    private boolean natural(Hand hand){
        return hand.natural();
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

    //dealer draws cards until hard 17
    private void completeDealerHand(){
        while ((dealerHand.value() < 17 && dealerHand.value() != -1) || dealerHand.softSeventeen()) {
            hitDealer(dealerHand);
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
        for (Hand hand : playerHands){
            if (!hand.busted()){
                living++;
            }
        }
        return living > 0;
    }

    private void revealHoleCard(){
        dealerHand.reveal();
        updateDealerInformation();
    }

    private boolean possibleDealerNatural(){
        return dealerUpCardIsAce() || dealerUpCardIsTenValued();
    }

    private boolean dealerUpCardIsAce(){
        return value(getUpCard()) == 1;
    }

    private boolean dealerUpCardIsTenValued(){
        return value(getUpCard()) == 10;
    }

    private BlackjackCard getUpCard(){
        return dealerHand.getUpCard();
    }

    private void evaluateAllResults() {
        currentPlayerHand = getLastHand(INACTIVE);
        while (currentPlayerHand != null){
            if (!currentPlayerHand.busted()) {
                evaluateResult(currentPlayerHand);
            }
            currentPlayerHand = getNextHand(currentPlayerHand, INACTIVE);
        }
        endRound();
    }

    private void evaluateResult(PlayerSpecific hand){
        int dealerValue = dealerHand.value();
        int playerValue = hand.value();
        boolean dealerNatural = dealerHand.natural();
        boolean playerNatural = hand.natural();

        if (playerNatural) {
            if (dealerNatural) {
                resolveHand(hand, Result.PUSH);
            } else {
                resolveHand(hand, Result.NATURAL);
            }
        } else if (dealerNatural) {
            resolveHand(hand, Result.LOSS);
        } else if (dealerValue == -1) {
            resolveHand(hand, Result.WIN);
        } else if (dealerValue == playerValue){
            resolveHand(hand, Result.PUSH);
        } else if (dealerValue < playerValue){
            resolveHand(hand, Result.WIN);
        } else {
            resolveHand(hand, Result.LOSS);
        }
    }

    private void resolveHand(PlayerSpecific hand, Result result){
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
        updateCardsRemainingDisplay();
    }

    private void initialiseHands(){
        dealerHand = new HandDealer();
        playerHands = new ArrayList<>();
        PlayerSpecific newHand = new HandPlayer(bet);
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

    private void hit(Hand hand){
        hand.add(shoe.draw());
        updateCardsRemainingDisplay();
    }

    private void hitFaceDown(Hand hand){
        hand.add(shoe.draw(false));
        updateCardsRemainingDisplay();
    }

    private void hitPlayer(PlayerSpecific hand) {
        hit(hand);
        updatePlayerInformation(hand);
        if (busted(hand)){
            bust(hand);
        } else if (twentyOne(hand)){
            stay(hand);
        }
    }

    private boolean busted(PlayerSpecific hand){
        return hand.busted() || hand.value() == -1;
    }

    private boolean twentyOne(PlayerSpecific hand){
        return hand.value() == 21;
    }

    private void hitDealer(DealerSpecific hand){
        hit(hand);
        updateDealerInformation();
    }

    private void play(){
        initialiseHands();
        updateBankDisplay();
        tryToggleBetButtons(false);

        clearHandDisplays();
        toggleInsuranceDisplay(false);
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
                offerEvenMoney(currentPlayerHand);
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
        tryToggleBetButtons(true);
    }

    private void updatePlayButton(){
        if (bet <= 0){
            playButton.setText("Place a bet!");
            tryTogglePlayButton(false);
        } else {
            playButton.setText(String.format(Locale.ENGLISH, "Bet $%d", bet));
            tryTogglePlayButton(true);
        }
    }

    private void decreaseBet(int amount){

    }

    private void resetBet(){
        bet = 0;
        tryToggleBetButtons(true);
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
        tryToggleBetButtons(true);
    }

    private void split(PlayerSpecific hand){
        PlayerSpecific master = hand;
        PlayerSpecific branch = splitHand(master);
        decreaseBank(branch.getBet());
        playerHands.add(branch);

        hit(branch);
        hit(master);

        if (twentyOne(branch)){
            branch.stay();
        }
        if (twentyOne(master)){
            master.stay();
        }

        updateHandStatus(branch);
        updateHandStatus(master);

        updateEventLog();
        currentPlayerHand = getLastHand(ACTIVE);
        updatePlayerInformation(currentPlayerHand);
        if (currentPlayerHand == null){
            dealerTurn(false);
        } else {
            toggleButton(splitButton, allowSplit(currentPlayerHand));
        }
    }
    //only to be used by split()
    private PlayerSpecific splitHand(PlayerSpecific hand){
        int bet = hand.getBet();
        BlackjackCard splitCard = hand.split();
        return new HandPlayer(bet, splitCard);
    }

    private void doubleDown(PlayerSpecific hand){
        int bet = hand.getBet();
        bank -= bet;
        hand.increaseBet(bet);
        updateBankDisplay();
        log("Doubled down\nAdded $"+Integer.toString(bet)+" to bet");
        hit(hand);
        updatePlayerInformation(hand);
        updateHandStatus(hand);
        if (busted(hand)){
            bust(hand);
        } else {
            stay(hand);
        }
    }

    private void surrender(PlayerSpecific hand){
        int recoup;
        recoup = (int)(hand.getBet()/2);
        increaseBank(recoup);
        log("Surrendered\nReturned $"+Integer.toString(recoup) +"\n");
        if (insuranceTaken()){
            revealHoleCard();
            resolveInsurance();
        }
        endRound();
    }

    private void firstTurnOptionsCheck(){
        toggleButton(doubleDownButton, allowDoubleDown(currentPlayerHand));
        toggleButton(splitButton, allowSplit(currentPlayerHand));
        toggleButton(insuranceButton, allowInsurance(currentPlayerHand));
        toggleButton(surrenderButton, true);
    }

    private void disableFirstTurnOptions(){
        toggleButton(doubleDownButton, false);
        toggleButton(splitButton, false);
        toggleButton(insuranceButton, false);
        toggleButton(surrenderButton, false);
    }
    private boolean allowDoubleDown(PlayerSpecific hand){
        return bank >= hand.getBet();
    }

    private boolean allowEvenMoney(PlayerSpecific hand){
        return natural(hand) && dealerUpCardIsAce();
    }

    private void offerEvenMoney(PlayerSpecific hand){
        toggleButton(hitButton, false);
        toggleButton(doubleDownButton, false);
        toggleButton(insuranceButton, allowInsurance(hand));
        toggleButton(surrenderButton, false);
    }

    private boolean allowInsurance(PlayerSpecific hand){
        int max = (int)hand.getBet()/2;
        if (count(hand) > 2) {
            return false;
        }
        if (!canBet(max)) {
            return false;
        }
        return dealerUpCardIsAce();
    }

    private void insurance(PlayerSpecific hand){
        int max = hand.getBet()/2;
        decreaseBank(max);
        insurance = max;
        toggleInsuranceDisplay(true);
        updateInsuranceStake();
    }

    private void toggleInsuranceDisplay(boolean enabled){
        if (enabled){
            show(insuranceDisplay);
        } else {
            hide(insuranceDisplay);
        }
    }

    private void updateInsuranceStake(){
        setText(insuranceDisplay, "value", String.format(Locale.ENGLISH, "$%d", insurance));
    }

    private void updateInsuranceOutcome(int net){
        if (net > 0){
            setText(insuranceDisplay, "value", String.format(Locale.ENGLISH, "+$%d", net));
        } else {
            setText(insuranceDisplay, "value", String.format(Locale.ENGLISH, "$%d", net));
        }
    }

    private boolean insuranceShouldPay(){
        return natural(dealerHand);
    }

    private void resolveInsurance(){
        if (insuranceShouldPay()) {
            payInsurance();
            updateInsuranceOutcome(calculateNetInsurance(true));
        } else {
            updateInsuranceOutcome(calculateNetInsurance(false));
        }
    }

    private void payInsurance(){
        int stake = insurance;
        int winnings = insurance*2;
        int payout = stake + winnings;
        increaseBank(payout);
    }

    private int calculateNetInsurance(boolean win){
        int stake = insurance;
        int net = 0;
        if (win){
            net = 2*stake;
        } else {
            net -= stake;
        }
        return net;
    }

    private boolean allowSplit(PlayerSpecific hand){
        if (hand.count() > 2) {
            return false;
        }
        if (!canBet(hand.getBet())) {
            return false;
        }
        if (playerHands.size() >= 4){
            return false;
        }
        return (value(hand.getCard(0)) == value(hand.getCard(1)));
    }

    private boolean canBet(int bet){
        return bank >= bet;
    }

    private void updateCardsRemainingDisplay(){
        setText(cardsRemainingDisplay, "value", Integer.toString(count(shoe)));
    }

    private void updateBankDisplay(){
        setText(bankDisplay, "value", "$"+Integer.toString(bank));
    }

    private void increaseBank(int amount){
        bank += amount;
        updateBankDisplay();
    }

    private void decreaseBank(int amount){
        bank -= amount;
        updateBankDisplay();
    }
    private void toggleGameButtons(boolean enabled){
        toggleButton(hitButton, enabled);
        toggleButton(stayButton, enabled);
    }

    private void tryToggleBetButtons(boolean enabled){
        if (!canBet(betValue1)){
            toggleButton(betButton1, false);
            toggleButton(betButton2, false);
            toggleButton(betButton3, false);
        } else {
            if (canBet(betValue1)) {
                toggleButton(betButton1, enabled);
            } else toggleButton(betButton1, false);
            if (canBet(betValue2)) {
                toggleButton(betButton2, enabled);
            } else toggleButton(betButton2, false);
            if (canBet(betValue3)) {
                toggleButton(betButton3, enabled);
            } else toggleButton(betButton3, false);
        }
    }

    private void tryTogglePlayButton(boolean enabled){
        if (bet > 0 && enabled){
            toggleButton(playButton, true);
        } else {
            toggleButton(playButton, false);
        }
    }

    private void showGameButtons(boolean play){
        if (play){
            hide(playBar);
            hide(betBar);
            show(hitStayBar);
            show(contextBar);
        } else {
            show(playBar);
            show(betBar);
            hide(hitStayBar);
            hide(contextBar);
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

    private int value(Valuable valuable){
        return valuable.value();
    }

    private int count(Countable countable){
        return countable.count();
    }

    private void toggleButton(Button button, boolean enabled){
        button.setEnabled(enabled);
    }

    private void setText(TextSettable element, String tag, CharSequence text){
        element.setText(tag, text);
    }

    private void hide(VisibilitySettable element){
        element.setVisibility(View.GONE);
    }

    private void show(VisibilitySettable element){
        element.setVisibility(View.VISIBLE);
    }

    private void greyOut(AlphaSettable element){
        element.setAlpha((float)0.5);
    }

    private void colourIn(AlphaSettable element){
        element.setAlpha((float)1.0);
    }

    private void hide(View view){
        view.setVisibility(View.GONE);
    }

    private void show(View view){
        view.setVisibility(View.VISIBLE);
    }

}
