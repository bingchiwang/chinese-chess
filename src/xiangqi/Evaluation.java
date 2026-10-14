package xiangqi;

/**
 * 棋盤估值系統（包含子力價值與位置價值表 PST）
 * 正分代表紅方優勢，負分代表黑方優勢
 */
public class Evaluation {

    // 卒/兵 位置加分表（以紅方視角，y=0 為黑方底線，y=9 為紅方底線）
    private static final int[][] PAWN_PST = {
        {  0,   3,   6,   9,  12,   9,   6,   3,   0}, // y=0 (黑方底線)
        { 18,  36,  56,  80,  90,  80,  56,  36,  18}, // y=1 (九宮深處)
        { 14,  26,  42,  60,  70,  60,  42,  26,  14}, // y=2 (九宮頂部)
        { 10,  20,  30,  34,  40,  34,  30,  20,  10}, // y=3 (黑方卒林線)
        {  6,   8,  14,  18,  20,  18,  14,   8,   6}, // y=4 (過河後)
        // ----------- 楚河 漢界 (y=4 / y=5) -----------
        {  0,   0,   0,   0,   0,   0,   0,   0,   0}, // y=5 (過河前)
        {  0,   0,  -2,   0,   4,   0,  -2,   0,   0}, // y=6 (紅兵初始線)
        {  0,   0,   0,   0,   0,   0,   0,   0,   0}, // y=7
        {  0,   0,   0,   0,   0,   0,   0,   0,   0}, // y=8
        {  0,   0,   0,   0,   0,   0,   0,   0,   0}  // y=9
    };

    // 馬 位置加分表
    private static final int[][] HORSE_PST = {
        {  4,   8,  16,  12,   4,  12,  16,   8,   4}, // y=0
        {  4,  10,  28,  16,   8,  16,  28,  10,   4}, // y=1
        { 12,  16,  30,  35,  40,  35,  30,  16,  12}, // y=2
        {  8,  24,  26,  30,  28,  30,  26,  24,   8}, // y=3
        {  6,  16,  18,  22,  24,  22,  18,  16,   6}, // y=4
        // ----------- 楚河 漢界 -----------
        {  4,  12,  16,  18,  20,  18,  16,  12,   4}, // y=5
        {  2,   8,  14,  16,  16,  16,  14,   8,   2}, // y=6
        {  2,   4,   8,  10,   6,  10,   8,   4,   2}, // y=7
        {  0,   2,   4,   4,  -2,   4,   4,   2,   0}, // y=8
        {  0,  -4,   0,   0,   0,   0,   0,  -4,   0}  // y=9
    };

    // 車 位置加分表
    private static final int[][] CHARIOT_PST = {
        { 14,  14,  12,  18,  16,  18,  12,  14,  14}, // y=0 (沉底車)
        { 16,  20,  18,  24,  26,  24,  18,  20,  16}, // y=1
        { 12,  12,  12,  18,  18,  18,  12,  12,  12}, // y=2
        { 12,  18,  16,  22,  22,  22,  16,  18,  12}, // y=3 (騎河車)
        { 10,  14,  12,  18,  18,  18,  12,  14,  10}, // y=4 (巡河車)
        // ----------- 楚河 漢界 -----------
        {  8,  12,  10,  14,  16,  14,  10,  12,   8}, // y=5
        {  6,  10,   8,  14,  14,  14,   8,  10,   6}, // y=6
        {  4,   8,   6,  14,  12,  14,   6,   8,   4}, // y=7
        {  8,   8,   6,  12,   8,  12,   6,   8,   8}, // y=8
        { -2,  10,   6,   8,  12,   8,   6,  10,  -2}  // y=9
    };

    // 炮 位置加分表
    private static final int[][] CANNON_PST = {
        {  6,   4,   0,  -6,  -8,  -6,   0,   4,   6}, // y=0
        {  2,   2,   0,  -4,  -4,  -4,   0,   2,   2}, // y=1
        {  4,   6,   4,   0,  10,   0,   4,   6,   4}, // y=2
        {  2,   6,   8,  10,  16,  10,   8,   6,   2}, // y=3
        {  0,   2,   4,   8,  12,   8,   4,   2,   0}, // y=4
        // ----------- 楚河 漢界 -----------
        {  0,   2,   4,   6,   8,   6,   4,   2,   0}, // y=5
        {  0,   0,   2,   6,  10,   6,   2,   0,   0}, // y=6
        {  0,   0,   0,   2,   6,   2,   0,   0,   0}, // y=7
        { -2,   0,   4,   2,   6,   2,   4,   0,  -2}, // y=8
        {  0,   0,   0,   2,   6,   2,   0,   0,   0}  // y=9
    };

    /**
     * 評估整個棋盤分值（正分表示紅方有利，負分表示黑方有利）
     */
    public static int evaluate(Board board) {
        int redScore = 0;
        int blackScore = 0;

        for (int x = 0; x < Board.COLS; x++) {
            for (int y = 0; y < Board.ROWS; y++) {
                Piece p = board.getPiece(x, y);
                if (p == null) continue;

                int baseVal = p.getType().getBaseValue();
                int pstVal = 0;

                if (p.getColor() == PieceColor.RED) {
                    pstVal = getPstBonus(p.getType(), x, y);
                    redScore += baseVal + pstVal;
                } else {
                    // 黑方位置映射（翻轉 y 軸）
                    pstVal = getPstBonus(p.getType(), x, 9 - y);
                    blackScore += baseVal + pstVal;
                }
            }
        }

        // 將軍評估微調
        if (board.isCheck(PieceColor.BLACK)) {
            redScore += 50; // 紅方正在將軍黑方
        }
        if (board.isCheck(PieceColor.RED)) {
            blackScore += 50; // 黑方正在將軍紅方
        }

        return redScore - blackScore;
    }

    private static int getPstBonus(PieceType type, int x, int y) {
        if (x < 0 || x >= Board.COLS || y < 0 || y >= Board.ROWS) return 0;
        switch (type) {
            case SOLDIER:
                return PAWN_PST[y][x];
            case HORSE:
                return HORSE_PST[y][x];
            case CHARIOT:
                return CHARIOT_PST[y][x];
            case CANNON:
                return CANNON_PST[y][x];
            default:
                return 0;
        }
    }
}
