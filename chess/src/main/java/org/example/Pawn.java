package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Pawn extends ChessPiece {

    private boolean started;

    Pawn(String color) {
        super(color);
    }

    @Override
    protected boolean canMoveToPosition(ChessBoard chessBoard, int line, int column, int toLine, int toColumn) {
        BoardPoint end = new BoardPoint(toLine, toColumn);
        boolean exist = resolveAvailablePoints(chessBoard, line, column).stream().anyMatch(end::equals);
        if (exist) this.started = true;
        return exist;
    }

    private List<BoardPoint> resolveAvailablePoints(ChessBoard chessBoard, int line, int column) {
        List<BoardPoint> availablePoints = new ArrayList<>();
        if (started && chessBoard.board[line + 2][column] == null) availablePoints.add(new BoardPoint(line + 2, column));
        if (chessBoard.board[line + 1][column] == null) availablePoints.add(new BoardPoint(line + 1, column));
        Optional.ofNullable(chessBoard.board[line + 1][column + 1])
                .filter(it -> !it.getColor().equals(getColor()))
                .map(it -> new BoardPoint(line + 1, column + 1))
                .ifPresent(availablePoints::add);
        Optional.ofNullable(chessBoard.board[line + 1][column - 1])
                .filter(it -> !it.getColor().equals(getColor()))
                .map(it -> new BoardPoint(line + 1, column - 1))
                .ifPresent(availablePoints::add);
        return availablePoints;
    }
    

    @Override
    protected String getSymbol() {
        return "P";
    }
}
