package com.khanhdew.flipping.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


public class Board {
    public static int MAX_COL ;
    public static int MAX_ROW ;
    public static double SCALE;
    public static int SQUARE_SIZE;
    public static int HALF_SQUARE_SIZE;

    public Board(int row, int col){
        MAX_COL = col;
        MAX_ROW = row;
        SCALE = (Math.max(MAX_COL,MAX_ROW)*1.0f/8);
        SQUARE_SIZE = (int) (100.0f/SCALE);
    }

    public static void draw(GraphicsContext gc){
        int x = 0;
        for (int row = 0; row < MAX_ROW; row++) {
            for (int col = 0; col < MAX_COL; col++) {
                if ((row + col) % 2 == 0) {
                    gc.setFill(Color.rgb(1, 140, 1));
                } else {
                    gc.setFill(Color.rgb(6, 93, 6));
                }
//                if(row==0||row==MAX_ROW-1||col==0||col==MAX_COL-1) gc.setFill(Color.web("#663F25FF"));
                gc.fillRect(    col * SQUARE_SIZE, row * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
//                gc.setFill(Color.BLACK);
//                gc.fillText(row+" "+col, col * SQUARE_SIZE + 10, row * SQUARE_SIZE + 10);
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

    public static int getSquareSize(){
        return SQUARE_SIZE;
    }

    public static double getSCALE(){
        return SCALE;
    }

    public static int getMaxCol() {
        return MAX_COL;
    }

    public static int getMaxRow() {
        return MAX_ROW;
    }
}
