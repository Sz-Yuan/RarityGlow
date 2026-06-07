# RarityGlow — Minecraft Fabric Mod

为掉落物品添加基于稀有度的彩色轮廓发光、3D 光柱和蜂巢地面图案的客户端 Mod。

## 项目

- **技术栈：** Java 25、Fabric Loom (Gradle)、Minecraft 26.1
- **入口：** `src/main/java/kitejs/RarityGlow.java` — 实现 `ClientModInitializer`
- **Mod ID：** `rarityglow`
- **依赖：** Fabric API、Cloth Config (≥26.1.154)、ModMenu (≥18.0.0-alpha.8，可选)

## 命令

| 操作   | 命令                                    |
|------|---------------------------------------|
| 构建   | `./gradlew build`（输出 → `build/libs/`） |
| 开发运行 | `./gradlew runClient`（通过 Fabric Loom） |
| 清理   | `./gradlew clean`                     |

> **⚠️ 不要自动执行构建命令。** 构建由用户手动触发（在 IDE 或终端中运行 `./gradlew build`）。Agent 只负责修改代码，不主动运行 gradlew。

本项目未配置测试框架——仓库中没有测试目录和测试依赖。

## 架构

```
kitejs/
├── RarityGlow.java          — Mod 入口：初始化配置，注册 BeamRenderer
├── config/
│   ├── RarityGlowConfig.java — AutoConfig 数据类（TOML 序列化）
│   └── ModMenuIntegration.java — ModMenu 配置界面工厂
├── mixin/
│   └── EntityRendererMixin.java — 注入 extractRenderState，设置 outlineColor
└── utils/
    ├── ItemRarityHelper.java — 解析稀有度 → 发光/光柱颜色（尊重开关）
    ├── GlowColorCache.java   — 解析 RGB 字符串 → ARGB 整数（volatile 缓存）
    └── BeamRenderer.java     — AfterTranslucentFeatures 回调：蜂巢 + 3D 光柱
```

### 数据流

1. `RarityGlow#onInitializeClient` → 注册 `BeamRenderer` 为 `LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES` 监听器，通过 AutoConfig 加载配置，填充 `GlowColorCache`。
2. 每帧：`EntityRendererMixin` 为 `ItemEntity` 设置 `state.outlineColor` → 原版发光轮廓使用该颜色。
3. `BeamRenderer` 遍历实体，计算蜂巢和锥形光柱四边形 → 通过 `debugQuads` buffer 渲染。

### 配置

TOML 文件位于 `<游戏目录>/config/rarityglow.toml`，由 AutoConfig 管理。主开关（`glowEnabled`、`beamEnabled`）覆盖各稀有度子开关。每种稀有度有 `rgb` 字符串（如 `"255,255,85"`）+ 独立的发光/光柱开关。`BeamSettings` 部分控制高度、宽度、偏移、渲染距离和蜂巢图案开关。

## 约定

- **命名：** 字段/变量 `lowerCamelCase`，类 `PascalCase`，常量 `UPPER_SNAKE_CASE`。
- **配置注解：** 使用 `@ConfigEntry.Gui.Tooltip`、`@ConfigEntry.Gui.CollapsibleObject`、`@ConfigEntry.BoundedDiscrete` 注解配置字段——不手写 GUI 代码。
- **配置监听：** 在 config holder 上注册 `saveListener` 来更新 volatile 缓存（见 `GlowColorCache`）。
- **Mixin 命名：** 注入方法前缀 `rarityglow$`。用 `@Mixin` 注解标注目标 Minecraft 类。
- **渲染：** 使用 `debugQuads()` beta buffer 绘制自定义几何体。push/pop pose stack，遍历结束后调用 `endBatch()`。
- **空值安全：** 用 early return 守护 world/camera/entity。对 Fabric API 回调参数使用 `@NonNull`。
- **资源命名空间：** 语言键遵循 `text.autoconfig.rarityglow.option.<section>.<key>` 格式。

## 备注

*留空，随时补充。*
