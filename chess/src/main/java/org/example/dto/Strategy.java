package org.example.dto;

import org.example.service.ChessBoard;

import java.util.List;

public interface Strategy {
    List<BoardPoint> resolveAvailablePoints(ChessBoard chessBoard, int line, int column);
}
