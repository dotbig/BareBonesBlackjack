package dotbig.barebonesblackjack;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class InfoDisplayGroup implements TextSettable,
                                        VisibilitySettable,
                                        AlphaSettable {
    ViewGroup group;

    InfoDisplayGroup(ViewGroup group){
        this.group = group;
    }

    public void setVisibility(int visibility){
        group.setVisibility(visibility);
    }

    public void setText(CharSequence text){
        View viewWithTag = group.findViewWithTag("text");
        if (viewWithTag instanceof TextView){
            TextView textViewWithTag = (TextView)viewWithTag;
            textViewWithTag.setText(text);
        }
    }

    public void setAlpha(float alpha){
        group.setAlpha(alpha);
    }
}
