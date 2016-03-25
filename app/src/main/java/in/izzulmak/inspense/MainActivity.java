package in.izzulmak.inspense;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Settings;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import in.izzulmak.inspense.input_listeners.ILCancel;
import in.izzulmak.inspense.input_listeners.ILSaveInspenseOk;
import in.izzulmak.inspense.main_activity.MAPlaceholderFragment;
import in.izzulmak.inspense.main_activity.MASectionsPagerAdapter;


public class MainActivity extends AppCompatActivity {

    public static int dbv_baseAccount_id;
    public static int baseAccount_pos;//--page position
    public static int closeboook_date;
    public static int reportPickedMonth=0;
    public static int reportPickedYear=0;
    public static String dbv_baseAccount_name;
    public final static int ROOM_ADDEXPENSE_ID = 0;
    public final static int ROOM_ADDINCOME_ID = 1;
    public final static int ROOM_CHANGEBASEACCOUNT_ID = 2;
    public static SQLiteDatabase dbmain;
    public static AdRequest adRequest;
    static int firstRefresh = 0;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    MASectionsPagerAdapter mSectionsPagerAdapter;
    /*** The {@link ViewPager} that will host the section contents.*/
    ViewPager mViewPager;
    /**
     * place holder fragment reference. Instantiated in mSectionsPagerAdapter.
     * to
     */
    MAPlaceholderFragment mPlaceholderFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);

        dbmain = openOrCreateDatabase(getResources().getString(R.string.databasename),MODE_PRIVATE,null);


        /* reset
        dbmain.execSQL("DROP TABLE IF EXISTS accounts;");
        dbmain.execSQL("DROP TABLE IF EXISTS account_balances;");
        dbmain.execSQL("DROP TABLE IF EXISTS incomesexpenses;");
        dbmain.execSQL("DROP TABLE IF EXISTS settings;"); //*/
        Cursor dbc_Accounttable = dbmain.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='accounts';",null);
        if (dbc_Accounttable.getCount()<1)
        {
            //-- no table yet, let's create all of them
            dbmain.execSQL("CREATE TABLE IF NOT EXISTS accounts(id INT, name VARCHAR, type VARCHAR, balance INT, enabled BOOLEAN)"); //-- Main accounts
            dbmain.execSQL("CREATE TABLE IF NOT EXISTS account_balances(id INT, base_account_id INT, balance_before INT, balance INT, date DATE)");
            dbmain.execSQL("CREATE TABLE IF NOT EXISTS incomesexpenses(id INT, base_account_id INT, from_account_id INT, description VARCHAR, type VARCHAR, amount INT, date DATE)");
            dbmain.execSQL("CREATE TABLE IF NOT EXISTS settings(name VARCHAR, value VARCHAR)");

            dbmain.execSQL("INSERT INTO accounts VALUES(1, 'Cash in Hand', 'BASE', 0, 1)");
            dbmain.execSQL("INSERT INTO accounts VALUES(2, 'Bank', 'BASE', 0 ,1)");
            dbmain.execSQL("INSERT INTO accounts VALUES(8, 'e-Money', 'BASE', 0 ,1)");

            //-- basic accounts
            dbmain.execSQL("INSERT INTO accounts VALUES(3, 'Main Income',      'INCOME',   0, 1)");
            dbmain.execSQL("INSERT INTO accounts VALUES(4, 'Job Salary',       'INCOME',   0, 1)");
            dbmain.execSQL("INSERT INTO accounts VALUES(5, 'Remaining Cash',   'INCOME',   0, 1)"); //-- remaining cash in hand
            dbmain.execSQL("INSERT INTO accounts VALUES(6, 'Eating',           'EXPENSE',  0, 1)");
            dbmain.execSQL("INSERT INTO accounts VALUES(7, 'Transportation',   'EXPENSE',  0, 1)");

            dbmain.execSQL("INSERT INTO settings VALUES('base_account', 1)");
            dbmain.execSQL("INSERT INTO settings VALUES('base_account_page','0')");
            dbmain.execSQL("INSERT INTO settings VALUES('close_date','1')");
        }
