package dotbig.barebonesblackjack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import static java.lang.System.out;

//TODO: replace setText(Integer.string...) with setText(String.format("%d",value));

public class GameActivity extends AppCompatActivity implements OnClickListener {

    private enum Result{
        SURRENDER, LOSS, PUSH, WIN, NATURAL
    }
    private final boolean ACTIVE = true;
    private final boolean INACTIVE = false;
    private final boolean PLAYER = true;
    private final boolean DEALER = false;

    private boolean acted = false;
    private boolean inspecting = false;
    private boolean roundInProgress = false;
    private boolean preGame = true;

    //buttons
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



    private ShoeBlackjack shoe;
    private int shoeSize;
    private int penetration;

    private int bank;
    private int bet;
    private int insurance;

    private TextView bankDisplay;
    private TextView cardsRemainingDisplay;

    private ConstraintLayout insuranceParent;
    private TextView insuranceValue;

    private int betValue1;
    private int betValue2;
    private int betValue3;

    private List<PlayerSpecific> playerHands;
    private PlayerSpecific currentPlayerHand;
    private DealerSpecific dealerHand;

    //layout stuff
    private double desiredMargin = 0.18;
    private int dealerHandDisplayWidth;
    private int dealerHandDisplayHeight;
    private int totalHandDisplayWidth;
    private int totalHandDisplayHeight;
    private int inspectorDisplayWidth;
    private int inspectorDisplayHeight;

    private ViewGroup uiParent;
    private LinearLayout handParent;
    private LinearLayout betParent;

    private ConstraintLayout dealerCards;
    private List<HandDisplayGroup> handDisplays;
    private ConstraintLayout inspector;

    private GestureDetector detector;
    private View touchedView;

