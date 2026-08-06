# ClearDu Pexels Proxy

Pexels 图片中转代理服务，用于清渡 App 天气背景功能。

## 当前部署方案：Cloudflare Workers（已上线）

**线上地址**：`https://cleardu-pexels-proxy.cleardu.workers.dev`

### 为什么用 Workers

- 无冷启动（不同于 Koyeb/Render 闲置休眠机制），无需保活定时任务
- Cloudflare 免费版每天 10 万次请求，远超个人演示需求
- 全球边缘节点，延迟低
- 内置 Cache API，2 小时边缘缓存，减少 Pexels API 调用

### 部署/更新步骤

```bash
cd pexels-proxy

# 1. 设置 API Token（从 https://dash.cloudflare.com/profile/api-tokens 创建）
export CLOUDFLARE_API_TOKEN=your_token_here

# 2. 部署
wrangler deploy

# 3. 注入 Pexels API Key（首次部署或更换 Key 时执行）
echo "your_pexels_api_key" | wrangler secret put PEXELS_API_KEY
```

### 配置文件

- `worker.js` — Workers 主代码（ES Module 格式）
- `wrangler.toml` — Workers 部署配置
- `PEXELS_API_KEY` — 通过 `wrangler secret put` 注入，加密存储在 Cloudflare，不写入代码或配置文件

## 备选部署方案：Koyeb / Render

若 Cloudflare 不可用，可改用传统 Node.js 服务部署（`server.js`）：

### Koyeb（主力）

1. 注册 [Koyeb](https://www.koyeb.com/) 账号
2. 创建新 Service → 选择 GitHub 仓库 → 选择 `pexels-proxy` 目录
3. 配置环境变量：`PEXELS_API_KEY` = 你的 Pexels API Key
4. 端口：3000（Koyeb 自动注入 PORT）
5. 配置 [cron-job.org](https://cron-job.org) 每 45 分钟访问 `/health` 保活

### Render（备用）

1. 注册 [Render](https://render.com/) 账号
2. 创建 Web Service → Root Directory: `pexels-proxy`
3. 配置环境变量：`PEXELS_API_KEY` = 你的 Pexels API Key
4. Build: `npm install`，Start: `npm start`

**禁止配置 Render 保活**，避免耗尽每月 750 小时免费时长。

## API

### GET /health

健康检查端点。

```json
{ "status": "ok", "timestamp": 1700000000000, "service": "cleardu-pexels-proxy" }
```

### GET /api/search?query=sunny+landscape&orientation=landscape

代理 Pexels 图片搜索，返回单张横版图片信息。

```json
{
  "imageUrl": "https://images.pexels.com/photos/.../large2x.jpg",
  "photographer": "Author",
  "alt": "description"
}
```

## 注意事项

- 本代理仅供清渡 App 个人演示使用，禁止作为公共代理服务
- Pexels 图片禁止二次打包分发售卖
- Pexels API Key 通过 `wrangler secret` 或环境变量注入，不硬编码在代码中
- Cloudflare API Token 仅供本地部署使用，不提交到 Git 仓库