//        dbmain.execSQL("CREATE TABLE IF NOT EXISTS settings(name VARCHAR, value VARCHAR)");
//        dbmain.execSQL("INSERT INTO settings VALUES('base_account', 1)");
//        dbmain.execSQL("INSERT INTO settings VALUES('base_account_page','0')");
//        dbmain.execSQL("INSERT INTO settings VALUES('close_date','1')");
        dbc_Accounttable.close();

        Cursor dbc_IndexCreated = dbmain.rawQuery("SELECT name FROM settings WHERE name=?;", new String[]{"index_created"});
        if (!dbc_IndexCreated.moveToNext()) {
            //-- delete duplicates
            Cursor dbc_duplicates = dbmain.rawQuery("SELECT name,value FROM settings GROUP BY name",null);
            while (dbc_duplicates.moveToNext())
            {
                String settingname = dbc_duplicates.getString(0);
                String settingvalue = dbc_duplicates.getString(1);//-- grab the first value
                Cursor dbc_dup = dbmain.rawQuery("SELECT name,value FROM settings WHERE name=? ",new String[] {settingname});
                if (dbc_dup.getCount()>1)
                {
                    dbc_dup.moveToNext();
                    String lastValueN =  dbc_dup.getString(0);
                    String lastValueV =  dbc_dup.getString(1);

                    dbmain.execSQL("DELETE FROM settings WHERE name='"+lastValueN+"'");
                    dbmain.execSQL("INSERT OR REPLACE INTO settings VALUES('"+lastValueN+"', '"+lastValueV+"') ");
                    debugToast("Duplicate found, fixing for settings."+settingname);
                }
                dbc_dup.close();
            }
            dbc_duplicates.close();
            dbmain.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS name ON settings (name)");
//            ContentValues thisInstantioationsSucks = new ContentValues();
//            thisInstantioationsSucks.put("name","index_created");
//            thisInstantioationsSucks.put("values","1");
//            dbmain.insert("settings", null, thisInstantioationsSucks);
            dbmain.execSQL("INSERT OR REPLACE INTO settings VALUES('index_created',1)");
        }
        dbc_IndexCreated.close();

        //-- close book date, where sum is restarted
        Cursor dbc_closedate = dbmain.rawQuery("SELECT value FROM settings WHERE name='close_date';",null);
        if (dbc_closedate.moveToNext())
        {
            closeboook_date = dbc_closedate.getInt(0);
        }
        else
        {
            dbmain.execSQL("INSERT INTO settings VALUES('close_date', 1)");
            closeboook_date = 1;
        }
        dbc_closedate.close();

        //--active baseaccount, deprecated. we use baseAccount_pos now
        Cursor dbc_baseAccount = dbmain.rawQuery("SELECT settings.value,settings.name,accounts.name FROM settings,accounts WHERE settings.name='base_account' AND accounts.id=settings.value",null);
        dbc_baseAccount.moveToNext();
        dbv_baseAccount_id = dbc_baseAccount.getInt(0);
        dbv_baseAccount_name = dbc_baseAccount.getString(2);
        dbc_baseAccount.close();
        Cursor dbc_basePage = dbmain.rawQuery("SELECT value FROM settings WHERE name='base_account_page';",null);
        if (dbc_basePage.moveToNext())
            baseAccount_pos = dbc_basePage.getInt(0);
        dbc_basePage.close();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new MASectionsPagerAdapter(getSupportFragmentManager(), this);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        //refreshBaseAccount();
        firstRefresh = 0;
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (firstRefresh!=0)
                    MAPlaceholderFragment.refreshPage();
                saveCurrentPage(position);
                baseAccount_pos = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        final Calendar c = Calendar.getInstance();
        if (reportPickedYear==0)
            reportPickedYear = c.get(Calendar.YEAR);
        if (reportPickedMonth==0)
            reportPickedMonth = c.get(Calendar.MONTH) +1;
        adRequest = new AdRequest.Builder().addTestDevice("E05BA7C7DB8B7BFC90E0ECE539108CDA").build();


        if (baseAccount_pos!=0)
        {
            mViewPager.setCurrentItem(baseAccount_pos);
            firstRefresh= 1;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
//        debugToast("Triggered result");
        super.onActivityResult(requestCode, resultCode, data);
        mViewPager.setCurrentItem(baseAccount_pos);
        MAPlaceholderFragment.updateBalanceTM();
        if (resultCode!= Activity.RESULT_OK)
            return;
        switch (requestCode) {
            case (ROOM_CHANGEBASEACCOUNT_ID) :
            {
                int newbaseaccount = data.getIntExtra("v_new_baseaccount_id",-1);
                if (newbaseaccount!=-1) {
                    dbmain.execSQL("UPDATE settings SET value=" + newbaseaccount + " WHERE name='base_account';");

                    Cursor dbc_baseAccount = dbmain.rawQuery("SELECT settings.value,settings.name,accounts.name FROM settings,accounts WHERE settings.name='base_account' AND accounts.id=settings.value", null);
                    dbc_baseAccount.moveToNext();
                    dbv_baseAccount_id = dbc_baseAccount.getInt(0);
                    dbv_baseAccount_name = dbc_baseAccount.getString(2);
                    dbc_baseAccount.close();
                    //tv_Accountname.setText("Base account: " + dbv_baseAccount_name);
                }
                break;
            }
        }
        //updateBalanceTM();
        resetRoom();
    }

    public void saveCurrentPage()
    {
        int position = mViewPager.getCurrentItem();
        baseAccount_pos = position;
//        dbmain.execSQL("INSERT OR REPLACE INTO settings(name,value) VALUES('base_account_page','" + position + "')");
//        dbmain.execSQL("INSERT OR REPLACE INTO settings(name,value) " +
//                "VALUES((SELECT name FROM settings WHERE name='base_account_page'),'" + position + "')");
        saveCurrentPage(position);
    }
    public void saveCurrentPage(int position)
    {
        baseAccount_pos = position;
        dbmain.execSQL("INSERT OR REPLACE INTO settings(name,value) " +
                "VALUES((SELECT name FROM settings WHERE name='base_account_page'),'" + position + "')");
//        ContentValues thisInstantiationSucks = new ContentValues();
//        thisInstantiationSucks.put("name", "base_account_page");
//        thisInstantiationSucks.put("values", position);
//        dbmain.replace("settings", null, thisInstantiationSucks);
    }

    public void refreshBaseAccount()
    {
//        int position = mViewPager.getCurrentItem();
//        baseAccount_pos = position;
//        dbmain.execSQL("INSERT OR REPLACE INTO settings(name,value) VALUES('base_account_page','" + position + "')");
//
        Cursor dbc_Base = dbmain.rawQuery("SELECT id,name FROM accounts WHERE enabled=1 AND type='BASE' ", null);
        if (baseAccount_pos<dbc_Base.getCount())
        {
            dbc_Base.moveToPosition(baseAccount_pos);
            dbv_baseAccount_name = dbc_Base.getString(1);
            dbv_baseAccount_id = dbc_Base.getInt(0);
        }
        dbc_Base.close();
    }
    /**
     * Optional feature to backup database to firebase
     * @param item related to menu selection triggered this
     * TODO: use dialog and inflat, because only one setView and it applies the whole dialog
     * TODO: check Identifier is authenticated or not
     */
    public void saveInspense(MenuItem item) {
        final AlertDialog.Builder inputBuilder = new AlertDialog.Builder(this);
        final EditText et_id = new EditText(this);

        inputBuilder.setView(et_id);
        inputBuilder.setCancelable(true);
        inputBuilder.setPositiveButton("Auth ID", new ILSaveInspenseOk(this, et_id));
        inputBuilder.setNegativeButton("Cancel", ILCancel.get());
        AlertDialog inputDialog = inputBuilder.create();
        inputDialog.show();
    }

    /**
     * The saving process after done the dialog. Called by MainActivity.saveInspense(MenuItem)
     * with no target_id set, default to current phone android id
     */
    public void saveInspenseDo() {
        String thisphid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String pw = "no password";/* firebase do backup */

        Firebase fb_ref = new Firebase("https://inspense.firebaseio.com/");
        Firebase fb_thisref = fb_ref.child(thisphid);
        Cursor dbc_datamaster = dbmain.rawQuery("SELECT name FROM sqlite_master WHERE type='table'",null);
        while (dbc_datamaster.moveToNext())
        {
            String dbv_table = dbc_datamaster.getString(0);
            Cursor dbc_datatable = dbmain.rawQuery("SELECT * FROM " + dbv_table + "", null);
            int dbrow = 0;
            while(dbc_datatable.moveToNext()) {
                for (int i = 0; i < dbc_datatable.getColumnCount(); i++) {
                    fb_thisref.
                            child(dbv_table).
                            child(Integer.toString(dbrow)).
                            child("col"+i).
                            setValue(dbc_datatable.getString(i));
                }
                dbrow += 1;
            }
            dbc_datatable.close();
        }
        dbc_datamaster.close();
        debugToast("Data Saved (using your internet)");
    }

    /**
     * The saving process after done the dialog. Called by MainActivity.saveInspense(MenuItem)
     * @param target_id the identifier of the data to be saved on cloud
     */
    public void saveInspenseDo(String target_id, final String target_pw) {
        /* firebase do backup */
        Firebase fb_ref = new Firebase("https://inspense.firebaseio.com/");
        Firebase fb_thisref = fb_ref.child(target_id);
        //fb_thisref.setValue(target_pw);
        class password_check implements ValueEventListener {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue().toString().equals(target_pw))
                    debugToast("Auth success");
                else
                    debugToast("Auth failed");
            }
            @Override
            public void onCancelled(FirebaseError firebaseError) {
                debugToast("Error. Can't get to the server" + firebaseError.getMessage());
            }
        };
        fb_thisref.addValueEventListener(new password_check());
        /*
        Cursor dbc_datamaster = dbmain.rawQuery("SELECT name FROM sqlite_master WHERE type='table'",null);
        while (dbc_datamaster.moveToNext())
        {
            String dbv_table = dbc_datamaster.getString(0);
            Cursor dbc_datatable = dbmain.rawQuery("SELECT * FROM " + dbv_table + "", null);
            int dbrow = 0;
            while(dbc_datatable.moveToNext()) {
                for (int i = 0; i < dbc_datatable.getColumnCount(); i++) {
                    fb_thisref.
                            child(dbv_table).
                            child(Integer.toString(dbrow)).
                            child("col"+i).
                            setValue(dbc_datatable.getString(i));
                }
                dbrow += 1;
            }
            dbc_datatable.close();
        }
        dbc_datamaster.close();
        debugToast("Data Saved (using your internet)");
        */
    }
    public void gotoDebug(MenuItem item)
    {
//        baseAccount_pos = mViewPager.getCurrentItem();
        Intent mi = new Intent(MainActivity.this, DebugActivity.class);
        MainActivity.this.startActivity(mi);
    }

    public void gotoAddincome(View view)
    {
        refreshBaseAccount();
        Intent mi = new Intent(MainActivity.this, AddincomeActivity.class);
        mi.putExtra("v_account", dbv_baseAccount_name);
        mi.putExtra("v_account_id", dbv_baseAccount_id);
        MainActivity.this.startActivityForResult(mi, ROOM_ADDINCOME_ID);
    }
    public void gotoAddexpense(View view)
    {
        refreshBaseAccount();
        Intent mi = new Intent(MainActivity.this, AddexpenseActivity.class);
        mi.putExtra("v_account", dbv_baseAccount_name);
        mi.putExtra("v_account_id", dbv_baseAccount_id);
        MainActivity.this.startActivityForResult(mi, ROOM_ADDEXPENSE_ID);
    }
    public void gotoEditaccount(View view)
    {
        refreshBaseAccount();
        Intent mi = new Intent(MainActivity.this, AccountActivity.class);
        MainActivity.this.startActivityForResult(mi, ROOM_CHANGEBASEACCOUNT_ID);
    }
    public void changeCloseBookDate(View view)
    {
        ArrayList<CharSequence> al_dates = new ArrayList<CharSequence>();
        for (int i=1;i<=28;i+=1)
        {
            al_dates.add(Integer.toString(i));
        }
        CharSequence[] dates = new CharSequence[al_dates.size()];
        dates = al_dates.toArray(dates);
        final CharSequence[] fn_dates = dates;

        AlertDialog.Builder selector = new AlertDialog.Builder(MainActivity.this);
        selector.setTitle("Pick close book date: ");
        selector.setItems(
                fn_dates,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        int selecteddate = item + 1;
                        MainActivity.closeboook_date = selecteddate;
                        Button bt_closebookchange = (Button) findViewById(R.id.bt_closebookchange);
                        bt_closebookchange.setText(Integer.toString(selecteddate) + " to " + Integer.toString(selecteddate));
                        dbmain.execSQL("UPDATE settings SET value=" + selecteddate + " WHERE name='close_date' ");
                        MAPlaceholderFragment.refreshPage();

                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                }
        );
        selector.show();
    }
    public void resetRoom() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void gotoEditexpenseActivity(MenuItem item)
    {
        refreshBaseAccount();
        Intent mi = new Intent(MainActivity.this, EditexpenseActivity.class);
        MainActivity.this.startActivity(mi);
    }

    public void gotoEditincomeActivity(MenuItem item)
    {
        refreshBaseAccount();
        Intent mi = new Intent(MainActivity.this, EditincomeActivity.class);
        MainActivity.this.startActivity(mi);
    }

    public void gotoAccount(MenuItem item)
    {
        refreshBaseAccount();
        Intent mi = new Intent(MainActivity.this, AccountActivity.class);
        MainActivity.this.startActivity(mi);
    }

    public CharSequence[] reportDrawArray()
    {
        final Calendar c = Calendar.getInstance();
        int thisYear = c.get(Calendar.YEAR);
        int thisMonth = c.get(Calendar.MONTH)+1;
        int thisDay = c.get(Calendar.DAY_OF_MONTH);

        ArrayList<CharSequence> arResult = new ArrayList<CharSequence>();
        Cursor dbc_mostearly = dbmain.rawQuery("SELECT date FROM incomesexpenses ORDER BY date ASC LIMIT 1",null);
        //closeboook_date
        String start, startDay,startMonth,startYear;
        String select;
        if (dbc_mostearly.moveToNext()) {
            start = dbc_mostearly.getString(0);

            startYear = start.substring(0,4);
//            startMonth = start.substring(5,7);
            startMonth = "01";//-- start counting from january
            startDay = start.substring(8);
            String month = startMonth;
            String year = startYear;
            while (true) {
                String monthName = Ctrl.monthName[Integer.valueOf(month)-1];
//                String monthName = month;
                String cbdate = String.format("%02d", closeboook_date);
                if (Integer.valueOf(startDay) < closeboook_date) {
                    select = "WHERE date BETWEEN DATE('" + year + "-" + month + "-" + cbdate  + "', '-1 month') AND " +
                            "DATE('" + year + "-" + month + "-" + cbdate  + "', '-1 month', '+1 month', '-1 day')";
                } else {
                    select = "WHERE date BETWEEN DATE('" + year + "-" + month + "-" + cbdate  + "') AND " +
                            "DATE('" + year + "-" + month + "-" + cbdate  + "', '+1 month', '-1 day')";
                }



                int y = Integer.valueOf(year);
                int m = Integer.valueOf(month)+1;
                if (m>12) {
                    m = 1;
                    y += 1;
                }
                Cursor dbc_specific = dbmain.rawQuery("SELECT date FROM incomesexpenses "+select,null);
                if (dbc_specific.getCount()>0) {
                    String todate = ""+y+"-"+String.format("%02d",m)+"-"+cbdate;
                    //debugToast(todate);
                    Cursor dbc_intodate = dbmain.rawQuery("SELECT date('"+todate+"', '-1 day')", null);
                    dbc_intodate.moveToNext();
                    String intodate = dbc_intodate.getString(0);
                    String toMonthName = Ctrl.monthName[Integer.valueOf(intodate.substring(5, 7))-1];
                    arResult.add(year+"-"+monthName+"-"+cbdate +" to\n" + y+"-"+toMonthName+"-"+intodate.substring(8,10));
                    dbc_intodate.close();
//                    arResult.add(year + "-" + monthName + "-" + cbdate + " to " + String.format("%02d", y) + "-" + String.format("%02d", m) + "-" + cbdate);
                }
                dbc_specific.close();

                year = String.format("%02d",y);
                month = String.format("%02d",m);
                if (y>thisYear) {
                    //debugToast("stopped due "+y+"..."+thisYear+" and "+m+"..."+thisMonth);
                    //debugToast(select);
                    break;
                }
                else if (y==thisYear && m>thisMonth){
                    //debugToast("stopped due "+y+"..."+thisYear+" and "+m+"..."+thisMonth);
                    //debugToast(select);
                    break;
                }
                else if (y==thisYear && m==thisMonth && closeboook_date>thisDay){
                    //debugToast("stopped due "+y+"..."+thisYear+" and "+m+"..."+thisMonth);
                    //debugToast(select);
                    break;
                }
            }
        }
        dbc_mostearly.close();


        CharSequence[] result = new CharSequence[arResult.size()];
        result = arResult.toArray(result);
        return  result;
    }
    public void closeReport(View view)
    {
        reportPickedYear = Calendar.getInstance().get(Calendar.YEAR);
        reportPickedMonth = Calendar.getInstance().get(Calendar.MONTH)+1;
        MAPlaceholderFragment.actionIsHidden = false;
        MAPlaceholderFragment.showAction();
        try{
            ((Button) findViewById(R.id.bt_closeReport)).setVisibility(View.INVISIBLE);
        }catch (Exception e){}
        try{
            ((LinearLayout) findViewById(R.id.ll_ButtonWrapper)).setVisibility(View.VISIBLE);
        }catch (Exception e){}
        MAPlaceholderFragment.refreshPage();
        refreshBaseAccount();
        resetRoom();
    }
    public void reportDraw()
    {
        final CharSequence[] dategroup = reportDrawArray();
        AlertDialog.Builder selector = new AlertDialog.Builder(MainActivity.this);
        selector.setTitle("Pick period ");
        selector.setItems(
                dategroup,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        String pickeddate = dategroup[item].toString();
                        reportPickedYear = Integer.valueOf(pickeddate.substring(0, 4));
                        reportPickedMonth = Ctrl.getMonthNum(pickeddate);
                        try {
                            ((TextView) findViewById(R.id.tv_Summary)).setText("Report for\n" + pickeddate.replaceAll("\\n", " "));
                        }catch (Exception e){}
                        MAPlaceholderFragment.summaryFor = "Report for\n"+pickeddate.replaceAll("\\n"," ");

                        if (item==(dategroup.length-1))
                        {
                            //-- this means it is current period
                            MAPlaceholderFragment.actionIsHidden = false;
                            MAPlaceholderFragment.showAction();
                            try{
                                ((Button) findViewById(R.id.bt_closeReport)).setVisibility(View.INVISIBLE);
                                }catch (Exception e){}
                            try{
                                ((LinearLayout) findViewById(R.id.ll_ButtonWrapper)).setVisibility(View.VISIBLE);
                            }catch (Exception e){}
                        }
                        else
                        {
                            MAPlaceholderFragment.actionIsHidden = true;
                            MAPlaceholderFragment.hideAction();
                            try{
                                ((Button) findViewById(R.id.bt_closeReport)).setVisibility(View.VISIBLE);
                            }catch (Exception e){}
                            try{
                                ((LinearLayout) findViewById(R.id.ll_ButtonWrapper)).setVisibility(View.GONE);
                            }catch (Exception e){}
                        }

                        firstRefresh = 1;
                        //debugToast("the year to show is "+reportPickedYear+"-"+reportPickedMonth);
                        MAPlaceholderFragment.refreshPage();
                        refreshBaseAccount();
                        resetRoom();
                    }
                }
        );
        selector.show();
    }

    public void gotoReportMenu(MenuItem item)
    {
        reportDraw();
    }
    public void gotoReportButton(View view)
    {
        reportDraw();
    }

    public void openSettings(MenuItem item)
    {
        refreshBaseAccount();
        Intent mi = new Intent(MainActivity.this, SettingsActivity.class);
        MainActivity.this.startActivity(mi);
    }

    public void debugToast(String text)
    {
        Toast toast = Toast.makeText(getApplicationContext(),text, Toast.LENGTH_SHORT);
        toast.show();
    }
}
