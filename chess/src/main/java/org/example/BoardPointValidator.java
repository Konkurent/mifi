package org.example;


public class BoardPointValidator {

    public static boolean isTheBoard(BoardPoint point) {
        return 0 <= point.line() && point.line() < 8
                && 0 <= point.column() && point.column() < 8;
    }

}
