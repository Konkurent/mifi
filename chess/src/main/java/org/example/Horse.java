package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Horse extends ChessPiece {

    Horse(String color) {
        super(color);
    }

    @Override
    protected boolean canMoveToPosition(ChessBoard chessBoard, int line, int column, int toLine, int toColumn) {
        BoardPoint endPoint = new BoardPoint(toLine, toColumn);
        if (endPoint.equals(new BoardPoint(line, column))) return false;
        return resolveAvailablePoints(line, column).stream().anyMatch(endPoint::equals)
                && Optional.ofNullable(chessBoard.board[toLine][toColumn])
                .filter(piece -> piece.getColor().equals(chessBoard.nowPlayer))
                .isEmpty();
    }

    private List<BoardPoint> resolveAvailablePoints(int line, int column) {
        List<BoardPoint> points = new ArrayList<>();
        points.add(new BoardPoint(line + 2, column + 1));
        points.add(new BoardPoint(line + 2, column - 1));
        points.add(new BoardPoint(line - 2, column + 1));
        points.add(new BoardPoint(line - 2, column - 1));
        points.add(new BoardPoint(line + 1, column + 2));
        points.add(new BoardPoint(line - 1, column + 2));
        points.add(new BoardPoint(line + 1, column - 2));
        points.add(new BoardPoint(line - 1, column - 2));
        return points.stream().filter(BoardPointValidator::isTheBoard).toList();
    }



    @Override
    protected String getSymbol() {
        return "H";
    }


}
