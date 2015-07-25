/*
 * Copyright (c) 2015. Ben O'Halloran/Pseudo Sudo Studios.
 * All rights reserved.
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * Full license can be found here: http://creativecommons.org/licenses/by-nc-nd/3.0/deed.en_US
 */
package com.pseudosudostudios.jdd.activities;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.pseudosudostudios.jdd.R;
import com.pseudosudostudios.jdd.utils.Difficulty;
import com.pseudosudostudios.jdd.views.Grid;
import com.pseudosudostudios.jdd.views.Tile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * The {@link android.app.Activity} that the game
 */
public class GameActivity extends Activity {

    //Views
    public Grid bg = null;

    //Keys and messages
    public final static String winMenuOption = "Win";
    public final static String isReplay = "replay string";
    public static final String hintMessage = "Hint";

    //Bundle Keys
    public static final String onSaveBaseString = "onsaved";
    public static final String onSaveSolution = "saveSolu";
    public static final String onSaveCeterTile = "center tile int array";
    public static final String onSaveMoves = "on save moves";
    public static final String onSaveTime = "on save times";
    public static final String instructionString = "Instructions";
    public static final String jsonTileSize = "tile size";
    public static final String bundleGameColors = "numberOfColorsBundle";

    //Save Data Keys
    public static final String saveDataFileName = "saved_game.txt";
    public static final String northKey = "north";
    public static final String eastKey = "east";
    public static final String southKey = "south";
    public static final String westKey = "west";
    public static final String pointX = "x";
    public static final String pointY = "y";
    public static final String moveKey = "moves";
    public static final String timeKey = "time";
    public static final String arrayKey = "tilesArray";
    public static final String levelKey = "level key";
    public static final String numColorsKey = "number of colors";

    /**
     * Relaunch stuffs, when the user backs into the app from the win screen
     */
    public static final int LAUNCH_WIN = 314;
    public static final int RELAUNCH = 235;

