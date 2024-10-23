package org.example.piece;

import org.example.dto.BoardPoint;
import org.example.service.ChessBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Pawn extends ChessPiece {

    public Pawn(String color) {
        super(color);
    }

    @Override
    public List<BoardPoint> resolveAvailablePoints(ChessBoard chessBoard, int line, int column) {
        List<BoardPoint> availablePoints = new ArrayList<>();
        if (!isStarted() && chessBoard.board[line + 2][column] == null) availablePoints.add(new BoardPoint(line + 2, column));
        if (chessBoard.board[line + 1][column] == null) availablePoints.add(new BoardPoint(line + 1, column));
        Optional.ofNullable(chessBoard.board[line + 1][column + 1])
                .map(it -> new BoardPoint(line + 1, column + 1))
                .ifPresent(availablePoints::add);
        Optional.ofNullable(chessBoard.board[line + 1][column - 1])
                .map(it -> new BoardPoint(line + 1, column - 1))
                .ifPresent(availablePoints::add);
        return availablePoints;
    }
    

    @Override
    public String getSymbol() {
        return "P";
    }
}
