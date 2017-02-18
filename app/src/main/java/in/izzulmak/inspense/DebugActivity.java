package in.izzulmak.inspense;

import android.app.AlertDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import in.izzulmak.inspense.console.ConsoleChangedListener;
import in.izzulmak.inspense.input_listeners.ILCancel;
import in.izzulmak.inspense.main_activity.server.LoadListenerOk;

//TODO: This deprecated
public class DebugActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);
        EditText et_debug = (EditText) findViewById(R.id.et_Debug);
        et_debug.addTextChangedListener(new ConsoleChangedListener(this,et_debug));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_debug, menu);
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

    public void dumpDebugQuery(View view)
    {
        String sqlitequery = ((EditText) findViewById(R.id.et_Debug)).getText().toString();
        SQLiteDatabase db = openOrCreateDatabase(getResources().getString(R.string.databasename), MODE_PRIVATE, null);

        Cursor dbv;
        try {
            dbv = db.rawQuery(sqlitequery, null);
            String hasil = "";
            while (dbv.moveToNext())
            {
                for (int i=0; i<dbv.getColumnCount(); i++)
                {
                    hasil += dbv.getString(i)+", ";
                }
                hasil += "\n";
            }

            ((TextView) findViewById(R.id.tv_Debug)).setText(hasil);
            dbv.close();
            db.close();
        }
        catch (Exception e) {
            ((TextView) findViewById(R.id.tv_Debug)).setText(e.toString());
        }

        return;
    }
    /**
     * Optional feature to replace current database with the one from server
     * @param item related to menu selection that triggered this
     */
    public void loadInspense(MenuItem item) {
        final AlertDialog.Builder inputBuilder = new AlertDialog.Builder(this);
        final EditText et_id = new EditText(this);

        inputBuilder.setView(et_id);
        inputBuilder.setCancelable(true);
        inputBuilder.setPositiveButton("Auth ID", new LoadListenerOk(this, et_id));
        inputBuilder.setNegativeButton("Cancel", ILCancel.get());
        AlertDialog inputDialog = inputBuilder.create();
        inputDialog.show();
    }
}
