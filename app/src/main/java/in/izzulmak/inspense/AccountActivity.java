package in.izzulmak.inspense;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import in.izzulmak.inspense.input_listeners.ILCancel;

//import in.izzulmak.inspense.input_listeners.Cancel;

public class AccountActivity extends AppCompatActivity {
    public static String EXTRA_IS_EDIT = "v_is_edit";
    public static String EXTRA_ID_EDIT = "v_id_edit";
    public static String EXTRA_NAME_EDIT = "v_name_edit";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void actAdd(String type)
    {
        final String fntype = type.toLowerCase();
        final AlertDialog.Builder inputBuilder = new AlertDialog.Builder(this);
        final EditText et_input = new EditText(this);

        inputBuilder.setView(et_input);
        inputBuilder.setCancelable(false);
        inputBuilder.setPositiveButton("Add "+type.toLowerCase()+" account",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        CharSequence infotext = "input shouldn't be empty";
                        String name = et_input.getText().toString();
                        if (! name.matches("")) {
                            MainActivity.dbmain.execSQL("INSERT INTO accounts VALUES(" +
                                                            "((SELECT id FROM accounts ORDER BY id DESC LIMIT 1)+1)," +
                                                            "'"+name+"', " +
                                                            "'"+fntype.toUpperCase()+"', " +
                                                            "0,1" +
                                                        "); ");
                            infotext = "[" + et_input.getText() + "] saved to " + fntype.toLowerCase() + " account";
                        }
                        Toast toast = Toast.makeText(getApplicationContext(), infotext , Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
            );
        inputBuilder.setNegativeButton("Cancel", ILCancel.get());
                /*
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }
        );*/
        AlertDialog inputDialog = inputBuilder.create();
        inputDialog.show();
    }

    public void actEdit(String type)
    {
        final CharSequence[] accountNames = Ctrl.queryToArray("SELECT name FROM accounts WHERE enabled=1 AND type='" + type + "' ");
        final CharSequence[] accountIds = Ctrl.queryToArray("SELECT id FROM accounts WHERE enabled=1 AND type='" + type + "' ");

        AlertDialog.Builder selector = new AlertDialog.Builder(AccountActivity.this);
        selector.setTitle("Pick "+type+" account name to edit");
        selector.setItems(
                accountNames,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        int id = Integer.valueOf(accountIds[item].toString());
                        String name = accountNames[item].toString();

                        Intent mi = new Intent(AccountActivity.this, AccountmanipulateActivity.class);
                        mi.putExtra(EXTRA_IS_EDIT,1);
                        mi.putExtra(EXTRA_NAME_EDIT,name);
                        mi.putExtra(EXTRA_ID_EDIT,id);
                        AccountActivity.this.startActivity(mi);
                    }
                }
        );
        selector.show();
    }

    public void actAddBase(View view)       { actAdd("BASE"); }
    public void actEditBase(View view)      { actEdit("BASE"); }
    public void actAddExpense(View view)    { actAdd("EXPENSE");}
    public void actEditExpense(View view)   { actEdit("EXPENSE");}
    public void actAddIncome(View view)     { actAdd("INCOME");}
    public void actEditIncome(View view)    { actEdit("INCOME");}
}
