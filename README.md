# 请假助手（LeaveSchool）

uni-app（Vue 2）+ 自建 Android WebView 外壳的请假记录 App。

## 目录

```
LeaveSchool/
├── src/                 uni-app 源码（唯一编辑入口）
│   ├── pages/
│   │   ├── index/       首页「今日假条」（MD3E）
│   │   ├── addLeave/    添加请假（MD3E + 自绘选择器）
│   │   ├── viewLeaves/  请假详情（按设计稿逐像素还原）
│   │   └── QRCode/      核验二维码
│   ├── static/img/      图片资源
│   ├── App.vue          全局样式 + MD3 设计变量
│   ├── main.js  pages.json  manifest.json  uni.scss
│
├── cli/                 H5 构建工程（webpack + @vue/cli-service + uni-build）
│   ├── src/             由 build.ps1 从 ../src 同步，勿手改
│   ├── dist/build/h5/   构建产物
│   ├── package.json  vue.config.js  babel.config.js
│   └── node_modules/    已安装的构建依赖
│
├── android/             Android 外壳（不依赖 Gradle）
│   ├── java/com/leaveschool/app/
│   │   ├── MainActivity.java   WebView + app.local 域名拦截 + 文件选择
│   │   └── BuildConfig.java    DEBUG_WEBVIEW 调试开关
│   ├── res/             图标与主题（浅色状态栏）
│   ├── AndroidManifest.xml
│   ├── build-apk.ps1    aapt2 + javac + d8 + zipalign + apksigner
│   ├── pack_apk.py      打包（resources.arsc 不压缩）
│   └── leave-school.jks 本地签名证书（不上传 GitHub）
│
├── dist/                leave-school-1.1.0.apk
├── screenshots/         各页面截图
├── build.ps1            一键构建
└── .npm-cache/          npm 缓存（离线重装依赖用）
```

## 构建

```powershell
# 完整构建：同步源码 → 构建 H5 → 打包 APK
.\build.ps1

# 依赖已装好时可跳过安装检查
.\build.ps1 -SkipInstall

# 只重打 APK（H5 已构建过）
.\android\build-apk.ps1
```

产物：`dist/leave-school-1.1.0.apk`

安装：

```powershell
adb install -r dist\leave-school-1.1.0.apk
```

## 环境要求

| 依赖 | 路径 |
|---|---|
| Node.js | 22.x（构建时需 `NODE_OPTIONS=--openssl-legacy-provider`，脚本已设置） |
| Android SDK | `ANDROID_SDK_ROOT` 或 `%USERPROFILE%\AppData\Local\Android\Sdk`（build-tools 35.0.0、platforms/android-35） |
| JDK | `%USERPROFILE%\.jdks\jbr-21.0.11` |
| Python | 用于 `android/pack_apk.py` |

## 实现要点

- **WebView 外壳**：AssetServer 不再启随机端口，改用 `shouldInterceptRequest` 拦截 `http://app.local/`，从 `assets/www` 读取，域名固定 ⇒ localStorage 不会因端口变化丢失。
- **状态栏**：`Theme.Material.Light.NoActionBar` + 白色 statusBar + `windowLightStatusBar`，避免深色模式下变黑。
- **详情页**：导航 48px、Tab 40px、「如何销假」条 21px、流程圆环 16.6px / 描边 1.14px、连接线 1.4px 居中于圆心。
- **添加页**：请假类型/抄送人/需要离校/审批意见/审批人身份全部走预设；总时长按起止时间自动计算；宿舍只需填 `B` 之后的部分。
- **选择器**：H5 端自绘 MD3 Bottom Sheet（遮罩 + 滚轮吸附 + 滚动即选中），打开时锁定页面滚动。
- **弹窗**：MD3 对话框 + Snackbar（`pointer-events: none`，不阻断操作）。

## 调试

`android/java/com/leaveschool/app/BuildConfig.java`：

```java
public static final boolean DEBUG_WEBVIEW = true;  // 打开后可 adb forward 到 WebView DevTools
```

改完重新打包即可（release 版本保持 `false`）。

## 签名与发布

签名证书不进入仓库。首次打包会生成本地开发证书，请自行妥善备份；覆盖升级必须使用同一证书。GitHub Actions 生成的 APK 使用临时开发证书，适合测试安装。

Android 打包脚本支持 -SdkDir 和 -JdkDir，也读取 ANDROID_SDK_ROOT、JAVA_HOME。

