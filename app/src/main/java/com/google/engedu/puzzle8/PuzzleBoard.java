package com.google.engedu.puzzle8;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;

import java.util.ArrayList;


public class PuzzleBoard {

    private static final int NUM_TILES = 3;
    private static final int[][] NEIGHBOUR_COORDS = {
            { -1, 0 },
            { 1, 0 },
            { 0, -1 },
            { 0, 1 }
    };
    public ArrayList<PuzzleTile> tiles;
    public int steps;
    PuzzleBoard previousBoard;

    PuzzleBoard(Bitmap bitmap, int parentWidth,int parentHeight) {
//        bitmap = Bitmap.createScaledBitmap(bitmap,parentWidth,parentWidth,false);
        int w=bitmap.getWidth();
        int h=bitmap.getHeight();

        if(h>w)
            bitmap = Bitmap.createBitmap(bitmap,0,(bitmap.getHeight()-bitmap.getWidth())/2-1,w,w);
        else bitmap = Bitmap.createBitmap(bitmap,-1*(bitmap.getHeight()-bitmap.getWidth())/2-1,0,h,h);

        bitmap = Bitmap.createScaledBitmap(bitmap,parentWidth,parentWidth,true);
        int widthOfTile = parentWidth/NUM_TILES;
        tiles = new ArrayList<PuzzleTile>();
        for(int i=0;i<NUM_TILES;i++){
            for(int j=0;j<NUM_TILES;j++){
                Bitmap tile = Bitmap.createBitmap(bitmap,j*widthOfTile,i*widthOfTile,
                        widthOfTile,widthOfTile);
                //j*widthOfTile is passed as width to maintain order! dikkat thi idhar!
                tiles.add(new PuzzleTile(tile,NUM_TILES*i+j));
            }
        }
        tiles.remove(NUM_TILES*NUM_TILES-1);
        tiles.add(null);

    }

    PuzzleBoard(PuzzleBoard otherBoard) {
        tiles = (ArrayList<PuzzleTile>) otherBoard.tiles.clone();
        this.steps = otherBoard.steps + 1;
        this.previousBoard = otherBoard;
    }

    public void reset() {
        // Nothing for now but you may have things to reset once you implement the solver.
    }

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        return tiles.equals(((PuzzleBoard) o).tiles);
    }

    public void draw(Canvas canvas) {
        if (tiles == null) {
            return;
        }
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                tile.draw(canvas, i % NUM_TILES, i / NUM_TILES);
            }
        }
    }

    public boolean click(float x, float y) {
        for (int i = 0; i < NUM_TILES * NUM_TILES; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile != null) {
                if (tile.isClicked(x, y, i % NUM_TILES, i / NUM_TILES)) {
                    return tryMoving(i % NUM_TILES, i / NUM_TILES);
                }
            }
        }
        return false;
    }

    private boolean tryMoving(int tileX, int tileY) {
        for (int[] delta : NEIGHBOUR_COORDS) {
            int nullX = tileX + delta[0];
            int nullY = tileY + delta[1];
            if (nullX >= 0 && nullX < NUM_TILES && nullY >= 0 && nullY < NUM_TILES &&
                    tiles.get(XYtoIndex(nullX, nullY)) == null) {
                swapTiles(XYtoIndex(nullX, nullY), XYtoIndex(tileX, tileY));
                return true;
            }

        }
        return false;
    }

    public boolean resolved() {
        for (int i = 0; i < NUM_TILES * NUM_TILES - 1; i++) {
            PuzzleTile tile = tiles.get(i);
            if (tile == null || tile.getNumber() != i)
                return false;
        }
        return true;
    }

    private int XYtoIndex(int x, int y) {
        return x + y * NUM_TILES;
    }

    protected void swapTiles(int i, int j) {
        PuzzleTile temp = tiles.get(i);
        tiles.set(i, tiles.get(j));
        tiles.set(j, temp);
    }

    public ArrayList<PuzzleBoard> neighbours() {
        ArrayList<PuzzleBoard> neighbours = new ArrayList<>();

        for(int k=0;k<tiles.size();k++){
            if(tiles.get(k)==null){
                for(int[] coord:NEIGHBOUR_COORDS){
                    if(k / NUM_TILES + coord[1] < NUM_TILES && k / NUM_TILES + coord[1] >= 0 &&
                            k % NUM_TILES + coord[0] < NUM_TILES && k % NUM_TILES + coord[0] >= 0){
                        PuzzleBoard copiedBoard = new PuzzleBoard(this);
                        copiedBoard.swapTiles(k,k+coord[0]+NUM_TILES*coord[1]);
                        neighbours.add(copiedBoard);
                    }
                }
            }
        }

        return neighbours;
    }

    public int priority() {
        int manhattanDistance = 0;
        for(int i=0;i<tiles.size();i++){
            if(tiles.get(i)!=null) {
                int rowChange = Math.abs(i % NUM_TILES - tiles.get(i).getNumber() % NUM_TILES);
                int columnChange = Math.abs(i / NUM_TILES - tiles.get(i).getNumber());
                manhattanDistance += rowChange + columnChange;
            }
            else{
                int rowChange = Math.abs(i % NUM_TILES - (NUM_TILES*NUM_TILES-1) % NUM_TILES);
                int columnChange = Math.abs(i / NUM_TILES - (NUM_TILES*NUM_TILES-1)/NUM_TILES);
                manhattanDistance += rowChange + columnChange;
            }
        }
        return manhattanDistance + steps;
    }


}
