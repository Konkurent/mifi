package org.example.piece;

import org.example.dto.BoardPoint;
import org.example.service.ChessBoard;
import org.example.dto.Strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Queen extends ChessPiece implements Strategy {

    private final List<Strategy> strategies = new ArrayList<>();

    public Queen(String color) {
        super(color);
        strategies.add(new King(color));
        strategies.add(new Bishop(color));
        strategies.add(new Rook(color));
    }

    @Override
    public String getSymbol() {
        return "Q";
    }

    @Override
    public List<BoardPoint> resolveAvailablePoints(ChessBoard chessBoard, int line, int column) {
        return strategies.stream()
                .map(it -> it.resolveAvailablePoints(chessBoard, line, column))
                .flatMap(Collection::stream).toList();
    }
}
