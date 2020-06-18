package com.healthapp.ui.contacts;


import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.healthapp.Contact;
import com.healthapp.GPStracker;
import com.healthapp.Meeting;
import com.healthapp.MeetingDatabase;
import com.healthapp.R;
import com.healthapp.util.BlurredDialogFragment;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class FragmentContact extends Fragment implements ContactsAdapterDelegate, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CONTACTS_LIST_ID = 1;
    private static final SimpleDateFormat FULL_DATE_FORMAT_24HR = new SimpleDateFormat("dd/ MM/ yyyy - HH:mm");

    public interface ActivityDelegate {
        void onContactAdded();
    }

    public static FragmentContact newInstance() {
        return new FragmentContact();
    }

    private ProgressBar loadingProgressBar;
    private RecyclerView myRecyclerView;

    private ActivityDelegate activityDelegate;
    private ContactRecyclerViewAdapter recyclerAdapter;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private GPStracker GPS;
    private MeetingDatabase db; // TODO: Move this into some kind of repository and use dependency injection

    private FusedLocationProviderClient fusedLocationClient;
    private Location lastKnownLocation;

    private String[] projection = {ContactsContract.Data._ID,
            ContactsContract.Data.DISPLAY_NAME,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.DATA1,
            ContactsContract.Data.DATA2,
            ContactsContract.Data.DATA3,
            ContactsContract.Contacts.PHOTO_THUMBNAIL_URI};
    private String selection = ContactsContract.Data.MIMETYPE + " IN ('" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "')";

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == CONTACTS_LIST_ID) {
            return new CursorLoader(
                    Objects.requireNonNull(getActivity()),
                    ContactsContract.Data.CONTENT_URI,
                    projection,
                    selection,
                    null,
                    ContactsContract.Contacts.DISPLAY_NAME
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {

        ArrayList<Contact> listContact = new ArrayList<>();

        String lastName = "";

        if (data.getCount() > 0) {
            while (data.moveToNext()) { // TODO: Refactor this!

                String name = data.getString(1);

                if (lastName.equals(name)) {
                    continue;
                }

                lastName = name;

                long id = data.getLong(0);
                String mime = data.getString(2); // type of data (e.g. "phone")
                String number = data.getString(3); // the actual info, e.g. +1-212-555-1234

                int type = data.getInt(4); // a numeric value representing type: e.g. home / office / personal
                String label = data.getString(5); // a custom label in case type is "TYPE_CUSTOM"
                String contactPhotoURI = data.getString(6);

                String labelStr = (String) ContactsContract.CommonDataKinds.Phone.getTypeLabel(getResources(), type, label);

                Contact currContact = new Contact(name, number, contactPhotoURI);
                listContact.add(currContact);
            }
        }
        if (!listContact.isEmpty()) {
            recyclerAdapter.setContacts(listContact);
            myRecyclerView.scheduleLayoutAnimation();
        }
        loadingProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        //
    }

    @Override
    public void onStop() {
        super.onStop();
        androidx.loader.app.LoaderManager.getInstance(this).destroyLoader(CONTACTS_LIST_ID);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.GPS = new GPStracker(getActivity().getApplicationContext());
        db = MeetingDatabase.getInstance(getActivity().getApplicationContext());
        LoaderManager.getInstance(this).initLoader(CONTACTS_LIST_ID, null, this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() != null) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
                this.lastKnownLocation = location;
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contact, container, false);
        loadingProgressBar = v.findViewById(R.id.loadingProgressBar);
        loadingProgressBar.setVisibility(View.VISIBLE);
        myRecyclerView = v.findViewById(R.id.contacts_recyclerview);
        recyclerAdapter = new ContactRecyclerViewAdapter(this);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        myRecyclerView.setAdapter(recyclerAdapter);

        final LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getActivity(), R.anim.layout_animation_fall_down);
        myRecyclerView.setLayoutAnimation(controller);

        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ActivityDelegate) {
            this.activityDelegate = ((ActivityDelegate) context);
        }
    }

    @Override
    public void onContactSelected(@NotNull Contact contact) {
        showAddDialog(contact);
    }

    private void showAddDialog(Contact contact) {
        BlurredDialogFragment deleteDialog = BlurredDialogFragment.Companion.newInstance(getString(R.string.add_contact_dialog_title),
                getString(R.string.add_contact_dialog_description),
                getString(R.string.add_contact_dialog_positive),
                getString(R.string.dialog_cancel));

        compositeDisposable.add(deleteDialog.getEventsObservable()
                .filter(event -> event == BlurredDialogFragment.DialogEvents.PositiveClicked)
                .doOnNext(event -> {
                    addContact(contact);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());

        deleteDialog.show(getChildFragmentManager(), "remove_contact");
    }

    private void addContact(Contact contact) {
        String name = contact.getName();
        String phoneNumber = contact.getPhoneNumber();
        String dateTime = FULL_DATE_FORMAT_24HR.format(Calendar.getInstance().getTime());
        String photoURI = contact.getPhoto();

        Location location;
        if (lastKnownLocation != null) {
            location = lastKnownLocation;
        } else {
            location = GPS.getLocation();
        }

        String latString = "0.00000000";
        String lonString = "0.00000000";
        if (location != null) {
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            latString = Double.toString(lat);
            lonString = Double.toString(lon);
        }

        Meeting newMeeting = new Meeting(
                name,
                phoneNumber,
                latString,
                lonString,
                dateTime,
                photoURI
        );
        db.meetingDao().InsertMeeting(newMeeting);

        Toast.makeText(this.getContext(), getString(R.string.add_contact_post) + " " + name, Toast.LENGTH_SHORT).show();

        activityDelegate.onContactAdded();
    }
}
