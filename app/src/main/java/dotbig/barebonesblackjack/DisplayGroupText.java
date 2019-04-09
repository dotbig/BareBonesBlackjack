package dotbig.barebonesblackjack;

import android.view.ViewGroup;
import android.widget.TextView;

public class DisplayGroupText implements TextViewDisplayGroup {
    private ViewGroup group;

    DisplayGroupText(ViewGroup group){
        this.group = group;
    }

    public void setValue(String tag, CharSequence value){
        TextView tv = group.findViewWithTag(tag);
        tv.setText(value);
    }

    public void setVisibility(int visibility){
        group.setVisibility(visibility);
    }
}
