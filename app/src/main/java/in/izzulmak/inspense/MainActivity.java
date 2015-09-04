package in.izzulmak.inspense;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    static int dbv_baseAccount_id;
    static int baseAccount_pos;//--page position
    public static int closeboook_date;
    public static int reportPickedMonth;
    public static int reportPickedYear;
    static String dbv_baseAccount_name;
    final static int ROOM_ADDEXPENSE_ID = 0;
    final static int ROOM_ADDINCOME_ID = 1;
    final static int ROOM_CHANGEBASEACCOUNT_ID = 2;
    public static SQLiteDatabase dbmain;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;
    /*** The {@link ViewPager} that will host the section contents.*/
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



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
        }
        dbc_Accounttable.close();

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

        //--active baseaccount, deprecated. we use baseAccount_pos now
        Cursor dbc_baseAccount = dbmain.rawQuery("SELECT settings.value,settings.name,accounts.name FROM settings,accounts WHERE settings.name='base_account' AND accounts.id=settings.value",null);
        dbc_baseAccount.moveToNext();
        dbv_baseAccount_id = dbc_baseAccount.getInt(0);
        dbv_baseAccount_name = dbc_baseAccount.getString(2);
        dbc_baseAccount.close();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        //refreshBaseAccount();
        if (baseAccount_pos!=0)
        {
            mViewPager.setCurrentItem(baseAccount_pos);
            PlaceholderFragment.updateBalanceTM();
        }
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //PlaceholderFragment.refreshPage();
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        final Calendar c = Calendar.getInstance();
        reportPickedYear = c.get(Calendar.YEAR);
        reportPickedMonth = c.get(Calendar.MONTH) +1;
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
        super.onActivityResult(requestCode, resultCode, data);
        mViewPager.setCurrentItem(baseAccount_pos);
        PlaceholderFragment.updateBalanceTM();
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
    }

    public void refreshBaseAccount()
    {
        int position = mViewPager.getCurrentItem();
        baseAccount_pos = position;
        Cursor dbc_Base = dbmain.rawQuery("SELECT id,name FROM accounts WHERE enabled=1 AND type='BASE' ",null);
        if (position<dbc_Base.getCount())
        {
            dbc_Base.moveToPosition(position);
            dbv_baseAccount_name = dbc_Base.getString(1);
            dbv_baseAccount_id = dbc_Base.getInt(0);
        }
    }

    public void gotoDebug(MenuItem item)
    {
        baseAccount_pos = mViewPager.getCurrentItem();
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
                        PlaceholderFragment.refreshPage();

                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                }
        );
        selector.show();
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
                            "DATE('" + startYear + "-" + month + "-" + cbdate  + "', '-1 month', '+1 month', '-1 day')";
                } else {
                    select = "WHERE date BETWEEN DATE('" + year + "-" + month + "-" + cbdate  + "') AND " +
                            "DATE('" + startYear + "-" + month + "-" + cbdate  + "', '+1 month', '-1 day')";
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
                    debugToast(todate);
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
                if (y>thisYear)
                    break;
                else if (y==thisYear && m>thisMonth)
                    break;
                else if (y==thisYear && m==thisMonth && closeboook_date>thisDay)
                    break;
            }
        }
        dbc_mostearly.close();


        CharSequence[] result = new CharSequence[arResult.size()];
        result = arResult.toArray(result);
        return  result;
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
//                        reportPickedMonth = Integer.valueOf(pickeddate.substring(5, 7));
                        reportPickedMonth = Ctrl.getMonthNum(pickeddate);

                        if (item==dategroup.length)
                        {
                            //-- this means it is current period
                            PlaceholderFragment.showAction();
                        }
                        else
                        {
                            PlaceholderFragment.hideAction();
                        }

                        PlaceholderFragment.refreshPage();
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



    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Cursor dbc_bases = dbmain.rawQuery("SELECT id,name FROM accounts WHERE enabled=1 AND type='BASE' ", null);
            dbc_bases.moveToPosition(position);
            int baseid = dbc_bases.getInt(0);
            String basename = dbc_bases.getString(1);
            dbc_bases.close();


            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1, baseid, basename);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            Cursor dbc_bases = dbmain.rawQuery("SELECT name FROM accounts WHERE enabled=1 AND type='BASE' ", null);
            int basenum = dbc_bases.getCount();
            dbc_bases.close();
            return basenum;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            return ("#"+position);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static final String ARG_BASEACCOUNT_ID = "base_account_id";
        private static final String ARG_BASEACCOUNT_NAME = "base_account_name";
        private static View rootView;
        private static int privateBaseAccountId;
        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber, int baseAccountId, String baseAccountName) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            args.putInt(ARG_BASEACCOUNT_ID, baseAccountId);
            args.putString(ARG_BASEACCOUNT_NAME, baseAccountName);
            fragment.setArguments(args);

            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            rootView = inflater.inflate(R.layout.fragment_main, container, false);

            String baseAccountName = getArguments().getString(ARG_BASEACCOUNT_NAME);
            int baseAccountId = getArguments().getInt(ARG_BASEACCOUNT_ID);
            privateBaseAccountId = baseAccountId;
            MainActivity.dbv_baseAccount_id = baseAccountId;
            MainActivity.dbv_baseAccount_name = baseAccountName;

            TextView tv_Accountname = (TextView) rootView.findViewById(R.id.tv_Accountname);
            tv_Accountname.setText("Base account: " + dbv_baseAccount_name);

            Button bt_closebookchange = (Button) rootView.findViewById(R.id.bt_closebookchange);
            bt_closebookchange.setText(Integer.toString(closeboook_date) + " to " + Integer.toString(closeboook_date));
            updateBalanceTM();

            return rootView;
        }

        public static void updateBalanceTM()
        {
            NumberFormat nf = NumberFormat.getCurrencyInstance();
            Double in,transIn;
            Double ex,transEx;
            in = getMonthSummary("INCOME",0, "BETWEEN");
            ex = getMonthSummary("EXPENSE",0, "BETWEEN");
            transIn = getMonthSummary("TRANSFERINCOME",0, "BETWEEN");
            transEx = getMonthSummary("TRANSFEREXPENSE",0, "BETWEEN");

            Double lin = getMonthSummary("INCOME",0, "BEFORE");
            Double lex = getMonthSummary("EXPENSE",0, "BEFORE");
            Double ltin = getMonthSummary("TRANSFERINCOME",0, "BEFORE");
            Double ltex = getMonthSummary("TRANSFEREXPENSE", 0, "BEFORE");
            Double lastbalance = lin +ltin - lex -ltex;


            ((TextView) rootView.findViewById(R.id.tv_SumIncome)).setText(nf.format(in));
            ((TextView) rootView.findViewById(R.id.tv_SumExpense)).setText(nf.format(ex));

            ((TextView) rootView.findViewById(R.id.tv_SumTransferExpense)).setText(nf.format(transEx));
            ((TextView) rootView.findViewById(R.id.tv_SumTransferIncome)).setText(nf.format(transIn));

            ((TextView) rootView.findViewById(R.id.tv_LastSumBalance)).setText(nf.format(lastbalance));

            ((TextView) rootView.findViewById(R.id.tv_SumBalance)).setText(nf.format(lastbalance + in + transIn - ex - transEx));
        }
        public static void refreshPage()
        {
            updateBalanceTM();
            ((Button) rootView.findViewById(R.id.bt_closebookchange)).setText(""+MainActivity.closeboook_date+" to "+MainActivity.closeboook_date);
        }
        public static Double getMonthSummary(String type, int month, String scope)
        //-- type: 'INCOME', 'EXPENSE',
        //-- month is month number 1=jan, 2=feb, 3=mar
        //--    or relative to current month 0=thismonth -1=last month, -2 before last month
        //-- scope is "BETWEEN" or "BEFORE"
        {
            final Calendar c = Calendar.getInstance();
            int mYear = reportPickedYear;
            int mMonth;
            if (month>0)
                mMonth = month;
            else
                mMonth = reportPickedMonth + month;
            int mDay = c.get(Calendar.DAY_OF_MONTH);

            if (mDay<closeboook_date) //-- if closebook_date is later than current date
                mMonth -= 1;

            String thismonth = ""+mYear+"-"+String.format("%02d",mMonth)+"-"+String.format("%02d",closeboook_date);

            String datefilter;
            if (scope.equals("BEFORE"))
                datefilter = "date < DATE('"+thismonth+"')";
            else
                datefilter = "date BETWEEN DATE('" + thismonth + "') AND DATE('" + thismonth + "','+1 month', '-1 day')";
            Cursor dbc;
            if (type.equals("TRANSFERINCOME"))
            {//--this one is a bit different, (searching TRANSFEREXPENSE)
                dbc = dbmain.rawQuery("SELECT SUM(amount) FROM incomesexpenses WHERE from_account_id='" + privateBaseAccountId + "' AND type='TRANSFEREXPENSE' AND "+datefilter, null);
            }
            else //--- for INCOME, EXPENSE, & TRANSFEREXPENSE
                dbc = dbmain.rawQuery("SELECT SUM(amount) FROM incomesexpenses WHERE base_account_id='" + privateBaseAccountId + "' AND type='" + type + "' AND "+datefilter, null);

            if (dbc.moveToNext())
            {
                Double hasil = dbc.getDouble(0);
                dbc.close();
                return hasil;
            }
            else {
                dbc.close();
                return 0.0;//*/
            }
        }

        public static void hideAction() {
            ((LinearLayout) rootView.findViewById(R.id.ll_ButtonWrapper)).setVisibility(View.INVISIBLE);
        }
        public static void showAction() {
            ((LinearLayout) rootView.findViewById(R.id.ll_ButtonWrapper)).setVisibility(View.VISIBLE);
        }
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
