package com.khanhdew.testjfx.utils;

import com.khanhdew.testjfx.model.Board;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;

public class Mouse implements EventHandler<MouseEvent> {
    public int x, y;
    public boolean mouseClicked = false;

    @Override
    public void handle(MouseEvent mouseEvent) {
        this.x = (int) mouseEvent.getX();
        this.y = (int) mouseEvent.getY();
        mouseClicked = true;
        System.out.println("X: " + x / Board.getSquareSize() + ", Y: " + y / Board.getSquareSize());
    }

    public int getCol() {
        return (x) / Board.getSquareSize();
    }

    public int getRow() {
        return (y) / Board.getSquareSize();
    }

    public void clear() {
        x = 0;
        y = 0;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}
