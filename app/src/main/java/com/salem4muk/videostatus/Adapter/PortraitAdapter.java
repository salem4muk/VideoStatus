package com.salem4muk.videostatus.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.salem4muk.videostatus.DataBase.DatabaseHandler;
import com.salem4muk.videostatus.InterFace.InterstitialAdView;
import com.salem4muk.videostatus.Item.SubCategoryList;
import com.salem4muk.videostatus.R;
import com.salem4muk.videostatus.Util.Events;
import com.salem4muk.videostatus.Util.GlobalBus;
import com.salem4muk.videostatus.Util.Method;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class PortraitAdapter extends RecyclerView.Adapter {

    private Activity activity;
    private Method method;
    private DatabaseHandler db;
    private String type;
    private List<SubCategoryList> subCategoryLists;

    private final int VIEW_TYPE_ITEM = 1;
    private final int VIEW_TYPE_LOADING = 0;

    public PortraitAdapter(Activity activity, List<SubCategoryList> subCategoryLists, InterstitialAdView interstitialAdView, String type) {
        this.activity = activity;
        method = new Method(activity, interstitialAdView);
        db = new DatabaseHandler(activity);
        this.subCategoryLists = subCategoryLists;
        this.type = type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(activity).inflate(R.layout.portrait_adapter, parent, false);
            return new ViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View v = LayoutInflater.from(activity).inflate(R.layout.layout_loading_item, parent, false);
            return new ProgressViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {

            final ViewHolder viewHolder = (ViewHolder) holder;

            if (db.checkId_Fav(subCategoryLists.get(position).getId())) {
                viewHolder.imageView_favourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_fav));
            } else {
                viewHolder.imageView_favourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_fav_hov));
            }

            Glide.with(activity).load(subCategoryLists.get(position).getVideo_thumbnail_s())
                    .placeholder(R.drawable.placeholder_portable).into(viewHolder.imageView);

            viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    method.interstitialAdShow(position, type, subCategoryLists.get(position).getId());
                }
            });

            viewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (db.checkId_Fav(subCategoryLists.get(position).getId())) {
                        method.addToFav(db, subCategoryLists, position);
                        viewHolder.imageView_favourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_fav_hov));
                    } else {
                        db.deleteFav(subCategoryLists.get(position).getId());
                        viewHolder.imageView_favourite.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_fav));
                    }
                    Events.FavouriteNotify homeNotify = new Events.FavouriteNotify(subCategoryLists.get(position).getId(), subCategoryLists.get(position).getVideo_layout());
                    GlobalBus.getBus().post(homeNotify);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return subCategoryLists.size() + 1;
    }

    private boolean isHeader(int position) {
        return position == subCategoryLists.size();
    }

    @Override
    public int getItemViewType(int position) {
        return isHeader(position) ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public void hideHeader() {
        PortraitAdapter.ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private RelativeLayout relativeLayout;
        private ImageView imageView, imageView_favourite;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_portrait_adapter);
            imageView_favourite = itemView.findViewById(R.id.imageView_fav_portrait_adapter);
            relativeLayout = itemView.findViewById(R.id.relativeLayout_fav_portrait_adapter);

        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public static ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar);
        }
    }

}
