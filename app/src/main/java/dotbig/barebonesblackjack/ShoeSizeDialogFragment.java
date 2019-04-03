package dotbig.barebonesblackjack;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.app.AlertDialog;
import android.app.Dialog;

public class ShoeSizeDialogFragment extends DialogFragment {

    public interface ShoeDialogListener{
        void onShoeSizeSelect(DialogFragment dialog, int which);
    }

    ShoeDialogListener listener;

    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            listener = (ShoeDialogListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString()
                    + " must implement ShoeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Choose number of decks");
        builder.setItems(R.array.shoe_sizes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onShoeSizeSelect(ShoeSizeDialogFragment.this, which);
            }
        });
        return builder.create();
    }
}
