# MaidTech Server Toolkit

> LLM 伴从 × 生电友好 × Fabric 1.21.1 模组化 Minecraft 服务器的生产环境工具链。
> 一个真实运行中的多人服务器沉淀下来的运维工具、自研 mod 与数据包集合。

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green)](https://www.minecraft.net/)
[![Loader](https://img.shields.io/badge/Loader-Fabric%200.16.14-blue)](https://fabricmc.net/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 这是什么

一套让 **LLM 驱动的 AI 智能体** 与 **生电(红石/自动化)玩法** 在同一个 Fabric 服务器上长期共存的生产级方案,包含:

- **多 AI 共存架构** —— mindcraft LLM bot(GLM / DeepSeek)+ numen 私人 AI 伴从 + TLM 女仆 AI,
  三套 AI 同时在线互不干扰,含启动链、礼包排除、人数占用等工程细节的解法;
- **自研 Fabric mod `botcompat`** —— 通过 mixin 修复 AI bot 在 modded 握手阶段被
  Cardinal Components API 校验踢出的问题;
- **进程自愈启动链** —— 解决"优雅停服后 JVM 假死、持锁不退"的 modpack 通病;
- **种子分析器** —— 纯 JS 复刻原版 Java RNG 语义(BigInt 模拟 64 位长整型),
  精确计算史莱姆区块与 14 类结构的生成候选点;
- **i18n 汉化补全包** —— 2617 条 CFPA 未收录 mod 的手工汉化。

## 目录结构

```
├── tools/
│   ├── start-all-detached.ps1   # WMI 分离式启动:服务器→等端口→bot;含僵尸 JVM 自愈
│   ├── rc.ps1                   # RCON 命令行(密码走环境变量)
│   └── seed-analysis.mjs        # 种子分析器(史莱姆区块/结构候选点)
├── datapacks/
│   ├── maidtech-starter/        # 初始礼包:进度触发器,每 UUID 一次,AI 名单排除
│   └── maidtech-nojegstruct/    # 数据包方式禁用第三方 mod 的天空船结构
├── i18n-completion/             # 汉化补全资源包(7 个 mod,2617 条)
├── botcompat/                   # 自研 Fabric mod:AI bot 握手兼容层(源码)
├── docs/
│   └── SANITIZATION.md          # 发布前敏感信息清洗清单(重要,先读)
└── LICENSE
```

## 快速开始

前置:Fabric 1.21.1 服务器、Node.js ≥ 20、PowerShell 5.1+。

1. 克隆本仓库,把 `tools/` 放到你的运维目录,按你的安装路径修改
   `start-all-detached.ps1` 顶部的 `$Base / $Java / $Node` 常量;
2. 设置 RCON 密码环境变量(server.properties 中 `enable-rcon=true`、`rcon.port=25575`):

   ```powershell
   $env:MAIDTECH_RCON_PASS = '你的RCON密码'
   ```

3. 把 `datapacks/` 里需要的数据包复制进 `world/datapacks/`;
4. 启动:

   ```powershell
   powershell -NoProfile -ExecutionPolicy Bypass -File .\tools\start-all-detached.ps1
   ```

   服务器就绪后自动拉起 bot,全程与启动会话解耦,关掉终端不影响运行。

5. 关服永远走优雅路径(RCON `stop` 或服务器控制台输入 `stop`),
   启动链会在下次启动时自动清理假死残留进程。

## 种子分析器

复刻原版 `LegacyRandomSource` / `setLargeFeatureSeedWithSalt` / `random_spread`
结构放置与史莱姆区块判定的完整算法(Java `long` 溢出语义用 BigInt 精确模拟):

```bash
node tools/seed-analysis.mjs <worldSeed> <rangeBlocks> [centerX] [centerZ] [outFile]
```

- 史莱姆区块结果为**精确值**(纯数学判定);
- 结构点位为 random_spread 候选位置,**不含群系校验**——最终生成还需群系匹配。

## 按键冲突治理方法论

重度整合包(130+ 按键绑定)的冲突治理思路,沉淀在
[快捷键优化提示词](docs/keybind-prompt.md)(可直接粘贴给 AI 助手执行):
解绑低作用键 → 上下文限定的冲突用 Keybind Fix 共存 → 组合键只在支持的框架内设置。

## 安全与发布

**先读 [docs/SANITIZATION.md](docs/SANITIZATION.md)。**
本仓库发布的是清洗后的工具与数据包;mod jar 一律不二次分发(许可证原因,README 只提供清单);
API key、RCON 密码、世界存档、玩家数据全部走排除清单。若历史文件中出现过明文密钥,
**发布前必须轮换全部密钥**。

## 已知问题 / 路线图

- Forge→Fabric 移植 mod 的输入事件翻译层存在结构性缺陷
  (案例:某枪械 mod 在 Fabric 移植版中出现全交互死亡),详见 `docs/` 分析;
- Multi-bot 的聊天寻址仍需 `/msg` 逐个指派,计划支持群组指令路由;
- numen 伴从的模型 ID 需跟随上游 API 的模型下架节奏更新。

## License

代码以 [MIT](LICENSE) 发布;i18n 汉化包以 [CC-BY-4.0](https://creativecommons.org/licenses/by/4.0/) 发布。
第三方 mod 的版权归属其原作者,本仓库不分发其 jar 文件。
