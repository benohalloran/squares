/*
 * Copyright (c) 2015. Ben O'Halloran/Pseudo Sudo Studios.
 * All rights reserved.
 * This work is licensed under a Creative Commons Attribution-NonCommercial-NoDerivs 3.0 Unported License.
 * Full license can be found here: http://creativecommons.org/licenses/by-nc-nd/3.0/deed.en_US
 */
package com.pseudosudostudios.jdd.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.pseudosudostudios.jdd.R;
import com.pseudosudostudios.jdd.activities.GameActivity;
import com.pseudosudostudios.jdd.utils.Difficulty;
import com.pseudosudostudios.jdd.utils.TileFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Random;

public class Grid extends View {
    //Constants
    public static int tileSize = -1;
    public static final double tilePercent = .25D;

    public static final int divisions = 12;
    private static final Paint divPaint = new Paint();

    //Tile things
    public Tile[][] tiles = new Tile[3][3];
    public Tile[][] sol1;
    private Tile[][] sol2;
    private Tile[][] sol3;
    private Tile[][] sol4;
    private Tile[][] usingSol;
    private double padding;
    private int vertical_offset, horizontal_offset;
    private int move_count = 0;
    private final int[] blackArray = {0, 0, 0, 0};

    /**
     * Instance Variables
     */
    private Difficulty level = Difficulty.EASY;
    private Tile firstSelect = null;
    public long startTime;
    public int moves = 0;
    private Tile centerTile;
    private boolean isNotFirstDraw = false;
    private long extraTime = 0; // TODO make relevant

    final GestureDetector gestureDetect = new GestureDetector(getContext(),
            new GridListener(), new Handler());

    public static int numberOfColors = TileFactory.colors.length;

    public boolean hasUsed = false;

    public Grid(Context context) {
        super(context);
        constructorCommon();
    }

    public Grid(Context context, AttributeSet s, int i) {
        super(context, s, i);
        constructorCommon();
    }

    public Grid(Context context, AttributeSet s) {
        super(context, s);
        constructorCommon();
    }

