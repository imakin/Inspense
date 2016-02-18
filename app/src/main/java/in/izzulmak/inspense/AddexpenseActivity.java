package in.izzulmak.inspense;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;


public class AddexpenseActivity extends ActionBarActivity {

    Button bt_Addexpense_Date_Pick;
    Button bt_Addexpense_Account;
    Button bt_Addexpense_AccountTrans;
    private int mYear, mMonth, mDay;
    static String v_ExpenseAmount;
    static int v_ExpenseAccount_id;
    static int v_TransferAccount_id;
    static int v_BaseAccount_id;
    static String v_ExpenseDate;
    private int isEditing;
    private int editId;
    private boolean isTransfer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addexpense);

        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addTestDevice("E05BA7C7DB8B7BFC90E0ECE539108CDA").build();
        mAdView.loadAd(adRequest);

        SQLiteDatabase db = openOrCreateDatabase(getResources().getString(R.string.databasename), MODE_PRIVATE, null);

        isTransfer = false;
        Intent ti = getIntent();
        String v_account = ti.getStringExtra("v_account");
        v_BaseAccount_id = ti.getIntExtra("v_account_id",1);
        isEditing = ti.getIntExtra("isEditing",0);

        final TextView tv_Addexpense_Accountname = (TextView) findViewById(R.id.tv_Addexpense_Accountname);
        tv_Addexpense_Accountname.setText(this.getString(R.string.title_expense)+" "+v_account);

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        bt_Addexpense_Date_Pick = (Button) findViewById(R.id.bt_Addexpense_Date_Pick);
        bt_Addexpense_Date_Pick.setText(String.format("%02d",mYear)+ "-" + String.format("%02d",mMonth+1) + "-" + String.format("%02d",mDay));

        bt_Addexpense_Date_Pick.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DatePickerDialog dpd = new DatePickerDialog(AddexpenseActivity.this,
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                        bt_Addexpense_Date_Pick.setText(String.format("%02d",year)+ "-" + String.format("%02d", monthOfYear + 1) + "-" + String.format("%02d",dayOfMonth));
                                    }
                                },
                                mYear, mMonth, mDay
                        );
                        dpd.show();
                    }
                }
        );
        //-- for normal Expense account
        Cursor dbv_accounts = db.rawQuery("SELECT * FROM accounts WHERE enabled=1 AND type='EXPENSE';",null);
        //-- Make ArrayList and push every needed row value
        ArrayList<CharSequence> ALaccounts_list = new ArrayList<CharSequence>();
        ArrayList<Integer> accounts_listID = new ArrayList<Integer>();
        while (dbv_accounts.moveToNext())
        {
            String row = dbv_accounts.getString(dbv_accounts.getColumnIndex("name"));
            ALaccounts_list.add(row);
            accounts_listID.add(dbv_accounts.getInt(dbv_accounts.getColumnIndex("id")));
        }
        //-- covert the ArrayList to an Array
        CharSequence[] accounts_list = new CharSequence[ALaccounts_list.size()];
        accounts_list = ALaccounts_list.toArray(accounts_list);
        final CharSequence[] finalAccounts_list = accounts_list; //-- The array is ready to use in AlertDialog.Builder.setItems
        final ArrayList<Integer> finalAccounts_listID = accounts_listID;

        //-- for transfer Expense account
        Cursor dbv_accountsbase = db.rawQuery("SELECT * FROM accounts WHERE enabled=1 AND type='BASE';",null);
        ArrayList<CharSequence> ALaccountsbase_list = new ArrayList<CharSequence>();
        ArrayList<Integer> accountsbase_listID = new ArrayList<Integer>();
        while (dbv_accountsbase.moveToNext())
        {
            String row = dbv_accountsbase.getString(dbv_accountsbase.getColumnIndex("name"));
            ALaccountsbase_list.add(row);
            accountsbase_listID.add(dbv_accountsbase.getInt(dbv_accountsbase.getColumnIndex("id")));
        }
        CharSequence[] accountsbase_list = new CharSequence[ALaccountsbase_list.size()];
        accountsbase_list = ALaccountsbase_list.toArray(accountsbase_list);
        final CharSequence[] finalAccountsbase_list = accountsbase_list;
        final ArrayList<Integer> finalAccountsbase_listID = accountsbase_listID;


        bt_Addexpense_Account = (Button) findViewById(R.id.bt_Addexpense_Account);
        bt_Addexpense_Account.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddexpenseActivity.this);
                        builder.setTitle("Pick Expense Account Name");
                        builder.setItems(finalAccounts_list,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int item){
                                        bt_Addexpense_Account.setText(finalAccounts_list[item]);
                                        AddexpenseActivity.v_ExpenseAccount_id = finalAccounts_listID.get(item);
                                    }
                                }
                        );
                        builder.show();
                    }
                }
        );

        bt_Addexpense_AccountTrans = (Button) findViewById(R.id.bt_Addexpense_AccountTrans);
        bt_Addexpense_AccountTrans.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddexpenseActivity.this);
                        builder.setTitle("Pick Base Account To Transfer Into");
                        builder.setItems(finalAccountsbase_list,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int item){
                                        bt_Addexpense_AccountTrans.setText(finalAccountsbase_list[item]);
                                        AddexpenseActivity.v_TransferAccount_id = finalAccountsbase_listID.get(item);
                                    }
                                }
                        );
                        builder.show();
                    }
                }
        );


        if (isEditing==1)
        {
            editId = ti.getIntExtra("v_idEdit", -1);
            String v_amount = ti.getStringExtra("v_amount");
            String v_expense_account_id = ti.getStringExtra("v_expense_account_id");
            String v_expense_account_name = ti.getStringExtra("v_expense_account_name");
            String v_description = ti.getStringExtra("v_description");
            String v_date = ti.getStringExtra("v_date");
            String v_type = ti.getStringExtra("v_type");

            /*amount*/                      ((EditText) findViewById(R.id.et_Addexpense_Amount)).setText(v_amount);
            /*expenseaccount id*/           AddexpenseActivity.v_ExpenseAccount_id = Integer.valueOf(v_expense_account_id).intValue();
            /*expenseaccount name (pick)*/  bt_Addexpense_Account.setText(v_expense_account_name);
            /*description*/                 ((EditText) findViewById(R.id.et_Addexpense_Description)).setText(v_description);
            /*date*/                        bt_Addexpense_Date_Pick.setText(v_date);
            if (v_type=="TRANSFEREXPENSE")
            {
                /*transfer account id*/         AddexpenseActivity.v_TransferAccount_id = Integer.valueOf(v_expense_account_id).intValue();
                /*transfer account name*/       bt_Addexpense_AccountTrans.setText(v_expense_account_name);
            }

            ((TextView) findViewById(R.id.tv_Addexpense_IdEdit)).setText(""+editId);
        }

        db.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_addexpense, menu);
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

    public void saveData(View view) {
        String amount = ((EditText) findViewById(R.id.et_Addexpense_Amount)).getText().toString();
        String date = ((Button) findViewById(R.id.bt_Addexpense_Date_Pick)).getText().toString();
        String description = ((EditText) findViewById(R.id.et_Addexpense_Description)).getText().toString();
        int baseid = v_BaseAccount_id;
        int expenseid = v_ExpenseAccount_id;
        String type = "EXPENSE";
        if (isTransfer) {
            expenseid = v_TransferAccount_id;
            type = "TRANSFEREXPENSE";
        }
        SQLiteDatabase db = openOrCreateDatabase(getResources().getString(R.string.databasename), MODE_PRIVATE, null);
        if (isEditing==1)
        {
            db.execSQL("UPDATE incomesexpenses SET " +
                    "from_account_id='"+expenseid+"', " +
                    "description='"+description+"'," +
                    "type='"+type+"'," +
                    "amount='"+amount+"'," +
                    "date='"+date+"' " +
                    "WHERE id="+editId+"");
        }
        else
        {
            Cursor c = db.rawQuery("SELECT * FROM incomesexpenses;", null);
            if (c.getCount() > 0)
                db.execSQL("INSERT INTO incomesexpenses VALUES((SELECT id FROM incomesexpenses ORDER BY id DESC LIMIT 1)+1, '" +
                        baseid + "','" + expenseid + "','" + description + "','"+type+"','" + amount + "','" + date + "')");//--expensetype = 1
            else
                db.execSQL("INSERT INTO incomesexpenses VALUES(1,'" + baseid + "','" + expenseid + "','" + description + "','"+type+"','" + amount + "','" + date + "') ");
            c.close();
        }
        db.execSQL("UPDATE accounts SET balance=balance-" + amount + " WHERE id=" + baseid + ";");
        db.close();
        Intent i = new Intent();
        setResult(RESULT_OK,i);
        finish();
    }
    public void toggleExpense(View view)
    {
        Switch sw = (Switch) findViewById(R.id.sw_Addexpense_Switch);
        if (sw.isChecked()) {
            isTransfer = true;
            ((Button) findViewById(R.id.bt_Addexpense_Account)).setVisibility(Button.GONE);
            ((Button) findViewById(R.id.bt_Addexpense_AccountTrans)).setVisibility(Button.VISIBLE);
        }
        else {
            isTransfer = false;
            ((Button) findViewById(R.id.bt_Addexpense_Account)).setVisibility(Button.VISIBLE);
            ((Button) findViewById(R.id.bt_Addexpense_AccountTrans)).setVisibility(Button.GONE);
        }
    }
}
