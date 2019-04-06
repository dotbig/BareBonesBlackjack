package dotbig.barebonesblackjack;

import android.support.v4.app.DialogFragment;

public interface SingleChoiceDialogListener {
    void onChoiceSelect(DialogFragment dialog, int which);
}

