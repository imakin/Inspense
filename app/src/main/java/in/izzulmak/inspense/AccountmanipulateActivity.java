package in.izzulmak.inspense;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AccountmanipulateActivity extends AppCompatActivity {
    private static int isEditing=0;
    private static int idEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountmanipulate);

        Intent it = getIntent();
        isEditing = it.getIntExtra(AccountActivity.EXTRA_ID_EDIT,0);
        if (isEditing!=1)
        {
            idEdit = it.getIntExtra(AccountActivity.EXTRA_ID_EDIT,0);
            ((TextView) findViewById(R.id.tv_Accountmanipulate_Id)).setText(Integer.toString(idEdit));
            String nameEdit = it.getStringExtra(AccountActivity.EXTRA_NAME_EDIT);
            ((TextView) findViewById(R.id.et_Accountmanipulate_Name)).setText(nameEdit);

            ((Button) findViewById(R.id.bt_Accountmanipulate_Delete)).setVisibility(Button.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_accountmanipulate, menu);
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

    public void actSave(View view){
        String name = ((EditText) findViewById(R.id.et_Accountmanipulate_Name)).getText().toString();
        if (isEditing!=0)
        {
            MainActivity.dbmain.execSQL("UPDATE accounts SET name='"+name+"' where id="+idEdit);
            Toast toast = Toast.makeText(getApplicationContext(), "Show" , Toast.LENGTH_SHORT);
            toast.show();
        }
        finish();
    }
    public void actDelete(View view){
        if (isEditing!=0)
        {
            AlertDialog.Builder confirm = new AlertDialog.Builder(this);
            confirm.setMessage("Are you sure?");
            confirm.setNegativeButton("cancel", null);
            confirm.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.dbmain.execSQL("UPDATE accounts SET enabled=0 where id="+idEdit);
                    Toast toast = Toast.makeText(getApplicationContext(), "Deleted" , Toast.LENGTH_SHORT);
                    toast.show();
                    AccountmanipulateActivity.this.finish();
                }
            });
            confirm.show();
        }
    }
}
