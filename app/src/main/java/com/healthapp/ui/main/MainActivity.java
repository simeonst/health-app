package com.healthapp.ui.main;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.healthapp.R;
import com.healthapp.ui.contacts.FragmentContact;
import com.healthapp.ui.meetings.MeetingsDelegate;
import com.healthapp.util.ActivityExtKt;
import com.healthapp.util.BlurredDialogFragment;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements FragmentContact.ActivityDelegate {

    private SectionsPagerAdapter sectionsPagerAdapter;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private static final int REQ_PERM_CONTACTS = 1001;
    private static final int REQ_PERM_LOCATION = 1002;

    private BlurredDialogFragment delayedDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAndRequestPermissions();

        ExtendedFloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            share();
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        ActivityExtKt.setTransparentStatusBar(this);
        return super.onCreateView(name, context, attrs);
    }

    private void requestContactsPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, REQ_PERM_CONTACTS);
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERM_LOCATION);
    }

    private void initApp() {
        this.sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
    }

    private void checkForStartupDialog() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        boolean previouslyStarted = prefs.getBoolean(getString(R.string.pref_previously_started), false);
        if (!previouslyStarted) {
            SharedPreferences.Editor edit = prefs.edit();
            edit.putBoolean(getString(R.string.pref_previously_started), Boolean.TRUE);
            edit.apply();
            delayedDialog = showStartUpDialog();
        } else {
            initApp();
        }
    }

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();
            } else {
                checkForStartupDialog();
            }
        } else {
            requestContactsPermission();
        }
    }

    private void share() {

        MeetingsDelegate fragMeetings = sectionsPagerAdapter.getFragmentMeetings();
        String exportMeetings = fragMeetings.getShareableContent();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, exportMeetings);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Only for Android Marshmallow and lower versions
        // Show explanation dialog here in order to prevent IllegalStateException when called on onRequestPermissionsResult()
        if (delayedDialog != null) {
            delayedDialog.show(getSupportFragmentManager(), "permissionExplanationDialog");
            delayedDialog = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQ_PERM_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                    delayedDialog = showContactsPermissionRationaleDialog();
                } else {
                    delayedDialog = showSettingsContactsPermissionDialog();
                }
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkAndRequestPermissions();
            }
        } else if (requestCode == REQ_PERM_LOCATION) {
            checkForStartupDialog(); // Let the user proceed regardless of the location permission outcome
        }
    }

    // TODO extract strings
    private BlurredDialogFragment showSettingsContactsPermissionDialog() {
        BlurredDialogFragment settingsDialog = BlurredDialogFragment.Companion.newInstance(getString(R.string.contact_permission_dialog_title),
                getString(R.string.settings_permission_dialog_description),
                getString(R.string.settings_permission_dialog_positive));

        settingsDialog.setCancelable(false);
        compositeDisposable.add(settingsDialog.getEventsObservable()
                .doOnNext(event -> {
                    switch (event) {
                        case PositiveClicked: {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                        break;
                        case NegativeClicked: {
                            finish();
                        }
                        break;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());

        return settingsDialog;
    }

    private BlurredDialogFragment showContactsPermissionRationaleDialog() {
        BlurredDialogFragment startUpDialog = BlurredDialogFragment.Companion.newInstance(getString(R.string.contact_permission_dialog_title),
                getString(R.string.settings_permission_dialog_description_attempt),
                getString(R.string.settings_permission_dialog_grant),
                getString(R.string.settings_permission_dialog_cancel));

        startUpDialog.setCancelable(false);
        compositeDisposable.add(startUpDialog.getEventsObservable()
                .doOnNext(event -> {
                    switch (event) {
                        case PositiveClicked: {
                            checkAndRequestPermissions();
                        }
                        break;
                        case NegativeClicked: {
                            finish();
                        }
                        break;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());

        return startUpDialog;
    }

    @Override
    public void onContactAdded() {
        sectionsPagerAdapter.getFragmentMeetings().onContactAdded();
    }

    private BlurredDialogFragment showStartUpDialog() {
        BlurredDialogFragment startUpDialog = BlurredDialogFragment.Companion.newInstance(getString(R.string.start_up_dialog_title),
                getString(R.string.start_up_dialog_description),
                getString(R.string.start_up_dialog_positive));

        startUpDialog.setCancelable(false);
        compositeDisposable.add(startUpDialog.getEventsObservable()
                .filter(event -> event == BlurredDialogFragment.DialogEvents.PositiveClicked)
                .doOnNext(event -> {
                    initApp();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());

        return startUpDialog;
    }

    @Override
    protected void onPause() {
        super.onPause();
        compositeDisposable.clear();
    }
}