package xiangqi;

/**
 * 棋子兵種類型
 */
public enum PieceType {
    GENERAL("帥", "將", 10000),
    ADVISOR("仕", "士", 200),
    ELEPHANT("相", "象", 200),
    HORSE("傌", "馬", 450),
    CHARIOT("俥", "車", 1000),
    CANNON("炮", "砲", 450),
    SOLDIER("兵", "卒", 100);

    private final String redChar;
    private final String blackChar;
    private final int baseValue;

    PieceType(String redChar, String blackChar, int baseValue) {
        this.redChar = redChar;
        this.blackChar = blackChar;
        this.baseValue = baseValue;
    }

    public String getCharacter(PieceColor color) {
        return color == PieceColor.RED ? redChar : blackChar;
    }

    public int getBaseValue() {
        return baseValue;
    }
}
