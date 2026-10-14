package xiangqi;

/**
 * 中國象棋標準中文記譜法工具類
 */
public class Notation {
    private static final String[] RED_NUMERALS = {"", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
    private static final String[] BLACK_NUMERALS = {"", "１", "２", "３", "４", "５", "６", "７", "８", "９"};

    public static String toChineseNotation(Board boardBeforeMove, Move move) {
        if (move == null || move.movedPiece == null) return "";

        Piece piece = move.movedPiece;
        PieceColor color = piece.getColor();
        boolean isRed = color == PieceColor.RED;

        // 計算起始列與目標列（從該方視角，右至左為 1~9）
        int fromCol = isRed ? (9 - move.fromX) : (move.fromX + 1);
        int toCol = isRed ? (9 - move.toX) : (move.toX + 1);

        String pieceName = piece.getCharacter();
        String prefix = pieceName + (isRed ? RED_NUMERALS[fromCol] : BLACK_NUMERALS[fromCol]);

        // 檢查同列是否有同種棋子（處理「前/後」情況）
        int sameTypeCount = 0;
        int frontY = isRed ? 10 : -1;
        for (int y = 0; y < 10; y++) {
            Piece p = boardBeforeMove.getPiece(move.fromX, y);
            if (p != null && p.getColor() == color && p.getType() == piece.getType()) {
                sameTypeCount++;
                if (isRed) {
                    if (y < frontY) frontY = y; // 紅方向上為前（y 較小）
                } else {
                    if (y > frontY) frontY = y; // 黑方向下為前（y 較大）
                }
            }
        }

        if (sameTypeCount == 2) {
            boolean isFront = (isRed && move.fromY == frontY) || (!isRed && move.fromY == frontY);
            prefix = (isFront ? "前" : "後") + pieceName;
        }

        // 判斷走向：進、退、平
        int dy = move.toY - move.fromY;
        String action;
        String distanceOrTarget;

        if (dy == 0) {
            // 平
            action = "平";
            distanceOrTarget = isRed ? RED_NUMERALS[toCol] : BLACK_NUMERALS[toCol];
        } else {
            boolean isForward = isRed ? (dy < 0) : (dy > 0);
            action = isForward ? "進" : "退";

            // 直行棋子（車、炮、兵、將）：進退為步數
            // 斜行棋子（馬、相、仕）：進退為目標列
            if (piece.getType() == PieceType.HORSE ||
                piece.getType() == PieceType.ELEPHANT ||
                piece.getType() == PieceType.ADVISOR) {
                distanceOrTarget = isRed ? RED_NUMERALS[toCol] : BLACK_NUMERALS[toCol];
            } else {
                int steps = Math.abs(dy);
                distanceOrTarget = isRed ? RED_NUMERALS[steps] : BLACK_NUMERALS[steps];
            }
        }

        return prefix + action + distanceOrTarget;
    }
}
