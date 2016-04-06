package in.izzulmak.inspense.main_activity.server;

import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import in.izzulmak.inspense.MainActivity;

/**
 * Created by Izzulmakin on 05/04/16.
 */
public class LoadModel {
    private static LoadModel self=null;
    protected LoadModel(){}
    /**
     * get singleton
     * @return this object singleton
     */
    public static LoadModel object() {
        if (self == null)
            self = new LoadModel();
        return self;
    }

    /**
     * The checking before load process. Called by SaveListenerOk.onClick
     *
     * @param target_id the User Id owning the data to be loaded from cloud
     * @param target_pw the password for given target_id
     * @param caller_activity the caller activity reference
     */
    public void loadInspenseDo(String target_id, final String target_pw, AppCompatActivity caller_activity) {
        Firebase fb_ref = new Firebase("https://inspense.firebaseio.com/");
        final Firebase fb_thisref = fb_ref.child(target_id);

        class password_check implements ValueEventListener {
            Firebase fbref;
            AppCompatActivity mactivity;
            /**
             * instantiate
             * @param fbref reference to Firebase object which is specific to a user
             */
            public password_check(Firebase fbref, AppCompatActivity mactivity){
                this.fbref = fbref;
                this.mactivity = mactivity;
            }
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.getValue()!=null) {
                    if (snapshot.getValue().toString().equals(target_pw)) {
                        self.loadProcess(fbref);
                        mDebugToast("Auth success");
                    }
                    else
                        mDebugToast("Auth failed");
                }
                else{
                    //-- user not exist
                    mDebugToast("Invalid user");
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                mDebugToast("Error. Can't get to the server" + firebaseError.getMessage());
            }
            private void mDebugToast(String text)
            {
                try {
                    if (!MainActivity.DEBUG_ENABLED)
                        return;
                    Toast toast = Toast.makeText(mactivity.getApplicationContext(),text, Toast.LENGTH_SHORT);
                    toast.show();
                } catch (Exception e) { }
            }
        };
        fb_thisref.child("password").addListenerForSingleValueEvent(new password_check(fb_thisref, caller_activity));
    }

    /**
     * the load process
     * @param fbref Firebase object reference where is in occupied user's path
     */
    private void loadProcess(Firebase fbref) {
        int current_row = 0;
        MainActivity.dbmain.execSQL("delete from accounts");
        MainActivity.dbmain.execSQL("delete from incomesexpenses");
        fbref.child("accounts").addListenerForSingleValueEvent(new load("accounts"));
        fbref.child("incomesexpenses").addListenerForSingleValueEvent(new load("incomesexpenses"));
    }

    /**
     * Subclass listener for each firebase table loaded
     */
    class load implements ValueEventListener {
        /** contains table name: accounts, incomesexpenses */
        String table;
        /**
         * instantiate
         * @param table table name: accounts, incomesexpenses
         */
        public load(String table){
            this.table = table;
        }
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            int i=0;
            long n = dataSnapshot.getChildrenCount();
            for (DataSnapshot data: dataSnapshot.getChildren()) {
                /*
                if (this.table.equals("accounts"))
                    sub_mainref.setMessageBar(data.getValue().toString());
                /*/
                //if (i%10==0)
                //    sub_mainref.setMessageBar("("+i+"/"+n+") loaded");
                if (this.table.equals("accounts")) {
                    //FirebaseModel.Accounts d = data.getValue(FirebaseModel.Accounts.class);
                    MainActivity.dbmain.execSQL("INSERT INTO accounts (id, name, type, enabled) VALUES(" +
                                    "'"+data.child("id").getValue()   +"', " +
                                    "'"+data.child("name").getValue() +"', " +
                                    "'"+data.child("type").getValue() +"', " +
                                    "'"+data.child("enabled").getValue() +"' " +
                                    ") "
                    );
                }
                else if (this.table.equals("incomesexpenses")) {
                    //FirebaseModel.IncomesExpenses d = data.getValue(FirebaseModel.IncomesExpenses.class);
                    MainActivity.dbmain.execSQL("INSERT INTO incomesexpenses "+
                                    "(id,base_account_id,from_account_id,description,type,amount,date) "+
                                    "VALUES("+
                                    "'"+data.child("id").getValue()               +"', "+
                                    "'"+data.child("base_account_id").getValue()  +"', "+
                                    "'"+data.child("from_account_id").getValue()  +"', "+
                                    "'"+data.child("description").getValue()      +"', "+
                                    "'"+data.child("type").getValue()             +"', "+
                                    "'"+data.child("amount").getValue()           +"', "+
                                    "'"+data.child("date").getValue()             +"' "+
                                    ") "
                    );
                }
                //*/
            }
        }
        @Override
        public void onCancelled(FirebaseError firebaseError) {

        }
    }
}
