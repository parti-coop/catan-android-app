package xyz.parti.catan.ui.view;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import xyz.parti.catan.R;
import xyz.parti.catan.data.model.Post;
import xyz.parti.catan.helper.KeyboardHelper;
import xyz.parti.catan.ui.presenter.BasePostBindablePresenter;

/**
 * Created by dalikim on 2017. 6. 30..
 */

public class NewOptionFormAlertBuilder {
    public static AlertDialog build(final Context context, final BasePostBindablePresenter presenter, final Post post) {
        View optionDialogLayout = LayoutInflater.from(context).inflate(R.layout.references_survey_option_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context, R.style.AppAlertDialog);
        alertDialogBuilder.setView(optionDialogLayout);

        LinearLayout newOptionFormLinerLayout = (LinearLayout) optionDialogLayout.findViewById(R.id.layout_option_dialog_form);
        ProgressBar optionDialogProgressbar = (ProgressBar) optionDialogLayout.findViewById(R.id.progressbar_option_dialog);
        final ProgressToggler progressToggler = new ProgressToggler(newOptionFormLinerLayout, optionDialogProgressbar);
        progressToggler.toggle(false);

        final EditText newOptionInputEditText = (EditText) optionDialogLayout.findViewById(R.id.edittext_new_option_input);
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.save, null)
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                            }
                        });

        final AlertDialog optionDialog = alertDialogBuilder.create();

        newOptionInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                Button button = optionDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                if (button == null) return;
                if(TextUtils.isEmpty(charSequence)){
                    button.setEnabled(false);
                } else {
                    button.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        optionDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                optionDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
                optionDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
                optionDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

                optionDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        optionDialog.getButton(AlertDialog.BUTTON_POSITIVE).setVisibility(View.GONE);
                        optionDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setVisibility(View.GONE);
                        optionDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        progressToggler.toggle(true);
                        presenter.saveOption(post, newOptionInputEditText.getText().toString());
                    }
                });
                newOptionInputEditText.requestFocus();
                KeyboardHelper.showKey(context, newOptionInputEditText);
            }
        });
        return optionDialog;
    }
}
