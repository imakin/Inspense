package in.izzulmak.inspense.input_listeners;

import android.content.DialogInterface;

/**
 * Created by Izzulmakin on 24/03/16.
 * for the negative cancel action
 */
public class ILCancel implements DialogInterface.OnClickListener {
        private static ILCancel singleton;
        protected ILCancel(){}

        /**
         * return the cancel object. <br/>
         * though GC is not a myth, this listener is simple and always behave the same, so
         * this will just yield single object intended to be used all over the program
         *
         * @return the singleton of ILCancel object. instantiated when first time called
         */
        public static ILCancel get(){
            if (singleton==null)
            {
                singleton = new ILCancel();
            }
            return singleton;
        }
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.cancel();
        }
}