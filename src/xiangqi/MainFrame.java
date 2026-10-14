package xiangqi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

/**
 * 象棋遊戲主視窗介面
 * 支援更換棋盤樣式（木紋、大理石、純色等）、3 次悔棋限制與書法字體致中展示
 */
public class MainFrame extends JFrame implements GameController.GameListener, ChessBoardPanel.MoveListener {

    private final GameController controller;
    private final ChessBoardPanel boardPanel;

    // 介面元件
    private JComboBox<AIPlayer.Difficulty> difficultyCombo;
    private JComboBox<String> sideCombo;
    private JComboBox<BoardTheme> themeCombo;
    private JButton newGameBtn;
    private JButton undoBtn;
    private JButton hintBtn;
    private JButton resignBtn;

    private JPanel centerWrapper;

    private JLabel turnStatusLabel;
    private JLabel checkStatusLabel;
    private JLabel difficultyInfoLabel;
    private JLabel moveCountLabel;
    private JLabel undoCountLabel;

    private JPanel capturedRedPanel;
    private JPanel capturedBlackPanel;

    private DefaultTableModel historyTableModel;
    private JTable historyTable;
    private JScrollPane historyScrollPane;

    private JLabel bottomStatusLabel;

    public MainFrame() {
        super("中國象棋 - 人機對弈系統");
        this.controller = new GameController();
        this.boardPanel = new ChessBoardPanel(controller.getBoard());

        controller.setListener(this);
        boardPanel.setMoveListener(this);

        initUI();

        // 啟動默認新局
        controller.startNewGame(AIPlayer.Difficulty.MEDIUM, PieceColor.RED);
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int res = JOptionPane.showConfirmDialog(
                    MainFrame.this,
                    "確定要退出象棋遊戲嗎？",
                    "退出確認",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                );
                if (res == JOptionPane.YES_OPTION) {
                    controller.cancelAiThinking();
                    dispose();
                    System.exit(0);
                }
            }
        });

        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 240, 230));

        // 頂部控制列
        add(createTopToolBar(), BorderLayout.NORTH);

        // 中間棋盤容器
        centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(boardPanel.getTheme().wrapperBg);
        centerWrapper.setBorder(new EmptyBorder(10, 10, 10, 10));
        centerWrapper.add(boardPanel);
        add(centerWrapper, BorderLayout.CENTER);

        // 右側資訊面板
        add(createRightPanel(), BorderLayout.EAST);

        // 底部狀態列
        add(createBottomStatusBar(), BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(960, 730));
        setLocationRelativeTo(null);
    }

    private JPanel createTopToolBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        bar.setBackground(new Color(60, 42, 28));
        bar.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(120, 80, 45)));

        // 1. 棋盤樣式選擇
        JLabel themeLbl = new JLabel("樣式：");
        themeLbl.setForeground(Color.WHITE);
        themeLbl.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        bar.add(themeLbl);

        themeCombo = new JComboBox<>(BoardTheme.values());
        themeCombo.setSelectedItem(BoardTheme.WOOD);
        themeCombo.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        themeCombo.setToolTipText("切換棋盤材質風格（經典木紋、大理石、簡約純色、翡翠玉石等）");
        themeCombo.addActionListener(e -> {
            BoardTheme theme = (BoardTheme) themeCombo.getSelectedItem();
            if (theme != null) {
                boardPanel.setTheme(theme);
                if (centerWrapper != null) {
                    centerWrapper.setBackground(theme.wrapperBg);
                    centerWrapper.repaint();
                }
                bottomStatusLabel.setText("已切換棋盤樣式為：" + theme.getDisplayName());
            }
        });
        bar.add(themeCombo);

        // 2. 難易度選擇
        JLabel diffLbl = new JLabel("難度：");
        diffLbl.setForeground(Color.WHITE);
        diffLbl.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        bar.add(diffLbl);

        difficultyCombo = new JComboBox<>(AIPlayer.Difficulty.values());
        difficultyCombo.setSelectedItem(AIPlayer.Difficulty.MEDIUM);
        difficultyCombo.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        difficultyCombo.setToolTipText("選擇電腦 AI 的棋力水準");
        bar.add(difficultyCombo);

        // 3. 執子選擇
        JLabel sideLbl = new JLabel("執子：");
        sideLbl.setForeground(Color.WHITE);
        sideLbl.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
        bar.add(sideLbl);

        sideCombo = new JComboBox<>(new String[]{"執紅 (先手)", "執黑 (後手)"});
        sideCombo.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        sideCombo.setToolTipText("紅方先走，黑方後走");
        bar.add(sideCombo);

        // 4. 按鈕組
        newGameBtn = createStyledButton("開始新局", new Color(46, 139, 87), Color.WHITE);
        newGameBtn.setToolTipText("依據所選難度與執子方重新開始對局");
        newGameBtn.addActionListener(e -> onNewGameClicked());
        bar.add(newGameBtn);

        undoBtn = createStyledButton("悔棋 (3/3)", new Color(180, 100, 30), Color.WHITE);
        undoBtn.setToolTipText("撤銷上一輪走子（每局上限 3 次）");
        undoBtn.addActionListener(e -> controller.undo());
        bar.add(undoBtn);

        hintBtn = createStyledButton("提示", new Color(70, 130, 180), Color.WHITE);
        hintBtn.setToolTipText("請 AI 協助分析並給出一步推薦走法");
        hintBtn.addActionListener(e -> onHintClicked());
        bar.add(hintBtn);

        resignBtn = createStyledButton("認輸", new Color(178, 34, 34), Color.WHITE);
        resignBtn.setToolTipText("向電腦投降認輸");
        resignBtn.addActionListener(e -> onResignClicked());
        bar.add(resignBtn);

        JButton ruleBtn = createStyledButton("說明", new Color(105, 105, 105), Color.WHITE);
        ruleBtn.addActionListener(e -> showRuleHelpDialog());
        bar.add(ruleBtn);

        return bar;
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setPreferredSize(new Dimension(300, 640));
        rightPanel.setBackground(new Color(248, 244, 237));
        rightPanel.setBorder(new EmptyBorder(12, 10, 12, 14));

        // 1. 回合狀態卡片
        JPanel statusCard = new JPanel(new GridLayout(5, 1, 3, 3));
        statusCard.setBackground(Color.WHITE);
        statusCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(210, 195, 175), 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));

        turnStatusLabel = new JLabel("輪到：紅方走棋", JLabel.CENTER);
        turnStatusLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 17));
        turnStatusLabel.setForeground(new Color(180, 20, 20));
        statusCard.add(turnStatusLabel);

        checkStatusLabel = new JLabel("", JLabel.CENTER);
        checkStatusLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        checkStatusLabel.setForeground(new Color(220, 20, 20));
        statusCard.add(checkStatusLabel);

        difficultyInfoLabel = new JLabel("當前難度：中等 (業餘進階)", JLabel.CENTER);
        difficultyInfoLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        difficultyInfoLabel.setForeground(new Color(80, 80, 80));
        statusCard.add(difficultyInfoLabel);

        undoCountLabel = new JLabel("悔棋機會：剩餘 3 次 (每局限 3 次)", JLabel.CENTER);
        undoCountLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        undoCountLabel.setForeground(new Color(180, 100, 30));
        statusCard.add(undoCountLabel);

        moveCountLabel = new JLabel("已走步數：0 步", JLabel.CENTER);
        moveCountLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        moveCountLabel.setForeground(new Color(80, 80, 80));
        statusCard.add(moveCountLabel);

        rightPanel.add(statusCard);
        rightPanel.add(Box.createVerticalStrut(10));

        // 2. 吃子展示區
        JPanel capturedBox = new JPanel(new GridLayout(2, 1, 4, 4));
        capturedBox.setBackground(Color.WHITE);
        capturedBox.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(210, 195, 175), 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));

        capturedRedPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        capturedRedPanel.setBackground(Color.WHITE);
        capturedRedPanel.add(new JLabel("紅方得子: "));
        capturedBox.add(capturedRedPanel);

        capturedBlackPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        capturedBlackPanel.setBackground(Color.WHITE);
        capturedBlackPanel.add(new JLabel("黑方得子: "));
        capturedBox.add(capturedBlackPanel);

        rightPanel.add(capturedBox);
        rightPanel.add(Box.createVerticalStrut(10));

        // 3. 著法記錄表格
        JLabel historyTitle = new JLabel("對局著法記錄 (棋譜)");
        historyTitle.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        historyTitle.setForeground(new Color(70, 45, 25));
        historyTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        rightPanel.add(historyTitle);
        rightPanel.add(Box.createVerticalStrut(5));

        String[] columnNames = {"回合", "紅方走法", "黑方走法"};
        historyTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        historyTable = new JTable(historyTableModel);
        historyTable.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        historyTable.setRowHeight(22);
        historyTable.getTableHeader().setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        historyTable.getTableHeader().setBackground(new Color(230, 220, 205));
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(45);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(110);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(110);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        historyTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        historyScrollPane = new JScrollPane(historyTable);
        historyScrollPane.setBorder(new LineBorder(new Color(210, 195, 175), 1));
        historyScrollPane.getViewport().setBackground(Color.WHITE);
        rightPanel.add(historyScrollPane);

        return rightPanel;
    }

    private JPanel createBottomStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 6));
        bar.setBackground(new Color(235, 225, 212));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 185, 165)));

        bottomStatusLabel = new JLabel("歡迎來到中國象棋！請點擊棋子進行移動。");
        bottomStatusLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        bottomStatusLabel.setForeground(new Color(60, 60, 60));
        bar.add(bottomStatusLabel);

        return bar;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(bg.darker(), 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        return btn;
    }

    private void onNewGameClicked() {
        if (!controller.isGameOver() && !controller.getMoveHistory().isEmpty()) {
            int res = JOptionPane.showConfirmDialog(
                this,
                "當前對局仍在進行中，確定要開啟新局嗎？",
                "確認開局",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (res != JOptionPane.YES_OPTION) return;
        }

        AIPlayer.Difficulty diff = (AIPlayer.Difficulty) difficultyCombo.getSelectedItem();
        PieceColor playerColor = (sideCombo.getSelectedIndex() == 0) ? PieceColor.RED : PieceColor.BLACK;

        // 若玩家執黑，則將棋盤翻轉為黑方視角（黑方在下）
        boardPanel.setFlipped(playerColor == PieceColor.BLACK);
        boardPanel.setLastMove(null);

        // 清空歷史記錄表格
        historyTableModel.setRowCount(0);
        checkStatusLabel.setText("");

        controller.startNewGame(diff, playerColor);
        difficultyInfoLabel.setText("當前難度：" + diff.getDisplayName());
        updateUndoUI();
    }

    private void onHintClicked() {
        if (controller.isAiThinking() || controller.isGameOver()) return;
        if (controller.getBoard().getCurrentTurn() != controller.getPlayerColor()) {
            bottomStatusLabel.setText("提示：目前不是您的回合！");
            return;
        }

        bottomStatusLabel.setText("AI 正在為您計算推薦走法...");
        Move hint = controller.requestHint();
        if (hint != null) {
            boardPanel.setHintMove(hint);
            String notation = Notation.toChineseNotation(controller.getBoard(), hint);
            bottomStatusLabel.setText("推薦走法：" + notation + "（已在棋盤以綠色箭頭標示）");
        } else {
            bottomStatusLabel.setText("暫無推薦走法。");
        }
    }

    private void onResignClicked() {
        if (controller.isGameOver() || controller.isAiThinking()) return;
        int res = JOptionPane.showConfirmDialog(
            this,
            "確定要認輸本局嗎？",
            "認輸確認",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (res == JOptionPane.YES_OPTION) {
            controller.resign();
        }
    }

    private void updateUndoUI() {
        int rem = controller.getRemainingUndos();
        undoBtn.setText("悔棋 (" + rem + "/3)");
        undoBtn.setEnabled(rem > 0 && !controller.isAiThinking() && !controller.isGameOver());
        if (rem > 0) {
            undoCountLabel.setText("悔棋機會：剩餘 " + rem + " 次");
            undoCountLabel.setForeground(new Color(180, 100, 30));
        } else {
            undoCountLabel.setText("悔棋機會：已用盡 (0/3)");
            undoCountLabel.setForeground(new Color(160, 40, 40));
        }
    }

    private void rebuildHistoryTable() {
        historyTableModel.setRowCount(0);
        List<Move> history = controller.getMoveHistory();
        for (int i = 0; i < history.size(); i++) {
            Move m = history.get(i);
            int round = (i / 2) + 1;
            boolean isRed = m.movedPiece.getColor() == PieceColor.RED;
            if (isRed) {
                historyTableModel.addRow(new Object[]{round, m.getNotation(), ""});
            } else {
                if (historyTableModel.getRowCount() == 0) {
                    historyTableModel.addRow(new Object[]{1, "", m.getNotation()});
                } else {
                    int lastRow = historyTableModel.getRowCount() - 1;
                    historyTableModel.setValueAt(m.getNotation(), lastRow, 2);
                }
            }
        }
    }

    private void showRuleHelpDialog() {
        String rules = "<html><body style='width: 390px; font-family: Microsoft YaHei; font-size: 11pt; padding: 6px;'>"
            + "<h2 style='color: #8B0000; text-align: center; margin-bottom: 6px;'>中國象棋 走法與規則</h2>"
            + "<p><b>【將/帥】</b>：九宮內直橫走一格；雙方將帥不可同列照面（飛將對面笑）。</p>"
            + "<p><b>【士/仕】</b>：九宮內沿斜線走一格。</p>"
            + "<p><b>【象/相】</b>：走『田』字，不可過河；若田字中心有子（塞象眼）則不可走。</p>"
            + "<p><b>【馬/傌】</b>：走『日』字；若前進方向緊鄰有子（蹩馬腿）則不可走。</p>"
            + "<p><b>【車/俥】</b>：直線任意格移動，無阻擋即可走子或吃子。</p>"
            + "<p><b>【炮/砲】</b>：直線移動無阻擋；吃子時必須隔一個任意棋子（炮架）。</p>"
            + "<p><b>【兵/卒】</b>：過河前只能向前一步；過河後可向前或左右橫走一步，不可後退。</p>"
            + "<hr>"
            + "<p><b>特殊功能與機制：</b><br>"
            + "• <b>悔棋次數限制</b>：每局提供 <b>3 次悔棋機會</b>，用盡後將無法再悔棋。<br>"
            + "• <b>棋盤樣式切換</b>：提供經典木紋、典雅大理石、簡約純色、翡翠玉石、復古羊皮等多款主題。<br>"
            + "• <b>書法棋子</b>：採用傳統書法標楷體，文字經幾何輪廓絕對水平與垂直致中。"
            + "</p></body></html>";

        JOptionPane.showMessageDialog(this, rules, "象棋棋規與特色說明", JOptionPane.INFORMATION_MESSAGE);
    }

    // ========== ChessBoardPanel.MoveListener 實作 ==========
    @Override
    public void onPlayerMove(Move move) {
        controller.handlePlayerMove(move);
    }

    // ========== GameController.GameListener 實作 ==========
    @Override
    public void onTurnChanged(PieceColor turn, boolean isAiThinking) {
        boardPanel.setInteractive(!isAiThinking && !controller.isGameOver());

        if (isAiThinking) {
            turnStatusLabel.setText("電腦思考中...");
            turnStatusLabel.setForeground(new Color(30, 144, 255));
        } else {
            boolean isPlayer = (turn == controller.getPlayerColor());
            String role = isPlayer ? " (玩家)" : " (電腦)";
            turnStatusLabel.setText("輪到：" + turn.getFullName() + role);
            if (turn == PieceColor.RED) {
                turnStatusLabel.setForeground(new Color(190, 25, 25));
            } else {
                turnStatusLabel.setForeground(new Color(30, 30, 30));
            }
        }

        // 更新步數與悔棋狀態
        moveCountLabel.setText("已走步數：" + controller.getMoveHistory().size() + " 步");
        updateUndoUI();

        // 若無將軍，清空將軍文字
        if (!controller.getBoard().isCheck(turn)) {
            checkStatusLabel.setText("");
        }
    }

    @Override
    public void onMoveMade(Move move, String notation, int moveIndex) {
        boardPanel.setLastMove(move);

        // 更新棋譜表格
        int round = (moveIndex + 1) / 2;
        boolean isRed = move.movedPiece.getColor() == PieceColor.RED;

        if (isRed) {
            historyTableModel.addRow(new Object[]{round, notation, ""});
        } else {
            if (historyTableModel.getRowCount() == 0) {
                historyTableModel.addRow(new Object[]{1, "", notation});
            } else {
                int lastRow = historyTableModel.getRowCount() - 1;
                historyTableModel.setValueAt(notation, lastRow, 2);
            }
        }

        // 自動滾動到底部
        SwingUtilities.invokeLater(() -> {
            int rowCount = historyTable.getRowCount();
            if (rowCount > 0) {
                historyTable.scrollRectToVisible(historyTable.getCellRect(rowCount - 1, 0, true));
            }
        });

        // 更新吃子展示
        updateCapturedPiecesUI();
        updateUndoUI();

        bottomStatusLabel.setText((move.movedPiece.getColor() == PieceColor.RED ? "紅方：" : "黑方：") + notation);
    }

    private void updateCapturedPiecesUI() {
        capturedRedPanel.removeAll();
        capturedRedPanel.add(new JLabel("紅方得子: "));
        for (Piece p : controller.getRedCaptured()) {
            JLabel lbl = new JLabel(p.getCharacter());
            lbl.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
            lbl.setForeground(new Color(20, 20, 20));
            capturedRedPanel.add(lbl);
        }

        capturedBlackPanel.removeAll();
        capturedBlackPanel.add(new JLabel("黑方得子: "));
        for (Piece p : controller.getBlackCaptured()) {
            JLabel lbl = new JLabel(p.getCharacter());
            lbl.setFont(new Font("Microsoft YaHei", Font.BOLD, 13));
            lbl.setForeground(new Color(180, 20, 20));
            capturedBlackPanel.add(lbl);
        }

        capturedRedPanel.revalidate();
        capturedRedPanel.repaint();
        capturedBlackPanel.revalidate();
        capturedBlackPanel.repaint();
    }

    @Override
    public void onCheck(PieceColor checkedColor) {
        checkStatusLabel.setText("⚠️ " + checkedColor.getFullName() + "被【將軍】！");
        bottomStatusLabel.setText("⚠️ 注意！" + checkedColor.getFullName() + "正在被將軍！");
        // 發出溫和提示音
        Toolkit.getDefaultToolkit().beep();
    }

    @Override
    public void onGameOver(String title, String message, PieceColor winner) {
        boardPanel.setInteractive(false);
        turnStatusLabel.setText("對局結束");
        turnStatusLabel.setForeground(new Color(100, 100, 100));
        checkStatusLabel.setText(winner == controller.getPlayerColor() ? "🎉 玩家獲勝！" : "電腦獲勝");
        bottomStatusLabel.setText(message);
        updateUndoUI();

        SwingUtilities.invokeLater(() -> {
            int res = JOptionPane.showOptionDialog(
                this,
                message + "\n\n是否再開一局？",
                title,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"再來一局", "查看棋盤"},
                "再來一局"
            );
            if (res == JOptionPane.YES_OPTION) {
                onNewGameClicked();
            }
        });
    }

    @Override
    public void onBoardUpdated() {
        boardPanel.setBoard(controller.getBoard());
        if (!controller.getMoveHistory().isEmpty()) {
            boardPanel.setLastMove(controller.getMoveHistory().get(controller.getMoveHistory().size() - 1));
        } else {
            boardPanel.setLastMove(null);
        }
        rebuildHistoryTable();
        updateCapturedPiecesUI();
        updateUndoUI();
    }

    @Override
    public void onStatusMessage(String message) {
        bottomStatusLabel.setText(message);
    }
}
