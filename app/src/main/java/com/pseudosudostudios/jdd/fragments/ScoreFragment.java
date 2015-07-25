/*
 * Copyright (c) 2015. Ben O'Halloran/Pseudo Sudo Studios.
 * All rights reserved.
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * Full license can be found here: http://creativecommons.org/licenses/by-nc-nd/3.0/deed.en_US
 */
package com.pseudosudostudios.jdd.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pseudosudostudios.jdd.R;
import com.pseudosudostudios.jdd.activities.WinActivity;
import com.pseudosudostudios.jdd.utils.Entry;
import com.pseudosudostudios.jdd.utils.HighScoresAdapter;
import com.pseudosudostudios.jdd.utils.ScoreSaves;
import com.pseudosudostudios.jdd.views.Grid;

/**
 * This is the fragment where the user sees the scores Complete: April 8 2013
 */
public class ScoreFragment extends Fragment implements OnClickListener {
    private TextView userNameView, scoreDisplay, timeDisplay, movesDisplay;
    private Button playAgain, shareSocial;
    private ListView highScores;
    private static final int REAUTH_ACTIVITY_CODE = 100;

    private View signIn, signOut;

    /* Score variables */
    int numColors;
    int moves;
    long time;
    int score;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REAUTH_ACTIVITY_CODE) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.score_display, container,
                false);
        // Find the user's profile picture custom view
        // Find the user's name view
        userNameView = (TextView) view.findViewById(R.id.selection_user_name);
        scoreDisplay = (TextView) view.findViewById(R.id.scoreDisplay);
        movesDisplay = (TextView) view.findViewById(R.id.movesDisplay);
        timeDisplay = (TextView) view.findViewById(R.id.timeDisplay);
        playAgain = (Button) view.findViewById(R.id.playAgainButton);
        highScores = (ListView) view.findViewById(R.id.high_score_list_view);

        signIn = view.findViewById(R.id.sign_in_button_google_play_services);
        signOut = view.findViewById(R.id.sign_out_button);


        TextView tv = new TextView(getActivity());
        tv.setText(getActivity().getString(R.string.high_scores));
        tv.setTextSize(20);
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        tv.setPadding(0, 6, 0, 6);
        highScores.addHeaderView(tv);

        shareSocial = (Button) view.findViewById(R.id.social_share);
        shareSocial.setVisibility(View.INVISIBLE);

        // onClickListeners
        shareSocial.setOnClickListener(this);

        signIn.setOnClickListener(this);
        signOut.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.social_share:
                shareStories();
                return;
            case R.id.playAgainButton:
                newGame();
                return;
            case R.id.sign_out_button:
                getWinActivity().signOut();
                viewsSignOut();
                return;
            case R.id.sign_in_button_google_play_services:
                getWinActivity().beginUserInitiatedSignIn();
                viewsSignIn();
                return;

        }
    }

    public void viewsSignIn() {
        signOut.setVisibility(View.VISIBLE);
        signIn.setVisibility(View.GONE);
    }

    public void viewsSignOut() {
        signOut.setVisibility(View.GONE);
        signIn.setVisibility(View.VISIBLE);
    }

    private WinActivity getWinActivity() {
        return (WinActivity) super.getActivity();
    }

    public void newGame() {
        getWinActivity().finish();
    }

    private void shareStories() {
        // View stuff
        View myView = ((LayoutInflater) getActivity().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.social_share_root, null);
        final EditText input = (EditText) myView
                .findViewById(R.id.colorInputET);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            input.setTextColor(Color.BLACK);
        final EditText messageBox = (EditText) myView
                .findViewById(R.id.social_message);
        // set which is visible
        if (!isGoogleSignedIn()) {
            Toast.makeText(getActivity(), R.string.no_sign_in,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        // AlertDialog stuff
        AlertDialog.Builder build = new AlertDialog.Builder(getActivity());

        build.setTitle("Select your service")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String message = messageBox.getText().toString();
                        publishGoogle(message);
                    }
                }).setView(myView).show();
    }

    private void publishGoogle(String message) {
        getWinActivity().publishGoogle(message);
    }

    private boolean isGoogleSignedIn() {
        return getWinActivity().isSignedIn();
    }

    public void setDisplays(Bundle data) {
        try {
            this.time = data.getLong(WinActivity.timeKey);
            this.moves = data.getInt(WinActivity.moveKey);
            this.numColors = Grid.numberOfColors;
            Entry n = new Entry(data.getString(WinActivity.levelKey), moves,
                    time, Grid.numberOfColors);
            this.score = n.getScore();
            timeDisplay.setText(time / 1000 + "");
            scoreDisplay.setText(score + "");
            movesDisplay.setText(moves + "");
            shareSocial.setVisibility(View.VISIBLE);
            ScoreSaves.addNewScore(getActivity(), n);
            highScores.setAdapter(new HighScoresAdapter(getActivity()));
            highScores.invalidate();
            // Google play games
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void setUserNameView(String name) {
        if (userNameView != null)
            userNameView.setText(name + " won!");
    }
}
