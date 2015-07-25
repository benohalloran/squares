/*
 * Copyright (c) 2015. Ben O'Halloran/Pseudo Sudo Studios.
 * All rights reserved.
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * Full license can be found here: http://creativecommons.org/licenses/by-nc-nd/3.0/deed.en_US
 */
package com.pseudosudostudios.jdd.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.pseudosudostudios.jdd.views.Grid;
import com.pseudosudostudios.jdd.views.Tile;

import java.util.HashSet;
import java.util.Random;

public class TileFactory {
    private static final Random rand = new Random();

    public static final int[] colors = {Color.RED, Color.GREEN, Color.CYAN,
            Color.YELLOW, Color.BLUE, Color.MAGENTA, Color.WHITE,
            Color.rgb(165, 42, 42), // brownish-red
            Color.rgb(255, 165, 0), // orange
            Color.rgb(184, 134, 11), // gold
            Color.rgb(112, 138, 144),// dark slate gray
            Color.rgb(128, 128, 0) // olive
    };


    public static Tile[] makeLiveGame(final Context context) {
        int[] gameColors = makeColorIndices();

        Tile[] tiles = new Tile[9];
        for (int i = 0; i < tiles.length; i++)
            tiles[i] = new Tile(context, makeSingleTile(gameColors, i));
        return tiles;
    }

    /**
     * Makes the array of all of the colors
     */
    private static int[] makeColorIndices() {
        int[] gameColors = new int[24];
        if (Grid.numberOfColors > colors.length)
            Grid.numberOfColors = colors.length;
        int loops = 0;
        do {
            for (int i = 0; i < gameColors.length; i++)
                gameColors[i] = colors[rand.nextInt(Grid.numberOfColors)];
            loops++;
        } while (!uniqueColors(gameColors) && loops < 500);
        if (!uniqueColors(gameColors))
            Log.w("Game Colors", "Aborted generation, too many attempts");
        return gameColors;
    }

    public static boolean uniqueColors(int gameColors[]) {
        HashSet<Integer> set = new HashSet<Integer>();
        for (int i : gameColors)
            set.add(i);
        return set.size() == Grid.numberOfColors;
    }

    //DO NOT TOUCH!
    private static int[] makeSingleTile(int[] gameColors, int tileIndex) {
        int n, e, s, w;
        if (tileIndex == 0) {
            n = 1;
            e = 2;
            s = 3;
            w = 4;
        } else if (tileIndex == 1) {
            n = 5;
            e = 6;
            s = 7;
            w = 2;
        } else if (tileIndex == 2) {
            n = 8;
            e = 9;
            s = 10;
            w = 6;
        } else if (tileIndex == 3) {
            n = 3;
            e = 11;
            s = 12;
            w = 13;
        } else if (tileIndex == 4) {
            n = 7;
            e = 14;
            s = 15;
            w = 11;
        } else if (tileIndex == 5) {
            n = 10;
            e = 16;
            s = 17;
            w = 14;
        } else if (tileIndex == 6) {
            n = 12;
            e = 18;
            s = 19;
            w = 20;
        } else if (tileIndex == 7) {
            n = 15;
            e = 21;
            s = 22;
            w = 18;
        } else if (tileIndex == 8) {
            n = 17;
            e = 23;
            s = 24;
            w = 21;
        } else
            return null;
        n--;
        e--;
        s--;
        w--;
        return new int[]{gameColors[n], gameColors[e], gameColors[s],
                gameColors[w]};
    }
}
