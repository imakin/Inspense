package in.izzulmak.inspense.input_listeners;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

import in.izzulmak.inspense.MainActivity;
import in.izzulmak.inspense.main_activity.model.ModelSaveInspense;

/**
 * Created by Izzulmakin on 24/03/16.
 */
public class ILSaveInspenseOk implements DialogInterface.OnClickListener {
    private MainActivity mainActivity;
    private EditText et_input;
    private boolean isAuthId;
    private String targetAuthId;

    /**
     * Construct the Dialoginterface for use in builder of the Save Inspense.This is the first step
     * and EditText input will be read as auth user
     * @param main_activity the MainActivity reference
     * @param input EditText of input reference
     */
    public ILSaveInspenseOk(MainActivity main_activity, EditText input)
    {
        mainActivity = main_activity;
        et_input = input;
        isAuthId = true;
    }
    /**
     * Construct the Dialoginterface for use in builder. This is the second step and EditText input
     * will be read as auth password
     * @param main_activity the MainActivity reference
     * @param input EditText of input reference
     * @param auth_id current step is authenticating password, auth_id shall be defined.
     */
    public ILSaveInspenseOk(MainActivity main_activity, EditText input, String auth_id)
    {
        mainActivity = main_activity;
        et_input = input;
        isAuthId = false;
        targetAuthId = auth_id;
    }
    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (isAuthId) {
            String target_id = et_input.getText().toString();
            final AlertDialog.Builder inputBuilder = new AlertDialog.Builder(mainActivity);
            final EditText et_password = new EditText(mainActivity);
            et_password.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);

            inputBuilder.setView(et_password);
            inputBuilder.setCancelable(true);
            inputBuilder.setPositiveButton("Auth Password", new ILSaveInspenseOk(mainActivity, et_password, target_id));
            inputBuilder.setNegativeButton("Cancel", ILCancel.get());
            AlertDialog inputDialog = inputBuilder.create();
            inputDialog.show();
        }
        else {
            String target_pw = et_input.getText().toString();
            ModelSaveInspense.object().saveInspenseDo(targetAuthId, target_pw, mainActivity);
            //mainActivity.saveInspenseDo(targetAuthId, target_pw);
        }
        /*
        CharSequence warning = "Wrong password auth"; // TODO auth this
        warning = target_id;
        if (!target_id.matches("")) {
        }
        mainActivity.saveInspenseDo(target_id);
        mainActivity.debugToast(warning.toString());
       */
    }
};
