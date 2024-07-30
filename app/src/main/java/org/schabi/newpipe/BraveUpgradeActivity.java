package org.schabi.newpipe;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.king.app.dialog.AppDialog;
import com.king.app.dialog.AppDialogConfig;
import com.king.app.updater.AppUpdater;
import com.king.app.updater.callback.UpdateCallback;
import com.king.app.updater.http.OkHttpManager;

import org.schabi.newpipe.util.ThemeHelper;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.IntentCompat;
import androidx.fragment.app.FragmentManager;

import static org.schabi.newpipe.util.Localization.assureCorrectAppLanguage;


/**
 * This is a transparent activity with the only purpose to launch the upgrade dialogFragment.
 */
public class BraveUpgradeActivity extends AppCompatActivity {
    public static final String UPGRADE_INFO = "upgrade_info";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        assureCorrectAppLanguage(this);
        super.onCreate(savedInstanceState);

        ThemeHelper.setDayNightMode(this);

        final Intent intent = getIntent();
        final BraveUpgradeInfo upgradeInfo =
                IntentCompat.getParcelableExtra(intent, UPGRADE_INFO, BraveUpgradeInfo.class);

        setupAppUpdaterDialog(this, upgradeInfo, getSupportFragmentManager());
    }

    private void setupAppUpdaterDialog(
            final Context context,
            final BraveUpgradeInfo braveUpgradeInfo,
            final FragmentManager supportManager
    ) {
        if (braveUpgradeInfo == null) {
            Toast.makeText(context, BraveUpgradeInfo.class.getName() + " was null",
                    Toast.LENGTH_LONG).show();
            return;
        }

        final AppDialogConfig config = new AppDialogConfig(context);
        config.setTitle(getString(R.string.update_new_version, braveUpgradeInfo.getVersionName()))
                .setCancel(context.getString(R.string.cancel))
                .setConfirm(context.getString(R.string.update))
                .setContent(braveUpgradeInfo.getChangeLog())
                .setOnDismissListener(
                        new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(final DialogInterface dialog) {
                                finish();
                            }
                        })
                .setOnClickCancel(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        finish(); // close this activity
                    }
                })
                .setOnClickConfirm(new View.OnClickListener() {
                    private AppUpdater mAppUpdater;

                    @Override
                    public void onClick(final View v) {
                        mAppUpdater = new AppUpdater.Builder(context)
                                .setUrl(braveUpgradeInfo.getApkUrl())
                                .build();
                        final OkHttpManager manager = new OkHttpManager(
                                DownloaderImpl.getInstance().getNewBuilder().build());
                        mAppUpdater.setHttpManager(manager)
                                // Download using OkHttp implementation
                                .setUpdateCallback(new UpdateCallback() { // Update callback
                                    @Override
                                    public void onDownloading(final boolean isDownloading) {
                                        // Downloading: When isDownloading is true, it means that
                                        // the download is already started, that is, the download
                                        // has been started before; when it is false, it means that
                                        // the download has not started yet and will start soon
                                    }

                                    @Override
                                    public void onStart(final String url) {
                                        // start download
                                    }

                                    @Override
                                    public void onProgress(
                                            final long progress,
                                            final long total,
                                            final boolean isChanged) {
                                        // Download progress update: It is recommended to
                                        // update the progress of the interface only when
                                        // isChanged is true; because the actual progress
                                        // changes frequently
                                    }

                                    @Override
                                    public void onFinish(final File file) {
                                        finish(); // close this activity
                                        // Download completed
                                    }

                                    @Override
                                    public void onError(final Exception e) {
                                        // download failed
                                    }

                                    @Override
                                    public void onCancel() {
                                        // Cancel download
                                    }
                                }).start();

                        AppDialog.INSTANCE.dismissDialogFragment(supportManager);
                    }
                });
        AppDialog.INSTANCE.showDialogFragment(supportManager, config);
    }
}
