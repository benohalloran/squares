/*
 * Copyright (c) 2015. Ben O'Halloran/Pseudo Sudo Studios.
 * All rights reserved.
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * Full license can be found here: http://creativecommons.org/licenses/by-nc-nd/3.0/deed.en_US
 */
package com.pseudosudostudios.jdd.utils;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ScoreSaves {
    private static final String fileName = "highscores.txt";
    /**
     * JSON Keys
     */
    private static final String levelKey = "level";
    private static final String moveKey = "move";
    private static final String timeKey = "time";
    private static final String colorsKey = "colors";
    private static final String dateKey = "date";

    public static final int IGNORE_SCORE = 2147483647;

    /**
     * @param c the application's context that owns the data file
     * @return a List of games saved in the file
     */
    public static List<Entry> getSaves(Context c) {
        List<Entry> entries = new ArrayList<Entry>();
        File f = new File(c.getFilesDir(), fileName);
        if (!f.exists() || f.isDirectory()) {
            return new ArrayList<Entry>();
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            StringBuffer buff = new StringBuffer();
            while (reader.ready())
                buff.append(reader.readLine());
            reader.close();
            JSONArray array = new JSONArray(buff.toString());
            for (int i = 0; i < array.length(); i++) {
                Entry e = makeEntry(array.getJSONObject(i));
                if (e.getScore() != IGNORE_SCORE)
                    entries.add(e);
            }

            Collections.sort(entries);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return entries;
    }

    /**
     * Returns an Entry from the JSONObject
     */
    private static Entry makeEntry(JSONObject j) {
        try {
            return new Entry(j.getString(levelKey), j.getInt(moveKey),
                    j.getLong(timeKey), j.getInt(colorsKey),
                    j.getString(dateKey));
        } catch (JSONException e) {
            return null;
        }
    }

    /**
     * Appends a new game @param e the new game to be added
     */
    public static void addNewScore(Context c, Entry e) {
        List<Entry> es = getSaves(c);
        for (Entry ez : es)
            if (ez.equals(e))
                return;
        if (!es.contains(e))
            es.add(e);
        writeFile(c, es);
    }

    public static boolean deleteFile(Context c) {
        return new File(c.getFilesDir(), fileName).delete();
    }

    /**
     * Gets the number of saved games
     */
    public static int getScoreCount(Context c) {
        return getSaves(c).size();
    }

    private static void writeFile(Context c, List<Entry> entries) {
        Collections.sort(entries);
        File file = new File(c.getFilesDir(), fileName);
        try {
            file.createNewFile();
            JSONArray array = new JSONArray();
            for (Entry e : entries)
                array.put(makeJSONObject(e));
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(array.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    private static JSONObject makeJSONObject(Entry e) {
        JSONObject r = new JSONObject();
        try {
            r.put(levelKey, e.getLevel());
            r.put(moveKey, e.getMoves());
            r.put(timeKey, e.getTimeRaw());
            r.put(colorsKey, e.getNumberOfColors());
            r.put(dateKey, e.getDate());
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        return r;
    }
}
