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

public class EditexpenseActivity extends ActionBarActivity {
    public static Menu editExpenseRoomMenu;
    private ArrayList<Integer> incomesexpenseslist_ids; //-- carries the listed data from `incomesexpenses`
    static int dbv_baseAccount_id;
    String dbv_baseAccount_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editexpense);
        new ListExpenseClickedClass(0);
        ListExpenseClickedClass.selected_idsClear();

        incomesexpenseslist_ids = new ArrayList<Integer>();
        LinearLayout container = (LinearLayout) findViewById(R.id.ll_Editexpense);
        SQLiteDatabase db = openOrCreateDatabase(getResources().getString(R.string.databasename), MODE_PRIVATE, null);
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH)+1;
        String thismonth = ""+mYear+"-"+String.format("%02d",mMonth)+"-01";
        Cursor dbv_dates = db.rawQuery("SELECT date FROM incomesexpenses WHERE type LIKE '%EXPENSE%' AND date BETWEEN DATE('"+thismonth+"') AND DATE('"+thismonth+"','+1 month', '-1 day') GROUP BY date; ",null);
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
                        "WHERE incomesexpenses.type LIKE '%EXPENSE%' " +
                            "AND incomesexpenses.date='"+thisdate+"' ORDER BY date;"
                    , null);
            int btcid = 500;
            while (dbv_thisdate.moveToNext())
            {
                Integer thisindex = dbv_thisdate.getInt(dbv_thisdate.getColumnIndex("id"));

                ToggleButton btc = new ToggleButton(ll.getContext());
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                btc.setLayoutParams(lp);
                btc.setId(100 + thisindex.intValue()); //-- the edit each item index is 100+`incomesexpenses`.`id`
                incomesexpenseslist_ids.add(thisindex);
                btc.setBackgroundResource(R.layout.listbutton);
                String thetext = dbv_thisdate.getString(dbv_thisdate.getColumnIndex("accounts.name")) + " \t " + dbv_thisdate.getString(dbv_thisdate.getColumnIndex("incomesexpenses.amount"));
                btc.setText(thetext);
                btc.setTextOff(thetext);
                btc.setTextOn("[ "+thetext+" ]");

                //btc.setText(thisindex.toString());/*/
                btc.setOnCheckedChangeListener(new ListExpenseClickedClass(thisindex));//*/
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
        editExpenseRoomMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editexpense, menu);
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
        int id = ListExpenseClickedClass.selected_idsGet(0);
        SQLiteDatabase db = openOrCreateDatabase(getResources().getString(R.string.databasename), MODE_PRIVATE, null);
        Cursor dbv_tobeEdit = db.rawQuery("SELECT incomesexpenses.*, base_accounts.name as bname, expense_accounts.name as expname FROM incomesexpenses " +
                                            "LEFT JOIN accounts as base_accounts ON incomesexpenses.base_account_id=base_accounts.id " +
                                            "LEFT JOIN accounts as expense_accounts ON incomesexpenses.from_account_id=expense_accounts.id " +
                                            "WHERE incomesexpenses.id='" + id + "';", null);
        if (dbv_tobeEdit.moveToNext()) {
            String base_account_id = dbv_tobeEdit.getString(dbv_tobeEdit.getColumnIndex("base_account_id"));
            String base_account_name = dbv_tobeEdit.getString(dbv_tobeEdit.getColumnIndex("bname"));
            String expense_account_id = dbv_tobeEdit.getString(dbv_tobeEdit.getColumnIndex("from_account_id"));
            String expense_account_name = dbv_tobeEdit.getString(dbv_tobeEdit.getColumnIndex("expname"));
            String amount = dbv_tobeEdit.getString(dbv_tobeEdit.getColumnIndex("amount"));
            String description = dbv_tobeEdit.getString(dbv_tobeEdit.getColumnIndex("description"));
            String date = dbv_tobeEdit.getString(dbv_tobeEdit.getColumnIndex("date"));
            String type = dbv_tobeEdit.getString(dbv_tobeEdit.getColumnIndex("type"));

            Intent mi = new Intent(EditexpenseActivity.this, AddexpenseActivity.class);
            mi.putExtra("isEditing",1);
            mi.putExtra("v_account", base_account_name);
            mi.putExtra("v_account_id", base_account_id);
            mi.putExtra("v_expense_account_id", expense_account_id);
            mi.putExtra("v_expense_account_name", expense_account_name);
            mi.putExtra("v_amount", amount);
            mi.putExtra("v_description", description);
            mi.putExtra("v_date", date);
            mi.putExtra("v_idEdit", id);
            mi.putExtra("v_type", type);

            EditexpenseActivity.this.startActivityForResult(mi, 999);
        }
        dbv_tobeEdit.close();
        ListExpenseClickedClass.selected_idsClear();
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
                for (iter = 0; iter < ListExpenseClickedClass.selected_idsCount(); iter++) {
                    db.execSQL("DELETE FROM incomesexpenses WHERE id='" + ListExpenseClickedClass.selected_idsGet(iter) + "';");
                    ToggleButton theitem = ((ToggleButton) findViewById(ListExpenseClickedClass.selected_idsGet(iter) + 100));
                    try {
                        theitem.setChecked(false);
                        theitem.setVisibility(View.GONE);
                    } catch (Exception e) {
                    }
                }
                db.close();
                ListExpenseClickedClass.selected_idsClear();
            }
        });
        confirm.show();
    }
}
