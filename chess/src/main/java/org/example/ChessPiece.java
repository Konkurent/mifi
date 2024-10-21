package org.example;

public abstract class ChessPiece {

    private final String color;

    ChessPiece(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    protected abstract boolean canMoveToPosition(ChessBoard chessBoard, int line, int column, int toLine, int toColumn);
    protected abstract String getSymbol();

}
