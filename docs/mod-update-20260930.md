# 客户端更新规格 2026-09-30（智能体可执行）

> 目标：把一台"旧版朋友包客户端"升级到与服务器一致。
> 执行环境：Fabric 1.21.1 / loader 0.19.5 / Java 21。游戏关闭状态操作。
> mods 目录定位：包含 `tacz_x_guns_lights_addon-fabric-1.21.1-2.8.2.jar` 的 mods 目录。

## 1. 删除以下旧文件（存在才删，不存在跳过）

| 文件名 | 原因 |
|---|---|
| `modmenu-11.0.4.jar` | 升级替换 |
| `entityculling-fabric-1.10.5-mc1.21.1.jar` | 升级替换 |
| `ImmediatelyFast-Fabric-1.6.13+1.21.1.jar` | 升级替换 |
| `spell_engine-fabric-1.10.5+1.21.1.jar` | 升级替换 |
| `numen-fabric-1.21.1-0.1.2.jar` | 升级替换 |
| `numen_api-0.0.8-SNAPSHOT-c447cc33054e4aa1.jar` | 升级替换（旧文件名可能不同，删任何 `numen_api-*0.0.8*` 即可） |
| `creaturechat-3.0.0+1.21.1.jar`、`creaturechat-3.0.0.jar` | 模组已下架（服务器编码 bug 修复成本高于价值） |
| `ai-player-1.0.5.3-release+1.21.1.jar` | 模组已下架 |
| `too-many-shortcuts-0.0.14.jar` | 与 Keybind Fix 冲突，禁止共存 |

## 2. 新增/替换为以下文件（Release 附件里都有）

| 文件名 | 大小 | 来源 Release |
|---|---|---|
| `modmenu-11.0.5.jar` | 975846 B | friendpack-update-20260930 |
| `entityculling-fabric-1.11.2-mc1.21.1.jar` | 1519213 B | friendpack-update-20260930 |
| `ImmediatelyFast-Fabric-1.6.14+1.21.1.jar` | 340166 B | friendpack-update-20260930 |
| `spell_engine-fabric-1.10.9+1.21.1.jar` | 4707862 B | friendpack-update-20260930 |
| `numen-fabric-1.21.1-0.1.3.jar` | 2519772 B | friendpack-update-20260930 |
| `numen_api-fabric-1.21.1-0.1.3.jar` | 1353762 B | friendpack-update-20260930 |
| `crosstalk-fabric-0.1.0.jar` | 24951 B | friendpack-update-20260930 |
| `crosstalk-collab-0.1.0.jar` | 4224 B | friendpack-update-20260930 |

## 3. 校验

- 全部 ADD 文件存在且大小一致
- 被删文件全部不存在
- 不允许出现同一 mod 的两个版本共存（如 spell_engine 1.10.5 和 1.10.9 同时在 = 失败）

## 4. 验证（启动游戏后）

- Mod 菜单（modmenu）显示版本正确
- 进服无注册表不一致报错
- numen 同伴可召唤（0.1.3）且头顶无对话气泡（CreatureChat 已移除）

## 5. 版本锁定（不要动）

以下为当前锁定的配套版本，**不可单独升级**：

| mod | 版本 | 锁定原因 |
|---|---|---|
| crosstalk | 0.1.0 | 依赖 numen 0.1.3 的 `NumenPlugins` 类；numen 升 0.1.4+ 需同步升 crosstalk |
| numen / numen_api | 0.1.3 | 与 crosstalk 0.1.0 配套；numen 0.1.3 为 beta |
| spell_power | 1.6.0 | spell_engine 1.10.9 的配套版本（当前已是最新） |
| PuzzlesLib | 21.1.60 | 与 EasyShulkerBoxes 21.1.3 配套 |

## 6. 服务器侧同步项（服主操作，智能体勿动）

- `spell_engine-fabric-1.10.9+1.21.1.jar`（服务器已升）
- `numen 0.1.3 + numen_api 0.1.3`（服务器已升，api 独立 jar）
- `crosstalk-fabric/collab 0.1.0`（服务器已装）
- CreatureChat / AI-Player / TMS（服务器已移除）
