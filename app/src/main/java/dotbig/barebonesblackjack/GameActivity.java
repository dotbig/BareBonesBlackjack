package dotbig.barebonesblackjack;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private Button placeholderButton;

    private Shoe shoe;
    private int shoeSize = 4;

    private List<Hand> playerHands;
    private Hand currentPlayerHand;
    private Hand dealerHand;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        //initialise textviews
        initialiseTextViews();
        //make sure our buttons do stuff
        configureReturnButton();
        configurePlayButton();
        configureHitButton();
        configureStayButton();

        configurePlaceHolderButton();

        initialiseGame();

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
            bust(hand);
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

    private void bust(Hand currentHand){
        //disable hit and stay buttons
        //pay out nothing
    }

    private void win(Hand currentHand, boolean blackjack){
        //disable hit and stay buttons
        //if blackjack, pay bet*1.5
        //else pay bet
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
        dealerHand.get(0).flip(true);
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

        if (dealerValue == -1){
            gameResult.setText("Dealer bust!");
        } else if (dealerValue == playerValue){
            if (dealerNatural) {
                if (playerNatural){
                    gameResult.setText("Natural push");
                } else {
                    gameResult.setText("Dealer natural wins");
                }
            } else if (playerNatural){
                gameResult.setText("Player natural wins");
            } else {
                gameResult.setText("Push");
            }
        } else if (dealerValue == 21){
            gameResult.setText("Dealer Blackjack");
        } else if (dealerValue < playerValue){
            gameResult.setText("Player wins!");
        } else if (dealerValue > playerValue){
            gameResult.setText("Dealer wins!");
        }
    }

    private void initialiseGame(){
        initialiseShoe();
        initialiseHands();
        //probably get player bet here, or before initialisehands
        deal();
        updatePlayerInformation(currentPlayerHand);
        updateDealerInformation(dealerHand);
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
        Hand newHand = new PlayHand();
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
                playAgain();
            }
        });
    }
    private void playAgain(){
        gameResult.setText("");
        initialiseHands();
        deal();
        updatePlayerInformation(currentPlayerHand);
        updateDealerInformation(dealerHand);
    }

    private void clearTable(){

    }

    private void configurePlaceHolderButton(){
        placeholderButton = findViewById(R.id.buttonPlaceHolder);
        placeholderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
