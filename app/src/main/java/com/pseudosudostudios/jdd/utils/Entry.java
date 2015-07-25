/*
 * Copyright (c) 2015. Ben O'Halloran/Pseudo Sudo Studios.
 * All rights reserved.
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * Full license can be found here: http://creativecommons.org/licenses/by-nc-nd/3.0/deed.en_US
 */
package com.pseudosudostudios.jdd.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A saved game file. Includes the level, the moves, number of colors, time of
 * the game and when it was played
 */
public class Entry implements Comparable<Entry> {
    private String level;
    private int moves;
    private int numberOfColors;
    private long time;
    private String date;

    public Entry(String level, int moves, long time, int numColors) {
        this(level, moves, time, numColors, DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM, DateFormat.DEFAULT).format(new Date()));
    }

    public Entry(String level, int moves, long time, int numColors, String t) {
        this.level = level;
        this.moves = moves;
        this.time = time;
        this.numberOfColors = numColors;
        this.date = t;
    }

    public String getDate() {
        return date;
    }

    public int getNumberOfColors() {
        return numberOfColors;
    }

    public int getScore() {
        return calcScoreBenMod(moves, getTime(), numberOfColors,
                level.toUpperCase(), 12);
    }

    public static int calcScoreBenMod(int moves, long time, int numColors,
                                      String level, int matches) {
        double num = levelScoreMod(level) * (200D / moves)
                + (30 * 60 * 100D / (time + 5));
        double denom = 1 + Math.pow(7 - numColors, 2);
        return (int) Math.abs((matches * (num / denom)));
    }

    private static int levelScoreMod(String level) {
        return levelScoreMod(Difficulty.valueOf(level));
    }

    private static int levelScoreMod(Difficulty d) {
        if (d == Difficulty.HARD)
            return 16;
        else if (d == Difficulty.MEDIUM)
            return 4;
        else
            return 1;
    }

    public String getLevel() {
        return level;
    }

    public int getMoves() {
        return moves;
    }

    public long getTime() {
        return time / 1000;
    }

    public long getTimeRaw() {
        return time;
    }

    public String getPrettyDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
        try {
            return dateFormat.format(DateFormat.getDateTimeInstance().parse(
                    getDate()));
        } catch (ParseException e) {
            e.printStackTrace();
            return getDate();
        }
    }

    @Override
    public int compareTo(Entry another) {
        if (another == null)
            return -1;
        int otherScore = another.getScore();
        int score = getScore();
        if (otherScore < score)
            return -1;
        if (otherScore > score)
            return 1;
        else {
            Date me = getDateObj();
            Date param = another.getDateObj();
            if (me == null || param == null)
                return 0;
            if (me.before(param))
                return -1;
            else if (me.after(param))
                return 1;
            else
                return 0;
        }
    }

    public Date getDateObj() {
        try {
            return DateFormat.getDateTimeInstance().parse(getDate());
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public String toString() {
        String baseDescription = "Score: %s, " + "Level: %s, " + "Moves: %s, "
                + "Time: %s, " + "Colors: %s, " + "Date: %s";
        return String.format(baseDescription, getScore() + "",
                Utils.returnFirstCap(level), moves + "",
                time + "" + "", numberOfColors + "", date);
    }

    @Override
    public int hashCode() {
        int result = level.hashCode();
        result = 31 * result + moves;
        result = 31 * result + numberOfColors;
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + date.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Entry other = (Entry) obj;
        if (date == null) {
            if (other.date != null)
                return false;
        } else if (!date.equals(other.date))
            return false;
        if (level == null) {
            if (other.level != null)
                return false;
        } else if (!level.equals(other.level))
            return false;
        if (moves != other.moves)
            return false;
        if (numberOfColors != other.numberOfColors)
            return false;
        return time == other.time;
    }

}
