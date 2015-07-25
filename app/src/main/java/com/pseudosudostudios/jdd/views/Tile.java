/*
 * Copyright (c) 2015. Ben O'Halloran/Pseudo Sudo Studios.
 * All rights reserved.
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * Full license can be found here: http://creativecommons.org/licenses/by-nc-nd/3.0/deed.en_US
 */
package com.pseudosudostudios.jdd.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.util.TypedValue;
import android.view.View;

import com.pseudosudostudios.jdd.utils.TileFactory;

public class Tile extends View {
    /**
     * Static variables
     */
    public static int glowExtra = 25;
    public static final int locationGlow = Color.rgb(249, 68, 5); // red-ish
    public static final int solvedGlow = Color.rgb(243, 243, 21); // yellow-yellowish
    public static final int selectedGlow = Color.rgb(77, 77, 255); // blue-yellowish
    private static Paint[] byColors = null;
    private static final Direction[] dirs = {Direction.NORTH, Direction.EAST,
            Direction.SOUTH, Direction.WEST};

    /**
     * Instance Variables
     */
    private boolean isMovable = true;
    private boolean isRotatable = true;
    public Paint glowPaint = new Paint();
    private int north, east, south, west;
    private int lastNorth, lastEast, lastSouth, lastWest;
    public Point center;

    public Tile(Tile tile) {
        this(tile.getContext(), tile.center, tile.north, tile.east, tile.south,
                tile.west);
    }

    public Tile(Context context) {
        this(context, new Point(90, 100));
    }


    public Tile(Context context, int[] colors) {
        this(context, colors[0], colors[1], colors[2], colors[3]);
    }

    public Tile(Context context, Point p) {
        this(context, p, Color.RED,
                Color.GREEN, Color.GRAY,
                Color.MAGENTA);
    }

    public Tile(Context context, int north, int east, int south, int west) {
        this(context, new Point(0, 0), north, east, south, west);
    }

    public Tile(Context context, Point p, int north, int east, int south,
                int west) {
        super(context);
        if (byColors == null)
            Tile.initPaints();
        this.center = p;
        this.north = north;
        this.east = east;
        this.west = west;
        this.south = south;

        glowPaint.setStyle(Style.FILL);
        glowPaint.setColor(Color.BLACK);

        setClickable(true);
    }


    public void setColors(int color) {
        north = color;
        south = color;
        east = color;
        west = color;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (getVisibility() != VISIBLE) {
            canvas.drawColor(0, PorterDuff.Mode.CLEAR);
            return;
        }
        // draw the glow
        if (glowPaint.getColor() != Color.BLACK)
            for (int i = 0; i < dirs.length; i++)
                canvas.drawPath(getQuadrant(dirs[i], glowExtra), glowPaint);
        if (glowPaint.getColor() != Color.BLACK)
            for (Direction dir : dirs)
                canvas.drawPath(getQuadrant(dir, glowExtra / 3),
                        byColors[byColors.length - 1]);

        //draw the actual square
        canvas.drawPath(getQuadrant(Direction.NORTH), getPaintByColor(north));
        canvas.drawPath(getQuadrant(Direction.EAST), getPaintByColor(east));
        canvas.drawPath(getQuadrant(Direction.SOUTH), getPaintByColor(south));
        canvas.drawPath(getQuadrant(Direction.WEST), getPaintByColor(west));
    }

    public static Paint getPaintByColor(int color) {
        for (int index = 0; index < TileFactory.colors.length && index < byColors.length; index++)
            if (TileFactory.colors[index] == color)
                return byColors[index];
        if (byColors[byColors.length - 1].getColor() == color)
            return byColors[byColors.length - 1];
        // Can't find the color, make a new paint object
        Paint p = new Paint(color);
        p.setStyle(Style.FILL);
        return p;
    }

    public void brightenColors() {
        north = brightenColor(north);
        east = brightenColor(east);
        south = brightenColor(south);
        west = brightenColor(west);
    }

    public void darkenColors() {
        north = darkenColor(north);
        east = darkenColor(east);
        south = darkenColor(south);
        west = darkenColor(west);
    }

