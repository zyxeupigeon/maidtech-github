# 朋友包清单(Friend Pack Manifest)

朋友分发包:`_share_打包/朋友包_女仆生电客户端_v3`(Fabric 1.21.1 / loader 0.19.5)。
**模组 jar 不进 git**(会让仓库无限膨胀),统一走 [GitHub Releases](../../releases) 发 zip。

## 同步铁律

服务器 mods 增删时,必须三处同步:`server/mods` + 主客户端 + 本朋友包,然后发一个新 Release。

## 2026-09-28 同步记录(friendpack-20260928)

**新增 22 个**(此前漏同步,导致朋友注册表不一致进不了服):

- 枪械:TaCZ Refabricated 0.7.0 + 灯光配件包 2.8.2(替代已移除的 JEG)
- 内容:BuildPaste、Companions 1.3.5、DoggyTalents 1.18.63、GrowableOres 3.8.0 + TLM 兼容、Knight Lib 2.0.3、鞘翅合成、潜影壳合成、经验瓶配方、Traveler's Backpack 10.1.39
- 魔法:Spell Engine 1.10.5 + Spell Power 1.6.0 + Wizards 3.1.2(+structure_pool_api)+ Runes 1.3.2
- 功能:AI-Player 1.0.5.3、Easy Shulker Boxes + PuzzlesLib 21.1.60、Trinkets 3.10.0、护甲模型 API

**移除 2 个**:packedup 1.1.1(服务器端已删)、PuzzlesLib 21.1.56(升级替换)。

**对账基线**:与 `server/mods`(90 个)比对,朋友包独有 = 客户端增强(sodium/iris/litematica 等),服务器专有(Chunky/botcompat/carpet 系/krypton/modernfix/noisium/ledger/spark/tabtps/QuickBackupMulti 等)不进客户端包。

## 分发规则

- 整包 zip ≈ 405MB,增量 zip 按本次变更单独打。
- 私有仓库的 Release 只有协作者能下载;新朋友需要先加协作者(或临时转公开)。
- 朋友连接地址随 DHCP 可能漂移,以交接文档/最新通知为准(2026-09-28 为 `192.168.110.138:25566`)。

## 2026-09-28 二次同步(friendpack-dims-20260928)

新增 8 个:Deeper and Darker 1.3.3(+owo-lib 0.13.0-alpha.15)、Eternal Starlight 0.9.1、Paradise Lost 2.4.6-beta、Dimensional Doors 6.2.2、The Bumblezone 7.16.0(+resourcefullib 3.0.12、midnightlib 1.9.3)。三处(服务器/主客户端/朋友包)已同步,服务器加载验证通过(8 个全绿)。

**同时服务器已重置全新世界**:旧档备份为 `server/world_old_survival_20260928_1814`,新种子 `-5036040615127431238`(随机),level-seed 已清空、Chunky 预生成(0,0 r3000)进行中,maidtech-starter 礼包数据包已装回。朋友需自行清理本机 Xaero 缓存(见 Release 说明)。

## 2026-09-28 三次热修(friendpack-keybindfix-20260928)

朋友包补上 `keybind_fix-1.0.2.jar`(Keybind Fix,键位冲突修复)——此前只有包作者主客户端有,朋友包漏发导致室友 TaCZ 左右键交互失灵。曾误判发布 TooManyShortcuts 方案(已撤回删除),以包作者实测的 keybind_fix 配置为准;两侧客户端均已移除 too-many-shortcuts 避免双重补丁。

## 2026-09-28 四次:按键方案对齐(friendpack-keybinds-config-20260928)

朋友包 options.txt 的 9 行按键配置已对齐包作者方案(冲突键解绑:xaero 设置/新路标/iris 重载/ad_astra 收音机/freecam/simplytooltips/slashblade/YSM,清 packedup 残留行),并补入 tacz-client/common/pre.toml。Release 附 options.txt+config zip 供已装机的朋友覆盖;真修复本体仍是 keybind_fix-1.0.2.jar。

## 2026-09-29:Polymorph(合成配方冲突解决器)

polymorph-fabric-1.2.0+1.21.1 三处同步(服务器/主客户端/朋友包),服务器加载验证通过。解决多 mod 同形状配方撞车时的产出随机问题。

## 2026-09-29 二:AI 阵容调整

- **移除 AI-Player**(效果不佳下架,服务器+朋友包已清;包作者主客户端那只 jar 因游戏运行锁定,下次退出游戏时删)。
- **新增 CreatureChat 3.0.0**(三处同步,对任意生物 LLM 对话):服务器级配置已完成(url=api.deepseek.com/chat/completions,key/model 服务端注入),朋友无需自行配 key。
- Smart Villagers AI 评估不装:依赖 Simple Voice Chat 语音基建,功能被 CreatureChat 覆盖。
- ollama-shim 退役(AI-Player 专属,start-all-detached 已同步)。
- **botwatch.ps1 上线**:每 2 分钟自检"服务器在线但 mindcraft bot 缺席"→自动重启 mindcraft(10 分钟冷却);`botwatch_off.txt` 可暂停。

## 2026-09-30:numen 升级 0.1.3 + CrossTalk(cross-talk 让 AI 互相听见)

- **numen 0.1.2→0.1.3**(三处同步;0.1.3 内嵌 numen_api 0.1.3,服务器另补独立 api jar——crosstalk 的运行时类加载需要独立 mod 形态的 api)。
- **新增 crosstalk-fabric/collab 0.1.0**(用户室友 a1206122381 的项目 https://github.com/a1206122381-cmyk/numen-crosstalk):让 numen 同伴互相说话(拉模型:speech.jsonl/heard.jsonl 管道,不污染主人窗口)。服务器日志确认"已注册工具: say / hear / who_is_here"。
- **版本依赖链(重要)**:crosstalk 0.1.0 需要 numen **0.1.3**(NumenPlugins 类 0.1.2 没有),不可降级混搭。
