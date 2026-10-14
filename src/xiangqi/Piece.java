package xiangqi;

import java.util.Objects;

/**
 * 代表一個象棋棋子
 */
public class Piece {
    private final PieceType type;
    private final PieceColor color;

    public Piece(PieceType type, PieceColor color) {
        this.type = Objects.requireNonNull(type);
        this.color = Objects.requireNonNull(color);
    }

    public PieceType getType() {
        return type;
    }

    public PieceColor getColor() {
        return color;
    }

    public String getCharacter() {
        return type.getCharacter(color);
    }

    public boolean isRed() {
        return color == PieceColor.RED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Piece piece = (Piece) o;
        return type == piece.type && color == piece.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, color);
    }

    @Override
    public String toString() {
        return color.getShortName() + getCharacter();
    }
}
