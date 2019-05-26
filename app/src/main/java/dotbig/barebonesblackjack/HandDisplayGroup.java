package dotbig.barebonesblackjack;

import android.support.constraint.ConstraintLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class HandDisplayGroup implements AlphaSettable, VisibilitySettable, TextSettable {
    private ConstraintLayout cardDisplay;
    private ConstraintLayout boxDisplay;
    private TextView betDisplay;
    private ImageView winDisplay;
    private ImageView lossDisplay;

    HandDisplayGroup(ConstraintLayout cardDisplay, ConstraintLayout boxDisplay, TextView betDisplay, ImageView winDisplay, ImageView lossDisplay){
        this.cardDisplay = cardDisplay;
        this.boxDisplay = boxDisplay;
        this.betDisplay = betDisplay;
        this.winDisplay = winDisplay;
        this.lossDisplay = lossDisplay;
    }


    ConstraintLayout getCardDisplay(){
        return cardDisplay;
    }

    TextView getBetDisplay(){
        return betDisplay;
    }

    public void setClickable(boolean enabled){
        cardDisplay.setClickable(enabled);
    }

    public void setAlpha(float alpha){
        cardDisplay.animate().alpha(alpha);
        boxDisplay.animate().alpha(alpha);
        betDisplay.animate().alpha(alpha);
    }

    public void setVisibility(int visibility){
        cardDisplay.setVisibility(visibility);
        boxDisplay.setVisibility(visibility);
        betDisplay.setVisibility(visibility);
    }

    public ImageView getOutcomeDisplay(boolean won){
        if (won){
            return winDisplay;
        } else {
            return lossDisplay;
        }
    }

    public void setText(CharSequence text){
        betDisplay.setText(text);
    }
}
