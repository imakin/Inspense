package in.izzulmak.inspense.main_activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.text.NumberFormat;
import java.util.Calendar;

import in.izzulmak.inspense.MainActivity;
import in.izzulmak.inspense.R;


/**
 * Created by Izzulmakin on 25/03/16.
 * A placeholder fragment containing a simple view.
 */
public class MAPlaceholderFragment extends Fragment {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String ARG_SECTION_TOTAL = "section_total";
    private static final String ARG_BASEACCOUNT_ID = "base_account_id";
    private static final String ARG_BASEACCOUNT_NAME = "base_account_name";
    private static View rootView;
    private static View adsView;
    private static int privateBaseAccountId;
    public static boolean actionIsHidden = false;
    public static String summaryFor = "Summary";
    InterstitialAd mInterstitialAd;
    public static boolean isAdds;
    //private MainActivity mainActivityRef;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MAPlaceholderFragment newInstance(
            int sectionNumber,
            int sectionTotal,
            int baseAccountId,
            String baseAccountName
            //,MainActivity mainActivity
    ) {
        MAPlaceholderFragment fragment = new MAPlaceholderFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putInt(ARG_SECTION_TOTAL, sectionTotal);
        args.putInt(ARG_BASEACCOUNT_ID, baseAccountId);
        args.putString(ARG_BASEACCOUNT_NAME, baseAccountName);
        fragment.setArguments(args);
        //fragment.mainActivityRef = mainActivity;

        return fragment;
    }

    public MAPlaceholderFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int secnum = getArguments().getInt(ARG_SECTION_NUMBER);
        int sectot = getArguments().getInt(ARG_SECTION_TOTAL);
        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        if (secnum<sectot) {
            isAdds = false;
            String baseAccountName = getArguments().getString(ARG_BASEACCOUNT_NAME);
            int baseAccountId = getArguments().getInt(ARG_BASEACCOUNT_ID);
            privateBaseAccountId = baseAccountId;
            MainActivity.dbv_baseAccount_id = baseAccountId;
            MainActivity.dbv_baseAccount_name = baseAccountName;

            TextView tv_Accountname = (TextView) rootView.findViewById(R.id.tv_Accountname);
            tv_Accountname.setText(
                            "("+(secnum+1)+"/"+sectot+") Base account: " +
                            MainActivity.dbv_baseAccount_name
                        );

            Button bt_closebookchange = (Button) rootView.findViewById(R.id.bt_closebookchange);
            bt_closebookchange.setText(
                            Integer.toString(MainActivity.closeboook_date) +
                            " to " +
                            Integer.toString(MainActivity.closeboook_date)
                        );
            updateBalanceTM();
            if (actionIsHidden) {
                ((LinearLayout) rootView.findViewById(R.id.ll_ButtonWrapper)).setVisibility(View.GONE);
                ((TextView) rootView.findViewById(R.id.tv_Summary)).setText(summaryFor);
                ((Button) rootView.findViewById(R.id.bt_closeReport)).setVisibility(View.VISIBLE);
            } else {
                ((LinearLayout) rootView.findViewById(R.id.ll_ButtonWrapper)).setVisibility(View.VISIBLE);
                ((TextView) rootView.findViewById(R.id.tv_Summary)).setText("Summary");
                ((Button) rootView.findViewById(R.id.bt_closeReport)).setVisibility(View.INVISIBLE);
            }
            showReport();
            return rootView;
        }
        else {
            adsView = inflater.inflate(R.layout.fragment_ads, container, false);
            AdView mAdView = (AdView) adsView.findViewById(R.id.adViewFull);
            mAdView.loadAd(MainActivity.adRequest);
//                mInterstitialAd = new InterstitialAd(adsView.getContext());
//                mInterstitialAd.setAdUnitId(getResources().getString(R.string.banner_ad_unit_id_full));
//                requestNewInterstitial();
            isAdds = true;
            return adsView;
        }
    }
    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("E05BA7C7DB8B7BFC90E0ECE539108CDA")
                .addTestDevice("C4F539AAF06BC38EF65C8A0564DC2C10")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    public static void showReport()
    {
        int backupBaseAccountId;
        if (isAdds)
            return;
        LinearLayout ll_report = (LinearLayout) rootView.findViewById(R.id.ll_report);
        ll_report.removeAllViews();
        Cursor dbc_rpAccountUsed = MainActivity.dbmain.rawQuery("SELECT " +
                "incomesexpenses.from_account_id, accounts.name FROM incomesexpenses LEFT JOIN accounts " +
                "WHERE incomesexpenses.from_account_id=accounts.id " +
                "GROUP BY incomesexpenses.from_account_id ", null);
        while (dbc_rpAccountUsed.moveToNext())
        {
            Double sumPerAccountUsed = getMonthSummary("EXPENSE", 0, "BETWEEN", dbc_rpAccountUsed.getInt(0));
            if (sumPerAccountUsed>0) {
                TextView rp = new TextView(ll_report.getContext());
                rp.setText(dbc_rpAccountUsed.getString(1) + " " + NumberFormat.getCurrencyInstance().format(sumPerAccountUsed));
                ll_report.addView(rp);
            }
        }
        dbc_rpAccountUsed.close();

        TextView at;


        LinearLayout ll_report_alltime = (LinearLayout) rootView.findViewById(R.id.ll_report_alltime);
        ll_report_alltime.removeAllViews();
        //-- all account sum
        at = new TextView(ll_report.getContext());
        Double aasum = 0.0;
        backupBaseAccountId = privateBaseAccountId;
        Cursor dbc_base = MainActivity.dbmain.rawQuery("SELECT id FROM accounts WHERE type='BASE' and enabled=1", null);
        while (dbc_base.moveToNext())
        {
            privateBaseAccountId = dbc_base.getInt(0);
            aasum = aasum + getMonthSummary("EXPENSE", 0, "BETWEEN", -1);
        }
        at.setText("All base account this period expense :"+NumberFormat.getCurrencyInstance().format(aasum) + "\n\n");
        ll_report_alltime.addView(at);
        dbc_base.close();
        privateBaseAccountId = backupBaseAccountId;

        //-- the all time sum
        Double atex = getMonthSummary("EXPENSE", 0, "ALL", -1);
        Double atin = getMonthSummary("INCOME", 0, "ALL", -1);


        at = new TextView(ll_report_alltime.getContext());
        at.setText("This base account all time expense: "+NumberFormat.getCurrencyInstance().format(atex));
        ll_report_alltime.addView(at);

        at = new TextView(ll_report_alltime.getContext());
        at.setText("This base account all time income: "+NumberFormat.getCurrencyInstance().format(atin));
        ll_report_alltime.addView(at);

        at = new TextView(ll_report_alltime.getContext());
        at.setText(" ");
        ll_report_alltime.addView(at);

        //-- all time all account
        Cursor dbc_atsum = MainActivity.dbmain.rawQuery("SELECT sum(amount) FROM incomesexpenses WHERE type=?",new String[]{"EXPENSE"});
        if (dbc_atsum.moveToNext())
        {
            at = new TextView(ll_report_alltime.getContext());
            at.setText("all account all time expense: "+NumberFormat.getCurrencyInstance().format(dbc_atsum.getDouble(0)));
            ll_report_alltime.addView(at);
        }
        dbc_atsum.close();

        dbc_atsum = MainActivity.dbmain.rawQuery("SELECT sum(amount) FROM incomesexpenses WHERE type=?",new String[]{"INCOME"});
        if (dbc_atsum.moveToNext())
        {
            at = new TextView(ll_report_alltime.getContext());
            at.setText("all account all time income: "+NumberFormat.getCurrencyInstance().format(dbc_atsum.getDouble(0)));
            ll_report_alltime.addView(at);
        }
        dbc_atsum.close();




    }

    public static void updateBalanceTM()
    {
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        Double in,transIn;
        Double ex,transEx;
        in = getMonthSummary("INCOME",0, "BETWEEN", -1);
        ex = getMonthSummary("EXPENSE",0, "BETWEEN", -1);
        transIn = getMonthSummary("TRANSFERINCOME",0, "BETWEEN", -1);
        transEx = getMonthSummary("TRANSFEREXPENSE",0, "BETWEEN", -1);

        Double lin = getMonthSummary("INCOME",0, "BEFORE", -1);
        Double lex = getMonthSummary("EXPENSE",0, "BEFORE", -1);
        Double ltin = getMonthSummary("TRANSFERINCOME",0, "BEFORE", -1);
        Double ltex = getMonthSummary("TRANSFEREXPENSE", 0, "BEFORE", -1);
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
        //firstRefresh = 0;
        updateBalanceTM();
        showReport();
        ((Button) rootView.findViewById(R.id.bt_closebookchange)).setText
                ("" + MainActivity.closeboook_date + " to " + MainActivity.closeboook_date);
    }

    /**
     * getMonthSummary with specificBaseAccountId filter
     * calculates per month(period) summary
     * @param type: 'INCOME', 'EXPENSE',
     * @param month is month number 1=jan, 2=feb, 3=mar
     *       or relative to current month 0=thismonth -1=last month, -2 before last month
     * @param scope is "BETWEEN" or "BEFORE" or "ALL",
     *       between is this month only, before is before this month, all is for no date filter
     * @param specificAccountId specific account is passed non (-1) if want to get only specific account INCOME/EXPENSE
     *       like passing (3) will set specific for "Main Income"
     *       only work for EXPENSE & INCOME type, doesn't work for TRANSFERINCOME/TRANSFEREXPENSE
     * @param specificBaseAccountId passed non (-1) to set specificBaseAccountId filter
     * @return the summary of specified params
     */
    public static Double getMonthSummary(String type, int month, String scope, int specificAccountId, int specificBaseAccountId)
    {
        int backupBaseAccountId = privateBaseAccountId;
        Double result;
        privateBaseAccountId = specificBaseAccountId;
        result = getMonthSummary(type, month, scope, specificAccountId);
        privateBaseAccountId = backupBaseAccountId;
        return result;
    }
    /**
     * calculates per month(period) summary
     * @param type: 'INCOME', 'EXPENSE',
     * @param month is month number 1=jan, 2=feb, 3=mar
     *       or relative to current month 0=thismonth -1=last month, -2 before last month
     * @param scope is "BETWEEN" or "BEFORE" or "ALL",
     *       between is this month only, before is before this month, all is for no date filter
     * @param specificAccountId specific account is passed non (-1) if want to get only specific account INCOME/EXPENSE
     *       like passing (3) will set specific for "Main Income"
     *       only work for EXPENSE & INCOME type, doesn't work for TRANSFERINCOME/TRANSFEREXPENSE
     * @return the summary of specified params
     */
    public static Double getMonthSummary(String type, int month, String scope, int specificAccountId)
    {
        String accountFilter;
        if (specificAccountId!=-1 && (type.equals("INCOME") || type.equals("EXPENSE")))
            accountFilter = " AND from_account_id="+specificAccountId+" ";
        else
            accountFilter = "";


        final Calendar c = Calendar.getInstance();
        int mYear = MainActivity.reportPickedYear;
        int mMonth;
        if (month>0)
            mMonth = month;
        else
            mMonth = MainActivity.reportPickedMonth + month;
        int mDay = c.get(Calendar.DAY_OF_MONTH);

        if (mDay<MainActivity.closeboook_date) //-- if closebook_date is later than current date
            mMonth -= 1;

        String thismonth = ""+mYear+"-"+String.format("%02d",mMonth)+"-"+String.format("%02d",MainActivity.closeboook_date);

        String datefilter;
        if (scope.equals("BEFORE"))
            datefilter = " AND date < DATE('"+thismonth+"')";
        else if (scope.equals("ALL"))
            datefilter = "";
        else
            datefilter = " AND date BETWEEN DATE('" + thismonth + "') AND DATE('" + thismonth + "','+1 month', '-1 day')";
        Cursor dbc;
        if (type.equals("TRANSFERINCOME"))
        {//--this one is a bit different, (searching TRANSFEREXPENSE)
            dbc = MainActivity.dbmain.rawQuery("SELECT SUM(amount) FROM incomesexpenses WHERE from_account_id='" + privateBaseAccountId + "' AND type='TRANSFEREXPENSE' "+datefilter, null);
        }
        else //--- for INCOME, EXPENSE, & TRANSFEREXPENSE
//                dbc = dbmain.rawQuery("SELECT SUM(amount) FROM incomesexpenses WHERE base_account_id='" + privateBaseAccountId + "' AND type='" + type + "' AND "+datefilter, null);
            dbc = MainActivity.dbmain.rawQuery("SELECT SUM(amount) " +
                            "FROM incomesexpenses " +
                            "WHERE base_account_id= ? " +
                            "AND type= ? " +
                            datefilter+" "+ accountFilter,
                    new String[] {
                            Integer.toString(privateBaseAccountId),
                            type
                    }
            );

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
        ((LinearLayout) rootView.findViewById(R.id.ll_ButtonWrapper)).setVisibility(View.GONE);
        ((Button) rootView.findViewById(R.id.bt_closeReport)).setVisibility(View.VISIBLE);
    }
    public static void showAction() {
        ((LinearLayout) rootView.findViewById(R.id.ll_ButtonWrapper)).setVisibility(View.VISIBLE);
        ((Button) rootView.findViewById(R.id.bt_closeReport)).setVisibility(View.INVISIBLE);
    }
}