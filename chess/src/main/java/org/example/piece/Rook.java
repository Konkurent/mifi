package org.example.piece;

import org.example.dto.BoardPoint;
import org.example.service.ChessBoard;
import org.example.util.CollectionUtils;
import org.example.dto.Strategy;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class Rook extends ChessPiece implements Strategy {

    public Rook(String color) {
        super(color);
    }

    @Override
    public List<BoardPoint> resolveAvailablePoints(ChessBoard chessBoard, int line, int column) {
        Deque<BoardPoint> top = new ArrayDeque<>();
        Deque<BoardPoint> left = new ArrayDeque<>();
        Deque<BoardPoint> right = new ArrayDeque<>();
        Deque<BoardPoint> down = new ArrayDeque<>();
        for (int i = 1; i < 8; i++) {
            int count = i - 1;
            if (top.size() == count && chessBoard.checkPos(line + i)
                    && chessBoard.board[line + i][column] == null) {
                top.add(new BoardPoint(line + i, column));
            }
            if (left.size() == count && chessBoard.checkPos(column - i)
                    && chessBoard.board[line][column - i] == null) {
                left.add(new BoardPoint(line, column - i));
            }
            if (right.size() == count && chessBoard.checkPos(column + i)
                    && chessBoard.board[line][column + i] == null) {
                right.add(new BoardPoint(line, column + i));
            }
            if (down.size() == count && chessBoard.checkPos(line - i)
                    && chessBoard.board[line - i][column] == null) {
                down.add(new BoardPoint(line - i, column));
            }
        }
        if (top.peekLast() != null && chessBoard.checkPos(top.getLast().line() + 1))
            top.add(new BoardPoint(top.getLast().line() + 1, top.getLast().column()));
        if (left.peekLast() != null && chessBoard.checkPos(left.getLast().column() - 1))
            left.add(new BoardPoint(left.getLast().line(), left.getLast().column() - 1));
        if (right.peekLast() != null && chessBoard.checkPos(right.getLast().column() + 1))
            right.add(new BoardPoint(right.getLast().line(), right.getLast().column() + 1));
        if (down.peekLast() != null && chessBoard.checkPos(down.getLast().line() - 1))
            down.add(new BoardPoint(down.getLast().line() - 1, down.getLast().column()));
        return CollectionUtils.ModifiableList.of(top, left, right, down);
    }

    @Override
    public String getSymbol() {
        return "R";
    }
}
