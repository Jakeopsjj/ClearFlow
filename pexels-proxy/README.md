# ClearDu Pexels Proxy

Pexels 图片中转代理服务，用于清渡 App 天气背景功能。

## 部署

### 1. Koyeb（主力）

1. 注册 [Koyeb](https://www.koyeb.com/) 账号
2. 创建新 Service → 选择 GitHub 仓库 → 选择 `pexels-proxy` 目录
3. 配置环境变量：
   - `PEXELS_API_KEY` = 你的 Pexels API Key
4. 构建类型：Node.js
5. 端口：3000（或 Koyeb 自动注入 PORT）
6. 部署完成后记下 URL，如 `https://cleardu-pexels-proxy.koyeb.app`

#### 保活定时任务

Koyeb 免费实例闲置 1 小时无请求会休眠。配置外部定时任务每 45 分钟访问 `/health`：

- 使用 [cron-job.org](https://cron-job.org) 或 [UptimeRobot](https://uptimerobot.com)
- URL: `https://你的koyeb地址/health`
- 间隔：每 45 分钟

### 2. Render（备用）

1. 注册 [Render](https://render.com/) 账号
2. 创建新 Web Service → 选择 GitHub 仓库 → Root Directory: `pexels-proxy`
3. 配置环境变量：
   - `PEXELS_API_KEY` = 你的 Pexels API Key
4. Build Command: `npm install`
5. Start Command: `npm start`
6. 记下 URL，如 `https://cleardu-pexels-proxy.onrender.com`

**禁止配置 Render 保活定时任务**，避免耗尽每月 750 小时免费时长。

## 本地开发

```bash
cd pexels-proxy
PEXELS_API_KEY=your_key_here npm start
```

## API

### GET /health
健康检查端点，用于保活。

### GET /api/search?query=sunny+landscape&orientation=landscape
代理 Pexels 图片搜索，返回单张横版图片信息。

## 注意事项

- 本代理仅供清渡 App 个人演示使用，禁止作为公共代理服务
- Pexels 图片禁止二次打包分发售卖
- Pexels API Key 通过环境变量注入，不硬编码在代码中
