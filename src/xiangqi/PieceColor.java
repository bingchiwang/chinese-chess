package xiangqi;

/**
 * 棋子顏色：紅方或黑方
 */
public enum PieceColor {
    RED("紅方", "紅"),
    BLACK("黑方", "黑");

    private final String fullName;
    private final String shortName;

    PieceColor(String fullName, String shortName) {
        this.fullName = fullName;
        this.shortName = shortName;
    }

    public String getFullName() {
        return fullName;
    }

    public String getShortName() {
        return shortName;
    }

    public PieceColor opponent() {
        return this == RED ? BLACK : RED;
    }
}
