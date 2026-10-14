package xiangqi;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import java.util.ArrayList;
import java.util.List;

/**
 * 遊戲主控制器：管理對局流程、人機交互、後台 AI 運算及悔棋/提示功能
 */
public class GameController {

    public interface GameListener {
        void onTurnChanged(PieceColor turn, boolean isAiThinking);
        void onMoveMade(Move move, String notation, int moveIndex);
        void onCheck(PieceColor checkedColor);
        void onGameOver(String title, String message, PieceColor winner);
        void onBoardUpdated();
        void onStatusMessage(String message);
    }

    private Board board;
    private AIPlayer aiPlayer;
    private PieceColor playerColor = PieceColor.RED;
    private AIPlayer.Difficulty difficulty = AIPlayer.Difficulty.MEDIUM;

    public static final int MAX_UNDOS = 3;
    private int remainingUndos = MAX_UNDOS;

    private final List<Move> moveHistory = new ArrayList<>();
    private final List<Piece> redCaptured = new ArrayList<>();
    private final List<Piece> blackCaptured = new ArrayList<>();

    private boolean isGameOver = false;
    private boolean isAiThinking = false;
    private SwingWorker<Move, Void> aiWorker;
    private GameListener listener;

    public GameController() {
        this.board = new Board();
        this.aiPlayer = new AIPlayer(difficulty);
    }

    public void setListener(GameListener listener) {
        this.listener = listener;
    }

    public Board getBoard() {
        return board;
    }

    public PieceColor getPlayerColor() {
        return playerColor;
    }

    public AIPlayer.Difficulty getDifficulty() {
        return difficulty;
    }

