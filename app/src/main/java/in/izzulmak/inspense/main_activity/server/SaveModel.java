package in.izzulmak.inspense.main_activity.server;

import android.database.Cursor;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import in.izzulmak.inspense.MainActivity;

/**
 * Created by Izzulmakin on 27/03/16.
 * TODO: encrypt data based on password
 * TODO: desktop client
 */
public class SaveModel {
    private static SaveModel self = null;

    protected SaveModel() {

    }

    /**
     * get singleton
     * @return this object singleton
     */
    public static SaveModel object() {
        if (self == null)
            self = new SaveModel();
        return self;
    }

    /**
     * The checking before saving process. Called by SaveListenerOk.onClick
     *
     * @param target_id the user id owning the data to be saved on cloud
     * @param target_pw the password for given target_id
     * @param mref MainActivity object reference
     */
    public void saveInspenseDo(String target_id, final String target_pw, final MainActivity mref) {
        /* firebase do backup */
        Firebase fb_ref = new Firebase("https://inspense.firebaseio.com/");
        final Firebase fb_thisref = fb_ref.child(target_id);

        class password_check implements ValueEventListener {
            MainActivity sub_mainref;
            Firebase fbref;
            /**
             * instantiate
             * @param mref reference to main activity
             * @param fbref reference to Firebase object which is specific to a user
             */
            public password_check(MainActivity mref, Firebase fbref){
                this.sub_mainref = mref;
                this.fbref = fbref;
            }
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue()!=null) {
                    if (snapshot.getValue().toString().equals(target_pw)) {
                        self.saveProcess(sub_mainref, fbref);
                        mDebugToast("Auth success");
                    }
                    else
                        mDebugToast("Auth failed");
                }
                else{
                    //-- user not exist
                    mDebugToast("New user");
                    fbref.child("password").setValue(target_pw);
                    self.saveProcess(sub_mainref, fbref);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                mDebugToast("Error. Can't get to the server" + firebaseError.getMessage());
            }
            private void mDebugToast(String text)
            {
                try {
                    if (sub_mainref != null)
                        sub_mainref.debugToast(text);
                } catch (Exception e) { }
            }
        };
        fb_thisref.child("password").addListenerForSingleValueEvent(new password_check(mref, fb_thisref));
    }

    /**
     * The saving process
     * @param mref MainActivity reference
     * @param fbref The Firebase reference where is in occupied user's path
     */
    public void saveProcess(MainActivity mref, Firebase fbref){
        int current_row = 0;
        int total_rows;
        Firebase fb_accounts = fbref.child("accounts");
        Firebase fb_incomesexpenses = fbref.child("incomesexpenses");


        Cursor dbc_accounts = mref.dbmain.rawQuery("SELECT id, name, type, enabled FROM accounts", null);
        total_rows = dbc_accounts.getCount();
        current_row = 0;
        while (dbc_accounts.moveToNext()) {
            String id = dbc_accounts.getString(0);
            String name = dbc_accounts.getString(1);
            String type = dbc_accounts.getString(2);
            String enabled = dbc_accounts.getString(3);

            Map<String, String> tosave = new HashMap<String, String>();
            tosave.put("id", id);
            tosave.put("name", name);
            tosave.put("type", type);
            tosave.put("enabled", enabled);

            if (current_row%10==0 || (current_row+1)>=total_rows)
                fb_accounts.child(Integer.toString(current_row)).
                        setValue(tosave, new LoadingStatus(mref, current_row, total_rows));
            else
                fb_accounts.child(Integer.toString(current_row)).
                        setValue(tosave);
            current_row += 1;
        }
        dbc_accounts.close();


        Cursor dbc_incomesexpenses = mref.dbmain.rawQuery("SELECT id, base_account_id, "+
                                        "from_account_id, description, type, amount, date FROM incomesexpenses",
                                        null);
        current_row = 0;
        total_rows = dbc_incomesexpenses.getCount();
        while(dbc_incomesexpenses.moveToNext()) {
            String id = dbc_incomesexpenses.getString(0);
            String base_account_id = dbc_incomesexpenses.getString(1);
            String from_account_id = dbc_incomesexpenses.getString(2);
            String description = dbc_incomesexpenses.getString(3);
            String type = dbc_incomesexpenses.getString(4);
            String amount = dbc_incomesexpenses.getString(5);
            String date = dbc_incomesexpenses.getString(6);

            Map<String, String> tosave = new HashMap<String, String>();
            tosave.put("id", id);
            tosave.put("base_account_id", base_account_id);
            tosave.put("from_account_id", from_account_id);
            tosave.put("description", description);
            tosave.put("type", type);
            tosave.put("amount", amount);
            tosave.put("date", date);

            if (current_row%10==0 || (current_row+1)>=total_rows)
                fb_incomesexpenses.child(Integer.toString(current_row)).
                        setValue(tosave, new LoadingStatus(mref, current_row, total_rows));
            else
                fb_incomesexpenses.child(Integer.toString(current_row)).
                        setValue(tosave);
            current_row += 1;
        }
        dbc_incomesexpenses.close();
    }

    /**
     * subclass for each firebase item saving success
     */
    class LoadingStatus implements Firebase.CompletionListener {
        MainActivity sub_mainref = null;
        int currentItem;
        int totalItem;
        /**
         * update loading status info
         * @param mref MainActivity reference
         * @param currentItem current completed item number
         * @param totalItem total item to be loaded
         */
        public LoadingStatus(MainActivity mref, int currentItem, int totalItem)
        {
            this.sub_mainref = mref;
            this.currentItem = currentItem+1; //-- index starts from 0
            this.totalItem = totalItem;
        }
        @Override
        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
            if (firebaseError==null){
                if (currentItem<totalItem)
                    sub_mainref.setMessageBar("("+currentItem+"/"+totalItem+") uploaded");
                else
                    sub_mainref.setMessageBar("");
            }
            else{
                sub_mainref.setMessageBar("error at "+currentItem);
            }
        }
    }
}
