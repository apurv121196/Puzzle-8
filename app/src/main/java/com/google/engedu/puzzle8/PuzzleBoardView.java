package com.google.engedu.puzzle8;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

public class PuzzleBoardView extends View {
    public static final int NUM_SHUFFLE_STEPS = 40;
    private Activity activity;
    private PuzzleBoard puzzleBoard;
    private ArrayList<PuzzleBoard> animation;
    private Random random = new Random();

    public PuzzleBoardView(Context context) {
        super(context);
        activity = (Activity) context;
        animation = null;
    }

    public void initialize(Bitmap imageBitmap) {
        int width = getWidth();
        int height = getHeight();
        puzzleBoard = new PuzzleBoard(imageBitmap, width, height);
        puzzleBoard.draw(new Canvas());
//        this.shuffle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (puzzleBoard != null) {
            if (animation != null && animation.size() > 0) {
                puzzleBoard = animation.remove(0);
                puzzleBoard.draw(canvas);
                if (animation.size() == 0) {
                    animation = null;
                    puzzleBoard.reset();
                    Toast toast = Toast.makeText(activity, "Solved! ", Toast.LENGTH_LONG);
                    toast.show();
                } else {
                    this.postInvalidateDelayed(500);
                }
            } else {
                puzzleBoard.draw(canvas);
            }
        }
    }

    public void shuffle() {
        if (animation == null && puzzleBoard != null) {
            for(int i=0;i<NUM_SHUFFLE_STEPS;i++){
                ArrayList<PuzzleBoard> neighbour = puzzleBoard.neighbours();
                puzzleBoard = neighbour.get(random.nextInt(neighbour.size()));
            }
            puzzleBoard.reset();
            invalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (animation == null && puzzleBoard != null) {
            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (puzzleBoard.click(event.getX(), event.getY())) {
                        invalidate();
                        if (puzzleBoard.resolved()) {
                            Toast toast = Toast.makeText(activity, "Congratulations!", Toast.LENGTH_LONG);
                            toast.show();
                        }
                        return true;
                    }
            }
        }
        return super.onTouchEvent(event);
    }

    public void solve() {
        PriorityQueue<PuzzleBoard> priorityQueue = new PriorityQueue<>(1000, new Comparator<PuzzleBoard>() {
            @Override
            public int compare(PuzzleBoard lhs, PuzzleBoard rhs) {
                return lhs.priority()-rhs.priority();
            }
        });
        this.puzzleBoard.steps=0;
        this.puzzleBoard.previousBoard = null;

        priorityQueue.add(this.puzzleBoard);
        while(!priorityQueue.isEmpty()){
            PuzzleBoard removedBoard = priorityQueue.remove();
            if(!removedBoard.resolved()){
//                ArrayList<PuzzleBoard> neighbours = removedBoard.neighbours();
//                for(PuzzleBoard pBoard:neighbours){
//                    if(pBoard.previousBoard!=removedBoard)priorityQueue.add(pBoard);
//                }
                priorityQueue.addAll(removedBoard.neighbours());
            }
            else{
                priorityQueue.clear();
                ArrayList<PuzzleBoard> solutionBoards = new ArrayList<>();
                while(removedBoard.previousBoard!=null){
                    solutionBoards.add(removedBoard);
                    removedBoard = removedBoard.previousBoard;
                }
                Collections.reverse(solutionBoards);
                animation = solutionBoards;
                invalidate();
            }
        }

    }
}
