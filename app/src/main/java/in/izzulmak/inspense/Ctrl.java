package in.izzulmak.inspense;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by pingsan on 28/08/15.
 */
public class Ctrl {
    final static String monthName[] ={
                    "January",
                    "February",
                    "March",
                    "April",
                    "May",
                    "June",
                    "July",
                    "August",
                    "September",
                    "October",
                    "November",
                    "December"
                };
    public static int getMonthNum(String fullDateString)
    {
        String monthexcerpt = fullDateString.substring(5,8);
        for (int i=0;i<12; i++)
        {
            if (monthexcerpt.equals(monthName[i].substring(0,3)))
            {
                return i+1;
            }
        }
        return 12;
    }

    public static CharSequence[] queryToArray(String query)
    //--support only 1 field, e.g.: SELECT name FROM accounts WHERE type='BASE'
    {
        Cursor dbc = MainActivity.dbmain.rawQuery(query,null);
        //-- Make ArrayList and push every needed row value
        ArrayList<CharSequence> AL_list = new ArrayList<CharSequence>();
        while (dbc.moveToNext())
        {
            String row = dbc.getString(0);
            AL_list.add(row);
        }
        //-- covert the ArrayList to an Array
        CharSequence[] list = new CharSequence[AL_list.size()];
        list = AL_list.toArray(list);
        dbc.close();
        return list;
    }
}
