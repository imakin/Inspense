package in.izzulmak.inspense;

import android.app.AlertDialog;
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
import android.widget.FrameLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;


public class ChangebaseaccountActivity extends ActionBarActivity {
    FrameLayout fr_Add;
    Button bt_Cba_Baseaccount;
    TextView tv_Accountname;
    static int baseAccount;

    private ArrayList<CharSequence> fromCursorToArrayListString(Cursor c, int colindextoget){
        ArrayList<CharSequence> result = new ArrayList<CharSequence>();
        c.moveToFirst();
        for(int i = 0; i < c.getCount(); i++){
            String row = c.getString(colindextoget);
            result.add(row);
            c.moveToNext();
        }
        return result;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changebaseaccount);

        fr_Add = (FrameLayout) findViewById(R.id.fr_Cba_Add);
        fr_Add.setVisibility(FrameLayout.INVISIBLE);

        Button bt_Cba_Add = (Button) findViewById(R.id.bt_Cba_Add);
        bt_Cba_Baseaccount = (Button) findViewById(R.id.bt_Cba_Baseaccount);
        tv_Accountname = (TextView) findViewById(R.id.tv_Accountname);


        //-- set to used base account
        Intent i = getIntent();
        bt_Cba_Baseaccount.setText(i.getStringExtra("v_baseaccount_name"));

        //-- Draw all list of base account
        SQLiteDatabase db = openOrCreateDatabase(getResources().getString(R.string.databasename),MODE_PRIVATE,null);
        Cursor dbv_accounts = db.rawQuery("SELECT * FROM accounts WHERE type='BASE'",null);
        //-- Make ArrayList and push every needed row value
        ArrayList<CharSequence> ALaccounts_list = new ArrayList<CharSequence>();
        ArrayList<Integer> accounts_listID = new ArrayList<Integer>();
        while (dbv_accounts.moveToNext())
        {
            String row = dbv_accounts.getString(dbv_accounts.getColumnIndex("name"));
            String balance = dbv_accounts.getString(dbv_accounts.getColumnIndex("balance"));
            ALaccounts_list.add(row+" ["+balance+"]");
            accounts_listID.add(dbv_accounts.getInt(dbv_accounts.getColumnIndex("id")));
        }
        db.close();
        //-- covert the ArrayList to an Array
        CharSequence[] accounts_list = new CharSequence[ALaccounts_list.size()];
        accounts_list = ALaccounts_list.toArray(accounts_list);
        final CharSequence[] finalAccounts_list = accounts_list; //-- The array is ready to use in AlertDialog.Builder.setItems
        final ArrayList<Integer> finalAccounts_listID = accounts_listID;
        bt_Cba_Baseaccount.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(ChangebaseaccountActivity.this);
                        builder.setTitle("Pick Base Account Name");
                        builder.setItems(
                                finalAccounts_list,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int item){
                                        bt_Cba_Baseaccount.setText(finalAccounts_list[item]);
                                        ChangebaseaccountActivity.baseAccount = finalAccounts_listID.get(item);
                                    }
                                }
                        );
                        builder.show();
                    }
                }
        );

        Button bt_Cba_Save = (Button) findViewById(R.id.bt_Cba_Save);
        bt_Cba_Save.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent();
                        i.putExtra("v_new_baseaccount_id", ChangebaseaccountActivity.baseAccount);
                        setResult(RESULT_OK,i);
                        finish();
                    }
                }
        );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_changebaseaccount, menu);
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

    public void showFrameAdd(View view){
        fr_Add.setVisibility(FrameLayout.VISIBLE);
    }


    public void dbCreateAccount(View view){
        String newaccount = (((TextView) findViewById(R.id.et_Cba_Add_Name)).getText()).toString();
        if (newaccount!="") {
            SQLiteDatabase db = openOrCreateDatabase(getResources().getString(R.string.databasename), MODE_PRIVATE, null);
            db.execSQL("INSERT INTO accounts VALUES((SELECT id FROM accounts ORDER BY id DESC LIMIT 1)+1, '" + newaccount + "', 'BASE', 0, 1)");
            db.close();
        }
    }
}
