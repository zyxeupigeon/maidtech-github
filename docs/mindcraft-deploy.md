# Mindcraft 部署指南

在一台电脑上部署 LLM 驱动的 Minecraft bot,连接到 MaidTech 服务器(或任何 modded Fabric 服务器)。

## 前置

- Node.js ≥ 20 LTS(nodejs.org)
- 服务器端已安装 `botcompat` 兼容 mod(本仓库 `botcompat/`,服主的事)
- 你自己的 LLM API key(DeepSeek:platform.deepseek.com)

## 部署步骤

1. 把 `mindcraft/` 整个文件夹复制到你的电脑任意位置;
2. 命令行进入文件夹,执行 `npm install`
   (postinstall 会自动应用 `patches/` 里的 mineflayer 补丁,无需手动);
3. 复制 `keys.example.json` 为 `keys.json`,填入你自己的 key:

   ```json
   { "DEEPSEEK_API_KEY": "sk-你的key" }
   ```

   **keys.json 永远不要分享/提交**;
4. 编辑 `settings.js`:
   - `"host"`:改成服务器局域网 IP(例如 `192.168.110.181`);
   - `"port"`:`25566`;
   - `"profiles"`:按需保留/添加 bot profile;
5. 启动:`node main.js` → bot 进服,聊天里直接说话即可指挥。

## 多 bot

- `settings.js` 的 `profiles` 数组加多个 profile 文件 = 多个 bot;
- 公开发言两个 bot 都会听;单独指挥用 `/msg <bot名> <指令>`;
- **bot 名字报给服主**,加入初始礼包排除名单(AI 不领新手礼包)。

## 换模型 / 调推理

`builder_X.json` 的 `model` 段:

```json
"model": {
  "api": "deepseek",
  "model": "deepseek-flash",
  "params": { "reasoning_effort": "high" }
}
```

- `reasoning_effort`:`off/low/medium/high` —— high 更聪明但 token 消耗成倍;
- 用智谱 GLM 时把 `api` 换成 `openai` + `url: https://open.bigmodel.cn/api/paas/v4`,
  key 放 `keys.json` 的 `OPENAI_API_KEY`。

## 常见问题

- bot 进服被踢 "client-requirement":服务器缺 `botcompat` mod(服主侧);
- bot 不动:看本窗口日志;服务器重启后 bot 不自动重连,重跑 `node main.js` 即可;
- keys.json 换了 key 不生效:重启 `node main.js`。

## 能力边界(重要)

bot 底层是 mineflayer,说的是**原版协议**——在 modded 服务器上:

- ✅ 能做:一切原版动作——移动/挖掘/建造/原版配方合成/打开原版容器(箱子/木桶)/战斗/跟随/搬运;
- ❌ 不能:操作 modded 机器的界面(TechReborn/Oritech 的机器 GUI 打不开)、合成 modded 物品(配方不进原版配方同步)、使用 modded 物品的定制功能;
- **进服兼容**由服务器侧的 `botcompat` mod 解决(抑制 mod 握手阶段的踢出)——bot 能不能进服取决于**服务器**,和 bot 所在的电脑无关;
- 使用原则:把 bot 当"**原版能力全通的工人**"用——搬运、基建、清怪、跑腿都行;modded 机器和物品交给玩家自己操作。
