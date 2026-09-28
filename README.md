# Mindcraft 部署分支

> 本分支只包含 **mindcraft bot 部署内容** —— 给要在自己电脑上部署 AI bot、
> 连接到 MaidTech 服务器的朋友。服务器工具链在 `main` 分支。

## 这是什么

[mindcraft](mindcraft/) 是基于 [kolbyml/Mindcraft](https://github.com/kolbyml/Mindcraft)
的调优 fork:LLM 驱动的 Minecraft bot,连接到 modded Fabric 服务器,
支持 DeepSeek / 智谱 GLM 双模型、中文人设、指令优先、补丁自动应用。

## 部署步骤(五步)

1. 安装 [Node.js ≥ 20 LTS](https://nodejs.org/);
2. 复制 `mindcraft/` 文件夹到任意位置;
3. 命令行进入文件夹,执行 `npm install`
   (`patches/` 里的 mineflayer 补丁会自动应用);
4. 复制 `keys.example.json` 为 `keys.json`,填入**你自己的** DeepSeek API key;
5. 编辑 `settings.js`:
   - `"host"`:改成服务器局域网 IP(`192.168.110.181`);
   - `"port"`:`25566`;
   - `"profiles"`:按需保留/添加你的 bot profile;

启动:`node main.js` → bot 进服,聊天里直接说话指挥。

详细说明与常见问题:[docs/mindcraft-deploy.md](docs/mindcraft-deploy.md)。

## 多 bot 与礼节

- 一个进程可挂多个 bot(`settings.js` 的 `profiles` 数组);
- 公开发言所有 bot 都会听;单独指挥用 `/msg <bot名> <指令>`;
- **bot 名字报给服主**,加入初始礼包排除名单(AI 不领新手礼包);
- bot 在服务器上权限等同真人,服主可用 ops.json 管理。

## 能力边界

bot 底层是 mineflayer(原版协议),在 modded 服务器上:

- ✅ 能做:一切原版动作——移动/挖掘/建造/原版配方合成/打开原版容器/战斗/跟随/搬运;
- ❌ 不能:操作 modded 机器 GUI(TechReborn/Oritech 等)、合成 modded 物品;
- 进服兼容由服务器侧的 `botcompat` mod 负责,bot 电脑无需任何额外 mod。

详见 [docs/mindcraft-deploy.md](docs/mindcraft-deploy.md) 的能力边界章节。
