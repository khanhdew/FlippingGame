package com.khanhdew.flipping.model;


import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.util.Duration;


public class Piece extends Circle {
    public int row;
    public int col;
    private PieceState lastState, currentState;
    private final int x;
    private final int y;
    public static int PIECE_SIZE = Board.SQUARE_SIZE - 15;
    public static double SCALE = PIECE_SIZE / 100.0;
    public boolean highlight;

    private final RotateTransition rotateTransition = new RotateTransition();

    public Piece(int row, int col, PieceState currentState) {
        this.row = row;
        this.col = col;
        this.x = getX(col);
        this.y = getY(row);
        this.currentState = currentState;
        this.lastState = currentState;
    }

    public void initCir(PieceState pieceState) {
        if (pieceState == PieceState.EMPTY) {
            setCenterX(x);
            setCenterY(y);
            setRadius((double) PIECE_SIZE / 2);
            setFill(Color.TRANSPARENT);
            setStroke(Color.TRANSPARENT);
        } else if (pieceState == PieceState.BLACK) {
            LinearGradient gradient1 = new LinearGradient(0, 0, 1, 0, true, // proportional
                    javafx.scene.paint.CycleMethod.NO_CYCLE, // cycle colors
                    new Stop(0, Color.web("#3a3a3a")), new Stop(1, Color.web("#111")));
            setCenterX(x);
            setCenterY(y);
            setRadius((double) PIECE_SIZE / 2);
            setFill(gradient1);
            setStroke(Color.web("#000"));
            setStrokeWidth(5 * SCALE);
            setStrokeType(StrokeType.INSIDE);

        } else if (pieceState == PieceState.WHITE) {
            LinearGradient gradient2 = new LinearGradient(0, 0, 1, 0, true, // proportional
                    javafx.scene.paint.CycleMethod.NO_CYCLE, // cycle colors
                    new Stop(0, Color.web("#aaa")), new Stop(1, Color.web("#eee")));
            setCenterX(x);
            setCenterY(y);
            setRadius((double) PIECE_SIZE / 2);
            setFill(gradient2);
            setStroke(Color.web("#bababa"));
            setStrokeWidth(5 * SCALE);
            setStrokeType(StrokeType.INSIDE);
        }


    }

    public void updateState(PieceState pieceState) {
        this.lastState = this.currentState;
        this.currentState = pieceState;
        initCir(pieceState);
    }

    public int getX(int col) {
        return col * Board.SQUARE_SIZE + Board.SQUARE_SIZE / 2;
    }

    public int getY(int row) {
        return row * Board.SQUARE_SIZE + Board.SQUARE_SIZE / 2;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public PieceState getCurrentState() {
        return currentState;
    }

    public PieceState getLastState() {
        return lastState;
    }

    public void setState(PieceState pieceState) {
        this.lastState = currentState;
        this.currentState = pieceState;
    }

    public boolean isHighlight() {
        return highlight;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

    public void rotate() {
        rotateTransition.setNode(this);
        rotateTransition.setDuration(Duration.seconds(3));
        rotateTransition.setByAngle(360);
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.play();
    }

    @Override
    public String toString() {
        return "Piece{" +
                "col= " + col +
                ", row= " + row +
                ",lastState= " + lastState +
                ", currentState= " + currentState +
                '}';
    }

    public void setPieceSize(int newSize) {
        PIECE_SIZE = newSize - 15;
        SCALE = PIECE_SIZE / 100.0;
        initCir(currentState);
    }

}
