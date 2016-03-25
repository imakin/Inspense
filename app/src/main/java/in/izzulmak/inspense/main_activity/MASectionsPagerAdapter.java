package in.izzulmak.inspense.main_activity;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.Locale;

import in.izzulmak.inspense.MainActivity;

/**
 * Created by pingsan on 25/03/16.
 */
public class MASectionsPagerAdapter extends FragmentStatePagerAdapter {
    private MainActivity mainActivityRef;
    public MAPlaceholderFragment placeholderFragmentRef;
    /**
     * the pager adapter for MainActivity pager
     * @param fm use MainActivity.getSupportFragmentManager() for this
     * @param mainActivity pass the MainActivity object
     */
    public MASectionsPagerAdapter(FragmentManager fm, MainActivity mainActivity) {
        super(fm);
        mainActivityRef = mainActivity;
    }

    @Override
    public Fragment getItem(int position) {
        Cursor dbc_bases = mainActivityRef.dbmain.rawQuery("SELECT id,name FROM accounts WHERE enabled=1 AND type='BASE' ", null);
        int basecount = dbc_bases.getCount();
        int baseid;
        String basename;
        if (position<basecount) {
            dbc_bases.moveToPosition(position);
            baseid = dbc_bases.getInt(0);
            basename = dbc_bases.getString(1);
        }
        else {
            baseid=0;
            basename="Offer";
        }
        dbc_bases.close();


        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return MAPlaceholderFragment.newInstance(position, basecount, baseid, basename);
    }

    @Override
    public int getCount() {
        Cursor dbc_bases = mainActivityRef.dbmain.rawQuery("SELECT name FROM accounts WHERE enabled=1 AND type='BASE' ", null);
        int basenum = dbc_bases.getCount();
        dbc_bases.close();
        return basenum+1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Locale l = Locale.getDefault();
        return ("#"+position);
    }
}
