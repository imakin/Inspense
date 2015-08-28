package in.izzulmak.inspense;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Locale;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    static int dbv_baseAccount_id;
    static int baseAccount_pos;//--page position
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
        Cursor dbc_Base = dbmain.rawQuery("SELECT id,name FROM accounts WHERE type='BASE' ",null);
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
    public void gotoChangebaseaccount(View view)
    {
        refreshBaseAccount();
        Intent mi = new Intent(MainActivity.this, ChangebaseaccountActivity.class);
        mi.putExtra("v_baseaccount_name",dbv_baseAccount_name);
        mi.putExtra("v_baseaccount_id",dbv_baseAccount_id);
        MainActivity.this.startActivityForResult(mi,ROOM_CHANGEBASEACCOUNT_ID);
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
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Cursor dbc_bases = dbmain.rawQuery("SELECT id,name FROM accounts WHERE type='BASE' ", null);
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
            Cursor dbc_bases = dbmain.rawQuery("SELECT name FROM accounts WHERE type='BASE' ", null);
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
            MainActivity.dbv_baseAccount_id = baseAccountId;
            MainActivity.dbv_baseAccount_name = baseAccountName;

            TextView tv_Accountname = (TextView) rootView.findViewById(R.id.tv_Accountname);
            tv_Accountname.setText("Base account: "+dbv_baseAccount_name);
            updateBalanceTM();

            return rootView;
        }

        public static Double getThisMonthSummary(String type)
        //-- type: 'INCOME', 'EXPENSE'
        {
            final Calendar c = Calendar.getInstance();
            int mYear = c.get(Calendar.YEAR);
            int mMonth = c.get(Calendar.MONTH)+1;
            int mDay = c.get(Calendar.DAY_OF_MONTH);
            String thismonth = ""+mYear+"-"+String.format("%02d",mMonth)+"-01";

            Cursor dbc_Income;
            if (type=="TRANSFERINCOME")
            {//--this one is a bit different, (searching TRANSFEREXPENSE)
                dbc_Income = dbmain.rawQuery("SELECT SUM(amount) FROM incomesexpenses WHERE from_account_id='" + MainActivity.dbv_baseAccount_id + "' AND type='TRANSFEREXPENSE' AND date BETWEEN DATE('" + thismonth + "') AND DATE('" + thismonth + "','+1 month', '-1 day'); ", null);
            }
            else //--- for INCOME, EXPENSE, & TRANSFEREXPENSE
                dbc_Income = dbmain.rawQuery("SELECT SUM(amount) FROM incomesexpenses WHERE base_account_id='" + MainActivity.dbv_baseAccount_id + "' AND type='" + type + "' AND date BETWEEN DATE('" + thismonth + "') AND DATE('" + thismonth + "','+1 month', '-1 day'); ", null);

            if (dbc_Income.moveToNext())
            {
                Double hasil = dbc_Income.getDouble(0);
                dbc_Income.close();
                return hasil;
            }
            else {
                dbc_Income.close();
                return 0.0;//*/
            }
        }
        public static void updateBalanceTM()
        {
            NumberFormat nf = NumberFormat.getCurrencyInstance();
            Double in,transIn;
            Double ex,transEx;
            in = getThisMonthSummary("INCOME");
            ex = getThisMonthSummary("EXPENSE");
            transIn = getThisMonthSummary("TRANSFERINCOME");
            transEx = getThisMonthSummary("TRANSFEREXPENSE");
            ((TextView) rootView.findViewById(R.id.tv_SumIncome)).setText(nf.format(in));
            ((TextView) rootView.findViewById(R.id.tv_SumExpense)).setText(nf.format(ex));

            ((TextView) rootView.findViewById(R.id.tv_SumTransferExpense)).setText(nf.format(transEx));
            ((TextView) rootView.findViewById(R.id.tv_SumTransferIncome)).setText(nf.format(transIn));

            ((TextView) rootView.findViewById(R.id.tv_SumBalance)).setText(nf.format(in + transIn - ex - transEx));
        }
    }

}
