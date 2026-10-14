package xiangqi;

import java.util.Objects;

/**
 * 代表一步棋
 */
public class Move {
    public final int fromX;
    public final int fromY;
    public final int toX;
    public final int toY;
    public final Piece movedPiece;
    public final Piece capturedPiece;
    private String notation;

    public Move(int fromX, int fromY, int toX, int toY, Piece movedPiece, Piece capturedPiece) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.movedPiece = movedPiece;
        this.capturedPiece = capturedPiece;
    }

    public boolean isCapture() {
        return capturedPiece != null;
    }

    public String getNotation() {
        return notation;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return fromX == move.fromX && fromY == move.fromY &&
               toX == move.toX && toY == move.toY;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromX, fromY, toX, toY);
    }

    @Override
    public String toString() {
        return notation != null ? notation : String.format("(%d,%d)->(%d,%d)", fromX, fromY, toX, toY);
    }
}
