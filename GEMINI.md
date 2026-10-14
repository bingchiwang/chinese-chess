# 專案開發規範 (Project Guidelines)

## GitHub 自動同步規則 (Auto-Sync to GitHub)
- **遠端倉庫**：`https://github.com/bingchiwang/chinese-chess.git` (分支：`main`)
- **強制執行標準**：
  使用者明確要求：往後若有修改檔案，都必須同步上傳 GitHub。
  因此，在任何對專案檔案進行新增、修改或刪除的操作之後，必須執行：
  1. 若有修改 Java 原始碼，務必重新編譯並更新 `ChineseChess.jar`（確保可執行包為最新狀態）。
  2. 執行 `git add .` 暫存變更。
  3. 執行 `git commit -m "<清楚具體的提交訊息>"` 建立提交。
  4. 執行 `git push origin main` 推送至 GitHub 遠端倉庫。
