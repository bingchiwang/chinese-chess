package xiangqi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * AI 對手邏輯，支援三種難度（簡單、中等、困難）
 * 採用 Alpha-Beta 剪枝、靜態搜索 (Quiescence Search) 與 MVV-LVA 走法排序
 */
public class AIPlayer {

    public enum Difficulty {
        EASY("簡單 (初學者)", "反應迅速，偶有疏漏，適合入門學習"),
        MEDIUM("中等 (業餘進階)", "具備戰術攻防能力，算力適中"),
        HARD("困難 (棋力大師)", "深層搜索與靜態攻殺，具備高水平棋力");

        private final String displayName;
        private final String description;

        Difficulty(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private final Difficulty difficulty;
    private final Random random = new Random();
    private volatile boolean cancelled = false;

    public AIPlayer(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void cancel() {
        this.cancelled = true;
    }

    /**
     * 根據當前棋盤狀態計算出最佳走法
     */
    public Move findBestMove(Board board) {
        this.cancelled = false;
        PieceColor aiColor = board.getCurrentTurn();
        List<Move> legalMoves = board.generateLegalMoves(aiColor);

        if (legalMoves.isEmpty()) {
            return null;
        }

        // 簡單難度：深度 1~2 搜索 + 偶爾隨機擾動
        if (difficulty == Difficulty.EASY) {
            return findEasyMove(board, legalMoves, aiColor);
        }

        // 中等難度：深度 3 Alpha-Beta 剪枝
        if (difficulty == Difficulty.MEDIUM) {
            return findAlphaBetaMove(board, legalMoves, aiColor, 3, false);
        }

        // 困難難度：深度 4 + 靜態搜索
        return findAlphaBetaMove(board, legalMoves, aiColor, 4, true);
    }

    private Move findEasyMove(Board board, List<Move> legalMoves, PieceColor aiColor) {
        // 先排序走法
        sortMoves(legalMoves);

        Move bestMove = legalMoves.get(0);
        int bestScore = Integer.MIN_VALUE;

        for (Move move : legalMoves) {
            if (cancelled) return null;

            board.makeMove(move);
            // 深度 1 估值，並加入少許隨機誤差 (±35分)
            int score = (aiColor == PieceColor.RED ? 1 : -1) * Evaluation.evaluate(board);
            score += random.nextInt(71) - 35;
            board.undoMove(move);

            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
        }

        return bestMove;
    }

    private Move findAlphaBetaMove(Board board, List<Move> legalMoves, PieceColor aiColor, int maxDepth, boolean useQuiescence) {
        sortMoves(legalMoves);

        Move bestMove = legalMoves.get(0);
        int alpha = -1000000;
        int beta = 1000000;

        List<Move> candidateMoves = new ArrayList<>();

        for (Move move : legalMoves) {
            if (cancelled) return null;

            board.makeMove(move);
            int score = -negamax(board, maxDepth - 1, -beta, -alpha, aiColor.opponent(), maxDepth, useQuiescence);
            board.undoMove(move);

            if (score > alpha) {
                alpha = score;
                bestMove = move;
                candidateMoves.clear();
                candidateMoves.add(move);
            } else if (score == alpha) {
                candidateMoves.add(move);
            }
        }

        // 若有多個評分相同的最優走法，隨機挑選一個增加走子豐富度
        if (!candidateMoves.isEmpty()) {
            bestMove = candidateMoves.get(random.nextInt(candidateMoves.size()));
        }

        return bestMove;
    }

    private int negamax(Board board, int depth, int alpha, int beta, PieceColor color, int maxDepth, boolean useQuiescence) {
        if (cancelled) return 0;

        if (depth <= 0) {
            if (useQuiescence) {
                return quiescence(board, alpha, beta, color, 2);
            } else {
                return (color == PieceColor.RED ? 1 : -1) * Evaluation.evaluate(board);
            }
        }

        List<Move> legalMoves = board.generateLegalMoves(color);
        if (legalMoves.isEmpty()) {
            // 無子可走：負局（將死或困斃）
            return -90000 + (maxDepth - depth);
        }

        sortMoves(legalMoves);

        for (Move move : legalMoves) {
            board.makeMove(move);
            int score = -negamax(board, depth - 1, -beta, -alpha, color.opponent(), maxDepth, useQuiescence);
            board.undoMove(move);

            if (cancelled) return 0;

            if (score >= beta) {
                return beta; // 剪枝
            }
            if (score > alpha) {
                alpha = score;
            }
        }

        return alpha;
    }

    /**
     * 靜態搜索：避免地平線效應，只對吃子走法進行深搜
     */
    private int quiescence(Board board, int alpha, int beta, PieceColor color, int qDepth) {
        if (cancelled) return 0;

        int standPat = (color == PieceColor.RED ? 1 : -1) * Evaluation.evaluate(board);
        if (standPat >= beta) return beta;
        if (standPat > alpha) alpha = standPat;
        if (qDepth <= 0) return alpha;

        List<Move> moves = board.generateLegalMoves(color);
        // 只保留吃子走法
        moves.removeIf(m -> !m.isCapture());
        sortMoves(moves);

        for (Move move : moves) {
            board.makeMove(move);
            int score = -quiescence(board, -beta, -alpha, color.opponent(), qDepth - 1);
            board.undoMove(move);

            if (cancelled) return 0;

            if (score >= beta) return beta;
            if (score > alpha) alpha = score;
        }

        return alpha;
    }

    /**
     * MVV-LVA (Most Valuable Victim - Least Valuable Attacker) 走法排序
     * 吃子價值高、攻擊子價值低的走法排在前面，顯著提升剪枝效率
     */
    private void sortMoves(List<Move> moves) {
        moves.sort(new Comparator<Move>() {
            @Override
            public int compare(Move m1, Move m2) {
                int score1 = getMoveOrderScore(m1);
                int score2 = getMoveOrderScore(m2);
                return Integer.compare(score2, score1);
            }
        });
    }

    private int getMoveOrderScore(Move move) {
        if (move.isCapture()) {
            int victimVal = move.capturedPiece.getType().getBaseValue();
            int attackerVal = move.movedPiece.getType().getBaseValue();
            return victimVal * 10 - attackerVal + 1000;
        }
        return 0;
    }
}
