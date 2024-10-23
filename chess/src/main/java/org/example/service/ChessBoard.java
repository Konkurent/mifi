package org.example.service;

import org.example.piece.ChessPiece;
import org.example.piece.King;
import org.example.piece.Rook;

import java.util.Optional;

public class ChessBoard {
    public ChessPiece[][] board = new ChessPiece[8][8]; // creating a field for game
    String nowPlayer;

    public ChessBoard(String nowPlayer) {
        this.nowPlayer = nowPlayer;
    }

    public String nowPlayerColor() {
        return this.nowPlayer;
    }

    public boolean moveToPosition(int startLine, int startColumn, int endLine, int endColumn) {
        if (checkPos(startLine) && checkPos(startColumn) && checkPos(endLine) && checkPos(endColumn)) {
            if (startLine == endLine && startColumn == endColumn) return false;
            if (!nowPlayer.equals(board[startLine][startColumn].getColor())) return false;
            if (Optional.ofNullable(board[endLine][endColumn]).filter(piece -> piece.getColor().equals(nowPlayer)).isPresent()) { return false; }

            if (Optional.ofNullable(board[startLine][startColumn])
                    .filter(it -> it.canMoveToPosition(this, startLine, startColumn, endLine, endColumn))
                    .isPresent()) {
                board[endLine][endColumn] = board[startLine][startColumn]; // if piece can move, we moved a piece
                board[startLine][startColumn] = null; // set null to previous cell
                this.nowPlayer = this.nowPlayerColor().equals("White") ? "Black" : "White";

                return true;
            } else return false;
        } else return false;
    }

    public void printBoard() {  //print board in console
        System.out.println("Turn " + nowPlayer);
        System.out.println();
        System.out.println("Player 2(Black)");
        System.out.println();
        System.out.println("\t0\t1\t2\t3\t4\t5\t6\t7");

        for (int i = 7; i > -1; i--) {
            System.out.print(i + "\t");
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == null) {
                    System.out.print(".." + "\t");
                } else {
                    System.out.print(board[i][j].getSymbol() + board[i][j].getColor().substring(0, 1).toLowerCase() + "\t");
                }
            }
            System.out.println();
            System.out.println();
        }
        System.out.println("Player 1(White)");
    }

    public boolean checkPos(int pos) {
        return pos >= 0 && pos <= 7;
    }

    public boolean castling0() {
        return nowPlayer.equals("White") ? whiteCastling0() : blackCastling0();
    }

    private boolean whiteCastling0() {
        if (board[0][0] == null || board[0][4] == null) return false;
        if (board[0][0].getSymbol().equals("R") && board[0][4].getSymbol().equals("K") && // check that King and Rook
                board[0][1] == null & board[0][2] == null && board[0][3] == null) {
            if (board[0][0].getColor().equals("White") && board[0][4].getColor().equals("White") &&
                    !board[0][0].isStarted() && !board[0][4].isStarted() && new King( "White").isUnderAttack(this, 0, 2)) {// check that position not in under attack
                board[0][4] = null;
                board[0][2] = new King("White"); // move King
                board[0][2].start();
                board[0][0] = null;
                board[0][3] = new Rook("White"); // move Rook
                board[0][3].start();
                nowPlayer = "Black"; // next turn
                return true;
            } else return false;
        } else return false;
    }

    private boolean blackCastling0() {
        if (board[7][0] == null || board[7][4] == null) return false;
        if (board[7][0].getSymbol().equals("R") && board[7][4].getSymbol().equals("K") && // check that King and Rook
                board[7][1] == null && board[7][2] == null && board[7][3] == null) {
            if (board[7][0].getColor().equals("Black") && board[7][4].getColor().equals("Black") &&
                    !board[7][0].isStarted() && !board[7][4].isStarted() && new King("Black").isUnderAttack(this, 7, 2)) {
                board[7][4] = null;
                board[7][2] = new King( "Black");
                board[7][2].start();
                board[7][0] = null;
                board[7][3] = new Rook("Black");
                board[7][3].start();
                nowPlayer = "White"; // next turn
                return true;
            } else return false;
        } else return false;
    }

    public boolean castling7() {
        return nowPlayer.equals("White") ? whiteCastling7() : blackCastling7();
    }

    private boolean whiteCastling7() {
        if (board[0][7] == null || board[0][4] == null) return false;
        if (board[0][7].getSymbol().equals("R") && board[0][4].getSymbol().equals("K") && // check that King and Rook
                board[0][5] == null & board[0][5] == null) {
            if (board[0][7].getColor().equals("White") && board[0][4].getColor().equals("White") &&
                    !board[0][7].isStarted() && !board[0][4].isStarted() && new King( "White").isUnderAttack(this, 0, 6)) {// check that position not in under attack
                board[0][4] = null;
                board[0][6] = new King("White"); // move King
                board[0][6].start();
                board[0][7] = null;
                board[0][5] = new Rook("White"); // move Rook
                board[0][5].start();
                nowPlayer = "Black"; // next turn
                return true;
            } else return false;
        } else return false;
    }

    private boolean blackCastling7() {
        if (board[7][7] == null || board[7][4] == null) return false;
        if (board[7][7].getSymbol().equals("R") && board[7][4].getSymbol().equals("K") && // check that King and Rook
                board[7][6] == null && board[7][5] == null) {
            if (board[7][7].getColor().equals("Black") && board[7][4].getColor().equals("Black") &&
                    !board[7][7].isStarted() && !board[7][4].isStarted() && new King("Black").isUnderAttack(this, 7, 6)) {
                board[7][4] = null;
                board[7][6] = new King( "Black");
                board[7][6].start();
                board[7][7] = null;
                board[7][5] = new Rook("Black");
                board[7][5].start();
                nowPlayer = "White"; // next turn
                return true;
            } else return false;
        } else return false;
    }
}
