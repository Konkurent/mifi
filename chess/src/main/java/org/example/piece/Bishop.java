package org.example.piece;

import org.example.dto.BoardPoint;
import org.example.service.ChessBoard;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends ChessPiece{

    private BoardSegment segment;

    public Bishop(String color) {
        super(color);
    }

    @Override
    public boolean canMoveToPosition(ChessBoard chessBoard, int line, int column, int toLine, int toColumn) {
        BoardPoint endPoint = new BoardPoint(toLine, toColumn);
        segment = resolveSegment(line, column, toLine, toColumn);
        boolean res = resolveAvailablePoints(chessBoard, line, column).stream().anyMatch(endPoint::equals);
        if (res) start();
        return res;
    }

    @Override
    public String getSymbol() {
        return "B";
    }

    @Override
    public List<BoardPoint> resolveAvailablePoints(ChessBoard chessBoard, int line, int column) {
        List<BoardPoint> availablePoints = new ArrayList<>();
        for (int i = 1; i < 8; i++) {
            BoardPoint point = switch (segment) {
                case TOP_LEFT -> new BoardPoint(line + i, column - i);
                case TOP_RIGHT -> new BoardPoint(line + i, column + i);
                case BOTTOM_LEFT -> new BoardPoint(line - i, column - i);
                case BOTTOM_RIGHT -> new BoardPoint(line + 1, column + 1);
            };
            if (chessBoard.board[point.line()][point.column()] != null || chessBoard.checkPos(point.line()) || chessBoard.checkPos(point.column())) {
                break;
            } else {
                availablePoints.add(point);
            }
        }
        return availablePoints;
    }

    private enum BoardSegment {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    private BoardSegment resolveSegment(int line, int column, int toLine, int toColumn) {
        if (line < toLine && column < toColumn) {
            return BoardSegment.TOP_RIGHT;
        } else if (line > toLine && column > toColumn) {
            return BoardSegment.BOTTOM_LEFT;
        } else if (line < toLine && column > toColumn) {
            return BoardSegment.TOP_LEFT;
        } else {
            return BoardSegment.BOTTOM_RIGHT;
        }
    }
}
