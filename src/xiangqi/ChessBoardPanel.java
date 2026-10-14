package xiangqi;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * 棋盤繪製與滑鼠互動元件
 */
public class ChessBoardPanel extends JPanel {

    public interface MoveListener {
        void onPlayerMove(Move move);
    }

    private Board board;
    private MoveListener moveListener;
    private boolean isFlipped = false; // 是否翻轉視角（黑方視角）
    private boolean isInteractive = true; // 是否允許玩家滑鼠操作

    private int selectedX = -1;
    private int selectedY = -1;
    private List<Move> legalMovesForSelected = new ArrayList<>();
    private Move lastMove = null;
    private Move hintMove = null;

    // 字型快取
    private Font pieceFont;
    private Font riverFont;
    private Font labelFont;

    public ChessBoardPanel(Board board) {
        this.board = board;
        setBackground(new Color(230, 204, 168));
        setPreferredSize(new Dimension(580, 640));
        setMinimumSize(new Dimension(480, 530));

        initFonts();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isInteractive) return;
                handleMouseClick(e.getX(), e.getY());
            }
        });
    }

    private void initFonts() {
        String[] fontNames = {"DFKai-SB", "Microsoft YaHei", "SimSun", "Serif"};
        String chosenFont = "Serif";
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] available = ge.getAvailableFontFamilyNames();

        for (String candidate : fontNames) {
            for (String av : available) {
                if (av.equalsIgnoreCase(candidate)) {
                    chosenFont = candidate;
                    break;
                }
            }
            if (!chosenFont.equals("Serif")) break;
        }

        pieceFont = new Font(chosenFont, Font.BOLD, 26);
        riverFont = new Font(chosenFont, Font.BOLD, 22);
        labelFont = new Font(chosenFont, Font.PLAIN, 13);
    }

    public void setBoard(Board board) {
        this.board = board;
        clearSelection();
        repaint();
    }

    public void setMoveListener(MoveListener listener) {
        this.moveListener = listener;
    }

    public void setFlipped(boolean flipped) {
        this.isFlipped = flipped;
        clearSelection();
        repaint();
    }

    public boolean isFlipped() {
        return isFlipped;
    }

    public void setInteractive(boolean interactive) {
        this.isInteractive = interactive;
        if (!interactive) {
            clearSelection();
        }
        repaint();
    }

    public void setLastMove(Move move) {
        this.lastMove = move;
        this.hintMove = null;
        repaint();
    }

    public void setHintMove(Move move) {
        this.hintMove = move;
        repaint();
    }

    public void clearSelection() {
        this.selectedX = -1;
        this.selectedY = -1;
        this.legalMovesForSelected.clear();
        repaint();
    }

    // 計算棋盤渲染尺寸與偏移
    private int getCellSize() {
        int w = getWidth() - 70;
        int h = getHeight() - 70;
        return Math.max(38, Math.min(w / 8, h / 9));
    }

    private int getOffsetX(int cellSize) {
        return (getWidth() - 8 * cellSize) / 2;
    }

    private int getOffsetY(int cellSize) {
        return (getHeight() - 9 * cellSize) / 2;
    }

    private Point toScreenPoint(int x, int y, int cellSize, int offsetX, int offsetY) {
        int dx = isFlipped ? (8 - x) : x;
        int dy = isFlipped ? (9 - y) : y;
        return new Point(offsetX + dx * cellSize, offsetY + dy * cellSize);
    }

    private Point toBoardPoint(int px, int py, int cellSize, int offsetX, int offsetY) {
        double rX = (double) (px - offsetX) / cellSize;
        double rY = (double) (py - offsetY) / cellSize;
        int dx = (int) Math.round(rX);
        int dy = (int) Math.round(rY);

        if (dx < 0 || dx > 8 || dy < 0 || dy > 9) return null;

        // 點擊距離容差（半徑內有效）
        int screenX = offsetX + dx * cellSize;
        int screenY = offsetY + dy * cellSize;
        double dist = Math.hypot(px - screenX, py - screenY);
        if (dist > cellSize * 0.48) return null;

        int bx = isFlipped ? (8 - dx) : dx;
        int by = isFlipped ? (9 - dy) : dy;
        return new Point(bx, by);
    }

    private void handleMouseClick(int px, int py) {
        if (board == null) return;

        int cellSize = getCellSize();
        int offsetX = getOffsetX(cellSize);
        int offsetY = getOffsetY(cellSize);

        Point bp = toBoardPoint(px, py, cellSize, offsetX, offsetY);
        if (bp == null) {
            clearSelection();
            return;
        }

        int x = bp.x;
        int y = bp.y;
        Piece piece = board.getPiece(x, y);

        // 若已經選中某子，嘗試移動到目標格
        if (selectedX != -1 && selectedY != -1) {
            for (Move move : legalMovesForSelected) {
                if (move.toX == x && move.toY == y) {
                    clearSelection();
                    if (moveListener != null) {
                        moveListener.onPlayerMove(move);
                    }
                    return;
                }
            }
        }

        // 點選自己的棋子進行選中
        if (piece != null && piece.getColor() == board.getCurrentTurn()) {
            selectedX = x;
            selectedY = y;
            legalMovesForSelected = board.getLegalMovesForSquare(x, y);
            hintMove = null;
            repaint();
        } else {
            clearSelection();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int cellSize = getCellSize();
        int offsetX = getOffsetX(cellSize);
        int offsetY = getOffsetY(cellSize);

        // 1. 繪製仿木紋棋盤背景
        drawBoardBackground(g2, cellSize, offsetX, offsetY);

        // 2. 繪製棋盤線條（九宮、楚河漢界、十字標記）
        drawBoardGrid(g2, cellSize, offsetX, offsetY);

        // 3. 繪製上一步走子高亮提示
        drawLastMoveHighlight(g2, cellSize, offsetX, offsetY);

        // 4. 繪製 AI 提示走法
        drawHintMove(g2, cellSize, offsetX, offsetY);

        // 5. 繪製將軍警示光圈
        drawCheckAlert(g2, cellSize, offsetX, offsetY);

        // 6. 繪製棋子
        drawPieces(g2, cellSize, offsetX, offsetY);

        // 7. 繪製選中標記與可走目標點
        drawSelectionAndLegalMoves(g2, cellSize, offsetX, offsetY);

        g2.dispose();
    }

    private void drawBoardBackground(Graphics2D g2, int cellSize, int offsetX, int offsetY) {
        int x0 = offsetX - 28;
        int y0 = offsetY - 28;
        int w = 8 * cellSize + 56;
        int h = 9 * cellSize + 56;

        // 漸層實木底板
        GradientPaint woodPaint = new GradientPaint(
            x0, y0, new Color(238, 206, 155),
            x0 + w, y0 + h, new Color(208, 168, 114)
        );
        g2.setPaint(woodPaint);
        g2.fill(new RoundRectangle2D.Float(x0, y0, w, h, 18, 18));

        // 外框木邊陰影與金色邊條
        g2.setColor(new Color(102, 60, 24));
        g2.setStroke(new BasicStroke(3.5f));
        g2.draw(new RoundRectangle2D.Float(x0, y0, w, h, 18, 18));

        // 內框裝飾線
        g2.setColor(new Color(140, 85, 35, 180));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(new RoundRectangle2D.Float(offsetX - 10, offsetY - 10, 8 * cellSize + 20, 9 * cellSize + 20, 8, 8));
    }

    private void drawBoardGrid(Graphics2D g2, int cellSize, int offsetX, int offsetY) {
        Color lineColor = new Color(74, 42, 18);
        g2.setColor(lineColor);
        g2.setStroke(new BasicStroke(1.8f));

        // 橫線 (10 條)
        for (int y = 0; y < 10; y++) {
            int py = offsetY + y * cellSize;
            g2.drawLine(offsetX, py, offsetX + 8 * cellSize, py);
        }

        // 直線 (9 條，注意河界處不畫中間 7 條)
        for (int x = 0; x < 9; x++) {
            int px = offsetX + x * cellSize;
            if (x == 0 || x == 8) {
                // 兩側邊線通達上下
                g2.drawLine(px, offsetY, px, offsetY + 9 * cellSize);
            } else {
                // 上半部 (行 0~4) 與 下半部 (行 5~9)
                g2.drawLine(px, offsetY, px, offsetY + 4 * cellSize);
                g2.drawLine(px, offsetY + 5 * cellSize, px, offsetY + 9 * cellSize);
            }
        }

        // 繪製九宮斜線
        // 上九宮
        g2.drawLine(offsetX + 3 * cellSize, offsetY, offsetX + 5 * cellSize, offsetY + 2 * cellSize);
        g2.drawLine(offsetX + 5 * cellSize, offsetY, offsetX + 3 * cellSize, offsetY + 2 * cellSize);
        // 下九宮
        g2.drawLine(offsetX + 3 * cellSize, offsetY + 7 * cellSize, offsetX + 5 * cellSize, offsetY + 9 * cellSize);
        g2.drawLine(offsetX + 5 * cellSize, offsetY + 7 * cellSize, offsetX + 3 * cellSize, offsetY + 9 * cellSize);

        // 楚河 漢界 書法文字
        g2.setFont(riverFont);
        g2.setColor(new Color(110, 65, 30));
        FontMetrics fm = g2.getFontMetrics();

        String leftText = isFlipped ? "漢 界" : "楚 河";
        String rightText = isFlipped ? "楚 河" : "漢 界";

        int riverY = offsetY + 4 * cellSize + (cellSize + fm.getAscent() - fm.getDescent()) / 2;
        int leftX = offsetX + 2 * cellSize - fm.stringWidth(leftText) / 2;
        int rightX = offsetX + 6 * cellSize - fm.stringWidth(rightText) / 2;

        g2.drawString(leftText, leftX, riverY);
        g2.drawString(rightText, rightX, riverY);

        // 繪製兵位與炮位標準十字折角標記
        drawAllCornerMarks(g2, cellSize, offsetX, offsetY);
    }

    private void drawAllCornerMarks(Graphics2D g2, int cellSize, int offsetX, int offsetY) {
        g2.setStroke(new BasicStroke(1.2f));
        g2.setColor(new Color(74, 42, 18));

        // 炮位
        drawCornerMarkAt(g2, 1, 2, cellSize, offsetX, offsetY, true, true);
        drawCornerMarkAt(g2, 7, 2, cellSize, offsetX, offsetY, true, true);
        drawCornerMarkAt(g2, 1, 7, cellSize, offsetX, offsetY, true, true);
        drawCornerMarkAt(g2, 7, 7, cellSize, offsetX, offsetY, true, true);

        // 兵/卒位
        int[] pawnCols = {0, 2, 4, 6, 8};
        for (int c : pawnCols) {
            boolean left = (c != 0);
            boolean right = (c != 8);
            drawCornerMarkAt(g2, c, 3, cellSize, offsetX, offsetY, left, right);
            drawCornerMarkAt(g2, c, 6, cellSize, offsetX, offsetY, left, right);
        }
    }

    private void drawCornerMarkAt(Graphics2D g2, int bx, int by, int cellSize, int offsetX, int offsetY, boolean left, boolean right) {
        Point p = toScreenPoint(bx, by, cellSize, offsetX, offsetY);
        int gap = Math.max(3, cellSize / 14);
        int len = Math.max(4, cellSize / 7);

        // 左上角
        if (left) {
            g2.drawLine(p.x - gap - len, p.y - gap, p.x - gap, p.y - gap);
            g2.drawLine(p.x - gap, p.y - gap - len, p.x - gap, p.y - gap);
            // 左下角
            g2.drawLine(p.x - gap - len, p.y + gap, p.x - gap, p.y + gap);
            g2.drawLine(p.x - gap, p.y + gap + len, p.x - gap, p.y + gap);
        }
        // 右上角
        if (right) {
            g2.drawLine(p.x + gap, p.y - gap, p.x + gap + len, p.y - gap);
            g2.drawLine(p.x + gap, p.y - gap - len, p.x + gap, p.y - gap);
            // 右下角
            g2.drawLine(p.x + gap, p.y + gap, p.x + gap + len, p.y + gap);
            g2.drawLine(p.x + gap, p.y + gap + len, p.x + gap, p.y + gap);
        }
    }

    private void drawLastMoveHighlight(Graphics2D g2, int cellSize, int offsetX, int offsetY) {
        if (lastMove == null) return;

        Point p1 = toScreenPoint(lastMove.fromX, lastMove.fromY, cellSize, offsetX, offsetY);
        Point p2 = toScreenPoint(lastMove.toX, lastMove.toY, cellSize, offsetX, offsetY);

        int r = (int) (cellSize * 0.42);
        // 起點框 (淡藍色半透明)
        g2.setColor(new Color(52, 152, 219, 90));
        g2.fill(new RoundRectangle2D.Float(p1.x - r, p1.y - r, 2 * r, 2 * r, 10, 10));
        g2.setColor(new Color(41, 128, 185, 200));
        g2.setStroke(new BasicStroke(2.0f));
        g2.draw(new RoundRectangle2D.Float(p1.x - r, p1.y - r, 2 * r, 2 * r, 10, 10));

        // 終點框 (金黃色半透明)
        g2.setColor(new Color(241, 196, 15, 110));
        g2.fill(new RoundRectangle2D.Float(p2.x - r, p2.y - r, 2 * r, 2 * r, 10, 10));
        g2.setColor(new Color(211, 84, 0, 220));
        g2.setStroke(new BasicStroke(2.2f));
        g2.draw(new RoundRectangle2D.Float(p2.x - r, p2.y - r, 2 * r, 2 * r, 10, 10));
    }

    private void drawHintMove(Graphics2D g2, int cellSize, int offsetX, int offsetY) {
        if (hintMove == null) return;

        Point p1 = toScreenPoint(hintMove.fromX, hintMove.fromY, cellSize, offsetX, offsetY);
        Point p2 = toScreenPoint(hintMove.toX, hintMove.toY, cellSize, offsetX, offsetY);

        // 畫箭頭提示
        g2.setColor(new Color(46, 204, 113, 230));
        g2.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(p1.x, p1.y, p2.x, p2.y);

        // 終點綠色外圈
        int r = (int) (cellSize * 0.44);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{6, 4}, 0));
        g2.drawOval(p2.x - r, p2.y - r, 2 * r, 2 * r);
    }

    private void drawCheckAlert(Graphics2D g2, int cellSize, int offsetX, int offsetY) {
        if (board == null) return;
        PieceColor turn = board.getCurrentTurn();
        if (board.isCheck(turn)) {
            int kx = (turn == PieceColor.RED) ? board.getRedKingX() : board.getBlackKingX();
            int ky = (turn == PieceColor.RED) ? board.getRedKingY() : board.getBlackKingY();
            Point kp = toScreenPoint(kx, ky, cellSize, offsetX, offsetY);

            int r = (int) (cellSize * 0.48);
            // 紅色擴散警示圈
            g2.setColor(new Color(231, 76, 60, 120));
            g2.fillOval(kp.x - r, kp.y - r, 2 * r, 2 * r);
            g2.setColor(new Color(192, 57, 43));
            g2.setStroke(new BasicStroke(3.0f));
            g2.drawOval(kp.x - r, kp.y - r, 2 * r, 2 * r);
        }
    }

    private void drawPieces(Graphics2D g2, int cellSize, int offsetX, int offsetY) {
        if (board == null) return;

        int pieceRadius = (int) (cellSize * 0.41);
        int diameter = 2 * pieceRadius;

        // 調節字型大小匹配格子
        int fontSize = Math.max(16, (int) (cellSize * 0.52));
        Font currentPieceFont = pieceFont.deriveFont(Font.BOLD, (float) fontSize);
        g2.setFont(currentPieceFont);
        FontMetrics fm = g2.getFontMetrics();

        for (int x = 0; x < Board.COLS; x++) {
            for (int y = 0; y < Board.ROWS; y++) {
                Piece piece = board.getPiece(x, y);
                if (piece == null) continue;

                Point pt = toScreenPoint(x, y, cellSize, offsetX, offsetY);
                drawSinglePiece(g2, piece, pt.x, pt.y, pieceRadius, fm);
            }
        }
    }

    private void drawSinglePiece(Graphics2D g2, Piece piece, int cx, int cy, int radius, FontMetrics fm) {
        int diameter = 2 * radius;
        int px = cx - radius;
        int py = cy - radius;

        // 1. 棋子立體陰影
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fill(new Ellipse2D.Float(px + 2, py + 3, diameter, diameter));

        // 2. 棋子木質球體立體漸層
        GradientPaint tokenGradient = new GradientPaint(
            px + radius * 0.4f, py + radius * 0.4f, new Color(254, 247, 237),
            px + diameter, py + diameter, new Color(225, 203, 172)
        );
        g2.setPaint(tokenGradient);
        g2.fill(new Ellipse2D.Float(px, py, diameter, diameter));

        // 3. 棋子邊緣環形線條
        boolean isRed = piece.isRed();
        Color themeColor = isRed ? new Color(198, 30, 30) : new Color(25, 25, 25);
        Color rimColor = isRed ? new Color(175, 45, 45, 170) : new Color(80, 80, 80, 170);

        g2.setColor(rimColor);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new Ellipse2D.Float(px + 1, py + 1, diameter - 2, diameter - 2));

        // 內環細圈
        int innerGap = Math.max(3, radius / 7);
        g2.draw(new Ellipse2D.Float(px + innerGap, py + innerGap, diameter - 2 * innerGap, diameter - 2 * innerGap));

        // 4. 書法棋子文字
        g2.setColor(themeColor);
        String text = piece.getCharacter();
        int textX = cx - fm.stringWidth(text) / 2;
        int textY = cy + (fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(text, textX, textY);
    }

    private void drawSelectionAndLegalMoves(Graphics2D g2, int cellSize, int offsetX, int offsetY) {
        // 繪製選中框
        if (selectedX != -1 && selectedY != -1) {
            Point sp = toScreenPoint(selectedX, selectedY, cellSize, offsetX, offsetY);
            int r = (int) (cellSize * 0.44);

            // 外圈金光旋轉感標記
            g2.setColor(new Color(230, 126, 34));
            g2.setStroke(new BasicStroke(2.8f));
            g2.drawOval(sp.x - r, sp.y - r, 2 * r, 2 * r);
        }

        // 繪製合法著點
        for (Move move : legalMovesForSelected) {
            Point tp = toScreenPoint(move.toX, move.toY, cellSize, offsetX, offsetY);
            if (move.isCapture()) {
                // 吃子標記：紅色圓弧括角框住敵方棋子
                int r = (int) (cellSize * 0.45);
                g2.setColor(new Color(231, 76, 60, 220));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawOval(tp.x - r, tp.y - r, 2 * r, 2 * r);

                // 小內心圓
                g2.fillOval(tp.x - 4, tp.y - 4, 8, 8);
            } else {
                // 空位著點：柔和青綠色實心圓點
                int dotR = Math.max(5, cellSize / 8);
                g2.setColor(new Color(39, 174, 96, 200));
                g2.fillOval(tp.x - dotR, tp.y - dotR, 2 * dotR, 2 * dotR);
                g2.setColor(new Color(255, 255, 255, 220));
                g2.fillOval(tp.x - dotR / 2, tp.y - dotR / 2, dotR, dotR);
            }
        }
    }
}
