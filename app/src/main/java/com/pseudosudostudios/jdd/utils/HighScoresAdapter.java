/*
 * Copyright (c) 2015. Ben O'Halloran/Pseudo Sudo Studios.
 * All rights reserved.
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * Full license can be found here: http://creativecommons.org/licenses/by-nc-nd/3.0/deed.en_US
 */
package com.pseudosudostudios.jdd.utils;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pseudosudostudios.jdd.R;
import com.pseudosudostudios.jdd.fragments.ScoreFragment;

import java.util.Collections;
import java.util.List;

public class HighScoresAdapter extends BaseAdapter {
    private Context con;
    List<Entry> data;
    private static final String TAG = "Adapter";

    public HighScoresAdapter(Context context) {
        data = ScoreSaves.getSaves(context);
        Collections.sort(data);
        Log.d(TAG, "Data Loaded " + data.size());
        con = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).hashCode();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        TextView myView;
        if (convertView == null) {
            myView = (TextView) ((LayoutInflater) con
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(R.layout.scores_list, null);
        } else
            myView = (TextView) convertView;

        myView.setText(data.get(position).getScore() + " on "
                + data.get(position).getPrettyDate());
        myView.setPadding(0, 0, 0, 8);
        myView.setOnClickListener(new OnClickListener() {

            @SuppressLint("InlinedApi")
            @Override
            public void onClick(View v) {
                Builder d = new Builder(con, AlertDialog.THEME_DEVICE_DEFAULT_DARK);
                d.setCancelable(true);
                View holder = ((LayoutInflater) con
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                        .inflate(R.layout.high_score, null);
                TextView moves, time, colors, date, level;
                moves = (TextView) holder.findViewById(R.id.textViewMoves);
                time = (TextView) holder.findViewById(R.id.textViewTime);
                colors = (TextView) holder.findViewById(R.id.textViewColors);
                date = (TextView) holder.findViewById(R.id.textViewDate);
                level = (TextView) holder.findViewById(R.id.textViewLevel);

                moves.setText(data.get(position).getMoves() + "");
                time.setText(data.get(position).getTime() + "");
                colors.setText(data.get(position).getNumberOfColors() + "");
                date.setText(data.get(position).getPrettyDate());
                level.setText(Utils.returnFirstCap(data.get(position)
                        .getLevel()));
                d.setView(holder)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                    }
                                }).show();
            }
        });
        return myView;
    }
}
