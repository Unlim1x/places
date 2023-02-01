package ru.lim1x.places.ui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;




public class OneButtonDialog extends DialogFragment {

    String title;
    String msg;
    String button_text;
    int drawable;
    public OneButtonDialog(@NonNull String title,@NonNull String msg,@NonNull String button_text, @DrawableRes int id){
        this.title = title;
        this.msg = msg;
        this.button_text = button_text;
        drawable = id;
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setMessage(msg)
                .setIcon(drawable)
                .setPositiveButton(button_text, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Закрываем окно
                        dialog.cancel();
                    }
                });
        return builder.create();
    }

}
