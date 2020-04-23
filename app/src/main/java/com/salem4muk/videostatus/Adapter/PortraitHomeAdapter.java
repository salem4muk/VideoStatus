package com.salem4muk.videostatus.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.salem4muk.videostatus.DataBase.DatabaseHandler;
import com.salem4muk.videostatus.InterFace.InterstitialAdView;
import com.salem4muk.videostatus.Item.SubCategoryList;
import com.salem4muk.videostatus.R;
import com.salem4muk.videostatus.Util.Method;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class PortraitHomeAdapter extends RecyclerView.Adapter<PortraitHomeAdapter.ViewHolder> {

    private Method method;
    private Activity activity;
    private DatabaseHandler db;
    private String type;
    private List<SubCategoryList> portraitLists;

    public PortraitHomeAdapter(Activity activity, List<SubCategoryList> portraitLists, InterstitialAdView interstitialAdView, String type) {
        this.activity = activity;
        method = new Method(activity, interstitialAdView);
        db = new DatabaseHandler(activity);
        this.portraitLists = portraitLists;
        this.type = type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.portrait_home_adapter, parent, false);

        return new PortraitHomeAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        if (db.checkId_Fav(portraitLists.get(position).getId())) {
            holder.imageView_favourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_fav));
        } else {
            holder.imageView_favourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_fav_hov));
        }

        Glide.with(activity).load(portraitLists.get(position).getVideo_thumbnail_s())
                .placeholder(R.drawable.placeholder_portable).into(holder.imageView);

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                method.interstitialAdShow(position, type, portraitLists.get(position).getId());
            }
        });

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (db.checkId_Fav(portraitLists.get(position).getId())) {
                    method.addToFav(db, portraitLists, position);
                    holder.imageView_favourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_fav_hov));
                } else {
                    db.deleteFav(portraitLists.get(position).getId());
                    holder.imageView_favourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_fav));
                }
            }
        });

    }


    @Override
    public int getItemCount() {
        return portraitLists.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout relativeLayout;
        private RoundedImageView imageView;
        private ImageView imageView_favourite;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_portrait_home_adapter);
            imageView_favourite = itemView.findViewById(R.id.imageView_fav_portrait_home_adapter);
            relativeLayout = itemView.findViewById(R.id.relativeLayout_fav_portrait_home_adapter);

        }
    }
}
