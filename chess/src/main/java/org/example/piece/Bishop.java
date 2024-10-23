package org.example.piece;

import org.example.dto.BoardPoint;
import org.example.service.ChessBoard;
import org.example.util.CollectionUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
        Deque<BoardPoint> top = new ArrayDeque<>();
        Deque<BoardPoint> left = new ArrayDeque<>();
        Deque<BoardPoint> right = new ArrayDeque<>();
        Deque<BoardPoint> down = new ArrayDeque<>();
        for (int i = 1; i < 8; i++) {
            int count = i - 1;
            if (top.size() == count && chessBoard.checkPos(line + i) && chessBoard.checkPos(column + i)
                    && chessBoard.board[line + i][column + i] == null) {
                top.add(new BoardPoint(line + i, column + i));
            }
            if (left.size() == count && chessBoard.checkPos(column - i) && chessBoard.checkPos(line + i)
                    && chessBoard.board[line + i][column - i] == null) {
                left.add(new BoardPoint(line + i, column - i));
            }
            if (right.size() == count && chessBoard.checkPos(column + i) && chessBoard.checkPos(line - i)
                    && chessBoard.board[line - i][column + i] == null) {
                right.add(new BoardPoint(line - i, column + i));
            }
            if (down.size() == count && chessBoard.checkPos(line - i) && chessBoard.checkPos(column - i)
                    && chessBoard.board[line - i][column] == null) {
                down.add(new BoardPoint(line - i, column - i));
            }
        }
        if (top.peekLast() != null && chessBoard.checkPos(top.getLast().line() + 1) && chessBoard.checkPos(top.getLast().column() + 1))
            top.add(new BoardPoint(top.getLast().line() + 1, top.getLast().column() + 1));
        if (left.peekLast() != null && chessBoard.checkPos(left.getLast().line() + 1) && chessBoard.checkPos(left.getLast().column() - 1))
            left.add(new BoardPoint(left.getLast().line() + 1, left.getLast().column() - 1));
        if (right.peekLast() != null && chessBoard.checkPos(right.getLast().line() - 1) && chessBoard.checkPos(right.getLast().column() + 1))
            right.add(new BoardPoint(right.getLast().line() - 1, right.getLast().column() + 1));
        if (down.peekLast() != null && chessBoard.checkPos(down.getLast().line() - 1) && chessBoard.checkPos(down.getLast().column() - 1))
            down.add(new BoardPoint(down.getLast().line() - 1, down.getLast().column() - 1));
        return CollectionUtils.ModifiableList.of(top, left, right, down);
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
