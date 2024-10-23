package org.example.piece;

import org.example.dto.BoardPoint;
import org.example.service.ChessBoard;
import org.example.util.CollectionUtils;
import org.example.dto.Strategy;

import java.util.ArrayList;
import java.util.List;

public class Rook extends ChessPiece implements Strategy {

    public Rook(String color) {
        super(color);
    }

    @Override
    public List<BoardPoint> resolveAvailablePoints(ChessBoard chessBoard, int line, int column) {
        List<BoardPoint> top = new ArrayList<>();
        List<BoardPoint> left = new ArrayList<>();
        List<BoardPoint> right = new ArrayList<>();
        List<BoardPoint> down = new ArrayList<>();
        for (int i = 1; i < 8; i++) {
            int count = i - 1;
            if (top.size() == count && chessBoard.board[line + i][column] == null && chessBoard.checkPos(line + i)) {
                top.add(new BoardPoint(line + i, column));
            }
            if (left.size() == count && chessBoard.board[line][column - i] == null && chessBoard.checkPos(column - i)) {
                left.add(new BoardPoint(line, column - i));
            }
            if (right.size() == count && chessBoard.board[line][column + i] == null && chessBoard.checkPos(column + i)) {
                right.add(new BoardPoint(line, column + i));
            }
            if (down.size() == count && chessBoard.board[line - i][column] == null && chessBoard.checkPos(line - i)) {
                down.add(new BoardPoint(line - i, column));
            }
        }
        return CollectionUtils.ModifiableList.of(top, left, right, down);
    }

    @Override
    public String getSymbol() {
        return "R";
    }
}
