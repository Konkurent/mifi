package org.example.piece;

import org.example.dto.BoardPoint;
import org.example.service.ChessBoard;

import java.util.ArrayList;
import java.util.List;

public class Horse extends ChessPiece {

    public Horse(String color) {
        super(color);
    }

    @Override
    public String getSymbol() {
        return "H";
    }

    @Override
    public List<BoardPoint> resolveAvailablePoints(ChessBoard chessBoard, int line, int column) {
        List<BoardPoint> points = new ArrayList<>();
        points.add(new BoardPoint(line + 2, column + 1));
        points.add(new BoardPoint(line + 2, column - 1));
        points.add(new BoardPoint(line - 2, column + 1));
        points.add(new BoardPoint(line - 2, column - 1));
        points.add(new BoardPoint(line + 1, column + 2));
        points.add(new BoardPoint(line - 1, column + 2));
        points.add(new BoardPoint(line + 1, column - 2));
        points.add(new BoardPoint(line - 1, column - 2));
        return points;
    }
}
