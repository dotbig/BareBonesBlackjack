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
        SingleChoiceDialogListener
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
                showSingleChoiceDialogFragment(
                        R.string.shoe_size_select_title,
                        R.array.shoe_sizes,
                        "shoeSize");
                break;
            case(R.id.buttonPenetration):
                showSingleChoiceDialogFragment(
                        R.string.penetration_select_title,
                        R.array.penetration_values,
                        "penetration");
                break;
        }
    }

    private void showSingleChoiceDialogFragment(int titleID, int itemsID, String tag){
        Bundle args = new Bundle();
        args.putInt("title", titleID);
        args.putInt("items", itemsID);

        DialogFragment singleChoiceDialogFragment = new SingleChoiceDialogFragment();
        singleChoiceDialogFragment.setArguments(args);
        singleChoiceDialogFragment.show(getSupportFragmentManager(), tag);
    }

    private void selectShoeSize(int which){
        switch(which){
            case(0):
                setShoeSize(2);
                break;
            case(1):
                setShoeSize(4);
                break;
            case(2):
                setShoeSize(6);
                break;
            case(3):
                setShoeSize(8);
                break;
        }
    }

    private void setShoeSize(int decks){
        shoeSize = decks;
        updateShoeSizeDisplay();
    }

    private int getShoeSize(){
        return 0;
    }

    private void updateShoeSizeDisplay() {
        shoeSizeButton.setText(Integer.toString(shoeSize)+" decks");
    }

    private void selectPenetration(int which){
        switch(which){
            case(0):
                setPenetration(50);
                break;
            case(1):
                setPenetration(60);
                break;
            case(2):
                setPenetration(70);
                break;
            case(3):
                setPenetration(80);
                break;
        }
    }

    private void validatePenetration(int pen){
        if (pen > maxPenetration(getShoeSize())){
            penetration = maxPenetration(getShoeSize());
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

    private void setPenetration(int pen){
        penetration = pen;
        updatePenetrationDisplay();
    }

    private void updatePenetrationDisplay(){
        penetrationButton.setText(Integer.toString(penetration)+"%");
    }

    public void onChoiceSelect(DialogFragment dialog, int which){
        String tag = dialog.getTag();
        System.out.println(tag);
        switch(tag){
            case("shoeSize"):
                System.out.println("case shoeSize");
                selectShoeSize(which);
                break;
            case("penetration"):
                System.out.println("case pen");
                selectPenetration(which);
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
        Bundle gameBundle = new Bundle();
        gameBundle.putInt("shoeSize", shoeSize);
        gameBundle.putInt("penetration", penetration);

        Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);
        gameIntent.putExtras(gameBundle);

        startActivity(gameIntent);
    }


}