    private int brightenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 1.1f; // value component
        if (hsv[2] > 1)
            hsv[2] = 1f;
        return Color.HSVToColor(hsv);
    }

    private int darkenColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        if (hsv[2] == 0)
            hsv[2] = .01f;
        hsv[2] *= 0.8f; // value component
        return Color.HSVToColor(hsv);
    }

    public static void initPaints() {
        byColors = new Paint[Grid.numberOfColors + 1];
        // test to see if its already been called
        for (int i = 0; i < byColors.length - 1; i++) {
            byColors[i] = new Paint();
            byColors[i].setColor(TileFactory.colors[i]);
            byColors[i].setStyle(Style.FILL);
            byColors[i].setFlags(Paint.ANTI_ALIAS_FLAG);
        }
        byColors[byColors.length - 1] = new Paint();
        byColors[byColors.length - 1].setColor(Color.BLACK);
        byColors[byColors.length - 1].setStyle(Style.FILL);
        byColors[byColors.length - 1].setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    private Path getQuadrant(Direction direction) {
        return getQuadrant(direction, 0);

    }

    private Path getQuadrant(Direction direction, int addition) {
        Point p2 = null, p3 = null;

        int dOff = Grid.tileSize / 2 + addition;

        if (direction == Direction.NORTH) {
            p2 = new Point(center.x + dOff, center.y - dOff);
            p3 = new Point(center.x - dOff, center.y - dOff);
        } else if (direction == Direction.EAST) {
            p2 = new Point(center.x + dOff, center.y + dOff);
            p3 = new Point(center.x + dOff, center.y - dOff);
        } else if (direction == Direction.SOUTH) {
            p2 = new Point(center.x + dOff, center.y + dOff);
            p3 = new Point(center.x - dOff, center.y + dOff);
        } else if (direction == Direction.WEST) {
            p2 = new Point(center.x - dOff, center.y + dOff);
            p3 = new Point(center.x - dOff, center.y - dOff);
        }
        Path path = new Path();
        path.moveTo(center.x, center.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);
        return path;
    }

    public void updateCenter(Point newCenter) {
        center = newCenter;
    }

    public Point getLocation() {
        return center;
    }

    /**
     * @param in the direction to be found
     * @return Color in represented as Color.rgb
     */
    public int getDir(Direction in) {
        if (in == Direction.EAST)
            return east;
        else if (in == Direction.NORTH)
            return north;
        else if (in == Direction.SOUTH)
            return south;
        else
            return west;
    }

    public void setMovable(boolean movable) {
        this.isMovable = movable;
    }

    public boolean isMovable() {
        return isMovable;
    }

    public void rotateCW() {
        if (isRotatable) {

            lastNorth = north;
            lastEast = east;
            lastWest = west;
            lastSouth = south;

            north = lastWest;
            east = lastNorth;
            south = lastEast;
            west = lastSouth;
        }
    }

    public boolean equals(Tile param) {
        if (param == null)
            return false;
        if (this.north != param.north)
            return false;
        else if (this.east != param.east)
            return false;
        else if (this.south != param.south)
            return false;
        else if (this.west != param.west)
            return false;
        return true;
    }

    /**
     * @param solution solution tiles to check colors against
     * @return true if solution would equal the color, ignoring the rotation;
     * that is, they have the same relative NESW
     */
    public boolean equalsColors(Tile solution) {
        if (solution == null)
            return false;
        Tile newTile = new Tile(solution);
        for (int i = 0; i <= 5; i++) {
            if (equals(newTile)) {
                return true;
            } else
                newTile.rotateCW();
        }
        return false;

    }

    public void setIsMovable(boolean movable) {
        isMovable = movable;
    }

    public boolean isRotatable() {
        return isRotatable;
    }

    public void setIsRotatable(boolean isRotatable) {
        this.isRotatable = isRotatable;
    }

    public void addLocationGlow() {
        if (glowPaint.getColor() != solvedGlow) {
            glowPaint.setColor(locationGlow);
            invalidate();
        }
    }

    public void addSolvedGlow() {
        glowPaint.setColor(solvedGlow);
        invalidate();
    }

    public void addSelectedGlow() {
        glowPaint.setColor(selectedGlow);
        invalidate();
    }

    public void removeGlow() {
        glowPaint.setColor(Color.BLACK);
        invalidate();
    }

    /**
     * @return an array north, east, south west in that order
     */
    public int[] toIntArray() {
        return new int[]{north, east, south, west};
    }

    @Override
    public String toString() {
        return "Center: " + center + "\nNorth" + north + "\nEast" + east
                + "\nSouth" + south + "\nWest" + west;
    }

    public enum Direction {
        NORTH, SOUTH, EAST, WEST
    }

    public void setColors(Tile tile) {
        this.north = tile.north;
        this.east = tile.east;
        this.west = tile.west;
        this.south = tile.south;
        invalidate();
    }


}
