package com.example.dheeraj.btmusicreceiver.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dheeraj.btmusicreceiver.R;
import com.example.dheeraj.btmusicreceiver.mainmenu;
import com.example.dheeraj.btmusicreceiver.models.Song;

import java.util.ArrayList;

/**
 * Created by dheeraj on 17/5/18.
 */

public class SongListAdapters extends RecyclerView.Adapter<SongListAdapters.viewholder> {
    Context ctx;
    ArrayList<Song> data;
    boolean server;
    int len;

    public SongListAdapters(Context ctx, ArrayList<Song> data,boolean server,int len) {
        this.ctx=ctx;
        this.data=data;
        this.server=server;
        this.len=len;
    }

    @Override
    public viewholder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(ctx).inflate(R.layout.songlayout,parent,false);
        return new viewholder(v);
    }

    @Override
    public void onBindViewHolder(viewholder holder, int position) {
        if(!server) {
            holder.tvtitle.setText(data.get(position).getTitle());
            holder.tvartist.setText(data.get(position).getArtist());
        }else{
            holder.tvtitle.setText(""+(position+1));
            holder.tvartist.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if(!server)
            return data.size();
        else
            return len;
    }

    public class viewholder extends RecyclerView.ViewHolder{

        TextView tvtitle,tvartist;
        public viewholder(View itemView) {
            super(itemView);
            tvartist=(TextView) itemView.findViewById(R.id.tvartist);
            tvtitle=(TextView) itemView.findViewById(R.id.tvtitle);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(((mainmenu)((Activity) ctx)).service == null){
                        Toast.makeText(ctx,"Could not fetch the service",Toast.LENGTH_SHORT).show();
                    }
                    else{

                        ((mainmenu)((Activity) ctx)).service.setSong(getAdapterPosition());
                        Log.d("mainmenu", "onClick: playing"+getAdapterPosition()+"th song");
                        ((mainmenu)((Activity) ctx)).service.playSong();

                    }

                }
            });

        }
    }
}
