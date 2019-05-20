package dotbig.barebonesblackjack;

import android.support.constraint.ConstraintLayout;
import android.widget.TextView;

public class HandDisplayGroup implements AlphaSettable, VisibilitySettable, TextSettable {
    private ConstraintLayout cardDisplay;
    private ConstraintLayout boxDisplay;
    private TextView betDisplay;

    HandDisplayGroup(ConstraintLayout cardDisplay, ConstraintLayout boxDisplay, TextView betDisplay){
        this.cardDisplay = cardDisplay;
        this.boxDisplay = boxDisplay;
        this.betDisplay = betDisplay;
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

    public void setText(CharSequence text){
        betDisplay.setText(text);
    }
}
