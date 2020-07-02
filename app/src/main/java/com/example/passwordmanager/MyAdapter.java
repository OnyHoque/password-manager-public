package com.example.passwordmanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>  {

    private OnNoteListener mOnNote;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public TextView r_account_name;
        public EditText r_username, r_password;
        OnNoteListener onNoteListener;
        public Button r_delete, r_edit;


        public MyViewHolder(View view, final OnNoteListener onNoteListener) {
            super(view);
            r_account_name = view.findViewById(R.id.text_account_name);
            r_username = view.findViewById(R.id.r_username);
            r_password = view.findViewById(R.id.r_password);
            r_delete = view.findViewById(R.id.r_delete);
            r_edit = view.findViewById(R.id.r_edit);
            this.onNoteListener = onNoteListener;

            r_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onNoteListener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            onNoteListener.onDeleteClick(position);
                        }
                    }
                }
            });

            r_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onNoteListener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            onNoteListener.onEditClick(position);
                        }
                    }
                }
            });
        }



    }

    private List<user_account>  user_accounts;

    public MyAdapter (List<user_account> ua_list, OnNoteListener onNoteListener) {
        this.user_accounts = ua_list;
        mOnNote = onNoteListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_accounts, parent, false);

        return new MyViewHolder(itemView, mOnNote);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        user_account ua = user_accounts.get(position);
        holder.r_account_name.setText(ua.getAccount_name());
        holder.r_username.setText(ua.getUsername());
        holder.r_password.setText(ua.getPassword());
    }

    @Override
    public int getItemCount() {
        return user_accounts.size();
    }

    public interface OnNoteListener{
        void onDeleteClick(int position);
        void onEditClick(int position);
    }
}
