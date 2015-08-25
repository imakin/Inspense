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
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;


public class AddincomeActivity extends ActionBarActivity {

    Button bt_Addincome_Date_Pick;
    Button bt_Addincome_Account;
    private int mYear, mMonth, mDay;
    static String v_IncomeAmount;
    static int v_IncomeAccount_id;
    static int v_BaseAccount_id;
    static String v_IncomeDate;
    private int isEditing;
    private int editId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addincome);

        SQLiteDatabase db = openOrCreateDatabase(getResources().getString(R.string.databasename), MODE_PRIVATE, null);

        Intent ti = getIntent();
        String v_account = ti.getStringExtra("v_account");
        v_BaseAccount_id = ti.getIntExtra("v_account_id",1);
        isEditing = ti.getIntExtra("isEditing",0);

        final TextView tv_Addincome_Accountname = (TextView) findViewById(R.id.tv_Addincome_Accountname);
        tv_Addincome_Accountname.setText(this.getString(R.string.title_income)+" "+v_account);

        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        bt_Addincome_Date_Pick = (Button) findViewById(R.id.bt_Addincome_Date_Pick);
        bt_Addincome_Date_Pick.setText(String.format("%02d",mYear)+ "-" + String.format("%02d",mMonth+1) + "-" + String.format("%02d",mDay));

        bt_Addincome_Date_Pick.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        DatePickerDialog dpd = new DatePickerDialog(AddincomeActivity.this,
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                        bt_Addincome_Date_Pick.setText(String.format("%02d",year)+ "-" + String.format("%02d", monthOfYear + 1) + "-" + String.format("%02d",dayOfMonth));
                                    }
                                },
                                mYear, mMonth, mDay
                        );
                        dpd.show();
                    }
                }
        );

        Cursor dbv_accounts = db.rawQuery("SELECT * FROM accounts WHERE type='INCOME';",null);
        //-- Make ArrayList and push every needed row value
        ArrayList<CharSequence> ALaccounts_list = new ArrayList<CharSequence>();
        ArrayList<Integer> accounts_listID = new ArrayList<Integer>();
        while (dbv_accounts.moveToNext())
        {
            String row = dbv_accounts.getString(dbv_accounts.getColumnIndex("name"));
            ALaccounts_list.add(row);
            accounts_listID.add(dbv_accounts.getInt(dbv_accounts.getColumnIndex("id")));
        }
        db.close();
        //-- covert the ArrayList to an Array
        CharSequence[] accounts_list = new CharSequence[ALaccounts_list.size()];
        accounts_list = ALaccounts_list.toArray(accounts_list);
        final CharSequence[] finalAccounts_list = accounts_list; //-- The array is ready to use in AlertDialog.Builder.setItems
        final ArrayList<Integer> finalAccounts_listID = accounts_listID;


        bt_Addincome_Account = (Button) findViewById(R.id.bt_Addincome_Account);
        bt_Addincome_Account.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(AddincomeActivity.this);
                        builder.setTitle("Pick Income Account Name");
                        builder.setItems(finalAccounts_list,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int item){
                                        bt_Addincome_Account.setText(finalAccounts_list[item]);
                                        AddincomeActivity.v_IncomeAccount_id = finalAccounts_listID.get(item);
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
            String v_income_account_id = ti.getStringExtra("v_income_account_id");
            String v_income_account_name = ti.getStringExtra("v_income_account_name");
            String v_description = ti.getStringExtra("v_description");
            String v_date = ti.getStringExtra("v_date");

            /*amount*/                      ((EditText) findViewById(R.id.et_Addincome_Amount)).setText(v_amount);
            /*incomeaccount id*/           AddincomeActivity.v_IncomeAccount_id = Integer.valueOf(v_income_account_id).intValue();
            /*incomeaccount name (pick)*/  bt_Addincome_Account.setText(v_income_account_name);
            /*description*/                 ((EditText) findViewById(R.id.et_Addincome_Description)).setText(v_description);
            /*date*/                        bt_Addincome_Date_Pick.setText(v_date);

            ((TextView) findViewById(R.id.tv_Addincome_IdEdit)).setText(""+editId);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_addincome, menu);
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
        String amount = ((EditText) findViewById(R.id.et_Addincome_Amount)).getText().toString();
        String date = ((Button) findViewById(R.id.bt_Addincome_Date_Pick)).getText().toString();
        String description = ((EditText) findViewById(R.id.et_Addincome_Description)).getText().toString();

        SQLiteDatabase db = openOrCreateDatabase(getResources().getString(R.string.databasename), MODE_PRIVATE, null);
        if (isEditing==1)
        {
            db.execSQL("UPDATE incomesexpenses SET from_account_id='"+v_IncomeAccount_id+"', " +
                    "description='"+description+"'," +
                    "amount='"+amount+"'," +
                    "date='"+date+"' " +
                    "WHERE id="+editId+"");
        }
        else
        {
            Cursor c = db.rawQuery("SELECT * FROM incomesexpenses;", null);
            if (c.getCount() > 0)
                db.execSQL("INSERT INTO incomesexpenses VALUES((SELECT id FROM incomesexpenses ORDER BY id DESC LIMIT 1)+1, '" +
                        v_BaseAccount_id + "','" + v_IncomeAccount_id + "','" + description + "','INCOME','" + amount + "','" + date + "')");//--incometype = 1
            else
                db.execSQL("INSERT INTO incomesexpenses VALUES(1,'" + v_BaseAccount_id + "','" + v_IncomeAccount_id + "','" + description + "','INCOME','" + amount + "','" + date + "') ");
            c.close();
        }
        db.execSQL("UPDATE accounts SET balance=balance-" + amount + " WHERE id=" + v_BaseAccount_id + ";");
        db.close();
        Intent i = new Intent();
        setResult(RESULT_OK,i);
        finish();
    }
}
