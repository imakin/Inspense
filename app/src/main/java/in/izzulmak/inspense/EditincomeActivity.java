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
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Calendar;

public class EditincomeActivity extends ActionBarActivity {
    public static Menu editIncomeRoomMenu;
    private ArrayList<Integer> incomesexpenseslist_ids; //-- carries the listed data from `incomesexpenses`
    static int dbv_baseAccount_id;
    String dbv_baseAccount_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editincome);
        new ListIncomeClickedClass(0);
        ListIncomeClickedClass.selected_idsClear();

        incomesexpenseslist_ids = new ArrayList<Integer>();
        LinearLayout container = (LinearLayout) findViewById(R.id.ll_Editincome);
        SQLiteDatabase db = openOrCreateDatabase(getResources().getString(R.string.databasename), MODE_PRIVATE, null);
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH)+1;
        String thismonth = ""+mYear+"-"+String.format("%02d",mMonth)+"-01";
        Cursor dbv_dates = db.rawQuery("SELECT date FROM incomesexpenses WHERE type='INCOME' AND date BETWEEN DATE('"+thismonth+"') AND DATE('"+thismonth+"','+1 month', '-1 day') GROUP BY date; ",null);
        while (dbv_dates.moveToNext())
        {
            String thisdate = dbv_dates.getString(0);

            Button bt = new Button (container.getContext());
            bt.setText(thisdate);
            container.addView(bt);

            final LinearLayout ll = new LinearLayout(container.getContext());
            ll.setOrientation(LinearLayout.VERTICAL);

            final Cursor dbv_thisdate = db.rawQuery(
                    "SELECT incomesexpenses.*, accounts.name FROM incomesexpenses " +
                            "LEFT JOIN accounts ON incomesexpenses.from_account_id=accounts.id " +
                            "WHERE incomesexpenses.type='INCOME' " +
                            "AND incomesexpenses.date='"+thisdate+"' ORDER BY date;"
                    , null);
            int btcid = 500;
            while (dbv_thisdate.moveToNext())
            {
                Integer thisindex = dbv_thisdate.getInt(dbv_thisdate.getColumnIndex("id"));

                ToggleButton btc = new ToggleButton(ll.getContext());
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                btc.setLayoutParams(lp);
                btc.setId(200 + thisindex.intValue()); //-- the edit each item index is 100+`incomesexpenses`.`id`
                incomesexpenseslist_ids.add(thisindex);
                btc.setBackgroundResource(R.layout.listbutton);
                String thetext = dbv_thisdate.getString(dbv_thisdate.getColumnIndex("accounts.name")) + " \t " + dbv_thisdate.getString(dbv_thisdate.getColumnIndex("incomesexpenses.amount"));
                btc.setText(thetext);
                btc.setTextOff(thetext);
                btc.setTextOn("[ "+thetext+" ]");

                //btc.setText(thisindex.toString());/*/
                btc.setOnCheckedChangeListener(new ListIncomeClickedClass(thisindex));//*/
                ll.addView(btc);
            }
            container.addView(ll);
            LinearLayout.LayoutParams lllp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            ll.setLayoutParams(lllp);
            dbv_thisdate.close();
            bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (ll.getLayoutParams().height == 0) {
                        LinearLayout.LayoutParams lllp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                        ll.setLayoutParams(lllp2);
                    } else {
                        LinearLayout.LayoutParams lllp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
                        ll.setLayoutParams(lllp2);
                    }
                }
            });
        }
        dbv_dates.close();
        db.close();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        editIncomeRoomMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editincome, menu);
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
    public void menuEditThis(MenuItem item)
    {
        int id = ListIncomeClickedClass.selected_idsGet(0);
        SQLiteDatabase db = openOrCreateDatabase(getResources().getString(R.string.databasename), MODE_PRIVATE, null);
        Cursor dbv_tobeEdit = db.rawQuery("SELECT incomesexpenses.*, base_accounts.name as bname, income_accounts.name as incname FROM incomesexpenses " +
                "LEFT JOIN accounts as base_accounts ON incomesexpenses.base_account_id=base_accounts.id " +
                "LEFT JOIN accounts as income_accounts ON incomesexpenses.from_account_id=income_accounts.id " +
                "WHERE incomesexpenses.id='" + id + "';", null);
        if (dbv_tobeEdit.moveToNext()) {
            String base_account_id = dbv_tobeEdit.getString(dbv_tobeEdit.getColumnIndex("base_account_id"));
            String base_account_name = dbv_tobeEdit.getString(dbv_tobeEdit.getColumnIndex("bname"));
            String income_account_id = dbv_tobeEdit.getString(dbv_tobeEdit.getColumnIndex("from_account_id"));
            String income_account_name = dbv_tobeEdit.getString(dbv_tobeEdit.getColumnIndex("incname"));
            String amount = dbv_tobeEdit.getString(dbv_tobeEdit.getColumnIndex("amount"));
            String description = dbv_tobeEdit.getString(dbv_tobeEdit.getColumnIndex("description"));
            String date = dbv_tobeEdit.getString(dbv_tobeEdit.getColumnIndex("date"));

            Intent mi = new Intent(EditincomeActivity.this, AddincomeActivity.class);
            mi.putExtra("isEditing",1);
            mi.putExtra("v_account", base_account_name);
            mi.putExtra("v_account_id", base_account_id);
            mi.putExtra("v_income_account_id", income_account_id);
            mi.putExtra("v_income_account_name", income_account_name);
            mi.putExtra("v_amount", amount);
            mi.putExtra("v_description", description);
            mi.putExtra("v_date", date);
            mi.putExtra("v_idEdit", id);

            EditincomeActivity.this.startActivityForResult(mi, 999);
        }
        dbv_tobeEdit.close();
        ListIncomeClickedClass.selected_idsClear();
    }

    public void menuDeleteThese(MenuItem item)
    {
        AlertDialog.Builder confirm = new AlertDialog.Builder(this);
        confirm.setMessage("Are you sure?");
        confirm.setNegativeButton("cancel", null);
        confirm.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SQLiteDatabase db = openOrCreateDatabase(getResources().getString(R.string.databasename), MODE_PRIVATE, null);

                int iter;
                for (iter = 0; iter < ListIncomeClickedClass.selected_idsCount(); iter++) {
                    db.execSQL("DELETE FROM incomesexpenses WHERE id='" + ListIncomeClickedClass.selected_idsGet(iter) + "';");
                    ToggleButton theitem = ((ToggleButton) findViewById(ListIncomeClickedClass.selected_idsGet(iter) + 200));
                    try {
                        theitem.setChecked(false);
                        theitem.setVisibility(View.GONE);
                    }
                    catch (Exception e) {}
                }
                db.close();
                ListIncomeClickedClass.selected_idsClear();
            }
        });
        confirm.show();
    }
}
