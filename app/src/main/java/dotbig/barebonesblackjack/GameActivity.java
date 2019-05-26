package dotbig.barebonesblackjack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.pm.ActivityInfo;
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

    private boolean allowInput = false;
    private boolean allowBet = false;
    private boolean acted = false;
    private boolean inspecting = false;
    private boolean roundInProgress = false;
    private boolean preGame = true;

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
    private ImageView insuranceHighlighter;
    private ImageView insuranceWinner;
    private ImageView insuranceLoser;

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
    private ImageView betHighlighter;

    private ConstraintLayout dealerCards;
    private List<HandDisplayGroup> handDisplays;
    private ConstraintLayout inspector;
    private ImageView handHighlighter;

    private GestureDetector detector;
    private View touchedView;

    private ConstraintLayout handHelp;
    private Handler helper;

    private View helperDoubleDot;
    private View helperDoubleLabel;
    private View helperDoubleHold;
    private View helperSplitArrow;
    private View helperSplitLabel;
    private View helperHitArrow;
    private View helperHitLabel;
    private View helperStayArrow;
    private View helperStayLabel;
    private View helperSurrenderLabel;
    private View helperBetTap;
    private View helperBetHold;
    private View helperBetBroke;

    private View helperClearTable;
    private boolean helping = false;

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
            return allowInput;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e){
            switch(touchedView.getId()){
                case(R.id.dealerCards):
                    inspectHand(-1);
                    return true;
                case(R.id.cardDisplay1):
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
                    return true;

                case(R.id.insuranceDisplay):
                    if(allowInsurance(currentPlayerHand)){
                        insurance(currentPlayerHand);
                    }
                    if (allowEvenMoney(currentPlayerHand)){
                        stay(currentPlayerHand);
                    }
                    return true;
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e){
            if (allowInput){
                switch(touchedView.getId()){
                    case(R.id.cardDisplay1):
                        if(allowDoubleDown(currentPlayerHand)){
                            doubleDown(currentPlayerHand);
                        }
                        break;
                    case(R.id.betDisplays):
                        if (!roundInProgress){
                            resetBet();
                        }
                        else if (allowSurrender()){
                            surrender(currentPlayerHand);
                        }
                }
            }
        }

        private void determineHandFling(boolean vertical, boolean positive){
            if (!roundInProgress){
                if (!preGame){
                    clearTable();
                } else if (bet > 0){
                    play();
                }
            } else {
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
        initialiseUI();
        initialiseGameState();
    }

    private void initialiseUI(){
        uiParent = findViewById(R.id.uiParent);

        bankDisplay = findViewById(R.id.bankValue);
        cardsRemainingDisplay = findViewById(R.id.remainingValue);

        insuranceParent = findViewById(R.id.insuranceDisplay);
        insuranceValue = findViewById(R.id.insuranceValue);
        insuranceHighlighter = findViewById(R.id.insuranceBackgroundHighlighted);
        insuranceWinner = findViewById(R.id.insuranceBackgroundWin);
        insuranceLoser = findViewById(R.id.insuranceBackgroundLoss);

        initialiseHandDisplays();
        initialiseInspector();
        establishDisplayDimensions();

        initialiseHelp();

        initialiseControls();

        activateDummies();
    }

    private void initialiseHelp(){
        helper = new Handler();

        handHighlighter = findViewById(R.id.handHighlighter);
        handHelp = findViewById(R.id.handHelp);

        helperDoubleDot = findViewById(R.id.dotDouble);
        helperDoubleLabel = findViewById(R.id.labelDouble);
        helperDoubleHold = findViewById(R.id.labelHold);
        helperSplitArrow = findViewById(R.id.arrowSplit);
        helperSplitLabel = findViewById(R.id.labelSplit);
        helperHitArrow = findViewById(R.id.arrowHit);
        helperHitLabel = findViewById(R.id.labelHit);
        helperStayArrow = findViewById(R.id.arrowStay);
        helperStayLabel = findViewById(R.id.labelStay);
        helperSurrenderLabel = findViewById(R.id.labelSurrender);
        helperBetTap = findViewById(R.id.tapBet);
        helperBetHold = findViewById(R.id.holdBet);
        helperBetBroke = findViewById(R.id.betBroke);

        helperClearTable = findViewById(R.id.clearHelp);
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
    }

    private void initialisePlayerDisplays(){
        handParent = findViewById(R.id.handDisplays);

        betParent = findViewById(R.id.betDisplays);
        betHighlighter = findViewById(R.id.box1Highlighted);

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
        ImageView winDisplay1 = findViewById(R.id.box1Win);
        ImageView winDisplay2 = findViewById(R.id.box2Win);
        ImageView winDisplay3 = findViewById(R.id.box3Win);
        ImageView winDisplay4 = findViewById(R.id.box4Win);
        ImageView lossDisplay1 = findViewById(R.id.box1Loss);
        ImageView lossDisplay2 = findViewById(R.id.box2Loss);
        ImageView lossDisplay3 = findViewById(R.id.box3Loss);
        ImageView lossDisplay4 = findViewById(R.id.box4Loss);

        handDisplays = new ArrayList<>();
        handDisplays.add(new HandDisplayGroup(cardDisplay1, boxDisplay1, betDisplay1, winDisplay1, lossDisplay1));
        handDisplays.add(new HandDisplayGroup(cardDisplay2, boxDisplay2, betDisplay2, winDisplay2, lossDisplay2));
        handDisplays.add(new HandDisplayGroup(cardDisplay3, boxDisplay3, betDisplay3, winDisplay3, lossDisplay3));
        handDisplays.add(new HandDisplayGroup(cardDisplay4, boxDisplay4, betDisplay4, winDisplay4, lossDisplay4));

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
            case (R.id.cardInspector):
                uninspect();
                break;
        }
    }

    private void initialiseGameState(){
        bank = 100;
        bet = 0;
        updateBankDisplay();

        initialiseShoe();

        toggleInsuranceDisplay(false);
        toggleAllowInput(true);
        toggleAllowBet(true);
        betHighlighter.postDelayed(new Runnable() {
            @Override
            public void run() {
                highlightBet(true);
            }
        }, 650);
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
                  constraints.connect(current, ConstraintSet.TOP,
                        elder, ConstraintSet.TOP, offsetTop);

            } else {
                current = group.getChildAt(i).getId();

                elder = group.getChildAt(i-1).getId();
                constraints.connect(current, ConstraintSet.LEFT,
                        elder, ConstraintSet.LEFT, marginLeft);
                constraints.connect(current, ConstraintSet.BOTTOM,
                        elder, ConstraintSet.BOTTOM, marginBottom);
            }

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
        if (!preGame && allowInput && !inspecting){
            inspecting = true;

            greyOut(dealerCards);
            greyOut(handParent);
            greyOut(betParent);
            if (helping){
                greyOut(helperClearTable);
            }

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

        hideInspector();

        colourIn(handParent);
        colourIn(dealerCards);
        colourIn(betParent);
        if (helping){
            colourIn(helperClearTable);
        }
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
        HandDisplayGroup group = getHandDisplayGroup(hand);

        int net = calculateNetProfit(hand, result);
        String profit;
        if (net < 0) {
            profit = "-$" + Integer.toString(Math.abs(net));
            fadeIn(group.getOutcomeDisplay(false));
        } else if (net == 0){
            profit = "Push";
        } else {
            profit = "+$"+Integer.toString(net);
            fadeIn(group.getOutcomeDisplay(true));
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
            fadeOut(h.getOutcomeDisplay(true));
            fadeOut(h.getOutcomeDisplay(false));
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

    private void clearTable(){
        helper.removeCallbacksAndMessages(null);
        highlightClearTable(false);
        resetInsuranceDisplay();

        toggleInsuranceDisplay(false);
        clearHandDisplays();
        activateDummies();

        toggleAllowBet(true);
        highlightBet(true);
        highlightInsurance(false);
        preGame = true;
    }

    private void highlightInsurance(boolean enabled){
        if (enabled){
            //insuranceHighlighter.setVisibility(View.VISIBLE);
            fadeIn(insuranceHighlighter);
        } else {
            //insuranceHighlighter.setVisibility(View.GONE);
            fadeOut(insuranceHighlighter);
        }
    }

    private void highlightBet(boolean enabled){
        if (enabled){
            //betHighlighter.setVisibility(View.VISIBLE);
            if (bank >= 10){
                fadeIn(betHighlighter);
                highlightBetTap(true);
            } else {
                highlightBroke();
            }
        } else {
            //betHighlighter.setVisibility(View.GONE);
            fadeOut(betHighlighter);
            highlightBetTap(false);
            highlightBetHold(false);
        }
    }

    private void highlightHand(boolean enabled){
        if (enabled){
            fadeIn(handHelp);
        } else {
            fadeOut(handHelp);
        }
    }

    private void highlightBroke(){
        helperBetBroke.setVisibility(View.VISIBLE);
        fadeIn(helperBetBroke);
    }

    private void highlightControls(boolean enabled){
        if (enabled){
            if (allowEvenMoney(currentPlayerHand)){
                highlightStay(true);
                greyOutHit();
                greyOutSplit();
                greyOutDouble();
            } else {
                highlightHit(true);
                highlightStay(true);
                highlightSurrender(true);
                if (allowSplit(currentPlayerHand)){
                    highlightSplit(true);
                } else {
                    greyOutSplit();
                }
                if (allowDoubleDown(currentPlayerHand)){
                    highlightDouble(true);
                } else {
                    greyOutDouble();
                }
            }
        } else {
            highlightHit(false);
            highlightStay(false);
            highlightSplit(false);
            highlightDouble(false);
            highlightSurrender(false);
        }
    }

    private void unhighlightContextuals() {
        highlightSplit(false);
        highlightDouble(false);
        highlightSurrender(false);
    }

    private void highlightHit(boolean enabled){
        if (enabled){
            fadeIn(helperHitArrow);
            fadeIn(helperHitLabel);
        } else {
            fadeOut(helperHitArrow);
            fadeOut(helperHitLabel);
        }
    }

    private void greyOutHit(){
        greyOut(helperHitArrow);
        greyOut(helperHitLabel);
    }

    private void highlightStay(boolean enabled){
        if (enabled){
            fadeIn(helperStayArrow);
            fadeIn(helperStayLabel);
        } else {
            fadeOut(helperStayArrow);
            fadeOut(helperStayLabel);
        }
    }

    private void greyOutStay(){
        greyOut(helperStayArrow);
        greyOut(helperStayLabel);
    }

    private void highlightDouble(boolean enabled){
        if (enabled){
            fadeIn(helperDoubleDot);
            fadeIn(helperDoubleLabel);
            fadeIn(helperDoubleHold);
        } else {
            fadeOut(helperDoubleDot);
            fadeOut(helperDoubleLabel);
            fadeOut(helperDoubleHold);
        }
    }

    private void greyOutDouble(){
        greyOut(helperDoubleDot);
        greyOut(helperDoubleLabel);
        greyOut(helperDoubleHold);
    }

    private void highlightSplit(boolean enabled){
        if (enabled){
            fadeIn(helperSplitArrow);
            fadeIn(helperSplitLabel);
        } else {
            fadeOut(helperSplitArrow);
            fadeOut(helperSplitLabel);
        }
    }

    private void greyOutSplit(){
        greyOut(helperSplitArrow);
        greyOut(helperSplitLabel);
    }

    private void highlightSurrender(boolean enabled){
        if (enabled){
            fadeIn(helperSurrenderLabel);
        } else {
            fadeOut(helperSurrenderLabel);
        }
    }

    private void highlightBetTap(boolean enabled){
        if (enabled){
            fadeIn(helperBetTap);
        } else {
            fadeOut(helperBetTap);
        }
    }

    private void highlightBetHold(boolean enabled){
        if (enabled){
            fadeIn(helperBetHold);
        } else {
            fadeOut(helperBetHold);
        }
    }

    private void highlightClearTable(boolean enabled){
        if (enabled){
            helping = true;
            fadeIn(helperClearTable);
        } else {
            helping = false;
            fadeOut(helperClearTable);
        }
    }

    private void fadeIn(View v){
        v.animate().alpha(1.0f).setDuration(300);
    }

    private void fadeOut(View v){
        v.animate().alpha(0.0f).setDuration(300);
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
            playerActed(false);
            toggleAllowInput(false);
            hand.stay();
            tryNextPlayerHand(hand);
        }
    }

    private void bust(PlayerSpecific hand){
        toggleAllowInput(false);
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
            toggleAllowInput(true);
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

    private void toggleAllowBet(boolean enabled){
        allowBet = enabled;
    }

    private void toggleAllowInput(boolean enabled){
        allowInput = enabled;
    }

    private void dealerTurn(boolean playerNatural){
        highlightInsurance(false);
        highlightControls(false);
        toggleAllowInput(false);

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
        hit(dealerHand);
        hit(currentPlayerHand);
    }


    private void hit(Hand hand){
        BlackjackCard toAdd = shoe.draw();
        hand.add(toAdd);
        addCardToHandDisplay(toAdd, hand);
        updateCardsRemainingDisplay();
    }

    //deal a custom card
    private void hitTest(Hand hand){
        BlackjackCard test = new CardBlackjack(0, 10);
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
            playerActed(true);
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

        bet = 0;
        highlightBet(false);
        updateBankDisplay();

        highlightHand(false);

        emptyView(dealerCards);
        clearHandDisplays();
        showActiveHandGroups();

        toggleInsuranceDisplay(false);

        deal();

        updateHandStatusBet(currentPlayerHand);

        if (!natural(currentPlayerHand)) {
            highlightControls(true);
            if (allowInsurance(currentPlayerHand)) {
                highlightInsurance(true);
            }
        } else {
            if (natural(currentPlayerHand)) {
                if (allowEvenMoney(currentPlayerHand)) {
                    highlightInsurance(true);
                } else {
                    forcedStay(currentPlayerHand);
                }
            }

        }
        //dont put things here
    }

    private void forcedStay(final PlayerSpecific hand){
        toggleAllowInput(false);
        Handler handler = new Handler();
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
        bet += amount;
        decreaseBank(amount);
        updateHandStatusBet(currentPlayerHand);
        highlightHand(true);
        highlightBetHold(true);
    }

    private void resetBet(){
        if (bet > 0){
            increaseBank(bet);
            handDisplays.get(0).getBetDisplay().setText("");
            highlightBetHold(false);
        }
        bet = 0;
    }

    private void endRound(){
        toggleAllowInput(true);
        toggleAllowBet(false);
        roundInProgress = false;

        highlightControls(false);
        summonClearTableHelper();

        resetBet();
        insurance = 0;

        if (shoe.penetrationCheck()) {
            initialiseShoe();
        }
    }

    private void summonClearTableHelper(){
        helper.postDelayed(new Runnable() {
            @Override
            public void run() {
                highlightClearTable(true);
            }
        }, 2000);
    }

    private void resetInsuranceDisplay(){
        fadeOut(insuranceLoser);
        fadeOut(insuranceWinner);
    }

    private void playerActed(boolean disableContextualsOnly){
        acted = true;
        highlightInsurance(false);
        if (disableContextualsOnly){
            unhighlightContextuals();
        } else {
            highlightControls(false);
        }
    }

    //TODO: if split results in blackjack, ensure hand gets unfocused
    private void split(PlayerSpecific hand){
        toggleAllowInput(false);
        playerActed(false);
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
            toggleAllowInput(true);
        }
    }
    //only to be used by split()
    private PlayerSpecific splitHand(PlayerSpecific hand){
        int bet = hand.getBet();
        BlackjackCard splitCard = hand.split();
        return new HandPlayer(bet, splitCard);
    }

    private void doubleDown(PlayerSpecific hand){
        playerActed(false);
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
        playerActed(false);
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
        highlightInsurance(false);
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
            fadeIn(insuranceWinner);
        } else {
            insuranceValue.setText(String.format(Locale.ENGLISH, "-$%d", (Math.abs(net))));
            fadeIn(insuranceLoser);
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
        if (allowBet){
            return bank >= bet;
        } else {
            return false;
        }
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

    private int value(Valuable valuable){
        return valuable.value();
    }

    private int count(Countable countable){
        return countable.count();
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
        element.setAlpha(0.4f);
    }

    private void colourIn(AlphaSettable element){
        element.setAlpha(1.0f);
    }

    private void greyOut(View element){
        element.animate().alpha(0.4f).setDuration(300);
    }

    private void colourIn(View element){
        element.animate().alpha(1.0f).setDuration(300);
    }

    private void hide(View view){
        view.setVisibility(View.GONE);
    }

    private void show(View view){
        view.setVisibility(View.VISIBLE);
    }

}
