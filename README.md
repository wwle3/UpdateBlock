# UpdateBlock

基于 libxposed 的 LSPosed 模块，用于拦截 Nagram 与 NagramX 的应用内更新检测与更新提示。

## 支持应用

- Nagram (`xyz.nextalone.nagram`)
- NagramX (`nu.gpu.nagram`)

## 运行环境

- Android 8.0 (API 26) 及以上
- 架构：`arm64-v8a`
- 框架支持：支持 libxposed API 101+ 的框架（如 LSPosed、Vector）

## 使用方法

1. 安装模块 APK。
2. 在 LSPosed / Vector 管理器中启用本模块。
3. 勾选对应客户端的作用域（`xyz.nextalone.nagram` 或 `nu.gpu.nagram`）。
4. 强行停止目标客户端后重新打开生效。

## 编译构建

项目基于 OpenJDK 21 与 Android SDK 构建：

```bash
./gradlew assembleRelease
```

构建生成的安装包位于 `app/build/outputs/apk/release/updateblock-release-arm64.apk`。