    /**
     * Common code between the constructors
     */
    public void constructorCommon() {
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetect.onTouchEvent(event);
                return true;
            }
        });
        for (int i = 0; i < tiles.length; i++) {
            for (int c = 0; c < tiles[i].length; c++) {
                Tile t = new Tile(getContext());
                t.setId(i * 10 + c);
                t.setVisibility(View.INVISIBLE);

                tiles[i][c] = t;
            }
        }
        divPaint.setColor(Color.TRANSPARENT);
        moves = 0;
    }

    /**
     * Defines the centers for the tiles
     */
    public void setCenters() {
        Point gridCenter = new Point(getWidth() / 2, getHeight() / 2);
        vertical_offset = Math.abs(vertical_offset);
        horizontal_offset = Math.abs(horizontal_offset);

        int space = Math.min(vertical_offset, horizontal_offset);

        int x, y;
        for (int row = 0; row < tiles.length; row++) {
            for (int col = 0; col < tiles[row].length; col++) {
                if (row == 0)
                    y = gridCenter.y - space;
                else if (row == 1)
                    y = gridCenter.y;
                else
                    y = gridCenter.y + space;

                if (col == 0)
                    x = gridCenter.x - space;
                else if (col == 1)
                    x = gridCenter.x;
                else
                    x = gridCenter.x + space;
                tiles[row][col].updateCenter(new Point(x, y));
            }
        }
    }

    private void testForColoredTiles() {
        while (move_count <= 500 && !isValidGame()) {
            loadNewGame();
            move_count++;
        }
        move_count = 0;
    }

    private boolean isValidGame() {
        for (Tile[] r : tiles)
            for (Tile t : r)
                if (Arrays.equals(t.toIntArray(), blackArray))
                    return false;
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isNotFirstDraw) {
            testForColoredTiles();
            isNotFirstDraw = true;
            setTileSize();
        }
        setCenters();
        for (Tile[] row : tiles) {
            for (Tile t : row)
                t.draw(canvas);
        }
    }

    /**
     * Prompts the user and calls loadGame()
     */
    public void makeNewGame() {
        hasUsed = false;
        if (tiles != null)
            for (Tile[] row : tiles)
                for (Tile t : row)
                    t.setVisibility(View.INVISIBLE);

        AlertDialog.Builder build = new AlertDialog.Builder(getContext());

        build.setTitle("How many colors?").setCancelable(false);

        View myView = ((LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE)).inflate(
                R.layout.number_colors_input, null);
        final EditText input = (EditText) myView
                .findViewById(R.id.colorInputET);
        final RadioButton easy = (RadioButton) myView.findViewById(R.id.easyRB);
        final RadioButton medium = (RadioButton) myView
                .findViewById(R.id.mediumRB);
        final RadioButton hard = (RadioButton) myView.findViewById(R.id.hardRB);
        build.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Grid.numberOfColors = Integer.parseInt(input.getText()
                            .toString());
                    if (numberOfColors < 2) {
                        numberOfColors = 2;
                        Toast.makeText(
                                getContext(),
                                getContext().getString(R.string.too_few_colors),
                                Toast.LENGTH_SHORT).show();
                    }
                    if (numberOfColors > TileFactory.colors.length)
                        numberOfColors = TileFactory.colors.length;
                } catch (NumberFormatException e) {
                    Grid.numberOfColors = 6;
                }
                if (easy.isChecked())
                    setDifficulty(Difficulty.EASY);
                if (medium.isChecked())
                    setDifficulty(Difficulty.MEDIUM);
                if (hard.isChecked())
                    setDifficulty(Difficulty.HARD);
                Tile.initPaints();
                loadNewGame();
                invalidate(); // let it draw!
            }
        }).setView(myView).show();
    }

    /**
     * Loads a game, and makes the Tiles
     */
    private void loadNewGame() {
        moves = 0;
        hasUsed = false;
        Tile.initPaints();
        Tile[] read = TileFactory.makeLiveGame(getContext());

        int i = 0;
        for (int row = 0; row < tiles.length; row++)
            for (int col = 0; col < tiles[row].length; col++) {
                tiles[row][col].setColors(read[i]);
                i++;
            }
        setCenterTile(tiles[1][1]);

        sol1 = sol2 = sol3 = sol4 = usingSol = null;
        sol1 = rotateCW(tiles);
        sol2 = rotateCW(sol1);
        sol3 = rotateCW(sol2);
        sol4 = rotateCW(sol3);

        setTileSize();
        setCenters();

        randomRotate();

        if (getDifficulty() != Difficulty.EASY)
            jumbleTiles();
        if (getDifficulty() == Difficulty.MEDIUM)
            updateGlows();

        startTime = System.currentTimeMillis();
        extraTime = 0;
        isNotFirstDraw = false;

        for (Tile[] r : tiles)
            for (Tile t : r)
                t.setVisibility(VISIBLE);
    }

    /**
     * Randomly swaps tiles in the array
     */
    public void jumbleTiles() {
        int size = tiles.length;
        int x, y, x1, y1;
        Random rand = new Random();
        Tile temp;
        for (int i = 0; i < 2500; i++) {
            x = rand.nextInt(size);
            y = rand.nextInt(size);
            temp = tiles[x][y];
            x1 = rand.nextInt(size);
            y1 = rand.nextInt(size);
            tiles[x][y] = tiles[x1][y1];
            tiles[x1][y1] = temp;
        }
    }

    /**
     * Returns a new array that is the parameter rotated about its center
     */
    public static Tile[][] rotateCW(Tile[][] mat) {
        Tile[][] ret = new Tile[mat.length][mat[0].length];
        // swap the corners
        ret[0][0] = new Tile(mat[2][0]);
        ret[0][2] = new Tile(mat[0][0]);
        ret[2][2] = new Tile(mat[0][2]);
        ret[2][0] = new Tile(mat[2][2]);
        // swap the interior points
        ret[0][1] = new Tile(mat[1][0]);
        ret[1][2] = new Tile(mat[0][1]);
        ret[2][1] = new Tile(mat[1][2]);
        ret[1][0] = new Tile(mat[2][1]);
        // set the center tile
        ret[1][1] = new Tile(mat[1][1]);
        // rotate everything clockwise once
        for (Tile[] row : ret)
            for (Tile t : row)
                t.rotateCW();
        return ret;
    }

    /**
     * @return the Point of the parameter in the gird
     */
    private Point getPointFirstPatch(Tile clicked) {
        for (int row = 0; row < tiles.length; row++)
            for (int col = 0; col < tiles[row].length; col++)
                if (tiles[row][col] == clicked)
                    return new Point(row, col);
        return null;
    }

    /**
     * @return the tile closest to the origin iff its within tolerance
     */
    private Tile findClosestByPoint(Point origin, double tolerance) {
        double distances[][] = new double[tiles.length][tiles[0].length];
        for (int row = 0; row < tiles.length; row++) {
            for (int col = 0; col < tiles[row].length; col++) {
                Point check = tiles[row][col].getLocation();
                double xDiffSquared = Math.pow(check.x - origin.x, 2);
                double yDiffSquared = Math.pow(check.y - origin.y, 2);
                distances[row][col] = Math.sqrt(xDiffSquared + yDiffSquared);
            }
        }
        int minRow = 0;
        int minCol = 0;
        for (int row = 0; row < tiles.length; row++) {
            for (int col = 0; col < tiles[row].length; col++) {
                if (distances[row][col] < distances[minRow][minCol]) {
                    minRow = row;
                    minCol = col;
                }
            }
        }
        if (distances[minRow][minCol] <= tolerance)
            return tiles[minRow][minCol];

        return null;
    }

    public void onUpdate() {
        if (isPuzzleSolved())
            winMethod();
        else if (level == Difficulty.MEDIUM)
            updateGlows();
        invalidate();
    }

    public void setTileArray(Tile[][] newTiles, Tile[][] solution, int moves, long time, int size) {
        tiles = newTiles;
        Grid.tileSize = size;
        System.out.println(Grid.tileSize);
        sol1 = sol2 = sol3 = sol4 = usingSol = null;
        sol1 = rotateCW(solution);
        sol2 = rotateCW(sol1);
        sol3 = rotateCW(sol2);
        sol4 = rotateCW(sol3);
        setCenterTile(sol1[1][1]);
        this.moves = moves;
        startTime = System.currentTimeMillis();
        Tile.initPaints();
        invalidate();
        setExtraTime(time);
        System.out.println("ET: " + extraTime);
    }

    /**
     * This method adds the medium level helper glows
     */
    private void updateGlows() {
        if (usingSol == null) {
            for (int r = 0; r < tiles.length; r++)
                for (int c = 0; c < tiles[r].length; c++) {
                    if (tiles[r][c].equals(sol1[r][c]))
                        usingSol = sol1;
                    else if (tiles[r][c].equals(sol2[r][c]))
                        usingSol = sol2;
                    else if (tiles[r][c].equals(sol3[r][c]))
                        usingSol = sol3;
                    else if (tiles[r][c].equals(sol4[r][c]))
                        usingSol = sol4;
                    if (usingSol != null) {
                        updateGlows();
                        return;
                    }
                }
            invalidate();
        } else
            handleSetPath();
    }

    private void handleSetPath() {
        for (int row = 0; row < tiles.length; row++) {
            for (int col = 0; col < tiles[row].length; col++) {
                if (tiles[row][col].equalsColors(usingSol[row][col])) {
                    tiles[row][col].addLocationGlow();
                    tiles[row][col].setMovable(false);
                    tiles[row][col].setIsRotatable(true);
                }
            }
        }
        invalidate();
    }

    public long getTimeSinceStart() {
        return (System.currentTimeMillis() - startTime) / 1000 + extraTime;
    }

    public long getTimeRaw() {
        return System.currentTimeMillis() - startTime + extraTime * 1000;
    }

    public void setExtraTime(long loadedTime) {
        extraTime = loadedTime;
    }

    /**
     * @return true iff the puzzle is solved
     */
    private boolean isPuzzleSolved() {
        for (int row = 0; row < tiles.length; row++) {
            if (tiles[row][0].getDir(Tile.Direction.EAST) != tiles[row][1]
                    .getDir(Tile.Direction.WEST))
                return false;
            else if (tiles[row][1].getDir(Tile.Direction.EAST) != tiles[row][2]
                    .getDir(Tile.Direction.WEST))
                return false;
            // col check
            if (tiles[0][row].getDir(Tile.Direction.SOUTH) != tiles[1][row]
                    .getDir(Tile.Direction.NORTH))
                return false;
            else if (tiles[1][row].getDir(Tile.Direction.SOUTH) != tiles[2][row]
                    .getDir(Tile.Direction.NORTH))
                return false;
        }
        return true;
    }

    /**
     * Sets the tile size, the offsets, and then calls the setTiles() method
     */
    public void setTileSize() {
        int height = getHeight();
        int width = getWidth();
        double toBeUsed;
        if (height == 0)
            height = 1000;
        if (width == 0)
            width = 5000;
        if (height >= width)
            toBeUsed = width;
        else
            toBeUsed = height;

        padding = toBeUsed / divisions;
        tileSize = (int) (padding * 3.5);

        // set the hor & vert offsets

        vertical_offset = tileSize + getHeight() / divisions;
        horizontal_offset = tileSize + getWidth() / divisions;

        while (height - (2 * vertical_offset) - tileSize - (Tile.glowExtra * 2) <= 7)
            vertical_offset--;
        while (width - 2 * horizontal_offset - tileSize - Tile.glowExtra * 2 <= 7)
            horizontal_offset--;
        if (toBeUsed - (3 * tileSize) - (6 * Tile.glowExtra) <= 10) {
            Tile.glowExtra--;
            setTileSize();
        }
        setCenters();
    }

    /**
     * Randomly rotates each tile in the grid
     */
    public void randomRotate() {
        Random rand = new Random();
        for (int row = 0; row < tiles.length; row++)
            for (int col = 0; col < tiles[row].length; col++) {
                int count = rand.nextInt(4); // 0, 1, 2, 3
                for (int rCount = 0; rCount < count; rCount++)
                    tiles[row][col].rotateCW();
            }
    }

    // TODO Remove for release
    public void hint() {
        Point oldCenterLocation = getPointFirstPatch(getCenterTile());
        swapTiles(tiles[1][1], tiles[oldCenterLocation.x][oldCenterLocation.y]);
        tiles[1][1].setMovable(false);
        if (level == Difficulty.MEDIUM)
            updateGlows();
        invalidate();
        hasUsed = true;
    }

    public boolean isReadyToPlay() {
        try {
            for (Tile[] row : tiles)
                for (Tile t : row)
                    for (int c : t.toIntArray())
                        if (c == Color.TRANSPARENT)
                            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public Difficulty getDifficulty() {
        return level;
    }

    public void setDifficulty(Difficulty d) {
        level = d;
    }

    public void setDifficulty(String string) {
        if (string.equals(Difficulty.HARD.toString()))
            setDifficulty(Difficulty.HARD);
        else if (string.equals(Difficulty.MEDIUM.toString()))
            setDifficulty(Difficulty.MEDIUM);
        else
            setDifficulty(Difficulty.EASY);
    }

    private void swapTiles(Tile firstSelect, Tile touched) {
        if (firstSelect == null || touched == null)
            return;
        if (!firstSelect.isMovable() || !touched.isMovable()) {
            return;
        }
        Point localPoint2 = getPointFirstPatch(touched);
        Point localPoint3 = getPointFirstPatch(firstSelect);
        if (localPoint2 == null || localPoint3 == null)
            return;
        tiles[localPoint3.x][localPoint3.y] = tiles[localPoint2.x][localPoint2.y];
        tiles[localPoint2.x][localPoint2.y] = firstSelect;

        // kill the glows for all tiles
        for (int r = 0; r < tiles.length; r++)
            for (int c = 0; c < tiles[r].length; c++)
                tiles[r][c].removeGlow();
    }

    public void winMethod() {
        new WinPretty().execute(-9);
        // ((GameActivity) getContext()).win(moves);
    }

    public Tile getCenterTile() {
        return centerTile;
    }

    public void setCenterTile(Tile centerTile) {
        this.centerTile = centerTile;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        JSONArray array = new JSONArray();
        JSONArray sol = new JSONArray();
        for (int row = 0; row < tiles.length; row++) {
            for (int col = 0; col < tiles[row].length; col++) {
                JSONObject currTileJSON = new JSONObject();
                int[] colors = tiles[row][col].toIntArray();
                // colors
                currTileJSON.put(GameActivity.northKey, colors[0]);
                currTileJSON.put(GameActivity.eastKey, colors[1]);
                currTileJSON.put(GameActivity.southKey, colors[2]);
                currTileJSON.put(GameActivity.westKey, colors[3]);
                // Location in the grid
                currTileJSON.put(GameActivity.pointX, tiles[row][col].center.x);
                currTileJSON.put(GameActivity.pointY, tiles[row][col].center.y);
                array.put(currTileJSON);

                JSONObject solution = new JSONObject();
                int[] solCol = tiles[row][col].toIntArray();
                // colors
                solution.put(GameActivity.northKey, solCol[0]);
                solution.put(GameActivity.eastKey, solCol[1]);
                solution.put(GameActivity.southKey, solCol[2]);
                solution.put(GameActivity.westKey, solCol[3]);
                // Location in the grid
                solution.put(GameActivity.pointX, sol1[row][col].center.x);
                solution.put(GameActivity.pointY, sol1[row][col].center.y);
                sol.put(currTileJSON);
            }
        }
        obj.put(GameActivity.moveKey, moves);
        obj.put(GameActivity.timeKey, getTimeSinceStart());
        obj.put(GameActivity.onSaveTime, getTimeSinceStart());
        obj.put(GameActivity.arrayKey, array);

        obj.put(GameActivity.numColorsKey, numberOfColors);
        obj.put(GameActivity.jsonTileSize, tileSize);
        obj.put(GameActivity.onSaveSolution, sol);
        obj.put(GameActivity.levelKey, getDifficulty().toString());
        obj.put(GameActivity.numColorsKey, numberOfColors);

        return obj;
    }

    /**
     * This class handles the input from the user including level logic
     */
    private class GridListener extends SimpleOnGestureListener {
        @Override
        /**Rotates the touched tile CCW*/
        public boolean onDoubleTap(MotionEvent event) {
            Tile touched = getTileFromEvent(event);
            if (touched != null) {
                touched.rotateCW();
                touched.rotateCW();
                touched.rotateCW();
                moves++;
                onUpdate();
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Tile touched = getTileFromEvent(e);
            if (level != Difficulty.EASY) {
                if (firstSelect == null) {
                    if (touched != null) {
                        firstSelect = touched;
                        firstSelect.addSelectedGlow();
                    }
                } else {
                    if (!firstSelect.equals(touched)) {
                        swapTiles(firstSelect, touched);
                        firstSelect = null;
                    } else {
                        firstSelect.removeGlow();
                        firstSelect = null;
                    }
                }
                moves++;
                onUpdate();
            } else {

                Toast.makeText(getContext(), R.string.no_long,
                        Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            // set firstSelect, if already defined swap the tiles
            Tile touched = getTileFromEvent(event);
            if (touched != null) {
                moves++;
                if (firstSelect != null && getDifficulty() != Difficulty.EASY) {
                    onLongPress(event);
                } else
                    touched.rotateCW();
            }
            onUpdate();
            return true;
        }

        private Tile getTileFromEvent(MotionEvent event) {
            Point clickPoint = new Point((int) event.getX(), (int) event.getY());
            return findClosestByPoint(clickPoint, tileSize / Math.sqrt(2.0D));
        }
    }

    /**
     * Starts off slow and then ramps up to almost no delay between rotations of
     * the entire grid of tiles.
     */
    private class WinPretty extends AsyncTask<Integer, Integer, String> {

        @Override
        protected String doInBackground(Integer... params) {
            for (int i = 0; i < 20; i++) {

                try {
                    Thread.sleep(100);
                    publishProgress(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            for (Tile[] r : tiles)
                for (Tile t : r)
                    t.rotateCW();
            invalidate();
        }

        @Override
        protected void onPostExecute(String result) {
            ((GameActivity) getContext()).win(moves);
        }
    }
}