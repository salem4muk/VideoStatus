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
import com.salem4muk.videostatus.Util.Events;
import com.salem4muk.videostatus.Util.GlobalBus;
import com.salem4muk.videostatus.Util.Method;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class RelatedPortraitAdapter extends RecyclerView.Adapter<RelatedPortraitAdapter.ViewHolder> {

    private Method method;
    private Activity activity;
    private DatabaseHandler db;
    private String type;
    private List<SubCategoryList> relatedLists;

    public RelatedPortraitAdapter(Activity activity, List<SubCategoryList> relatedLists, InterstitialAdView interstitialAdView, String type) {
        this.activity = activity;
        method = new Method(activity, interstitialAdView);
        db = new DatabaseHandler(activity);
        this.relatedLists = relatedLists;
        this.type = type;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.related_portrait_adapter, parent, false);

        return new RelatedPortraitAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        if (db.checkId_Fav(relatedLists.get(position).getId())) {
            holder.imageView_favourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_fav));
        } else {
            holder.imageView_favourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_fav_hov));
        }

        Glide.with(activity).load(relatedLists.get(position).getVideo_thumbnail_s())
                .placeholder(R.drawable.placeholder_portable).into(holder.imageView);

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                method.interstitialAdShow(position, type, relatedLists.get(position).getId());
            }
        });

        holder.imageView_favourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (db.checkId_Fav(relatedLists.get(position).getId())) {
                    method.addToFav(db, relatedLists, position);
                    holder.imageView_favourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_fav_hov));
                } else {
                    db.deleteFav(relatedLists.get(position).getId());
                    holder.imageView_favourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_fav));
                }
                Events.FavouriteNotify homeNotify = new Events.FavouriteNotify(relatedLists.get(position).getId(), relatedLists.get(position).getVideo_layout());
                GlobalBus.getBus().post(homeNotify);

            }
        });

    }

    @Override
    public int getItemCount() {
        if (relatedLists.size() == 0) {
            return 0;
        } else if (relatedLists.size() == 1) {
            return 1;
        } else if (relatedLists.size() == 2) {
            return 2;
        }else if (relatedLists.size() == 3) {
            return 3;
        } else if (relatedLists.size() == 4) {
            return 4;
        } else {
            return 5;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private RoundedImageView imageView;
        private ImageView imageView_favourite;
        private RelativeLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            relativeLayout = itemView.findViewById(R.id.relativeLayout_rel_por_adapter);
            imageView = itemView.findViewById(R.id.imageView_rel_por_adapter);
            imageView_favourite = itemView.findViewById(R.id.imageView_fav_rel_por_adapter);

        }
    }
}
