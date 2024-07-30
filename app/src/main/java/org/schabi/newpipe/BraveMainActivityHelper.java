package org.schabi.newpipe;

import android.content.Context;
import android.view.MenuItem;
import android.widget.Toast;

import org.schabi.newpipe.databinding.DrawerLayoutBinding;

public final class BraveMainActivityHelper {

    private BraveMainActivityHelper() {
    }

    // make sure it won't collide with any ITEM_ID_... in MainActivity
    static final int BRAVE_ITEM_ID_UPDATE = 100;

    public static void addBraveDrawers(
            final DrawerLayoutBinding drawerLayoutBinding,
                                       final int order) {
        drawerLayoutBinding.navigation.getMenu()
                .add(R.id.menu_options_about_group, BraveMainActivityHelper.BRAVE_ITEM_ID_UPDATE,
                        order, R.string.settings_category_updates_title)
                .setIcon(R.drawable.ic_newpipe_update);
    }

    public static void onSelectedItemInDrawer(
            final Context context,
            final MenuItem item) {

        if (item.getItemId() == BRAVE_ITEM_ID_UPDATE) {
            Toast.makeText(context, R.string.checking_updates_toast, Toast.LENGTH_SHORT).show();
            NewVersionWorker.enqueueNewVersionCheckingWork(context, true);
        }
    }
}
