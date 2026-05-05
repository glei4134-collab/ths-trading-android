# THS Trading AI - Android 原生应用

同花顺模拟比赛AI交易系统 - Android 原生客户端

## 功能

- 🔐 二维码/绑定码绑定电脑端
- 📊 实时查看账户资金、持仓
- 🤖 AI 智能聊天 + 交易指令
- ⚙️ 远程配置 AI 调度器、自动交易
- 🔔 实时交易通知
- 📱 原生 Material Design 3 UI

## 技术栈

- **语言**: Kotlin
- **UI**: Material Design 3
- **网络**: OkHttp + Gson
- **导航**: Navigation Component
- **异步**: Kotlin Coroutines
- **相机**: CameraX + ML Kit (二维码)

## 构建

```bash
# 需要 Android Studio Hedgehog+ 和 JDK 17+
./gradlew assembleDebug
```

APK 输出: `app/build/outputs/apk/debug/app-debug.apk`

## 使用

1. 安装 APK 到手机
2. 打开应用，输入电脑端服务器地址和绑定码
3. 绑定成功后即可使用
