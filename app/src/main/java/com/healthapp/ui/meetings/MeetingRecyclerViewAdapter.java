package com.healthapp.ui.meetings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.healthapp.Meeting;
import com.healthapp.R;

import java.util.ArrayList;
import java.util.List;

public class MeetingRecyclerViewAdapter extends RecyclerView.Adapter<MeetingRecyclerViewAdapter.MyViewHolder> {

    private List<Meeting> mData = new ArrayList<>();
    private MeetingsDelegate meetingsDelegate;
    private Context context;
    private boolean on_attach = true;

    MeetingRecyclerViewAdapter(MeetingsDelegate fragmentMeetings) {
        this.meetingsDelegate = fragmentMeetings;
    }

    void setData(List<Meeting> listMeeting) {
        this.mData.clear();
        this.mData.addAll(listMeeting);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_meeting_list_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.meetingContactName.setText(mData.get(position).getName());
        holder.meetingPhoneNumber.setText(mData.get(position).getPhoneNumber());
        holder.meetingDate.setText(mData.get(position).getTimeDate());
        String lat = mData.get(position).getLatitude();
        String lon = mData.get(position).getLongitude();
        if (lat != null && lon != null) {
            String location = mData.get(position).getLatitude().substring(0, 8) + " , " + mData.get(position).getLongitude().substring(0, 8);
            holder.meetingLocation.setText(location);
        }

        Glide.with(context)
                .load(mData.get(position).getPhotoURI())
                .centerCrop()
                .placeholder(R.drawable.ic_contact)
                .apply(RequestOptions.circleCropTransform())
                .into(holder.contactImg);

        ImageView img = holder.itemView.findViewById(R.id.deleteIcon);
        img.setOnClickListener((v -> meetingsDelegate.onContactRemoved(mData.get(position))));
//        ViewExtKt.setAnimation(holder.itemView, position, on_attach, 300, 5);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                on_attach = false;
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private AppCompatTextView meetingContactName;
        private AppCompatTextView meetingPhoneNumber;
        private AppCompatTextView meetingDate;
        private AppCompatTextView meetingLocation;
        private AppCompatImageView contactImg;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);

            meetingContactName = itemView.findViewById(R.id.meetingContactName);
            meetingPhoneNumber = itemView.findViewById(R.id.meetingPhoneNumber);
            meetingDate = itemView.findViewById(R.id.meetingDate);
            meetingLocation = itemView.findViewById(R.id.meetingLocation);
            contactImg = itemView.findViewById(R.id.contactImg);
        }
    }
}
