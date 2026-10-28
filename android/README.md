# Android 壳工程（APK 打包）

把 `leave-school-cli/dist/build/h5` 打成一个可安装的 APK。不依赖 Gradle，只用本机已有的
JDK 21（`~/.jdks/jbr-21.0.11`）和 Android SDK（build-tools 35.0.0 / platform android-35）。

## 结构

```
android-shell/
├─ AndroidManifest.xml          # 包名 com.leaveschool.app，minSdk 21 / targetSdk 33
├─ res/values/strings.xml       # 应用名：请假助手
├─ res/mipmap-xxhdpi/ic_launcher.png
├─ java/com/leaveschool/app/
│  ├─ MainActivity.java         # WebView + 返回键 + 选择图片（uni.chooseImage）
│  └─ AssetServer.java          # 127.0.0.1 迷你静态服务器，把 assets/www 提供出去
├─ pack_apk.py                  # 合并 base.apk + classes.dex + assets（resources.arsc 不压缩）
├─ build-apk.ps1                # 完整构建 + 签名
├─ leave-school.jks             # 签名 keystore（storepass/keypass 均为 android，别丢，否则无法覆盖升级）
└─ build/                       # 中间产物
```

## 构建

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\build-apk.ps1
# 产物：..\apk\leave-school-1.1.0.apk
```

流程：`aapt2 compile/link` → `javac --release 8` → `jar` → `d8` → `pack_apk.py` → `zipalign -p 4` → `apksigner sign (v1+v2+v3)`。

## 为什么用 WebView 壳

仓库自带的两个 APK 是**空的标准基座**（`assets/apps/__UNI__A6877D0/www/` 里只有 `manifest.json`），
它的 5+ 运行时是 HBuilderX 3.1.2 的，和现在能装到的最新 uni-app 编译器产物版本不匹配，
不能简单把资源塞进去。要原生 5+ 包只能用 HBuilderX + DCloud 云打包（需要账号）。

## 已知局限

- 本质是 WebView 壳：`uni.scanCode` 等 5+ 原生 API 不可用（H5 构建里本来也是降级处理）。
- 选择图片走系统文件选择器（`WebChromeClient.onShowFileChooser`），没有申请相机权限也能用相册。
- 应用签名用的是本地自建证书，和原作者的签名不同，**安装前需先卸载旧版**；后续升级要用同一个 `leave-school.jks`。
- 如需更换版本号，改 `AndroidManifest.xml` 里的 `android:versionCode/versionName`。

## 真机验证（已通过）

设备：realme RMX5010，Android 17。

| 检查项 | 结果 |
|---|---|
| 安装 / 冷启动 | 正常，无白屏（早先的抢跑问题已加自动重试） |
| 首页 | 导航栏、Tab、橙色提示条、底部按钮布局正确 |
| 添加请假页 | 表单完整渲染 |
| 列表页 | 记录类型/时间/状态显示正确 |
| 详情页 | 审批状态、字段、时间都正常 |
| 核验二维码页 | canvas 二维码正常绘制 |
| 返回键 | 逐级返回正常 |

验证过程中修掉的三个问题：

1. **整体布局被压扁**：项目 `manifest.json` 没写 `transformPx`，uni-app 默认把 CSS 里的 `px` 也当 rpx 缩放（70px 的导航栏变成 34px）。已在 `leave-school-cli/src/manifest.json` 显式设 `"transformPx": false`。
2. **小图不显示**：小于 4KB 的图片被 url-loader 内联成 base64，导出成 ES 模块对象 `{default: 'data:...'}`，而 `<image>` 只接受字符串，`$getRealPath` 里 `src.indexOf` 抛错。已在 `vue.config.js` 里把 `url-loader` 的 `limit` 设为 0。
3. **偶发白屏 / “连接服务器超时”**：`AssetServer` 原来只要某个连接抛 IO 异常就 `break` 掉 accept 循环，整个服务直接死掉。改成线程池逐连接处理、异常只影响当前请求，并给 WebView 加了主框架加载失败后 400ms 自动重试。

调试开关在 `java/com/leaveschool/app/BuildConfig.java`（`DEBUG_WEBVIEW`），出正式包时保持 `false`，需要 chrome://inspect 排查时改成 `true`。
