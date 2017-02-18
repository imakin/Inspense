package in.izzulmak.inspense.console;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Calendar;

import in.izzulmak.inspense.MainActivity;

/**
 * Created by Izzulmakin on 19/02/17.
 */
public class ConsoleChangedListener implements TextWatcher, Runnable {
    public Handler handler = new Handler(Looper.getMainLooper() /*UI thread*/);
    Runnable workRunnable;
    String consoleString;
    Context selfcontext;
    EditText et_debugconsole;
    public ConsoleChangedListener(Context context, EditText et_debugconsole) {
        selfcontext = context;
        this.et_debugconsole = et_debugconsole;
    }

    public void setConsoleString(String s) {
        consoleString = s.toLowerCase();
    }
    @Override
    public void run() {
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        final EditText console = et_debugconsole;

        if (consoleString.trim().endsWith(" from")) {
            //--  table
            final CharSequence[] finalTableList = {"accounts", "incomesexpenses", "settings"};
            AlertDialog.Builder builder = new AlertDialog.Builder(selfcontext);
            builder.setTitle("Pick Account Name");
            builder.setItems(finalTableList,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item){
                            consoleString = consoleString + " "+finalTableList[item]+" ";
                            console.setText(consoleString);
                            console.setSelection(consoleString.length());
                        }
                    }
            );
            builder.show();
        }
        else if (consoleString.trim().endsWith("incomesexpenses where")) {
            final CharSequence[] finalTableList = {"from_account_id", "base_account_id", "amount", "date"};
            AlertDialog.Builder builder = new AlertDialog.Builder(selfcontext);
            builder.setTitle("Pick Account Name");
            builder.setItems(finalTableList,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item){
                            consoleString = consoleString + " "+finalTableList[item]+" ";
                            console.setText(consoleString);
                            console.setSelection(consoleString.length());
                        }
                    }
            );
            builder.show();
        }
        else if (consoleString.trim().endsWith(" date")) {
            DatePickerDialog dpd = new DatePickerDialog(selfcontext,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            int cursorpos = (consoleString.length());
                            String y = String.valueOf(year);
                            monthOfYear += 1;
                            String m = monthOfYear<10?"0"+String.valueOf(monthOfYear):String.valueOf(monthOfYear);
                            String d = dayOfMonth<10?"0"+String.valueOf(dayOfMonth):String.valueOf(dayOfMonth);
                            consoleString = consoleString +
                                    "  DATE(\""+
                                        y+"-"+
                                        m+"-"+
                                        d+
                                    "\")";

                            console.setText(consoleString);
                            console.setSelection(cursorpos);
                        }
                    },
                    mYear, mMonth, mDay
            );
            dpd.show();
        }
        else if (consoleString.trim().endsWith(" from_account_id") || consoleString.trim().endsWith(" base_account_id")) {
            //--  account
            Cursor dbv_accounts;
            if (consoleString.trim().endsWith(" base_account_id"))
                dbv_accounts = MainActivity.dbmain.rawQuery("SELECT * FROM accounts WHERE enabled=1 AND type='BASE';", null);
            else
                dbv_accounts = MainActivity.dbmain.rawQuery("SELECT * FROM accounts WHERE enabled=1 AND type!='BASE';", null);

            //-- Make ArrayList and push every needed row value
            ArrayList<CharSequence> ALaccounts_list = new ArrayList<CharSequence>();
            ArrayList<Integer> accounts_listID = new ArrayList<Integer>();
            while (dbv_accounts.moveToNext())
            {
                String row = dbv_accounts.getString(dbv_accounts.getColumnIndex("name"));
                int id = dbv_accounts.getInt(dbv_accounts.getColumnIndex("id"));
                ALaccounts_list.add(row);
                accounts_listID.add(id);
            }
            //-- covert the ArrayList to an Array
            CharSequence[] accounts_list = new CharSequence[ALaccounts_list.size()];
            accounts_list = ALaccounts_list.toArray(accounts_list);
            final CharSequence[] finalAccounts_list = accounts_list; //-- The array is ready to use in AlertDialog.Builder.setItems
            final ArrayList<Integer> finalAccounts_listID = accounts_listID;
            AlertDialog.Builder builder = new AlertDialog.Builder(selfcontext);
            builder.setTitle("Pick Account Name");
            builder.setItems(finalAccounts_list,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int item){
                            consoleString = consoleString + "="+finalAccounts_listID.get(item)+" ";
                            console.setText(consoleString);
                            console.setSelection(consoleString.length());
                        }
                    }
            );
            builder.show();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        handler.removeCallbacks(this);
        this.setConsoleString(editable.toString());
        handler.postDelayed(this, 500);
    }
}
