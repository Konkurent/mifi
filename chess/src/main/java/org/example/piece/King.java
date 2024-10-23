package org.example.piece;

import org.example.dto.BoardPoint;
import org.example.service.ChessBoard;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class King extends ChessPiece {

    public King(String color) {
        super(color);
    }

    @Override
    public boolean canMoveToPosition(ChessBoard chessBoard, int line, int column, int toLine, int toColumn) {
        BoardPoint endPoint = new BoardPoint(toLine, toColumn);
        boolean res = resolveAvailablePoints(chessBoard, line, column).stream().anyMatch(endPoint::equals)
                && !isUnderAttack(chessBoard, line, column)
                && !isUnderAttack(chessBoard, line, column);
        if (res) start();
        return res;
    }

    public List<BoardPoint> resolveAvailablePoints(ChessBoard chessBoard, int line, int column) {
        List<BoardPoint> boardPoints = new ArrayList<>();

        boardPoints.add(new BoardPoint(line + 1, column + 1));
        boardPoints.add(new BoardPoint(line + 1, column));
        boardPoints.add(new BoardPoint(line + 1, column - 1));

        boardPoints.add(new BoardPoint(line - 1, column + 1));
        boardPoints.add(new BoardPoint(line - 1, column));
        boardPoints.add(new BoardPoint(line - 1, column - 1));

        boardPoints.add(new BoardPoint(line, column + 1));
        boardPoints.add(new BoardPoint(line, column - 1));
        return boardPoints;
    }

    public boolean isUnderAttack(ChessBoard chessBoard, int line, int column) {
        return IntStream.range(0, chessBoard.board.length).filter(startLine ->
            IntStream.range(0, chessBoard.board[line].length)
                    .filter(startColumn -> chessBoard.board[startLine][startColumn] != this && chessBoard.board[startLine][startColumn].canMoveToPosition(chessBoard, startLine, startColumn, line, column))
                    .toArray().length != 0
        ).toArray().length != 0;
    }

    @Override
    public String getSymbol() {
        return "K";
    }
}
