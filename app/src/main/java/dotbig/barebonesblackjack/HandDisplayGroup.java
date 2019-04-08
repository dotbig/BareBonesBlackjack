package dotbig.barebonesblackjack;

import android.support.constraint.ConstraintLayout;
import android.widget.TextView;

public class HandDisplayGroup implements HandDisplay {

    private ConstraintLayout layout;
    private TextView cardsView;
    private TextView statusView;

    HandDisplayGroup(ConstraintLayout layout){
        this.layout = layout;
        cardsView = (TextView)layout.findViewWithTag("cards");
        statusView = (TextView)layout.findViewWithTag("status");
    }

    public void setCards(CharSequence cards){
        cardsView.setText(cards);
    }

    public void setStatus(CharSequence status){
        statusView.setText(status);
    }

    public void setAlpha(float alpha){
        layout.setAlpha(alpha);
    }

    public void setVisibility(int visibility){
        layout.setVisibility(visibility);
    }

}
