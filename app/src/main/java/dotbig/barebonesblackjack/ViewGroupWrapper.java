package dotbig.barebonesblackjack;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ViewGroupWrapper implements TextSettable,
                                        VisibilitySettable,
                                        AlphaSettable {

    private ViewGroup group;

    ViewGroupWrapper(ViewGroup group){
        this.group = group;
    }

    public void setVisibility(int visibility){
        group.setVisibility(visibility);
    }

    public void setText(String tag, CharSequence text){
        View viewWithTag = group.findViewWithTag(tag);
        if (viewWithTag instanceof TextView){
            TextView textViewWithTag = (TextView)viewWithTag;
            textViewWithTag.setText(text);
        }
    }

    public void setAlpha(float alpha){
        group.setAlpha(alpha);
    }

}
