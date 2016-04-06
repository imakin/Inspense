package in.izzulmak.inspense.main_activity.server;

/**
 * Created by Izzulmakin on 05/04/16.
 * for Firebase object
 */
public class FirebaseModel {
    public class Accounts{
        String id;
        String name;
        String type;
        String enabled;
        public Accounts(){}
        public String getId(){return id;}
        public String getName(){return name;}
        public String getType(){return type;}
        public String getEnabled(){return enabled;}
    }
    public class IncomesExpenses{
        String id;
        String base_account_id;
        String from_account_id;
        String description;
        String type;
        String amount;
        String date;
        public IncomesExpenses(){}
        public String getId() { return id; }
        public String getBase_account_id() { return base_account_id; }
        public String getFrom_account_id() { return from_account_id; }
        public String getDescription() { return description; }
        public String getType() { return type; }
        public String getAmount() { return amount; }
        public String getDate() { return date; }
    }
}
