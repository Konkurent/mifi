package org.example.piece;

import org.example.dto.BoardPoint;
import org.example.service.ChessBoard;
import org.example.dto.Strategy;

public abstract class ChessPiece implements Strategy {

    private final String color;
    private boolean started = false;

    ChessPiece(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public boolean isStarted() {
        return started;
    }

    public void start() {
        this.started = true;
    }

    public boolean canMoveToPosition(ChessBoard chessBoard, int line, int column, int toLine, int toColumn) {
        BoardPoint endPoint = new BoardPoint(toLine, toColumn);
        if (endPoint.equals(new BoardPoint(line, column))) return false;
        boolean res = resolveAvailablePoints(chessBoard, line, column).stream().anyMatch(endPoint::equals);
        if (res) started = true;
        return res;
    }

    public abstract String getSymbol();

}
