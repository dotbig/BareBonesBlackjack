package dotbig.barebonesblackjack;

import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.content.Intent;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity implements
        OnClickListener,
        ShoeSizeDialogFragment.ShoeDialogListener,
        PenetrationDialogFragment.PenetrationDialogListener
        {

    private Button startGameButton;
    private Button shoeSizeButton;
    private Button penetrationButton;

    private int shoeSize;
    private int penetration;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialiseUI();

        shoeSize = 6;
        penetration = 70;

        updatePenetrationDisplay();
        updateShoeSizeDisplay();

    }

    public void onClick(View v){
        switch(v.getId()){
            case(R.id.buttonPlay):
                startGame();
                break;
            case(R.id.buttonShoeSize):
                DialogFragment shoeDialogFragment = new ShoeSizeDialogFragment();
                shoeDialogFragment.show(getSupportFragmentManager(), "shoeSize");
                break;
            case(R.id.buttonPenetration):
                DialogFragment penDialogFragment = new PenetrationDialogFragment();
                penDialogFragment.show(getSupportFragmentManager(), "penetration");
                break;
        }
    }

    private void setNumberOfDecks(int decks){
        shoeSize = decks;
        updateShoeSizeDisplay();
    }

    private int getNumberOfDecks(){
        return 0;
    }

    private void updateShoeSizeDisplay() {
        shoeSizeButton.setText(Integer.toString(shoeSize)+" decks");
    }

    private void setPenetration(int pen){
        if (pen > maxPenetration(getNumberOfDecks())){
            penetration = maxPenetration(getNumberOfDecks());
        } else {
            penetration = pen;
        }
    }

    private int maxPenetration(int decks){
        switch (decks){
            case(1):
                return 50;
            case(2):
                return 75;
            case(4):
                return 90;
            default:
                return 90;
        }
    }

    private void updatePenetrationDisplay(){
        penetrationButton.setText(Integer.toString(penetration)+"%");
    }

    private int getPenetration(){
        return penetration;
    }

    public void onShoeSizeSelect(DialogFragment dialog, int which){
        switch(which){
            case(0):
                setNumberOfDecks(2);
                break;
            case(1):
                setNumberOfDecks(4);
                break;
            case(2):
                setNumberOfDecks(6);
                break;
            case(3):
                setNumberOfDecks(8);
                break;
        }
    }

    public void onPenetrationSelect(DialogFragment dialog, int which){
        switch(which){
            case(0):
                penetration = 50;
                updatePenetrationDisplay();
                break;
            case(1):
                penetration = 60;
                updatePenetrationDisplay();
                break;
            case(2):
                penetration = 70;
                updatePenetrationDisplay();
                break;
            case(3):
                penetration = 80;
                updatePenetrationDisplay();
                break;
        }
    }

    private void initialiseUI(){
        startGameButton = findViewById(R.id.buttonPlay);
        startGameButton.setOnClickListener(this);

        penetrationButton = findViewById(R.id.buttonPenetration);
        penetrationButton.setOnClickListener(this);

        shoeSizeButton = findViewById(R.id.buttonShoeSize);
        shoeSizeButton.setOnClickListener(this);
    }

    private void startGame(){
        Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);

        Bundle gameBundle = new Bundle();
        gameBundle.putInt("shoeSize", shoeSize);
        gameBundle.putInt("penetration", penetration);

        gameIntent.putExtras(gameBundle);

        startActivity(gameIntent);
    }


}
