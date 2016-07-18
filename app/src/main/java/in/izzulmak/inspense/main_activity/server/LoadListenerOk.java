package in.izzulmak.inspense.main_activity.server;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.widget.EditText;

import in.izzulmak.inspense.DebugActivity;
import in.izzulmak.inspense.MainActivity;
import in.izzulmak.inspense.input_listeners.ILCancel;

/**
 * Created by Izzulmakin on 05/04/16.
 */
public class LoadListenerOk implements DialogInterface.OnClickListener {
    private AppCompatActivity caller_activity;
    private EditText et_input;
    private boolean isAuthId;
    private String targetAuthId;

    /**
     * Construct the Dialoginterface for use in builder of the Load Inspense.This is the first step
     * and EditText input will be read as auth user
     * @param caller_activity the Activity reference
     * @param input EditText of input reference
     */
    public LoadListenerOk(AppCompatActivity caller_activity, EditText input)
    {
        this.caller_activity = caller_activity;
        et_input = input;
        isAuthId = true;
    }
    /**
     * Construct the Dialoginterface for use in builder. This is the second step and EditText input
     * will be read as auth password
     * @param caller_activity the Activity reference
     * @param input EditText of input reference
     * @param auth_id current step is authenticating password, auth_id shall be defined.
     */
    public LoadListenerOk(AppCompatActivity caller_activity, EditText input, String auth_id)
    {
        this.caller_activity = caller_activity;
        et_input = input;
        isAuthId = false;
        targetAuthId = auth_id;
    }
    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        if (isAuthId) {
            //-- curently auth ID only
            String target_id = et_input.getText().toString();
            final AlertDialog.Builder inputBuilder = new AlertDialog.Builder(caller_activity);
            final EditText et_password = new EditText(caller_activity);
            et_password.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);

            inputBuilder.setView(et_password);
            inputBuilder.setCancelable(true);
            inputBuilder.setPositiveButton("Auth Password", new LoadListenerOk(caller_activity, et_password, target_id));
            inputBuilder.setNegativeButton("Cancel", ILCancel.get());
            AlertDialog inputDialog = inputBuilder.create();
            inputDialog.show();
        }
        else {
            //-- got both ID and password
            String target_pw = et_input.getText().toString();
            LoadModel.object().loadInspenseDo(targetAuthId, target_pw, caller_activity);

            Intent mi = new Intent(caller_activity, DebugActivity.class);
            caller_activity.startActivity(mi);
        }
    }
}
