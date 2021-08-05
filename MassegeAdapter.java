package com.example.chatapp;

import android.content.Context;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MassegeAdapter extends RecyclerView.Adapter<MassegeAdapter.MassegeHolderAdapter>{

    private static final int TYPE_MY_MASSEGE = 0;
    private static final int TYPE_OTHER_MASSEGE = 1;
    private Context context;
    List<Massege> masseges;
    public MassegeAdapter(Context context) {
        this.masseges = new ArrayList<>();
        this.context =context;
    }

    public List<Massege> getMasseges() {
        return masseges;
    }

    public void setMasseges(List<Massege> masseges) {
        this.masseges = masseges;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MassegeHolderAdapter onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_MY_MASSEGE){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.other_massege,parent,false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_massege,parent,false);
        }
        return new MassegeHolderAdapter(view);
    }

    @Override
    public int getItemViewType(int position) {
        Massege massege = masseges.get(position);
        String author = massege.getAuthor();
        if (author != null && author.equals(PreferenceManager.getDefaultSharedPreferences(context).getString("author", "Anonim"))){
            return TYPE_MY_MASSEGE;
        }else {
            return TYPE_OTHER_MASSEGE;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull  MassegeAdapter.MassegeHolderAdapter holder, int position) {
        Massege massege = masseges.get(position);
        String author = massege.getAuthor();
        String text = massege.getText();
        String urlToImage = massege.getImageURL();
        holder.textViewAuthor.setText(author);
        if (urlToImage == null || urlToImage.isEmpty()){
            holder.imageViewImage.setVisibility(View.GONE);
        } else {
            holder.imageViewImage.setVisibility(View.VISIBLE);
        }
        holder.textViewAuthor.setText(author);
        if (text != null && !text.isEmpty()){
            holder.textViewText.setText(text);
        } if (urlToImage != null&& !urlToImage.isEmpty()){
            Picasso.get().load(urlToImage).into(holder.imageViewImage);
        }
    }

    @Override
    public int getItemCount() {
        return masseges.size();
    }

    class MassegeHolderAdapter extends RecyclerView.ViewHolder{
        private TextView textViewAuthor;
        private TextView textViewText;
        private ImageView imageViewImage;

        public MassegeHolderAdapter(@NonNull View itemView) {
            super(itemView);
            textViewAuthor = itemView.findViewById(R.id.textViewAuthor);
            textViewText = itemView.findViewById(R.id.textViewText);
            imageViewImage = itemView.findViewById(R.id.imageViewImage);
        }
    }
}