    /**
     * Creates the view, loads from a rotation and from a relaunch
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Remove the ActionBar, same logic as the drama app
        super.onCreate(savedInstanceState);

        bg = new Grid(this);
        setContentView(bg);
        // if there was a savedInstanceGame, then load that one
        final SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        boolean hasScreen = prefs.getBoolean(instructionString, false);
        if (!hasScreen && savedInstanceState == null) {
            showInstructions();
            onCreateStuff(savedInstanceState);
        } else if (savedInstanceState != null
                && savedInstanceState.containsKey(onSaveBaseString + 0 + 0))
            onCreateStuff(savedInstanceState);
        else
            loadGamePrompt(savedInstanceState);

        scoreNoTime = 0;
        scorePrev = 0;

    }

    //Re-init a saved game from rotation or makes a new game if needed
    private void onCreateStuff(Bundle state) {
        // this is the re-load after rotating
        if (!loadFromBundle(state)) {
            bg.makeNewGame();
        }
    }

    private boolean loadFromBundle(Bundle state) {
        if (state == null)
            return false;
        Log.d("onCreateStuff", "Using bundle");
        Log.d("Loading", "Reading from bundle");
        // Reload the game after a screen rotation
        Tile.initPaints();
        Tile[][] loadedTiles = new Tile[3][3];
        Tile[][] loadedSol = new Tile[3][3];
        for (int r = 0; r < bg.tiles.length; r++)
            for (int c = 0; c < bg.tiles[r].length; c++) {
                loadedTiles[r][c] = new Tile(this,
                        state.getIntArray(onSaveBaseString
                                + r + c));
                int[] arr = state
                        .getIntArray(onSaveSolution + r + c);
                if (arr != null)
                    loadedSol[r][c] = new Tile(this, arr);
            }
        int moves = state.getInt(onSaveMoves, 0);
        long time = state.getLong(onSaveTime, 0L);
        bg.setTileArray(loadedTiles, loadedSol, moves, time,
                state.getInt(jsonTileSize, 0));
        bg.setDifficulty(findDifficulty(state
                .getString(onSaveBaseString)));
        Grid.numberOfColors = state
                .getInt(bundleGameColors);
        bg.setCenterTile(new Tile(this, state
                .getIntArray(onSaveCeterTile)));
        Tile.initPaints();
        bg.invalidate();
        return true;
    }

    /**
     * This handles the initial display and writing the boolean to the shared prefs
     */
    private void showInstructions() {
        if (bg.isReadyToPlay())
            return;
        final SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        boolean skipInstructions = prefs.getBoolean(instructionString, false);

        if (!skipInstructions) {
            Builder d = new Builder(this);
            d.setCancelable(false)
                    .setMessage(R.string.instructions)
                    .setTitle(R.string.instructions_title)
                    .setCancelable(false)
                    .setPositiveButton("Show next time",
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    Editor edit = prefs.edit();
                                    edit.putBoolean(instructionString, false);
                                    edit.commit();
                                }
                            })
                    .setNegativeButton("Don't show again",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    Editor edit = prefs.edit();
                                    edit.putBoolean(instructionString, true);
                                    edit.commit();
                                }
                            }).show();
        }
    }

    /**
     * Prompts the user to see if they want to load a saved game
     */
    private void loadGamePrompt(final Bundle saved) {
        File f = new File(getFilesDir(), saveDataFileName);
        if (!f.exists()) {
            onCreateStuff(saved);
            bg.setTileSize();
            bg.setCenters();
            bg.invalidate();
            return;
        } else if (!isSavedGameSolved()) {
            Builder d = new Builder(this);

            d.setCancelable(false)
                    .setMessage(R.string.load_game_prompt)
                    .setTitle(R.string.load_game_title)
                    .setPositiveButton(R.string.load_game_load,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    Log.d("Load", "Calling loadGame");
                                    loadGame(saved);
                                    bg.invalidate();
                                }
                            })
                    .setNegativeButton(R.string.load_game_new,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    onCreateStuff(null);
                                }
                            });
            d.show();
        } else {
            onCreateStuff(null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveToFile();
    }

    public void saveToFile() {
        if (bg == null)
            return;
        Log.i("Save game", "Saving game");
        File outFile = null;
        try {
            JSONObject obj = bg.toJSONObject();
            outFile = new File(getFilesDir(), saveDataFileName);
            if (!outFile.exists())
                outFile.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
            writer.write(obj.toString());
            writer.close();
            Log.i("Save game", "Game Saved");
        } catch (JSONException e) {
            Log.i("Save game", "Error saving game-JSON");
            e.printStackTrace();
        } catch (IOException e) {
            Log.i("Save game", "Error saving game - writing");
            e.printStackTrace();
        } catch (NullPointerException e) {
            if (outFile != null)
                outFile.delete();
        }
    }

    private boolean isSavedGameSolved() {
        return true;
    /*
     * File dataFile = new File(getFilesDir(), saveDataFileName); try {
	 * BufferedReader read = new BufferedReader(new FileReader(dataFile));
	 * String input = ""; while (read.ready()) input += read.readLine();
	 * read.close(); JSONObject master = new JSONObject(input); JSONArray
	 * tilesFromFile = master.getJSONArray(arrayKey); Tile[][] loadedTile =
	 * new Tile[3][3]; int jsonIndex = 0; for (int row = 0; row <
	 * loadedTile.length; row++) { for (int col = 0; col <
	 * loadedTile[row].length; col++) { JSONObject currTile = tilesFromFile
	 * .getJSONObject(jsonIndex); Point center = new
	 * Point(currTile.getInt(pointX), currTile.getInt(pointY)); int north =
	 * currTile.getInt(northKey); int east = currTile.getInt(eastKey); int
	 * south = currTile.getInt(southKey); int west =
	 * currTile.getInt(westKey); loadedTile[row][col] = new Tile(this,
	 * center, north, east, south, west);
	 *
	 * jsonIndex++; } } jsonIndex = 0; return
	 * bg.isPuzzleSolved(loadedTile); } catch (Exception e) {
	 * e.printStackTrace(); return false; }
	 */
    }

    private void loadGame(Bundle saved) {
        Log.i("Load game", "Loading game");
        if (bg == null)
            bg = new Grid(this);
        File dataFile = new File(getFilesDir(), saveDataFileName);
        try {
            BufferedReader read = new BufferedReader(new FileReader(dataFile));
            String input = "";
            while (read.ready())
                input += read.readLine();
            read.close();

            JSONObject master = new JSONObject(input);
            JSONArray tilesFromFile = master.getJSONArray(arrayKey);
            Tile[][] loadedTile = new Tile[3][3];
            Tile[][] solution = new Tile[3][3];
            int jsonIndex = 0;
            for (int row = 0; row < loadedTile.length; row++) {
                for (int col = 0; col < loadedTile[row].length; col++) {
                    JSONObject currTile = tilesFromFile
                            .getJSONObject(jsonIndex);
                    Point center = new Point(currTile.getInt(pointX),
                            currTile.getInt(pointY));
                    int north = currTile.getInt(northKey);
                    int east = currTile.getInt(eastKey);
                    int south = currTile.getInt(southKey);
                    int west = currTile.getInt(westKey);
                    loadedTile[row][col] = new Tile(this, center, north, east,
                            south, west);

                    jsonIndex++;
                }
            }
            jsonIndex = 0;
            JSONArray solJSon = master.getJSONArray(onSaveSolution);
            for (int row = 0; row < solution.length; row++) {
                for (int col = 0; col < solution[row].length; col++) {
                    JSONObject currTile = solJSon.getJSONObject(jsonIndex);
                    Point center = new Point(currTile.getInt(pointX),
                            currTile.getInt(pointY));
                    int north = currTile.getInt(northKey);
                    int east = currTile.getInt(eastKey);
                    int south = currTile.getInt(southKey);
                    int west = currTile.getInt(westKey);
                    solution[row][col] = new Tile(this, center, north, east,
                            south, west);
                    jsonIndex++;
                }
            }
            bg.moves = master.getInt(moveKey);
            bg.setTileArray(loadedTile, solution, master.getInt(moveKey),
                    master.getLong(onSaveTime), master.getInt(jsonTileSize));
            bg.setDifficulty(master.getString(levelKey));
            Grid.numberOfColors = master.getInt(numColorsKey);
            Tile.initPaints();
            Log.i("Load game", "Game Loaded");
        } catch (Exception e) {
            Log.i("Load game", "Error loading game");
            if (dataFile != null)
                dataFile.delete();
            e.printStackTrace();
            showInstructions();
        }
        bg.invalidate();
    }

    public static Difficulty findDifficulty(String string) {
        if (string.equals(Difficulty.EASY.toString()))
            return Difficulty.EASY;
        else if (string.equals(Difficulty.MEDIUM.toString()))
            return Difficulty.MEDIUM;
        return Difficulty.HARD;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_game_activity, menu);
        if (!isUserDebuggable())
            menu.removeItem(R.id.action_win);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_game:
                startNewGame();
                return true;
            case R.id.action_instructions:
                startActivity(new Intent(this, Instructions.class));
                return true;
            case R.id.action_win:
                win(bg.moves);
                return true;
            default:
                Log.w("Unkown menu item", item.toString());
                return false;
        }
    }

    private static long lastBackTime = -1;

    @Override
    public void onBackPressed() {
        if (bg != null) {
            if (lastBackTime == -1) {
                lastBackTime = System.currentTimeMillis();
                Toast.makeText(this, R.string.back_press_toast,
                        Toast.LENGTH_SHORT).show();
            } else if ((System.currentTimeMillis() - lastBackTime) / 1000L <= 5) {
                lastBackTime = -1;
                bg.makeNewGame();
            }
        } else
            super.onBackPressed();
    }

    public boolean isUserDebuggable() {
        return (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (bg.tiles == null || bg.tiles[2][2] == null || bg == null) {
            super.onSaveInstanceState(outState);
            return;
        }
        for (int r = 0; r < bg.tiles.length; r++)
            for (int c = 0; c < bg.tiles[r].length; c++) {
                outState.putIntArray(onSaveBaseString + r + c,
                        bg.tiles[r][c].toIntArray());
                if (bg.sol1 != null && bg.sol1[r][c] != null)
                    outState.putIntArray(onSaveSolution + r + c,
                            bg.sol1[r][c].toIntArray());
            }
        try {
            outState.putIntArray(onSaveCeterTile, bg.getCenterTile()
                    .toIntArray());
        } catch (NullPointerException e) {
            outState.putIntArray(onSaveCeterTile, bg.tiles[1][1].toIntArray());
        }
        outState.putInt(bundleGameColors, Grid.numberOfColors);
        outState.putInt(jsonTileSize, Grid.tileSize);
        outState.putString(onSaveBaseString, bg.getDifficulty().toString());
        outState.putInt(bundleGameColors, Grid.numberOfColors);
        outState.putLong(onSaveTime, bg.getTimeSinceStart());
    }

    /**
     * Launches the win screen flow, including dealing with Facebook
     */
    public void win(int moves) {
        new File(getFilesDir(), saveDataFileName).delete();
        Log.i("Win", "Time: " + bg.getTimeSinceStart());
        Bundle bundle = new Bundle();
        bundle.putInt(WinActivity.moveKey, moves);
        bundle.putString(WinActivity.levelKey, bg.getDifficulty().toString());
        bundle.putLong(WinActivity.timeKey,
                ((System.currentTimeMillis() - bg.startTime)));
        startActivityForResult(
                new Intent(this, WinActivity.class).putExtras(bundle),
                LAUNCH_WIN);
    }

    /**
     * This is the dumb method that creates a new game
     */
    public void startNewGame() {
        bg.makeNewGame();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("Result code", resultCode + "");
        if (requestCode == LAUNCH_WIN) {
            startNewGame();
        }
    }

    /**
     * @return the score
     * @param time
     * the DELTA of the times
     * @param moves
     * the number of moves made to solve the puzzle
     */
    static int scorePrev = 0;
    static int scoreNoTime = 0;
    static final int scoreMatchMult = 20000;

    public int calcScoreBenMod(int moves, long time, int numColors,
                               String level, int matches) {
        double num = levelScoreMod(level) * (200D / moves)
                + (30 * 60 * 100D / (time + 5));
        double denom = 1 + Math.pow(7 - numColors, 2);
        return (int) Math.abs((matches * (num / denom)));
    }

    public int benScoreCalc(int moves, long time) {
        int fromColors = 2500 / (Math.abs(7 - Grid.numberOfColors) + 1);
        int fromLevel = 100 * levelScoreMod(bg.getDifficulty());
        int fromParams = (int) (time / 1000) + moves * 10;

        int score = fromColors + fromLevel - fromParams + 5000;
        if (score < 0)
            return 0;
        else
            return score;
    }

    public int levelScoreMod(String level) {
        return levelScoreMod(Difficulty.valueOf(level));
    }

    public int levelScoreMod(Difficulty d) {
        if (d == Difficulty.HARD)
            return 16;
        else if (d == Difficulty.MEDIUM)
            return 4;
        else
            return 1;
    }
}
