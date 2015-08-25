package in.izzulmak.inspense;

import android.view.MenuItem;
import android.widget.CompoundButton;

import java.util.ArrayList;

/**
 * Created by makin on 13/08/15.
 */
public class ListIncomeClickedClass implements CompoundButton.OnCheckedChangeListener {
    private static ArrayList<Integer> selected_ids;
    public static Integer selected_idsIndex(Integer x) {return selected_ids.indexOf(x);}
    public static void selected_idsAdd(Integer x) {selected_ids.add(x);}
    public static void selected_idsRemove(int index) {selected_ids.remove(index);}
    public static int selected_idsCount() { return selected_ids.size();}
    public static int selected_idsGet(int index) {return selected_ids.get(index);}
    public static void selected_idsClear() {selected_ids.clear();}
    private Integer thisindex;
    public ListIncomeClickedClass(Integer vthisindex)
    {
        thisindex = vthisindex;
        selected_ids = new ArrayList<Integer>();
    }
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        MenuItem deletethese = EditincomeActivity.editIncomeRoomMenu.findItem(R.id.action_editincome_delete);
        MenuItem editthis = EditincomeActivity.editIncomeRoomMenu.findItem(R.id.action_editincome_edit);

        if (b) {
            if (this.selected_idsIndex(thisindex) == -1) {
                this.selected_idsAdd(thisindex);
            }
        } else {
            if (this.selected_idsIndex(thisindex) != -1) {
                this.selected_idsRemove(this.selected_idsIndex(thisindex));
            }
        }
        int checkedtotal = this.selected_idsCount();
        if (checkedtotal == 0) {
            editthis.setVisible(false);
            deletethese.setVisible(false);
        }
        else if (checkedtotal == 1) {
            editthis.setVisible(true);
            deletethese.setVisible(true);
        } else {
            editthis.setVisible(false);
            deletethese.setVisible(true);
        }
    }
}
