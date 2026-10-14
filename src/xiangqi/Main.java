package xiangqi;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * 程式啟動入口
 */
public class Main {
    public static void main(String[] args) {
        // 設定系統字型平滑渲染與現代外觀
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