    public boolean isAiThinking() {
        return isAiThinking;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public List<Move> getMoveHistory() {
        return moveHistory;
    }

    public List<Piece> getRedCaptured() {
        return redCaptured;
    }

    public List<Piece> getBlackCaptured() {
        return blackCaptured;
    }

    public int getRemainingUndos() {
        return remainingUndos;
    }

    /**
     * 開啟全新一局
     */
    public void startNewGame(AIPlayer.Difficulty diff, PieceColor playerColor) {
        cancelAiThinking();

        this.difficulty = diff;
        this.playerColor = playerColor;
        this.aiPlayer = new AIPlayer(diff);
        this.board = new Board();
        this.moveHistory.clear();
        this.redCaptured.clear();
        this.blackCaptured.clear();
        this.isGameOver = false;
        this.isAiThinking = false;
        this.remainingUndos = MAX_UNDOS;

        if (listener != null) {
            listener.onBoardUpdated();
            listener.onStatusMessage("新對局開始！難度：" + diff.getDisplayName() + "，玩家執" + playerColor.getFullName() + "（悔棋上限 3 次）");
            listener.onTurnChanged(board.getCurrentTurn(), false);
        }

        // 若玩家執黑，則紅方 (AI) 先手走棋
        if (playerColor == PieceColor.BLACK) {
            triggerAiMove();
        }
    }

    /**
     * 處理玩家下子
     */
    public void handlePlayerMove(Move move) {
        if (isGameOver || isAiThinking) return;
        if (board.getCurrentTurn() != playerColor) return;

        executeMove(move);

        if (!isGameOver && board.getCurrentTurn() != playerColor) {
            triggerAiMove();
        }
    }

    /**
     * 悔棋（每局限 3 次，撤銷電腦的上一步與玩家的上一步；若電腦正在思考則立即取消思考並撤銷玩家該步）
     */
    public boolean undo() {
        if (remainingUndos <= 0) {
            if (listener != null) listener.onStatusMessage("本局悔棋次數已用盡（每局限 3 次）！");
            return false;
        }
        if (moveHistory.isEmpty()) {
            if (listener != null) listener.onStatusMessage("目前尚未走子，無法悔棋。");
            return false;
        }

        // 若電腦正在思考中，先中斷 AI 計算，只須撤銷玩家的一步
        int stepsToUndo;
        if (isAiThinking) {
            cancelAiThinking();
            stepsToUndo = 1;
        } else {
            // 輪到玩家時悔棋：需要撤銷兩步（電腦的一步 + 玩家的一步）
            // 輪到電腦時（若有此狀態）：只需撤銷一步
            stepsToUndo = (board.getCurrentTurn() == playerColor) ? 2 : 1;
        }

        if (moveHistory.size() < stepsToUndo) {
            stepsToUndo = moveHistory.size();
        }

        for (int i = 0; i < stepsToUndo; i++) {
            if (moveHistory.isEmpty()) break;
            Move last = moveHistory.remove(moveHistory.size() - 1);
            board.undoMove(last);
            if (last.isCapture()) {
                if (last.capturedPiece.getColor() == PieceColor.RED) {
                    blackCaptured.remove(last.capturedPiece);
                } else {
                    redCaptured.remove(last.capturedPiece);
                }
            }
        }

        remainingUndos--;
        isGameOver = false;

        if (listener != null) {
            listener.onBoardUpdated();
            listener.onStatusMessage("已成功悔棋！本局剩餘悔棋次數：" + remainingUndos + " 次。");
            listener.onTurnChanged(board.getCurrentTurn(), false);
        }
        return true;
    }

    /**
     * 請求 AI 提供提示走法
     */
    public Move requestHint() {
        if (isGameOver || isAiThinking) return null;
        if (board.getCurrentTurn() != playerColor) return null;

        AIPlayer hintAI = new AIPlayer(AIPlayer.Difficulty.MEDIUM);
        return hintAI.findBestMove(new Board(board));
    }

    /**
     * 玩家認輸
     */
    public void resign() {
        if (isGameOver) return;
        cancelAiThinking();
        isGameOver = true;
        PieceColor winner = playerColor.opponent();
        if (listener != null) {
            listener.onGameOver("認輸提示", "您已認輸，" + winner.getFullName() + "獲勝！", winner);
        }
    }

    private void executeMove(Move move) {
        String notation = Notation.toChineseNotation(board, move);
        move.setNotation(notation);

        if (move.isCapture()) {
            if (move.capturedPiece.getColor() == PieceColor.RED) {
                blackCaptured.add(move.capturedPiece);
            } else {
                redCaptured.add(move.capturedPiece);
            }
        }

        board.makeMove(move);
        moveHistory.add(move);

        if (listener != null) {
            listener.onMoveMade(move, notation, moveHistory.size());
            listener.onBoardUpdated();
        }

        checkGameStateAfterMove();
    }

    private void checkGameStateAfterMove() {
        PieceColor nextTurn = board.getCurrentTurn();
        List<Move> legalMoves = board.generateLegalMoves(nextTurn);
        boolean inCheck = board.isCheck(nextTurn);

        if (inCheck && listener != null) {
            listener.onCheck(nextTurn);
        }

        if (legalMoves.isEmpty()) {
            isGameOver = true;
            PieceColor winner = nextTurn.opponent();
            String reason = inCheck ? "將死！" : "困斃（無子可走）！";
            String title = (winner == playerColor) ? "恭喜獲勝！" : "遺憾告負";
            String message = nextTurn.getFullName() + "被" + reason + " " + winner.getFullName() + "取得勝利！";

            if (listener != null) {
                listener.onGameOver(title, message, winner);
            }
        } else {
            if (listener != null) {
                listener.onTurnChanged(nextTurn, false);
            }
        }
    }

    private void triggerAiMove() {
        if (isGameOver) return;
        isAiThinking = true;

        if (listener != null) {
            listener.onTurnChanged(board.getCurrentTurn(), true);
            listener.onStatusMessage("電腦（" + difficulty.getDisplayName() + "）正在深思熟慮...");
        }

        // 深拷貝當前棋盤避免線程衝突
        final Board searchBoard = new Board(board);
        final AIPlayer currentAi = this.aiPlayer;

        aiWorker = new SwingWorker<Move, Void>() {
            @Override
            protected Move doInBackground() {
                // 為避免電腦瞬間出子過於突兀，增加極短的人性化延遲 (200ms)
                long startTime = System.currentTimeMillis();
                Move bestMove = currentAi.findBestMove(searchBoard);
                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed < 300) {
                    try {
                        Thread.sleep(300 - elapsed);
                    } catch (InterruptedException ignored) {}
                }
                return bestMove;
            }

            @Override
            protected void done() {
                if (isCancelled()) return;
                try {
                    Move bestMove = get();
                    isAiThinking = false;
                    if (bestMove != null && !isGameOver) {
                        // 在真實棋盤上重新包裝走法
                        Piece moved = board.getPiece(bestMove.fromX, bestMove.fromY);
                        Piece captured = board.getPiece(bestMove.toX, bestMove.toY);
                        Move realMove = new Move(bestMove.fromX, bestMove.fromY, bestMove.toX, bestMove.toY, moved, captured);
                        executeMove(realMove);
                    }
                } catch (Exception e) {
                    isAiThinking = false;
                    e.printStackTrace();
                }
            }
        };

        aiWorker.execute();
    }

    public void cancelAiThinking() {
        if (aiWorker != null && !aiWorker.isDone()) {
            if (aiPlayer != null) {
                aiPlayer.cancel();
            }
            aiWorker.cancel(true);
        }
        isAiThinking = false;
    }
}
