package org.example.dto;

public record BoardPoint(int line, int column) {
    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof BoardPoint that)) return false;

        return line == that.line && column == that.column;
    }

    @Override
    public int hashCode() {
        int result = line;
        result = 31 * result + column;
        return result;
    }
};
