package xiangqi;

import java.util.List;

/**
 * 象棋規則與 AI 演算法單元自測程式
 */
public class XiangqiTest {

    public static void main(String[] args) {
        System.out.println("=== 開始執行中國象棋核心邏輯與規則驗證 ===");

        testInitialBoard();
        testHorseLegRule();
        testElephantEyeAndRiverRule();
        testCannonJumpRule();
        testFlyingGeneralRule();
        testSoldierRules();
        testCheckDetection();
        testNotation();
        testAISearch();

        System.out.println("=== 所有規則與演算法測試全部通過！ ===");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("測試失敗: " + message);
        }
        System.out.println("  [通過] " + message);
    }

    private static void testInitialBoard() {
        System.out.println("\n--- 測試 1: 初始棋盤棋子佈局 ---");
        Board board = new Board();
        assertTrue(board.getCurrentTurn() == PieceColor.RED, "開局先手方為紅方");

        int redCount = 0;
        int blackCount = 0;
        for (int x = 0; x < Board.COLS; x++) {
            for (int y = 0; y < Board.ROWS; y++) {
                Piece p = board.getPiece(x, y);
                if (p != null) {
                    if (p.getColor() == PieceColor.RED) redCount++;
                    else blackCount++;
                }
            }
        }
        assertTrue(redCount == 16, "紅方棋子數為 16");
        assertTrue(blackCount == 16, "黑方棋子數為 16");

        // 初始紅方合法走法數量（開局炮和兵、馬共有很多走法）
        List<Move> legalMoves = board.generateLegalMoves(PieceColor.RED);
        assertTrue(legalMoves.size() == 44, "紅方初始合法著法數應為 44 步 (實得: " + legalMoves.size() + ")");
    }

    private static void testHorseLegRule() {
        System.out.println("\n--- 測試 2: 馬走日與蹩馬腿規則 ---");
        Board board = new Board();
        // 初始盤面紅傌在 (1, 9)，(1, 8) 為空，但 (1, 7) 有紅炮，(0, 9) 有俥，(2, 9) 有相
        // (1, 9) 跳 (2, 7) 或 (0, 7) 拐彎腿在 (1, 8)，(1, 8) 為空所以可以跳！
        List<Move> moves = board.getLegalMovesForSquare(1, 9);
        boolean canJumpTo27 = moves.stream().anyMatch(m -> m.toX == 2 && m.toY == 7);
        boolean canJumpTo07 = moves.stream().anyMatch(m -> m.toX == 0 && m.toY == 7);
        assertTrue(canJumpTo27 && canJumpTo07, "紅傌初起跳至 (2,7) 或 (0,7) 均合法");

        // 在 (1, 8) 放置棋子阻礙馬腿
        board.setPiece(1, 8, new Piece(PieceType.SOLDIER, PieceColor.RED));
        moves = board.getLegalMovesForSquare(1, 9);
        boolean blocked = moves.stream().noneMatch(m -> (m.toX == 2 && m.toY == 7) || (m.toX == 0 && m.toY == 7));
        assertTrue(blocked, "(1, 8) 有子時，傌向上的日字走法成功被蹩馬腿阻擋");
    }

    private static void testElephantEyeAndRiverRule() {
        System.out.println("\n--- 測試 3: 相走田、塞象眼與不能過河 ---");
        Board board = new Board();
        // 紅相在 (2, 9)，田字可跳 (4, 7) 或 (0, 7)，象眼在 (3, 8) 和 (1, 8) 均為空
        List<Move> moves = board.getLegalMovesForSquare(2, 9);
        boolean canJump47 = moves.stream().anyMatch(m -> m.toX == 4 && m.toY == 7);
        assertTrue(canJump47, "紅相可正常走田字到 (4, 7)");

        // 塞象眼 (3, 8)
        board.setPiece(3, 8, new Piece(PieceType.SOLDIER, PieceColor.RED));
        moves = board.getLegalMovesForSquare(2, 9);
        boolean eyeBlocked = moves.stream().noneMatch(m -> m.toX == 4 && m.toY == 7);
        assertTrue(eyeBlocked, "(3, 8) 有子時，塞象眼成功阻擋相飛 (4, 7)");

        // 測試過河限制：紅相不能走至 y < 5
        board = new Board();
        board.setPiece(2, 5, new Piece(PieceType.ELEPHANT, PieceColor.RED)); // 放在河沿
        moves = board.getLegalMovesForSquare(2, 5);
        boolean noCrossRiver = moves.stream().allMatch(m -> m.toY >= 5);
        assertTrue(noCrossRiver, "紅相不可跨越楚河漢界 (y < 5)");
    }

    private static void testCannonJumpRule() {
        System.out.println("\n--- 測試 4: 炮隔子打與平移 ---");
        Board board = new Board();
        // 紅炮在 (1, 7)，前面 (1, 6) 無子，(1, 2) 為黑砲，(1, 0) 為黑馬
        // 紅炮可以直接吃黑馬嗎？中間有黑砲 (1, 2)，剛好一個炮架！
        List<Move> moves = board.getLegalMovesForSquare(1, 7);
        boolean canCaptureHorse = moves.stream().anyMatch(m -> m.toX == 1 && m.toY == 0 && m.isCapture());
        assertTrue(canCaptureHorse, "紅炮隔著黑砲 (1, 2) 一子炮架，可合法吃掉 (1, 0) 的黑馬");

        // 紅炮不可吃 (1, 2) 的黑砲（因為中間沒有炮架）
        boolean cannotDirectCapture = moves.stream().noneMatch(m -> m.toX == 1 && m.toY == 2 && m.isCapture());
        assertTrue(cannotDirectCapture, "中間無炮架時，炮不可吃子");
    }

    private static void testFlyingGeneralRule() {
        System.out.println("\n--- 測試 5: 雙方將帥不可照面 (飛將/對面笑) ---");
        Board board = new Board();
        // 清空中路 (x=4) 上的所有子：(4, 3) 卒, (4, 6) 兵
        board.setPiece(4, 3, null);
        board.setPiece(4, 6, null);

        // 此時黑將在 (4, 0)，紅帥在 (4, 9)，中路全空！
        assertTrue(board.areKingsFacing(), "兩將帥同列無子時，判定為飛將照面");

        // 測試此時若紅方有任何走法不能解除照面，該走法應被判為非法
        List<Move> legalMoves = board.generateLegalMoves(PieceColor.RED);
        for (Move m : legalMoves) {
            board.makeMove(m);
            assertTrue(!board.areKingsFacing(), "任何合法走法完成後，將帥均不可處於照面狀態！");
            board.undoMove(m);
        }
    }

    private static void testSoldierRules() {
        System.out.println("\n--- 測試 6: 兵/卒 過河前與過河後 ---");
        Board board = new Board();
        // 紅兵在 (4, 6)，未過河 (y >= 5)，只能向前 (4, 5)
        List<Move> moves = board.getLegalMovesForSquare(4, 6);
        assertTrue(moves.size() == 1 && moves.get(0).toX == 4 && moves.get(0).toY == 5, "紅兵未過河前僅能向前一步");

        // 將紅兵移至過河後 (4, 4)
        board.setPiece(4, 6, null);
        board.setPiece(4, 4, new Piece(PieceType.SOLDIER, PieceColor.RED));
        moves = board.getLegalMovesForSquare(4, 4);
        // 可向前 (4, 3)，可左 (3, 4)，可右 (5, 4)，不可退 (4, 5)
        boolean hasForward = moves.stream().anyMatch(m -> m.toX == 4 && m.toY == 3);
        boolean hasLeft = moves.stream().anyMatch(m -> m.toX == 3 && m.toY == 4);
        boolean hasRight = moves.stream().anyMatch(m -> m.toX == 5 && m.toY == 4);
        boolean noBack = moves.stream().noneMatch(m -> m.toY > 4);
        assertTrue(hasForward && hasLeft && hasRight && noBack, "紅兵過河後可向前及左右橫走，絕不可後退");
    }

    private static void testCheckDetection() {
        System.out.println("\n--- 測試 7: 將軍判定 ---");
        Board board = new Board();
        // 初始狀態沒有任何一方處於將軍
        assertTrue(!board.isCheck(PieceColor.RED), "開局紅方未被將軍");
        assertTrue(!board.isCheck(PieceColor.BLACK), "開局黑方未被將軍");

        // 將紅車放到 (4, 1)，此時黑將在 (4, 0)
        board.setPiece(4, 1, new Piece(PieceType.CHARIOT, PieceColor.RED));
        assertTrue(board.isCheck(PieceColor.BLACK), "紅車逼近黑將，黑方處於被將軍狀態");
    }

    private static void testNotation() {
        System.out.println("\n--- 測試 8: 中文記譜法 ---");
        Board board = new Board();
        // 當頭炮：紅方 (1, 7) 炮 平至 (4, 7)
        // 紅方視角：x=1 為二路 (9 - 1 = 8 ? 錯，右邊 x=8 是一路，x=7 是二路，x=1 是八路)
        // (7, 7) 炮是二路炮！二路炮平五路：(7, 7) -> (4, 7) 是 "炮二平五"
        Piece p2 = board.getPiece(7, 7);
        Move movePao = new Move(7, 7, 4, 7, p2, null);
        String notPao = Notation.toChineseNotation(board, movePao);
        assertTrue("炮二平五".equals(notPao), "二路炮平五應記為 '炮二平五' (實得: " + notPao + ")");

        // 馬八進七：紅方 x=1 是八路，(1, 9) 馬跳 (2, 7) 為七路，前進
        Piece m8 = board.getPiece(1, 9);
        Move moveMa = new Move(1, 9, 2, 7, m8, null);
        String notMa = Notation.toChineseNotation(board, moveMa);
        assertTrue("傌八進七".equals(notMa), "八路傌進七路應記為 '傌八進七' (實得: " + notMa + ")");
    }

    private static void testAISearch() {
        System.out.println("\n--- 測試 9: AI 搜索與難易度測試 ---");
        Board board = new Board();

        AIPlayer easyAI = new AIPlayer(AIPlayer.Difficulty.EASY);
        Move easyMove = easyAI.findBestMove(board);
        assertTrue(easyMove != null, "簡單難度 AI 順利走子: " + easyMove);

        AIPlayer mediumAI = new AIPlayer(AIPlayer.Difficulty.MEDIUM);
        Move medMove = mediumAI.findBestMove(board);
        assertTrue(medMove != null, "中等難度 AI 順利走子: " + medMove);

        AIPlayer hardAI = new AIPlayer(AIPlayer.Difficulty.HARD);
        Move hardMove = hardAI.findBestMove(board);
        assertTrue(hardMove != null, "困難難度 AI 順利走子: " + hardMove);
    }
}
