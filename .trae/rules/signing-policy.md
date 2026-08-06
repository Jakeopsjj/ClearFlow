# 签名固化规则

## 概述

自 v1.7.14 起，项目签名永久锁定。所有后续版本的 Debug 和 Release APK 均使用相同的 keystore 签名，SHA1 和 SHA256 指纹禁止更改。

## 固化签名指纹

### Debug 签名

| 属性 | 值 |
|------|-----|
| 别名 | `cleardu-debug` |
| 密码 | `android` |
| SHA1 | `CE:55:41:52:05:48:5C:0A:58:DF:AC:1C:36:22:38:BB:15:2F:26:42` |
| SHA256 | `9A:70:79:8D:E3:BF:71:EF:0E:E8:C7:18:C6:38:B1:ED:33:50:D2:A1:44:CC:0E:7D:F7:B4:5A:BD:C0:E1:04:3B` |

### Release 签名

| 属性 | 值 |
|------|-----|
| 别名 | `cleardu-release` |
| 密码 | `cleardu-release` |
| SHA1 | `0F:06:9A:DC:BC:09:66:E9:9A:DC:00:31:EB:15:84:CB:4F:81:D9:66` |
| SHA256 | `5C:0D:51:26:7E:E9:A8:F2:EA:9F:F0:58:B4:F7:E6:68:65:1C:19:8F:56:EC:95:8C:38:AD:51:7B:6C:A4:05:B2` |

## Keystore 存储位置

- **Debug**: `app/keystore/debug.keystore`
- **Release**: `app/keystore/release.keystore`

keystore 文件已提交到 Git 仓库，新环境克隆后可直接构建，无需手动恢复。

## 构建行为

- `./gradlew assembleDebug` — 使用 `app/keystore/debug.keystore` 签名
- `./gradlew assembleRelease` — 使用 `app/keystore/release.keystore` 签名（或通过环境变量 `CLEARDU_RELEASE_KEYSTORE_PATH` / `CLEARDU_RELEASE_KEYSTORE_BASE64` 注入生产签名）

## 强制规则

1. **禁止重新生成 keystore**：`build.gradle.kts` 中的 `ensureDebugKeystore` 和 `ensureReleaseKeystore` 任务已改为检查模式——如果 keystore 文件缺失，构建将直接失败并提示恢复，不再自动生成新 keystore。
2. **禁止修改签名配置**：`signingConfigs` 中的 keystore 路径、密码、别名均不可更改。
3. **禁止修改 SHA1/SHA256**：任何情况下不得生成新的 keystore 替换现有文件。
4. **环境变量注入仅用于正式发布**：`CLEARDU_RELEASE_KEYSTORE_*` 环境变量仅在需要注入真实生产签名时使用，覆盖默认固化 keystore。日常开发构建直接使用固化的 keystore。

## 违规处理

- 任何未经授权修改签名配置、重新生成 keystore、更改签名指纹的行为均视为违规。
- AI Agent 发现 keystore 缺失时，必须立即停止构建，提示用户恢复备份，不得自行生成新 keystore。