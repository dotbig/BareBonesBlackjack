package dotbig.barebonesblackjack;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class SingleChoiceDialogFragment extends DialogFragment {

    int titleID;
    int itemsID;

    SingleChoiceDialogListener dialogListener;

    public void onAttach(Context context){
        super.onAttach(context);
        try{
            dialogListener = (SingleChoiceDialogListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString()
                    + " must implement SingleChoiceDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        //TODO: maybe check for null bundle, throw exception if null
        Bundle args = getArguments();
        titleID = args.getInt("title");
        itemsID = args.getInt("items");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(titleID);
        builder.setItems(itemsID, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogListener.onChoiceSelect(SingleChoiceDialogFragment.this, which);
            }
        });
        return builder.create();
    }
}
