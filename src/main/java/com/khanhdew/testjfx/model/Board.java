package com.khanhdew.testjfx.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


public class Board {
    public static int MAX_COL = 10;
    public static int MAX_ROW = 10;
    public static final double SCALE = (Math.max(MAX_COL,MAX_ROW)*1.0f/8);
    public static final int SQUARE_SIZE = (int) (100.0f/SCALE);
    public static final int HALF_SQUARE_SIZE = SQUARE_SIZE/2;
    public static void draw(GraphicsContext gc){
        int x = 0;
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                if ((row + col) % 2 == 0) {
                    gc.setFill(Color.rgb(1, 140, 1));
                } else {
                    gc.setFill(Color.rgb(6, 93, 6));
                }
                gc.fillRect(col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
//                gc.setFill(Color.BLACK);
//                gc.fillText(x+"", col * SQUARE_SIZE + 10, row * SQUARE_SIZE + 10);
                x++;
            }
        }
    }
    public static void setMAX_COL(int col){
        MAX_COL = col;
    }
    public static void setMAX_ROW(int row){
        MAX_ROW = row;
    }
}
