package com.healthapp.ui.contacts;

import android.content.Context;
import android.os.Build;
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
import com.healthapp.Contact;
import com.healthapp.R;
import com.healthapp.util.ViewExtKt;

import java.util.ArrayList;

public class ContactRecyclerViewAdapter extends RecyclerView.Adapter<ContactRecyclerViewAdapter.MyViewHolder> {

    private ArrayList<Contact> contacts = new ArrayList<>();
    private ContactsAdapterDelegate contactsDelegate;
    private Context context;
    private boolean on_attach = true;
    private boolean showListAnimation = Build.VERSION.SDK_INT > Build.VERSION_CODES.M;

    ContactRecyclerViewAdapter(ContactsAdapterDelegate contactsDelegate) {
        this.contactsDelegate = contactsDelegate;
    }

    public void setContacts(ArrayList<Contact> contactsList) {
        this.contacts.clear();
        this.contacts.addAll(contactsList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        this.context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_contact_list_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.contactName.setText(contacts.get(position).getName());
        holder.contactNumber.setText(contacts.get(position).getPhoneNumber());

        Glide.with(context)
                .load(contacts.get(position).getPhoto())
                .centerCrop()
                .placeholder(R.drawable.ic_contact)
                .apply(RequestOptions.circleCropTransform())
                .into(holder.imgContactIcon);

        ImageView img = holder.itemView.findViewById(R.id.addIcon);
        img.setOnClickListener(v -> contactsDelegate.onContactSelected(contacts.get(position)));
        if (showListAnimation) {
            ViewExtKt.setAnimation(holder.itemView, position, on_attach, 300, 5);
        }
    }

    @Override
    public int getItemCount() {
        return contacts != null ? contacts.size() : 0;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private AppCompatTextView contactName;
        private AppCompatTextView contactNumber;
        private AppCompatImageView imgContactIcon;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.contactName);
            contactNumber = itemView.findViewById(R.id.contactNumber);
            imgContactIcon = itemView.findViewById(R.id.contactImg);
        }
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
}
