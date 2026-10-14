package xiangqi;

import java.awt.Color;

/**
 * 棋盤樣式主題列舉（支援實木、大理石、純色、翡翠玉石、復古羊皮等）
 */
public enum BoardTheme {
    WOOD(
        "經典木紋",
        new Color(238, 206, 155), new Color(208, 168, 114), // 背景漸層
        new Color(102, 60, 24),                              // 外邊框木紋色
        new Color(140, 85, 35, 180),                        // 內飾線
        new Color(74, 42, 18),                               // 棋盤網格線
        new Color(110, 65, 30),                              // 楚河漢界字體色
        new Color(240, 232, 218)                             // 外圍包裹底色
    ),
    MARBLE(
        "典雅大理石",
        new Color(250, 250, 252), new Color(225, 230, 236), // 石材冷灰漸層
        new Color(52, 73, 94),                               // 石質深外框
        new Color(149, 165, 166, 180),                       // 銀白內飾線
        new Color(44, 62, 80),                               // 深青灰網格線
        new Color(41, 128, 185),                             // 青藍石刻河界
        new Color(236, 240, 243)                             // 外圍包裹底色
    ),
    SOLID(
        "簡約純色",
        new Color(246, 242, 233), new Color(246, 242, 233), // 無炫光純米色
        new Color(40, 40, 40),                               // 啞黑外框
        new Color(150, 150, 150, 160),                       // 細黑內飾線
        new Color(35, 35, 35),                               // 墨黑網格線
        new Color(45, 45, 45),                               // 墨字河界
        new Color(235, 232, 224)                             // 外圍包裹底色
    ),
    JADE(
        "翡翠玉石",
        new Color(225, 245, 232), new Color(175, 222, 190), // 青翠玉石漸層
        new Color(20, 75, 45),                               // 翠綠外框
        new Color(46, 139, 87, 180),                         // 金綠內飾線
        new Color(16, 64, 36),                               // 墨綠網格線
        new Color(18, 85, 46),                               // 翡翠河界
        new Color(228, 242, 232)                             // 外圍包裹底色
    ),
    PARCHMENT(
        "復古羊皮",
        new Color(248, 237, 212), new Color(230, 208, 168), // 仿古羊皮紙
        new Color(92, 64, 42),                               // 古典皮革邊框
        new Color(165, 122, 82, 180),                        // 銅色內飾線
        new Color(75, 48, 28),                               // 焦褐網格線
        new Color(105, 68, 38),                              // 褐色河界
        new Color(242, 233, 214)                             // 外圍包裹底色
    );

    private final String displayName;
    public final Color bgGradStart;
    public final Color bgGradEnd;
    public final Color outerBorder;
    public final Color innerBorder;
    public final Color gridLine;
    public final Color riverText;
    public final Color wrapperBg;

    BoardTheme(String displayName, Color bgGradStart, Color bgGradEnd, Color outerBorder,
               Color innerBorder, Color gridLine, Color riverText, Color wrapperBg) {
        this.displayName = displayName;
        this.bgGradStart = bgGradStart;
        this.bgGradEnd = bgGradEnd;
        this.outerBorder = outerBorder;
        this.innerBorder = innerBorder;
        this.gridLine = gridLine;
        this.riverText = riverText;
        this.wrapperBg = wrapperBg;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
