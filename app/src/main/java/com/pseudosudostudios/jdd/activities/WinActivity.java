/*
 * Copyright (c) 2015. Ben O'Halloran/Pseudo Sudo Studios.
 * All rights reserved.
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * Full license can be found here: http://creativecommons.org/licenses/by-nc-nd/3.0/deed.en_US
 */
package com.pseudosudostudios.jdd.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.games.Games;
import com.google.android.gms.plus.PlusShare;
import com.google.example.games.basegameutils.BaseGameActivity;
import com.pseudosudostudios.jdd.R;
import com.pseudosudostudios.jdd.fragments.ScoreFragment;
import com.pseudosudostudios.jdd.utils.Difficulty;
import com.pseudosudostudios.jdd.utils.Entry;
import com.pseudosudostudios.jdd.utils.ScoreSaves;
import com.pseudosudostudios.jdd.views.Grid;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class WinActivity extends BaseGameActivity {

    private MenuItem settings, showLeaderBoard, showAchievments;
    private boolean isResumed = false;
    public static final String moveKey = "dah move countah";
    public static final String timeKey = "dah time countah";
    public static final String levelKey = "dah challengah";
    public static final String skipSignInKey = "sign in facebook skip";
    public static final String gTag = "Google Games Service";
    // Request codes for google play
    private static final int GAMES_AGAIN = 18;
    public static final int LEADERBOARDS = 23;
    public static final int ACHIEVEMNETS = 76;
    public static final int GOOGLE_SHARE = 21;

    private static final String INIT_SYNC_PREF_KEY = "initial-sync";
    /**
     * String ids for all increment-type achievements
     */
    private static final int[] incr = {R.string.ach_novice,
            R.string.ach_rookie, R.string.ach_semi_pro, R.string.ach_master,
            R.string.ach_sudo_champion};


    ScoreFragment fragment;

    public WinActivity() {
        super();
        setRequestedClients(CLIENT_GAMES | CLIENT_PLUS);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win);
        printFacebookInfo();

        FragmentManager fm = getSupportFragmentManager();
        fragment = (ScoreFragment) fm.findFragmentById(R.id.scoreFragment);

        fragment.setDisplays(getIntent().getExtras());
    }

    private void printFacebookInfo() {
        if (!isUserDebuggable())
            return;
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    getPackageName(), PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:",
                        Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    public boolean isUserDebuggable() {
        return (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_achievements:
                startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()),
                        ACHIEVEMNETS);
                return true;
            case R.id.action_leaderboards:
                startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(getApiClient()),
                        LEADERBOARDS);
                return true;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_win_activity, menu);
        if (!isSignedIn()) {
            menu.removeItem(R.id.action_achievements);
            menu.removeItem(R.id.action_leaderboards);
        }
        return true;
    }


    @Override
    public void onResume() {
        super.onResume();
        isResumed = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        isResumed = false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GAMES_AGAIN) {
            if (resultCode == Activity.RESULT_OK) {
                beginUserInitiatedSignIn();
            } else
                Log.d(gTag, "Result code: " + resultCode);
        }
        /*else if (requestCode == ACHIEVEMNETS) {
            Toast.makeText(this, R.string.achievement_toast, Toast.LENGTH_SHORT)
                    .show();
        } else if (requestCode == LEADERBOARDS)
            Toast.makeText(this, R.string.leaderboard_toast, Toast.LENGTH_SHORT)
                    .show();*/
    }

    @Override
    public void onSignInFailed() {
        getGameHelper().showFailureDialog();
    }

    @Override
    /**When the user signs in, handle sync for current game and saved games*/
    public void onSignInSucceeded() {
        Log.d(gTag, "Succeeded");
        fragment.viewsSignIn();
        fragment.setUserNameView(Games.Players
                .getCurrentPlayer(getApiClient()).getDisplayName());
        unlocks(getIntent().getExtras());
        readFromSaveFile();
    }

    /**
     * Method for unlocks(Entry) from a bundle
     */
    public void unlocks(Bundle data) {
        Entry n = new Entry(data.getString(WinActivity.levelKey),
                data.getInt(WinActivity.moveKey),
                data.getLong(WinActivity.timeKey), Grid.numberOfColors);
        unlocks(n);
    }

    /**
     * Unlocks achievements and leaderboards for game
     */
    public void unlocks(Entry n) {
        unlockAchievements(n);
        updateLeaderBoards(n);
    }

    /**
     * Updates all leaderboards for the current game; only executed in
     * production mode
     */
    private void updateLeaderBoards(Entry n) {
        // only allows leaderboard scores to be posted in release mode
        if (isUserDebuggable())
            return;
        String lvl = n.getLevel();
        if (lvl.equals(Difficulty.EASY.toString())) {
            submitScore(R.string.leaderboard_easy_score, n.getScore());
            submitScore(R.string.leaderboard_easy_time, n.getTimeRaw());
        } else if (lvl.equals(Difficulty.MEDIUM.toString())) {
            submitScore(R.string.leaderboard_medium_score, n.getScore());
            submitScore(R.string.leaderboard_medium_time, n.getTimeRaw());
        } else if (lvl.equals(Difficulty.HARD.toString())) {
            submitScore(R.string.leaderboard_hard_score, n.getScore());
            submitScore(R.string.leaderboard_hard_time, n.getTimeRaw());
        }
    }

    /**
     * Updates the leaderBoard with the score
     *
     * @param leaderBoard the id of the string which is the leaderBoard key
     * @param score       the score to be submitted
     */
    private void submitScore(int leaderBoard, long score) {
        Games.Leaderboards.submitScore(getApiClient(), getString(leaderBoard), score);
    }

    /**
     * Unlocks all achievements and increments all relevant based on the game in
     * e This does not do the leaderboards
     *
     * @param e game data
     */
    private void unlockAchievements(Entry e) {
        ArrayList<Integer> toBeUnlocked = new ArrayList<Integer>();
        // level
        if (e.getNumberOfColors() == 6 || e.getNumberOfColors() == 7) {
            if (e.getLevel().equals(Difficulty.EASY.toString()))
                toBeUnlocked.add(R.string.ach_winner_easy);
            else if (e.getLevel().equals(Difficulty.MEDIUM.toString()))
                toBeUnlocked.add(R.string.ach_winner_medium);
            else if (e.getLevel().equals(Difficulty.HARD.toString()))
                toBeUnlocked.add(R.string.ach_winner_hard);
        }
        // speed
        if (e.getLevel().equals(Difficulty.EASY.toString())
                && e.getTime() <= 30)
            toBeUnlocked.add(R.string.ach_speedster_jr);
        else if (e.getLevel().equals(Difficulty.MEDIUM.toString())
                && e.getTime() <= 60)
            toBeUnlocked.add(R.string.ach_speedster);
        else if (e.getLevel().equals(Difficulty.HARD.toString())
                && e.getTime() <= 30 * 60)
            toBeUnlocked.add(R.string.ach_speedster_pro);

        if (e.getNumberOfColors() == 12)
            toBeUnlocked.add(R.string.ach_rainbow_game);

        allIncrementables();
        if (!e.getLevel().equals(Difficulty.EASY.toString()))
            incrementAchievement(R.string.ach_ultimate);
        for (int i : toBeUnlocked) {
            Log.d(gTag, i + "");
            unlockAchievement(i);
        }

    }

    /**
     * Unlocks achievement whose id is represented by R.string.i
     */
    public void unlockAchievement(int i) {
        Games.Achievements.unlock(getApiClient(), getString(i));
    }

    private void allIncrementables() {
        allIncrementables(1);
    }

    private void allIncrementables(int count) {
        for (int i : incr)
            incrementAchievement(i, count);
    }

    private void incrementAchievement(int stringId) {
        incrementAchievement(stringId, 1);
    }

    private void incrementAchievement(int stringId, int amount) {
        Games.Achievements.increment(getApiClient(), getString(stringId), amount);
    }

    /**
     * Checks to see if the initial file sync has completed Updates all
     * achievements unlocked from the saved games Updates all leaderboards from
     * the saved games
     */
    private void readFromSaveFile() {
        // check to see if this has happened before
        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        boolean hasSynced = prefs.getBoolean(INIT_SYNC_PREF_KEY, false);
        if (hasSynced)
            return;
        // Get the saves
        List<Entry> saves = ScoreSaves.getSaves(this);
        // Process the saved data in the same way as the current game unlocks
        for (Entry e : saves) {
            unlockAchievements(e); // achievements
            updateLeaderBoards(e); // leaderboards
        }
        // Update the shared prefs.
        prefs.edit().putBoolean(INIT_SYNC_PREF_KEY, true).commit();
    }

    public boolean isSignedIn() {
        return super.isSignedIn();
    }

    @Override
    public void signOut() {
        super.signOut();
        fragment.viewsSignOut();
    }

    public void beginUserInitiatedSignIn() {
        super.beginUserInitiatedSignIn();
    }

    public void publishGoogle(String message) {
        if (message != null && !message.equals("")
                && !message.trim().equals(""))
            Toast.makeText(getApplication(), message.trim(), Toast.LENGTH_LONG)
                    .show();
        Intent shareIntent = new PlusShare.Builder(this).setType("text/plain")
                .setText(message)
                .setContentUrl(Uri.parse(getString(R.string.play_link)))
                .getIntent();
        startActivityForResult(shareIntent, GOOGLE_SHARE);
    }
}