    private void initialiseControls(){
        detector = new GestureDetector(this, new GestureListener());
        View.OnTouchListener touchListener = new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent e){
                touchedView = v;
                return detector.onTouchEvent(e);
            }
        };

        //handParent.setOnTouchListener(touchListener);
        findViewById(R.id.cardDisplay1).setOnTouchListener(touchListener);
        findViewById(R.id.cardDisplay2).setOnTouchListener(touchListener);
        findViewById(R.id.cardDisplay3).setOnTouchListener(touchListener);
        findViewById(R.id.cardDisplay4).setOnTouchListener(touchListener);

        //TODO: insurance touch
        dealerCards.setOnTouchListener(touchListener);
        betParent.setOnTouchListener(touchListener);
        insuranceParent.setOnTouchListener(touchListener);
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e){
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e){
            switch(touchedView.getId()){
                case(R.id.dealerCards):
                    inspectHand(-1);
                    return true;
                case(R.id.cardDisplay1):
                    //TODO: cant inspect post game
                    if (!roundInProgress && bet > 0){
                        play();
                    } else {
                        inspectHand(0);
                    }
                    return true;
                case(R.id.cardDisplay2):
                    inspectHand(1);
                    return true;
                case(R.id.cardDisplay3):
                    inspectHand(2);
                    return true;
                case(R.id.cardDisplay4):
                    inspectHand(3);
                    return true;

                case(R.id.betDisplays):
                    if(!roundInProgress && canBet(10)){
                        increaseBet(10);
                    }

                case(R.id.insuranceDisplay):
                    if(allowInsurance(currentPlayerHand)){
                        insurance(currentPlayerHand);
                    }

            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e){
            switch(touchedView.getId()){
                case(R.id.cardDisplay1):
                    if(allowDoubleDown(currentPlayerHand)){
                        doubleDown(currentPlayerHand);
                    }
                    break;
                case(R.id.betDisplays):
                    if(allowSurrender()){
                        surrender(currentPlayerHand);
                    }
            }
        }

        private void determineHandFling(boolean vertical, boolean positive){
            if (vertical){
                if (positive){
                    stay(currentPlayerHand);
                } else if (!allowEvenMoney(currentPlayerHand)){
                    hitPlayer(currentPlayerHand);
                }

            } else {
                if(allowSplit(currentPlayerHand)){
                    split(currentPlayerHand);
                }
            }
        }

        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY){
            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();
            boolean vertical = Math.abs(distanceY) > Math.abs(distanceX);
            boolean fastEnough;
            boolean longEnough;
            boolean positive;
            if (vertical){
                positive = distanceY < 0;
                fastEnough = Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD;
                longEnough = Math.abs(distanceY) > SWIPE_DISTANCE_THRESHOLD;
            } else {
                positive = distanceX < 0;
                fastEnough = Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD;
                longEnough = Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD;
            }

            if (fastEnough && longEnough) {
                switch(touchedView.getId()){
                    case(R.id.cardDisplay1):
                        determineHandFling(vertical, positive);
                        return true;
                    case(R.id.cardDisplay2):
                        determineHandFling(vertical, positive);
                        return true;
                    case(R.id.cardDisplay3):
                        determineHandFling(vertical, positive);
                        return true;
                    case(R.id.cardDisplay4):
                        determineHandFling(vertical, positive);
                        return true;
                }
            }
            return false;

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        //back button on app bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //get game parameters
        Bundle gameBundle = getIntent().getExtras();
        if (gameBundle != null){
            shoeSize = gameBundle.getInt("shoeSize");
            penetration = gameBundle.getInt("penetration");
        } else {
            shoeSize = 4;
            penetration = 90;
        }

        initialiseBetValues(20, 50, 100);

        initialiseUI();
        initialiseGameState();
    }

    private void initialiseUI(){
        initialiseButtons();

        uiParent = findViewById(R.id.uiParent);

        bankDisplay = findViewById(R.id.bankValue);
        cardsRemainingDisplay = findViewById(R.id.remainingValue);

        insuranceParent = findViewById(R.id.insuranceDisplay);
        insuranceValue = findViewById(R.id.insuranceValue);

        initialiseHandDisplays();
        initialiseInspector();
        establishDisplayDimensions();

        initialiseControls();

        activateDummies();

        updateBetButtonText();
    }

    private void initialiseButtons(){
        hitButton = findViewById(R.id.buttonHit);
        stayButton = findViewById(R.id.buttonStay);
        playButton = findViewById(R.id.buttonPlay);
        betButton1 = findViewById(R.id.buttonBet1);
        betButton2 = findViewById(R.id.buttonBet2);
        betButton3 = findViewById(R.id.buttonBet3);

        hitButton.setOnClickListener(this);
        stayButton.setOnClickListener(this);
        playButton.setOnClickListener(this);
        betButton1.setOnClickListener(this);
        betButton2.setOnClickListener(this);
        betButton3.setOnClickListener(this);
        //option buttons
        splitButton = findViewById(R.id.buttonSplit);
        doubleDownButton = findViewById(R.id.buttonDoubleDown);
        insuranceButton = findViewById(R.id.buttonInsurance);
        surrenderButton = findViewById(R.id.buttonSurrender);

        splitButton.setOnClickListener(this);
        doubleDownButton.setOnClickListener(this);
        insuranceButton.setOnClickListener(this);
        surrenderButton.setOnClickListener(this);

        //button bar containers
        betBar = findViewById(R.id.barBets);
        contextBar = findViewById(R.id.barContextuals);
        playBar = findViewById(R.id.barPlay);
        hitStayBar = findViewById(R.id.barHitStay);
    }

    private void initialiseInspector(){
        inspector = findViewById(R.id.cardInspector);
        inspector.setOnClickListener(this);
        inspector.animate().setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (!inspecting){
                    hide(inspector);
                    emptyView(inspector);
                }
            }
        });
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

    private void establishDisplayDimensions(){
        setTotalDealerDisplayDimensions();
        setTotalHandDisplayDimensions();
        setInspectorDisplayDimensions();
    }

    private void setTotalDealerDisplayDimensions(){
        //need to wait until the display is drawn before finding the dimensions
        dealerCards.post(new Runnable() {
            @Override
            public void run() {
                dealerHandDisplayWidth = dealerCards.getWidth();
                dealerHandDisplayHeight = dealerCards.getHeight();
            }
        });

    }
    private void setTotalHandDisplayDimensions(){
        //need to wait until the display is drawn before finding the dimensions
        handParent.post(new Runnable() {
            @Override
            public void run() {
                totalHandDisplayWidth = handParent.getWidth();
                totalHandDisplayHeight = handParent.getHeight();
            }
        });
    }

    private void setInspectorDisplayDimensions(){
        inspector.post(new Runnable() {
            @Override
            public void run() {
                inspectorDisplayHeight = inspector.getHeight();
                inspectorDisplayWidth = inspector.getWidth();
                hide(inspector);
            }
        });
    }

    private void initialiseHandDisplays(){
        initialiseDealerDisplay();
        initialisePlayerDisplays();
    }

    private void initialiseDealerDisplay(){
        dealerCards = findViewById(R.id.dealerCards);
        //dealerCards.setOnClickListener(this);
    }

    private void initialisePlayerDisplays(){
        handParent = findViewById(R.id.handDisplays);
        betParent = findViewById(R.id.betDisplays);

        ConstraintLayout cardDisplay1 = findViewById(R.id.cardDisplay1);
        ConstraintLayout cardDisplay2 = findViewById(R.id.cardDisplay2);
        ConstraintLayout cardDisplay3 = findViewById(R.id.cardDisplay3);
        ConstraintLayout cardDisplay4 = findViewById(R.id.cardDisplay4);
        ConstraintLayout boxDisplay1 = findViewById(R.id.boxDisplay1);
        ConstraintLayout boxDisplay2 = findViewById(R.id.boxDisplay2);
        ConstraintLayout boxDisplay3 = findViewById(R.id.boxDisplay3);
        ConstraintLayout boxDisplay4 = findViewById(R.id.boxDisplay4);
        TextView betDisplay1 = findViewById(R.id.betDisplay1);
        TextView betDisplay2 = findViewById(R.id.betDisplay2);
        TextView betDisplay3 = findViewById(R.id.betDisplay3);
        TextView betDisplay4 = findViewById(R.id.betDisplay4);

        handDisplays = new ArrayList<>();
        handDisplays.add(new HandDisplayGroup(cardDisplay1, boxDisplay1, betDisplay1));
        handDisplays.add(new HandDisplayGroup(cardDisplay2, boxDisplay2, betDisplay2));
        handDisplays.add(new HandDisplayGroup(cardDisplay3, boxDisplay3, betDisplay3));
        handDisplays.add(new HandDisplayGroup(cardDisplay4, boxDisplay4, betDisplay4));

        clearHandDisplays();
        show(handDisplays.get(0));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case(android.R.id.home):
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onClick(View v){
        switch(v.getId()){
            case (R.id.buttonHit):
                disableOptions();
                hitPlayer(currentPlayerHand);
                break;
//
            case (R.id.buttonStay):
                disableOptions();
                stay(currentPlayerHand);
                break;
//
            case (R.id.buttonPlay):
                play();
                break;
//
            case (R.id.buttonBet1):
                increaseBet(20);
                break;
//
            case (R.id.buttonBet2):
                increaseBet(50);
                break;
            case (R.id.buttonBet3):
                increaseBet(100);
                break;
//
            case (R.id.buttonDoubleDown):
                disableOptions();
                doubleDown(currentPlayerHand);
                break;
//
            case (R.id.buttonSplit):
                disableOptions();
                split(currentPlayerHand);
                break;
//
            case (R.id.buttonInsurance):
                toggleButton(insuranceButton, false);
                insurance(currentPlayerHand);
                if (allowEvenMoney(currentPlayerHand)){
                    stay(currentPlayerHand);
                }
                break;
//
            case (R.id.buttonSurrender):
                surrender(currentPlayerHand);
                break;
//
            case (R.id.dealerCards):
                inspectHand(-1);
                break;
            case (R.id.cardDisplay1):
                inspectHand(0);
                break;
            case (R.id.cardDisplay2):
                inspectHand(1);
                break;
            case (R.id.cardDisplay3):
                inspectHand(2);
                break;
            case (R.id.cardDisplay4):
                inspectHand(3);
                break;
            case (R.id.cardInspector):
                uninspect();
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



    private ImageView createCardView(Card card){
        //set up new imageview
        ImageView newCardView = new ImageView(getApplicationContext());
        newCardView.setId(View.generateViewId());
        newCardView.setAdjustViewBounds(true);
        newCardView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        //allows shadows
        //newCardView.setOutlineProvider(ViewOutlineProvider.BOUNDS);

        //find and set card image to imageview
        int res = CardImage.findImage(card, getApplicationContext());
        newCardView.setImageResource(res);

        return newCardView;
    }

    /*
        stack of cards should take up as much of its container as possible while not exceeding the boundaries.
        marginProportion refers to the percentage of the card beneath we'd like to have visible
        if we want to show 18% of the card beneath, then marginProportion = 0.18
            marginSize = marginProportion * cardWidth

        the ideal card size is found by
            idealWidth = parentWidth - numberOfCards * marginSize

        since marginSize = marginProportion * idealWidth, we get
            idealWidth = parentWidth - numberOfCards * (marginProportion * idealWidth)

        solving for idealWidth gives us
            idealWidth = parentSize/((marginProportion * numberOfCards) + 1)
         */
    private int calculateCardWidth(ConstraintLayout group, boolean player){
        int hands = playerHands.size();
        int parentWidth;
        if(inspecting){
            parentWidth = inspectorDisplayWidth;
        } else if (!player){
            parentWidth = dealerHandDisplayWidth;
        } else {
            if (hands == 1){
                parentWidth = totalHandDisplayWidth;
            } else {
                parentWidth = totalHandDisplayWidth / hands;
            }
        }

        int children = group.getChildCount();
        int idealWidth;
        if (children > 1){
            idealWidth = (int)(parentWidth/(desiredMargin*children+1));
        } else {
            idealWidth = parentWidth;
        }
        return idealWidth;
    }

    private int calculateCardHeight(ConstraintLayout group, boolean player){
        int parentHeight;
        if (inspecting){
            parentHeight = inspectorDisplayHeight;
        } else if (!player){
            parentHeight = dealerHandDisplayHeight;
        } else {
            parentHeight = totalHandDisplayHeight;
            //parentHeight = dealerHandDisplayHeight;
        }

        int children = group.getChildCount();
        int idealHeight;
        if (children > 1){
            idealHeight = (int)(parentHeight/(desiredMargin*children+1));
        } else {
            idealHeight = parentHeight;
        }
        return idealHeight;
    }

    private void resizeCards(ConstraintLayout group, boolean player){
        int children = group.getChildCount();
        int idealWidth = calculateCardWidth(group, player);
        int idealHeight = calculateCardHeight(group, player);
        for (int i=0; i<children; i++){
            ImageView child = (ImageView)group.getChildAt(i);
            child.setMaxWidth(idealWidth);
            child.setMaxHeight(idealHeight);
        }
    }

    private void setCardElevations(ConstraintLayout group){
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        for (int i=0; i < group.getChildCount(); i++){
            group.getChildAt(i).setElevation((2*i) * density);
        }
    }

    private void setCardConstraints(ConstraintLayout group, boolean player){
        ConstraintSet constraints = new ConstraintSet();
        constraints.clone(group);

        int cardWidth = calculateCardWidth(group, player);
        int cardHeight = calculateCardHeight(group, player);
        int marginLeft = (int)(cardWidth*desiredMargin);
        int marginBottom = (int)(cardHeight*desiredMargin);

        int children = group.getChildCount();
        for (int i=children-1; i>=0; i--){

            int current;
            int elder;
            if (i == 0){
                //offset the first card depending on how many cards there are,
                //ensures the whole stack remains centered
                int offsetRight = (children-1)*marginLeft;
                int offsetTop = (children-1)*marginBottom;

                current = group.getChildAt(0).getId();
                elder = ConstraintSet.PARENT_ID;
                constraints.connect(current, ConstraintSet.LEFT,
                        elder, ConstraintSet.LEFT);
                constraints.connect(current, ConstraintSet.RIGHT,
                        elder, ConstraintSet.RIGHT, offsetRight);
                constraints.connect(current, ConstraintSet.BOTTOM,
                        elder, ConstraintSet.BOTTOM);
                if (!player){
                  constraints.connect(current, ConstraintSet.TOP,
                        elder, ConstraintSet.TOP, offsetTop);
                }

            } else {
                current = group.getChildAt(i).getId();

                elder = group.getChildAt(i-1).getId();
                constraints.connect(current, ConstraintSet.LEFT,
                        elder, ConstraintSet.LEFT, marginLeft);
                constraints.connect(current, ConstraintSet.BOTTOM,
                        elder, ConstraintSet.BOTTOM, marginBottom);
            }

        }
        if (children > 1){
            group.getChildAt(children-1).bringToFront();
        }
        constraints.applyTo(group);
    }

    private void calibrateMargins(Hand hand){
        ConstraintLayout group = getCardDisplay(hand);
        setCardConstraints(group, true);
    }

    private void calibrateAllMargins(){
        for (Hand h : playerHands){
            calibrateMargins(h);
        }
    }

    private void calibrateSizes(Hand hand){
        ConstraintLayout group = getCardDisplay(hand);
        TransitionManager.beginDelayedTransition(group, new ChangeBounds().setDuration(100));
        resizeCards(group, true);
    }

    private void calibrateAllSizes(){
        for (Hand h : playerHands){
            calibrateSizes(h);
        }
    }

    private void addCardToDisplay(ConstraintLayout group, Card card, boolean player){
        final ImageView newCardView = createCardView(card);
        TransitionSet transition = new TransitionSet();

        Fade fade = new Fade(Fade.IN);
        ChangeBounds changeBounds = new ChangeBounds();

        transition.addTransition(fade);
        transition.addTransition(changeBounds);
        transition.setOrdering(TransitionSet.ORDERING_TOGETHER);
        transition.setDuration(150);

        TransitionManager.beginDelayedTransition(group, transition);
        group.addView(newCardView);
        //setCardElevations(group);
        resizeCards(group, player);
        setCardConstraints(group, player);
    }

    private void addCardToHandDisplay(Card card, Hand hand){
        if (hand == dealerHand){
            addCardToDisplay(dealerCards, card, false);
        } else {
            addCardToDisplay(getCardDisplay(hand), card, true);
        }
    }

    private void redrawHand(Hand hand){
        if (hand == null){
            return;
        }
        ConstraintLayout group;
        if (hand == dealerHand){
            group = dealerCards;
        } else {
            group = getCardDisplay(hand);
        }

        group.removeAllViews();

        for (int i = 0; i < hand.count(); i++){
            addCardToHandDisplay(hand.getCard(i), hand);
        }
    }

    private void inspectHand(int index){
        if (!preGame){
            inspecting = true;
            toggleGameButtons(false);
            disableOptions();

            greyOut(dealerCards);
            greyOut(handParent);

            if (index >= 0){
                populateInspector(getHandAtIndex(index));
            } else {
                populateInspector(dealerHand);
            }
            showInspector();
        }
    }

    private void uninspect(){
        inspecting = false;
        optionsCheck();
        if (currentPlayerHand == null){
            toggleGameButtons(false);
        } else {
            toggleGameButtons(true);
        }

        hideInspector();

        colourIn(handParent);
        colourIn(dealerCards);
    }

    private void hideInspector(){
        inspector.setClickable(false);
        inspector.animate().alpha(0.0f).setDuration(150);
    }

    private Hand getHandAtIndex(int index){
        if (index >= 0){
            return playerHands.get(index);
        } else {
            return dealerHand;
        }
    }

    private void showInspector(){
        inspector.animate().alpha(1.0f).setDuration(150);
        show(inspector);
        inspector.bringToFront();
        inspector.setClickable(true);
    }

    private void populateInspector(Hand hand){
        for (BlackjackCard c : hand.getCards()){
            addCardToDisplay(inspector, c, false);
        }
    }

    private void showActiveHandGroups(){
        for(Hand h : playerHands){
            show(getHandDisplayGroup(h));
        }
    }

    private void focusActiveHand(PlayerSpecific hand){
        if (hand != null) {
            int currentHandIndex = playerHands.indexOf(hand);
            for (int i = 0; i< handDisplays.size(); i++){
                if (currentHandIndex == i){
                    colourIn(handDisplays.get(i));
                } else {
                    greyOut(handDisplays.get(i));
                }
            }
        } else {
            focusAllHandDisplays();
        }
    }

    private void updateHandStatusBet(PlayerSpecific hand){
        if (hand == null){
            return;
        }
        TextSettable group = getHandDisplayGroup(hand);
        String status;
        if(roundInProgress){
            status = String.format(Locale.ENGLISH, "$%d", hand.getBet());
        } else {
            status = String.format(Locale.ENGLISH, "$%d", bet);
        }
        group.setText(status);
    }

    private void updateHandStatusResult(PlayerSpecific hand, Result result){
        if (hand == null){
            return;
        }
        TextSettable group = getHandDisplayGroup(hand);

        int net = calculateNetProfit(hand, result);
        String profit;
        if (net < 0) {
            profit = "-$" + Integer.toString(Math.abs(net));
        } else if (net == 0){
            profit = "Push";
        } else {
            profit = "+$"+Integer.toString(net);
        }
        group.setText(profit);
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
                break;
            case SURRENDER:
                net -= (bet/2);
                break;
        }
        return net;
    }

    private int getHandIndex(Hand hand){
        if (hand != dealerHand){
            return playerHands.indexOf(hand);
        } else {
            return -1;
        }
    }

    private void clearHandDisplays(){
        emptyView(dealerCards);
        for (HandDisplayGroup h : handDisplays){
            emptyCardDisplay(h);
            setText(h, "");
        }
    }

    private void emptyCardDisplay(HandDisplayGroup group){
        hide(group);
        group.getCardDisplay().removeAllViews();
    }

    private void emptyView(ConstraintLayout group){
        group.removeAllViews();
    }

    private void popCard(Hand hand){
        if (hand != dealerHand){
            ViewGroup group = getCardDisplay(hand);
            if (group.getChildCount() >= 0) {
                hide(group.getChildAt(group.getChildCount()-1));
                group.removeViewAt(group.getChildCount()-1);
            }
        }
    }

    public void clearTable(){
        clearHandDisplays();
        activateDummies();
    }


    private void focusAllHandDisplays(){
        for (HandDisplayGroup h : handDisplays){
            colourIn(h);
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
        out.println("nextHand, index: " +Integer.toString(indexToCheck));
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

    private void stay(PlayerSpecific hand){
        if (roundInProgress){
            hand.stay();
            tryNextPlayerHand(hand);
        }
    }

    private void bust(PlayerSpecific hand){
        hand.bust();
        updateHandStatusResult(hand, Result.LOSS);
        tryNextPlayerHand(hand);
    }

    //use it whenever we need another hand from stay bust etc
    private void tryNextPlayerHand(PlayerSpecific hand){
        PlayerSpecific nextHand = getNextHand(hand, ACTIVE);
        focusActiveHand(nextHand);
        if (nextHand == null){
            dealerTurn(natural(hand));
        } else {
            currentPlayerHand = nextHand;
            toggleGameButtons(true);
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
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (!dealerShouldDraw()){
                    if (insuranceTaken()){
                        resolveInsurance();
                    }
                    evaluateAllResults();
                } else {
                    hitDealer(dealerHand);
                    handler.postDelayed(this, 300);
                }
            }
        };
        handler.postDelayed(runnable, 300);
    }

    private boolean dealerShouldDraw(){
        int value = dealerHand.value();
        boolean soft = dealerHand.softSeventeen();

        return (value < 17 && value != -1) || soft;
    }

    private void toggleInspection(boolean enabled){
        //dealerCards.setClickable(enabled);
        for (HandDisplayGroup display : handDisplays){
        //    display.setClickable(enabled);
        }
    }

    private void dealerTurn(boolean playerNatural){
        toggleInspection(false);
        toggleGameButtons(false);

        //decide whether or not to reveal the face down card
        if (shouldReveal(playerNatural)) {
            revealHoleCard();
        }

        //if the player has hands still in play and doesn't have natural, dealer draws til 17
        if (playerHasLiveHands() && !playerNatural) {
            completeDealerHand();
        } else {
            if (insuranceTaken()) {
                resolveInsurance();
            }
            evaluateAllResults();
        }

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
        flipCard();
    }

    //TODO: make it smoother with animation
    private void flipCard(){
        final ImageView hole = (ImageView)dealerCards.getChildAt(0);
        hole.animate().alpha(0.0f).setDuration(75).setListener(new AnimatorListenerAdapter() {
               @Override
               public void onAnimationEnd(Animator animation) {
                   super.onAnimationEnd(animation);
                   hole.setImageResource(CardImage.findImage(dealerHand.getHoleCard(), getApplicationContext()));
                   hole.animate().alpha(1.0f).setDuration(75).setListener(null);
               }
           });
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
                updateHandStatusResult(hand, result);
                roi = bet + profit;
                break;
            case WIN:
                updateHandStatusResult(hand, result);
                roi = bet + profit;
                break;
            case PUSH:
                updateHandStatusResult(hand, result);
                roi = bet;
                break;
            case LOSS:
                updateHandStatusResult(hand, result);
                break;
        }
        increaseBank(roi);
    }

    private ConstraintLayout getCardDisplay(Hand hand){
        if (hand == dealerHand){
            return dealerCards;
        } else {
            return getHandDisplayGroup(hand).getCardDisplay();
        }
    }

    private HandDisplayGroup getHandDisplayGroup(Hand hand){
        int index = getHandIndex(hand);
        return handDisplays.get(index);
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

    private void initialiseDummyHands(){
        dealerHand = new HandDealer();
        playerHands = new ArrayList<>();
        PlayerSpecific newHand = new HandPlayer(0);
        playerHands.add(newHand);
        currentPlayerHand = playerHands.get(0);
    }

    private void dealDummyCards(){
        dealerDummy();
        playerDummy();
    }

    private void dealerDummy(){
        dealerCards.post(new Runnable() {
            @Override
            public void run() {
                dummyHit(dealerHand);
                dummyHit(dealerHand);
            }
        });
    }

    private void playerDummy(){
        handParent.post(new Runnable() {
            @Override
            public void run() {
                dummyHit(currentPlayerHand);
                dummyHit(currentPlayerHand);
            }
        });
    }

    private void dummyHit(Hand hand){
        BlackjackCard dummy = new CardBlackjack(0,0);
        dummy.flip(false);
        hand.add(dummy);
        addCardToHandDisplay(dummy, hand);
    }

    private void showDummyHand(){
        showActiveHandGroups();
        getHandDisplayGroup(currentPlayerHand).getBetDisplay().setText("");
    }

    private void activateDummies(){
        initialiseDummyHands();
        showDummyHand();
        dealDummyCards();
    }

    //TODO: the second dealer hit should be face down, not the first
    private void deal(){
        hitFaceDown(dealerHand);
        hit(currentPlayerHand);
        //hitTest(currentPlayerHand);
        hit(dealerHand);
        hit(currentPlayerHand);
        //hitTest(currentPlayerHand);
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
        BlackjackCard toAdd = shoe.draw();
        hand.add(toAdd);
        addCardToHandDisplay(toAdd, hand);
        updateCardsRemainingDisplay();
    }

    //deal a custom card
    private void hitTest(Hand hand){
        BlackjackCard test = new CardBlackjack(0, 0);
        hand.add(test);
        addCardToHandDisplay(test, hand);
        updateCardsRemainingDisplay();
    }

    private void hitFaceDown(Hand hand){
        BlackjackCard toAdd = shoe.draw(false);
        hand.add(toAdd);
        addCardToHandDisplay(toAdd, hand);
        updateCardsRemainingDisplay();
    }

    private void hitPlayer(PlayerSpecific hand) {
        if (roundInProgress){
            acted = true;
            hit(hand);
            if (busted(hand)){
                bust(hand);
            } else if (twentyOne(hand)){
                forcedStay(hand);
            }
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
    }

    private void play(){
        uninspect();
        preGame = false;
        roundInProgress = true;
        acted = false;
        initialiseHands();
        updateBankDisplay();
        tryToggleBetButtons(false);

        emptyView(dealerCards);
        clearHandDisplays();
        showActiveHandGroups();

        toggleInsuranceDisplay(false);

        deal();
        //dealTest();

        updateHandStatusBet(currentPlayerHand);

        optionsCheck();
        if (natural(currentPlayerHand)){
            if (allowEvenMoney(currentPlayerHand)) {
                showGameButtons(true);
                offerEvenMoney(currentPlayerHand);
            } else {
                //dealerTurn(true);
                forcedStay(currentPlayerHand);
            }
        } else {
            showGameButtons(true);
            toggleGameButtons(true);
        }
        //dont put things here
    }

    private void forcedStay(final PlayerSpecific hand){
        Handler handler = new Handler();
        toggleGameButtons(false);

        if (natural(hand)){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dealerTurn(true);
                }
            }, 350);
        } else {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stay(hand);
                    //toggleGameButtons(true);
                }
            }, 350);
        }
    }

    private void increaseBet(int amount){
        if (bet == 0 && !preGame){
            clearTable();
        }

        bet += amount;
        decreaseBank(amount);
        updateBankDisplay();
        updateHandStatusBet(currentPlayerHand);

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

    private void resetBet(){
        bet = 0;
        tryToggleBetButtons(true);
    }

    private void endRound(){
        toggleInspection(true);
        roundInProgress = false;

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

    //TODO: if split results in blackjack, ensure hand gets unfocused
    private void split(PlayerSpecific hand){
        acted = true;
        PlayerSpecific master = hand;
        PlayerSpecific branch = splitHand(master);
        decreaseBank(branch.getBet());

        popCard(master);
        playerHands.add(branch);
        showActiveHandGroups();
        calibrateAllMargins();
        calibrateAllSizes();

        redrawHand(branch);

        hit(branch);
        hit(master);

        if (twentyOne(branch)){
            branch.stay();
        }
        if (twentyOne(master)){
            master.stay();
        }

        updateHandStatusBet(branch);
        updateHandStatusBet(master);

        currentPlayerHand = getLastHand(ACTIVE);

        focusActiveHand(currentPlayerHand);

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
        acted = true;
        int bet = hand.getBet();
        bank -= bet;
        hand.increaseBet(bet);
        updateBankDisplay();
        hit(hand);
        updateHandStatusBet(hand);
        if (busted(hand)){
            bust(hand);
        } else {
            forcedStay(hand);
        }
    }

    private void surrender(PlayerSpecific hand){
        acted = true;
        int recoup = hand.getBet()/2;
        increaseBank(recoup);
        updateHandStatusResult(currentPlayerHand, Result.SURRENDER);
        if (insuranceTaken()){
            revealHoleCard();
            resolveInsurance();
        }
        endRound();
    }

    private boolean allowSurrender(){
        if (!roundInProgress){
            return false;
        }
        if (acted) {
            return false;
        }
        return true;
    }

    private void optionsCheck(){
        if (currentPlayerHand != null){
            if (allowEvenMoney(currentPlayerHand)){
                offerEvenMoney(currentPlayerHand);
            } else {
                toggleButton(doubleDownButton, allowDoubleDown(currentPlayerHand));
                toggleButton(splitButton, allowSplit(currentPlayerHand));
                toggleButton(insuranceButton, allowInsurance(currentPlayerHand));
                toggleButton(surrenderButton, allowSurrender());
            }
        }
    }

    private void disableOptions(){
        toggleButton(doubleDownButton, false);
        toggleButton(splitButton, false);
        toggleButton(insuranceButton, false);
        toggleButton(surrenderButton, false);
    }
    private boolean allowDoubleDown(PlayerSpecific hand){
        if (!roundInProgress){
            return false;
        }
        if (acted) {
            return false;
        }
        return bank >= hand.getBet();
    }

    private boolean allowEvenMoney(PlayerSpecific hand){
        if (roundInProgress){
            return natural(hand) && dealerUpCardIsAce();
        } else return false;
    }

    private void offerEvenMoney(PlayerSpecific hand){
        toggleButton(hitButton, false);
        toggleButton(doubleDownButton, false);
        toggleButton(surrenderButton, false);
        toggleButton(splitButton, false);

        toggleButton(stayButton, true);
        toggleButton(insuranceButton, allowInsurance(hand));
    }

    //TODO: check logic
    private boolean allowInsurance(PlayerSpecific hand){
        if (!roundInProgress){
            return false;
        }
        int max = hand.getBet()/2;
        if (acted) {
            return false;
        }
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
            show(insuranceValue);
        } else {
            hide(insuranceValue);
        }
    }

    private void updateInsuranceStake(){
        insuranceValue.setText(String.format(Locale.ENGLISH, "$%d", insurance));
    }

    private void updateInsuranceOutcome(int net){
        if (net > 0){
            insuranceValue.setText(String.format(Locale.ENGLISH, "+$%d", net));
        } else {
            insuranceValue.setText(String.format(Locale.ENGLISH, "-$%d", (Math.abs(net))));
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
        if (!roundInProgress){
            return false;
        }
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
        cardsRemainingDisplay.setText(String.format(Locale.ENGLISH, "%d", count(shoe)));
    }

    private void updateBankDisplay(){
        bankDisplay.setText(String.format(Locale.ENGLISH, "$%d", bank));
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


    private int value(Valuable valuable){
        return valuable.value();
    }

    private int count(Countable countable){
        return countable.count();
    }

    private void toggleButton(Button button, boolean enabled){
        button.setEnabled(enabled);
    }

    private void setText(TextSettable element, CharSequence text){
        element.setText(text);
    }

    private void hide(VisibilitySettable element){
        element.setVisibility(View.GONE);
    }

    private void show(VisibilitySettable element){
        element.setVisibility(View.VISIBLE);
    }

    private void greyOut(AlphaSettable element){
        element.setAlpha(0.5f);
    }

    private void colourIn(AlphaSettable element){
        element.setAlpha(1.0f);
    }

    private void greyOut(View element){
        element.animate().alpha(0.5f).setDuration(150);
    }

    private void colourIn(View element){
        element.animate().alpha(1.0f).setDuration(150);
    }

    private void hide(View view){
        view.setVisibility(View.GONE);
    }

    private void show(View view){
        view.setVisibility(View.VISIBLE);
    }

}
