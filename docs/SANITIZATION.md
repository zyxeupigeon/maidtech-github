# 发布前敏感信息清洗清单

> 把本工作区任何内容发布到 GitHub / 分享给他人之前,逐条核对。
> 原则:**密钥轮换优先于删除文件**——文件能从 git 历史里翻出来,泄露过的密钥必须作废重生成。

## 一、密钥与凭证(最高危)

| 位置 | 内容 | 处理 |
|---|---|---|
| `_tools/mindcraft/keys.json` | DeepSeek、智谱两把真实 API key | **不入库**(.gitignore);若曾入库 → 立即在 DeepSeek/智谱后台轮换 |
| `server/config/numen.json` | `apiKey` 字段(DeepSeek 老 key) | 同上;发布版用 `"apiKey": "REPLACE_ME"` 占位 |
| `server/server.properties` | `rcon.password` | 发布版占位;**已泄露过的 RCON 密码必须更换** |
| `_tools/rc.ps1`(工作区原版) | 第 3 行硬编码 `$rpass='maidtech_rcon_****(原值,发布前轮换)'` | 仓库版已改为环境变量 `MAIDTECH_RCON_PASS`;原文件建议同样改造 |
| 聊天记录/交接文档 | 曾以打码形式(`sk-xxxx####`)提及 | 打码形式可保留,但注意前 7 位 + 语境仍可能被猜;文档入库前复核 |

**轮换入口**:DeepSeek → platform.deepseek.com API Keys 页;智谱 → open.bigmodel.cn 用户中心;RCON → server.properties + 重启。

## 二、隐私数据(中危)

| 位置 | 内容 | 处理 |
|---|---|---|
| `server/logs/*.log` | 玩家名、IP、坐标、聊天 | 不入库(.gitignore);公开的节选需匿名化 |
| `server/ops.json`、`usercache.json` | 玩家 UUID / 名单 | 不入库 |
| `server/world*/` | 世界存档(建筑、玩家数据、命名牌上的名字) | 不入库 |
| `bots/*/memory.json`、`bots/*/histories/` | bot 与玩家的对话记忆 | 不入库 |
| `options.txt` | 个人设置 | 不入库 |
| 崩溃报告 `crash-reports/` | 含玩家名 | 不入库 |

## 三、私域信息(低危,自行斟酌)

| 位置 | 内容 | 处理 |
|---|---|---|
| 交接文档(交接.md) | 完整运维史、玩家名、付费/决策上下文 | 不入库;公开版 README 只保留通用运维知识 |
| 初始礼包函数 | 排除名单里的 AI/玩家名字 | 可保留(游戏昵称),或替换为占位 |
| botcompat 源码 | 无敏感内容 | 可入库 ✓ |
| i18n 汉化包 | 纯翻译文本 | 可入库 ✓(CC-BY-4.0) |

## 四、入库前自检命令(在工作区根目录执行)

```bash
# 1. 确认密钥特征不存在于待发布目录
grep -rn "sk-[a-zA-Z0-9]\{20,\}" maidtech-github/ && echo "!!! 发现明文 key"
# 2. 确认 RCON 密码不存在
grep -rn "maidtech_rcon" maidtech-github/ && echo "!!! 发现 RCON 密码"
# 3. 确认 world/logs/ops 等目录未被误拷贝
find maidtech-github/ -type d | grep -E 'world|logs|crash' && echo "!!! 发现私域目录"
```

三条都无输出 = 可以 `git init && git add . && git commit`。

## 五、若密钥已经泄露过

1. 立即轮换(所有涉及的平台上重新生成);
2. 轮换后旧 key 自动作废,历史泄露不再构成风险;
3. 才可以考虑清理 git 历史(filter-repo)——**顺序不能反**。
