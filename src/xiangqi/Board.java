package xiangqi;

import java.util.ArrayList;
import java.util.List;

/**
 * 棋盤狀態與核心規則邏輯
 * 棋盤為 9 列 (x: 0~8) x 10 行 (y: 0~9)
 * 黑方在上方 (y: 0~4)，紅方在下方 (y: 5~9)
 */
public class Board {
    public static final int COLS = 9;
    public static final int ROWS = 10;

    private final Piece[][] grid = new Piece[COLS][ROWS];
    private PieceColor currentTurn = PieceColor.RED;

    private int redKingX = 4, redKingY = 9;
    private int blackKingX = 4, blackKingY = 0;

    public Board() {
        initInitialBoard();
    }

    /**
     * 深拷貝構造函數
     */
    public Board(Board other) {
        for (int x = 0; x < COLS; x++) {
            System.arraycopy(other.grid[x], 0, this.grid[x], 0, ROWS);
        }
        this.currentTurn = other.currentTurn;
        this.redKingX = other.redKingX;
        this.redKingY = other.redKingY;
        this.blackKingX = other.blackKingX;
        this.blackKingY = other.blackKingY;
    }

    /**
     * 初始化標準象棋開局棋盤
     */
    public void initInitialBoard() {
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                grid[x][y] = null;
            }
        }

        // 黑方棋子 (上方 y = 0 ~ 4)
        setPiece(0, 0, new Piece(PieceType.CHARIOT, PieceColor.BLACK));
        setPiece(1, 0, new Piece(PieceType.HORSE, PieceColor.BLACK));
        setPiece(2, 0, new Piece(PieceType.ELEPHANT, PieceColor.BLACK));
        setPiece(3, 0, new Piece(PieceType.ADVISOR, PieceColor.BLACK));
        setPiece(4, 0, new Piece(PieceType.GENERAL, PieceColor.BLACK));
        setPiece(5, 0, new Piece(PieceType.ADVISOR, PieceColor.BLACK));
        setPiece(6, 0, new Piece(PieceType.ELEPHANT, PieceColor.BLACK));
        setPiece(7, 0, new Piece(PieceType.HORSE, PieceColor.BLACK));
        setPiece(8, 0, new Piece(PieceType.CHARIOT, PieceColor.BLACK));

        setPiece(1, 2, new Piece(PieceType.CANNON, PieceColor.BLACK));
        setPiece(7, 2, new Piece(PieceType.CANNON, PieceColor.BLACK));

        setPiece(0, 3, new Piece(PieceType.SOLDIER, PieceColor.BLACK));
        setPiece(2, 3, new Piece(PieceType.SOLDIER, PieceColor.BLACK));
        setPiece(4, 3, new Piece(PieceType.SOLDIER, PieceColor.BLACK));
        setPiece(6, 3, new Piece(PieceType.SOLDIER, PieceColor.BLACK));
        setPiece(8, 3, new Piece(PieceType.SOLDIER, PieceColor.BLACK));

        // 紅方棋子 (下方 y = 5 ~ 9)
        setPiece(0, 9, new Piece(PieceType.CHARIOT, PieceColor.RED));
        setPiece(1, 9, new Piece(PieceType.HORSE, PieceColor.RED));
        setPiece(2, 9, new Piece(PieceType.ELEPHANT, PieceColor.RED));
        setPiece(3, 9, new Piece(PieceType.ADVISOR, PieceColor.RED));
        setPiece(4, 9, new Piece(PieceType.GENERAL, PieceColor.RED));
        setPiece(5, 9, new Piece(PieceType.ADVISOR, PieceColor.RED));
        setPiece(6, 9, new Piece(PieceType.ELEPHANT, PieceColor.RED));
        setPiece(7, 9, new Piece(PieceType.HORSE, PieceColor.RED));
        setPiece(8, 9, new Piece(PieceType.CHARIOT, PieceColor.RED));

        setPiece(1, 7, new Piece(PieceType.CANNON, PieceColor.RED));
        setPiece(7, 7, new Piece(PieceType.CANNON, PieceColor.RED));

        setPiece(0, 6, new Piece(PieceType.SOLDIER, PieceColor.RED));
        setPiece(2, 6, new Piece(PieceType.SOLDIER, PieceColor.RED));
        setPiece(4, 6, new Piece(PieceType.SOLDIER, PieceColor.RED));
        setPiece(6, 6, new Piece(PieceType.SOLDIER, PieceColor.RED));
        setPiece(8, 6, new Piece(PieceType.SOLDIER, PieceColor.RED));

        redKingX = 4;
        redKingY = 9;
        blackKingX = 4;
        blackKingY = 0;
        currentTurn = PieceColor.RED;
    }

    public Piece getPiece(int x, int y) {
        if (!isValidCoordinate(x, y)) return null;
        return grid[x][y];
    }

    public void setPiece(int x, int y, Piece piece) {
        if (!isValidCoordinate(x, y)) return;
        grid[x][y] = piece;
        if (piece != null && piece.getType() == PieceType.GENERAL) {
            if (piece.getColor() == PieceColor.RED) {
                redKingX = x;
                redKingY = y;
            } else {
                blackKingX = x;
                blackKingY = y;
            }
        }
    }

    public PieceColor getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(PieceColor turn) {
        this.currentTurn = turn;
    }

    public static boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < COLS && y >= 0 && y < ROWS;
    }

    public static boolean isInPalace(int x, int y, PieceColor color) {
        if (x < 3 || x > 5) return false;
        if (color == PieceColor.BLACK) {
            return y >= 0 && y <= 2;
        } else {
            return y >= 7 && y <= 9;
        }
    }

    /**
     * 執行走子
     */
    public void makeMove(Move move) {
        grid[move.toX][move.toY] = move.movedPiece;
        grid[move.fromX][move.fromY] = null;

        if (move.movedPiece.getType() == PieceType.GENERAL) {
            if (move.movedPiece.getColor() == PieceColor.RED) {
                redKingX = move.toX;
                redKingY = move.toY;
            } else {
                blackKingX = move.toX;
                blackKingY = move.toY;
            }
        }
        currentTurn = currentTurn.opponent();
    }

    /**
     * 撤銷走子
     */
    public void undoMove(Move move) {
        grid[move.fromX][move.fromY] = move.movedPiece;
        grid[move.toX][move.toY] = move.capturedPiece;

        if (move.movedPiece.getType() == PieceType.GENERAL) {
            if (move.movedPiece.getColor() == PieceColor.RED) {
                redKingX = move.fromX;
                redKingY = move.fromY;
            } else {
                blackKingX = move.fromX;
                blackKingY = move.fromY;
            }
        }
        currentTurn = currentTurn.opponent();
    }

    /**
     * 檢查雙方將帥是否照面（飛將/對面笑）
     */
    public boolean areKingsFacing() {
        if (redKingX != blackKingX) return false;
        int startY = Math.min(blackKingY, redKingY) + 1;
        int endY = Math.max(blackKingY, redKingY);
        for (int y = startY; y < endY; y++) {
            if (grid[redKingX][y] != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判斷某方是否處於被「將軍」狀態
     */
    public boolean isCheck(PieceColor kingColor) {
        int kx = (kingColor == PieceColor.RED) ? redKingX : blackKingX;
        int ky = (kingColor == PieceColor.RED) ? redKingY : blackKingY;
        PieceColor enemyColor = kingColor.opponent();

        // 1. 車、將（包含直線照面）
        int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] d : dirs) {
            int cx = kx + d[0];
            int cy = ky + d[1];
            while (isValidCoordinate(cx, cy)) {
                Piece p = grid[cx][cy];
                if (p != null) {
                    if (p.getColor() == enemyColor) {
                        if (p.getType() == PieceType.CHARIOT) return true;
                        // 對方將帥同列無阻擋
                        if (p.getType() == PieceType.GENERAL) return true;
                    }
                    break; // 遇到棋子阻擋
                }
                cx += d[0];
                cy += d[1];
            }
        }

        // 2. 炮（隔一子打將）
        for (int[] d : dirs) {
            int cx = kx + d[0];
            int cy = ky + d[1];
            int count = 0;
            while (isValidCoordinate(cx, cy)) {
                Piece p = grid[cx][cy];
                if (p != null) {
                    count++;
                    if (count == 2) {
                        if (p.getColor() == enemyColor && p.getType() == PieceType.CANNON) {
                            return true;
                        }
                        break;
                    }
                }
                cx += d[0];
                cy += d[1];
            }
        }

        // 3. 馬（日字攻擊將）
        // 敵方馬在 (kx+dx, ky+dy)，其馬腿在 (kx+dx/2, ky) 或 (kx, ky+dy/2) 逆推
        int[][] horseOffsets = {
            {-1, -2}, {1, -2}, {-1, 2}, {1, 2},
            {-2, -1}, {-2, 1}, {2, -1}, {2, 1}
        };
        for (int[] off : horseOffsets) {
            int hx = kx + off[0];
            int hy = ky + off[1];
            if (isValidCoordinate(hx, hy)) {
                Piece p = grid[hx][hy];
                if (p != null && p.getColor() == enemyColor && p.getType() == PieceType.HORSE) {
                    // 檢查這匹馬跳向 kx, ky 時馬腿是否受阻
                    // 從馬 (hx, hy) 跳向 (kx, ky):
                    // 若 |hx - kx| == 1, |hy - ky| == 2，馬腿在 (hx, (hy + ky) / 2) -> (hx, hy - off[1]/2)
                    // 若 |hx - kx| == 2, |hy - ky| == 1，馬腿在 ((hx + kx) / 2, hy) -> (hx - off[0]/2, hy)
                    int legX = (Math.abs(off[0]) == 1) ? hx : (hx - off[0] / 2);
                    int legY = (Math.abs(off[0]) == 1) ? (hy - off[1] / 2) : hy;
                    if (grid[legX][legY] == null) {
                        return true;
                    }
                }
            }
        }

        // 4. 兵/卒
        if (kingColor == PieceColor.RED) {
            // 黑卒向下前進攻擊 (kx, ky-1) 或左右橫移 (kx-1, ky), (kx+1, ky)
            int[][] pawnAttacks = {{0, -1}, {-1, 0}, {1, 0}};
            for (int[] pa : pawnAttacks) {
                int px = kx + pa[0];
                int py = ky + pa[1];
                if (isValidCoordinate(px, py)) {
                    Piece p = grid[px][py];
                    if (p != null && p.getColor() == enemyColor && p.getType() == PieceType.SOLDIER) {
                        return true;
                    }
                }
            }
        } else {
            // 紅兵向上前進攻擊 (kx, ky+1) 或左右橫移 (kx-1, ky), (kx+1, ky)
            int[][] pawnAttacks = {{0, 1}, {-1, 0}, {1, 0}};
            for (int[] pa : pawnAttacks) {
                int px = kx + pa[0];
                int py = ky + pa[1];
                if (isValidCoordinate(px, py)) {
                    Piece p = grid[px][py];
                    if (p != null && p.getColor() == enemyColor && p.getType() == PieceType.SOLDIER) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * 生成所有合法的走法（過濾掉自殺、將軍未解、照面）
     */
    public List<Move> generateLegalMoves(PieceColor color) {
        List<Move> pseudo = generatePseudoLegalMoves(color);
        List<Move> legal = new ArrayList<>(pseudo.size());

        for (Move m : pseudo) {
            makeMove(m);
            // 走棋後：自身不能處於被將軍狀態，且將帥不能照面
            if (!isCheck(color) && !areKingsFacing()) {
                legal.add(m);
            }
            undoMove(m);
        }
        return legal;
    }

    /**
     * 生成某特定棋子位置的所有合法目標點
     */
    public List<Move> getLegalMovesForSquare(int x, int y) {
        Piece piece = getPiece(x, y);
        List<Move> result = new ArrayList<>();
        if (piece == null || piece.getColor() != currentTurn) return result;

        List<Move> allLegal = generateLegalMoves(piece.getColor());
        for (Move m : allLegal) {
            if (m.fromX == x && m.fromY == y) {
                result.add(m);
            }
        }
        return result;
    }

    /**
     * 生成所有偽合法走法（符合棋子走法規則，暫未過濾送將與照面）
     */
    public List<Move> generatePseudoLegalMoves(PieceColor color) {
        List<Move> moves = new ArrayList<>(64);
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                Piece p = grid[x][y];
                if (p != null && p.getColor() == color) {
                    generatePieceMoves(x, y, p, moves);
                }
            }
        }
        return moves;
    }

    private void generatePieceMoves(int x, int y, Piece p, List<Move> moves) {
        PieceColor color = p.getColor();
        switch (p.getType()) {
            case GENERAL: {
                int[][] steps = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                for (int[] s : steps) {
                    int nx = x + s[0];
                    int ny = y + s[1];
                    if (isInPalace(nx, ny, color)) {
                        Piece target = grid[nx][ny];
                        if (target == null || target.getColor() != color) {
                            moves.add(new Move(x, y, nx, ny, p, target));
                        }
                    }
                }
                break;
            }
            case ADVISOR: {
                int[][] steps = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
                for (int[] s : steps) {
                    int nx = x + s[0];
                    int ny = y + s[1];
                    if (isInPalace(nx, ny, color)) {
                        Piece target = grid[nx][ny];
                        if (target == null || target.getColor() != color) {
                            moves.add(new Move(x, y, nx, ny, p, target));
                        }
                    }
                }
                break;
            }
            case ELEPHANT: {
                int[][] steps = {{2, 2}, {2, -2}, {-2, 2}, {-2, -2}};
                for (int[] s : steps) {
                    int nx = x + s[0];
                    int ny = y + s[1];
                    // 不能過河：紅方 y >= 5，黑方 y <= 4
                    boolean riverOk = (color == PieceColor.RED) ? (ny >= 5 && ny <= 9) : (ny >= 0 && ny <= 4);
                    if (nx >= 0 && nx < COLS && riverOk) {
                        // 象眼檢查
                        int eyeX = x + s[0] / 2;
                        int eyeY = y + s[1] / 2;
                        if (grid[eyeX][eyeY] == null) {
                            Piece target = grid[nx][ny];
                            if (target == null || target.getColor() != color) {
                                moves.add(new Move(x, y, nx, ny, p, target));
                            }
                        }
                    }
                }
                break;
            }
            case HORSE: {
                int[][] jumps = {
                    {-1, -2, 0, -1}, {1, -2, 0, -1},
                    {-1, 2, 0, 1},   {1, 2, 0, 1},
                    {-2, -1, -1, 0}, {-2, 1, -1, 0},
                    {2, -1, 1, 0},   {2, 1, 1, 0}
                };
                for (int[] j : jumps) {
                    int nx = x + j[0];
                    int ny = y + j[1];
                    if (isValidCoordinate(nx, ny)) {
                        int legX = x + j[2];
                        int legY = y + j[3];
                        if (grid[legX][legY] == null) {
                            Piece target = grid[nx][ny];
                            if (target == null || target.getColor() != color) {
                                moves.add(new Move(x, y, nx, ny, p, target));
                            }
                        }
                    }
                }
                break;
            }
            case CHARIOT: {
                int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                for (int[] d : dirs) {
                    int nx = x + d[0];
                    int ny = y + d[1];
                    while (isValidCoordinate(nx, ny)) {
                        Piece target = grid[nx][ny];
                        if (target == null) {
                            moves.add(new Move(x, y, nx, ny, p, null));
                        } else {
                            if (target.getColor() != color) {
                                moves.add(new Move(x, y, nx, ny, p, target));
                            }
                            break;
                        }
                        nx += d[0];
                        ny += d[1];
                    }
                }
                break;
            }
            case CANNON: {
                int[][] dirs = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                for (int[] d : dirs) {
                    int nx = x + d[0];
                    int ny = y + d[1];
                    // 第一階段：不吃子，尋找空位
                    while (isValidCoordinate(nx, ny)) {
                        Piece target = grid[nx][ny];
                        if (target == null) {
                            moves.add(new Move(x, y, nx, ny, p, null));
                        } else {
                            // 遇到炮架，進入第二階段（尋找跳吃目標）
                            nx += d[0];
                            ny += d[1];
                            while (isValidCoordinate(nx, ny)) {
                                Piece jumpTarget = grid[nx][ny];
                                if (jumpTarget != null) {
                                    if (jumpTarget.getColor() != color) {
                                        moves.add(new Move(x, y, nx, ny, p, jumpTarget));
                                    }
                                    break;
                                }
                                nx += d[0];
                                ny += d[1];
                            }
                            break;
                        }
                        nx += d[0];
                        ny += d[1];
                    }
                }
                break;
            }
            case SOLDIER: {
                if (color == PieceColor.RED) {
                    // 向前一步 (y - 1)
                    int fy = y - 1;
                    if (isValidCoordinate(x, fy)) {
                        Piece target = grid[x][fy];
                        if (target == null || target.getColor() != color) {
                            moves.add(new Move(x, y, x, fy, p, target));
                        }
                    }
                    // 過河後 (y <= 4) 可橫走
                    if (y <= 4) {
                        int[] sx = {x - 1, x + 1};
                        for (int nx : sx) {
                            if (isValidCoordinate(nx, y)) {
                                Piece target = grid[nx][y];
                                if (target == null || target.getColor() != color) {
                                    moves.add(new Move(x, y, nx, y, p, target));
                                }
                            }
                        }
                    }
                } else {
                    // 黑卒向前一步 (y + 1)
                    int fy = y + 1;
                    if (isValidCoordinate(x, fy)) {
                        Piece target = grid[x][fy];
                        if (target == null || target.getColor() != color) {
                            moves.add(new Move(x, y, x, fy, p, target));
                        }
                    }
                    // 過河後 (y >= 5) 可橫走
                    if (y >= 5) {
                        int[] sx = {x - 1, x + 1};
                        for (int nx : sx) {
                            if (isValidCoordinate(nx, y)) {
                                Piece target = grid[nx][y];
                                if (target == null || target.getColor() != color) {
                                    moves.add(new Move(x, y, nx, y, p, target));
                                }
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    public int getRedKingX() { return redKingX; }
    public int getRedKingY() { return redKingY; }
    public int getBlackKingX() { return blackKingX; }
    public int getBlackKingY() { return blackKingY; }
}
