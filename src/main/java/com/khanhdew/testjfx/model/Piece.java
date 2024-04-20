package com.khanhdew.testjfx.model;


import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
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
    public static final int PIECE_SIZE = Board.SQUARE_SIZE - 15;
    public static final double SCALE = PIECE_SIZE / 100.0;
    public boolean highlight;

    private final RotateTransition rotateTransition = new RotateTransition();
    ;

    public Piece(int row, int col, PieceState currentState) {
        this.row = row;
        this.col = col;
        this.x = getX(col);
        this.y = getY(row);
        this.currentState = currentState;
        this.lastState = currentState;
        initCir(currentState);

    }

    public void initCir(PieceState pieceState) {
        if (highlight) {
            setCenterX(x);
            setCenterY(y);
            setRadius((double) PIECE_SIZE / 2);
            setFill(Color.TRANSPARENT);
            setStroke(Color.web("#7DDA58"));
            setStrokeWidth(5*SCALE);
            getStrokeDashArray().addAll(25 * SCALE, 19 * SCALE);
            rotate();
        } else if (pieceState == PieceState.BLACK) {
            LinearGradient gradient1 = new LinearGradient(0, 0, 1, 0, true, // proportional
                    javafx.scene.paint.CycleMethod.NO_CYCLE, // cycle colors
                    new Stop(0, Color.web("#3a3a3a")), new Stop(1, Color.web("#111")));
            setCenterX(x);
            setCenterY(y);
            setRadius((double) PIECE_SIZE / 2);
            setFill(gradient1);
            setStroke(Color.web("#000"));
            setStrokeWidth(5*SCALE);
            setStrokeType(StrokeType.INSIDE);
            getStrokeDashArray().clear();

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
            getStrokeDashArray().clear();
        }
//        else {
//            setCenterX(x);
//            setCenterY(y);
//            setRadius((double) PIECE_SIZE / 2);
//            setStyle("-fx-fill: red;" +
//                    "-fx-stroke-dash-array: 5 5;");
//        }

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

    public void shrinkDisappear() {
        ScaleTransition st = new ScaleTransition(Duration.seconds(1), this);
        st.setToX(0);
        st.setToY(0);

        FadeTransition ft = new FadeTransition(Duration.seconds(1), this);
        ft.setToValue(0);

        st.play();
        ft.play();
    }

    public void growAppear() {
        ScaleTransition st = new ScaleTransition(Duration.seconds(1), this);
        st.setToX(1);
        st.setToY(1);

        FadeTransition ft = new FadeTransition(Duration.seconds(1), this);
        ft.setToValue(1);

        st.play();
        ft.play();
    }

    public void stopRotate() {
        rotateTransition.stop();
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
}
