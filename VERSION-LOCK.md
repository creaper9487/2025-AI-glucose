# 版本鎖定說明 (Version Lock Instructions)

## 概述
此項目已實施版本鎖定，確保所有協作者使用相同的依賴版本，避免"在我的機器上可以運行"的問題。

## 已鎖定的文件

### 前端 (glucose-FE)
- `package.json`: 移除了所有 `^` 和 `~` 前綴，使用精確版本
- `pnpm-lock.yaml`: pnpm 自動生成的鎖定文件（已存在）

### 後端 (glucose-BE)
- `requirements-lock.txt`: 包含所有依賴的確切版本
- `requirements.txt`: 保留作為主要依賴列表（開發用）

## 安裝指南

### 自動安裝（推薦）
```bash
cd batch
.\install.bat
```

### 手動安裝

#### 前端安裝
```bash
cd glucose-FE
pnpm install --frozen-lockfile
```

#### 後端安裝
```bash
cd glucose-BE
pip install -r requirements-lock.txt
```

## 開發流程

### 添加新依賴

#### 前端
1. 添加依賴: `pnpm add package-name`
2. 確認版本: 在 `package.json` 中移除 `^` 前綴
3. 提交 `package.json` 和 `pnpm-lock.yaml`

#### 後端
1. 添加到 `requirements.txt`
2. 安裝: `pip install package-name`
3. 更新鎖定文件: `pip freeze > requirements-lock.txt`
4. 提交兩個文件

### 更新依賴

#### 前端
```bash
pnpm update
# 然後移除 package.json 中的 ^ 前綴
```

#### 後端
```bash
pip install --upgrade -r requirements.txt
pip freeze > requirements-lock.txt
```

## 注意事項

1. **絕不要手動編輯鎖定文件**
2. **始終提交鎖定文件到版本控制**
3. **CI/CD 應該使用鎖定文件安裝**
4. **新團隊成員應該使用 `install.bat` 進行初始設置**

## 故障排除

### 前端依賴衝突
```bash
rm -rf node_modules pnpm-lock.yaml
pnpm install
```

### 後端依賴問題
```bash
pip uninstall -y -r requirements-lock.txt
pip install -r requirements-lock.txt
```

### 完全重置
使用 `install.bat` 重新安裝所有依賴。