package dotbig.barebonesblackjack;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class PenetrationDialogFragment extends DialogFragment {

    public interface PenetrationDialogListener{
        void onPenetrationSelect(DialogFragment dialog, int which);
    }

    PenetrationDialogFragment.PenetrationDialogListener listener;

    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            listener = (PenetrationDialogFragment.PenetrationDialogListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString()
                    + " must implement PenetrationDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Choose penetration percentage");
        builder.setItems(R.array.penetration_values, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onPenetrationSelect(PenetrationDialogFragment.this, which);
            }
        });
        return builder.create();
    }
}
