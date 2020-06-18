package com.healthapp.ui.meetings;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.healthapp.Meeting;
import com.healthapp.MeetingDatabase;
import com.healthapp.R;
import com.healthapp.util.BlurredDialogFragment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

// TODO: Move business logic into a ViewModel or Presenter

public class FragmentMeetings extends Fragment implements MeetingsDelegate {

    private List<Meeting> listMeetings = new ArrayList<>();
    private MeetingRecyclerViewAdapter recyclerViewAdapter;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private MeetingDatabase db; // TODO: Move this into some kind of repository and use dependency injection

    public static FragmentMeetings newInstance() {
        return new FragmentMeetings();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = MeetingDatabase.getInstance(getActivity().getApplicationContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meetings, container, false);
        RecyclerView myRecyclerView = view.findViewById(R.id.meetings_recyclerview);

        this.recyclerViewAdapter = new MeetingRecyclerViewAdapter(this);
        myRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        myRecyclerView.setAdapter(recyclerViewAdapter);

        fetchMeetings(); // TODO: Add loading animation

        return view;
    }

    @Override
    public void onContactRemoved(@NotNull Meeting meeting) {
        showDeleteDialog(meeting);
    }

    @Override
    public void onContactAdded() {
        fetchMeetings();
    }

    private void showDeleteDialog(Meeting meeting) {

        BlurredDialogFragment deleteDialog = BlurredDialogFragment.Companion.newInstance(getString(R.string.remove_contact_dialog_title),
                getString(R.string.remove_contact_dialog_description),
                getString(R.string.remove_contact_dialog_positive),
                getString(R.string.dialog_cancel));

        compositeDisposable.add(deleteDialog.getEventsObservable()
                .doOnNext(event -> {
                    switch (event) {
                        case PositiveClicked: {
                            doRemove(meeting);
                        }
                        case NegativeClicked: {

                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe());

        deleteDialog.show(getChildFragmentManager(), "remove_contact");
    }

    private void doRemove(Meeting meeting) {
        db.meetingDao().deleteMeeting(meeting);
        fetchMeetings();
        Toast.makeText(this.getContext(), getString(R.string.remove_contact_post) + " " + meeting.getName(), Toast.LENGTH_SHORT).show();
    }

    private void fetchMeetings() {
        listMeetings = db.meetingDao().getAllMeetings();
        recyclerViewAdapter.setData(listMeetings);
    }

    @NotNull
    @Override
    public String getShareableContent() {

        StringBuilder content = new StringBuilder();

        for (int i = 0; i < listMeetings.size(); i++) {
            Meeting meeting = listMeetings.get(i);
            String strMeeting = getString(R.string.name) + meeting.getName() +
                    getString(R.string.phone) + meeting.getPhoneNumber() +
                    getString(R.string.date) + meeting.getTimeDate() +
                    getString(R.string.latitude) + meeting.getLatitude() +
                    getString(R.string.longitude) + meeting.getLongitude();
            content.append(strMeeting).append("\n\n");
        }
        return content.toString();
    }
}
