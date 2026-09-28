// ollama-shim.mjs — 让 AI-Player(硬编码连 Ollama localhost:11434)使用 DeepSeek API
// 用法: node ollama-shim.mjs  (建议用 WMI detached 启动,见 start-ollama-shim.bat)
// 协议: 收 Ollama /api/chat 格式 → 转 DeepSeek /chat/completions → 转回 Ollama 格式
import http from 'http'
import fs from 'fs'

const SETTINGS = 'C:/PCL-main/server/config/settings.json5'
const API_URL = 'https://api.deepseek.com/chat/completions'
const MODEL_MAP = { 'deepseek-r1': 'deepseek-flash', 'qwen-qwq': 'deepseek-flash' }
const PORT = 11434

function readKey() {
  // env 优先(便于复用/部署),回落到本地 AI-Player 配置文件
  if (process.env.DEEPSEEK_API_KEY) return process.env.DEEPSEEK_API_KEY
  try {
    const t = fs.readFileSync(SETTINGS, 'utf8')
    const m = t.match(/"customApiKey"\s*:\s*"([^"]+)"/)
    return m ? m[1] : ''
  } catch { return '' }
}

function json(res, code, obj) {
  const body = JSON.stringify(obj)
  res.writeHead(code, { 'Content-Type': 'application/json', 'Content-Length': Buffer.byteLength(body) })
  res.end(body)
}

function slog(line) {
  try { fs.appendFileSync('C:/PCL-main/_tools/ollama-shim.log', line + '\n') } catch {}
}

// 伪 embeddings:nomic-embed-text = 768 维,按 token 哈希散布,归一化
function pseudoEmbedding(text) {
  const v = new Array(768).fill(0)
  const tokens = String(text).toLowerCase().match(/[a-z0-9\u4e00-\u9fff]+|[^\s]/g) || ['']
  for (const tk of tokens) {
    let h = 2166136261
    for (let i = 0; i < tk.length; i++) { h ^= tk.charCodeAt(i); h = Math.imul(h, 16777619) }
    v[Math.abs(h) % 768] += 1
    v[Math.abs(Math.imul(h, 31)) % 768] += 0.5
  }
  const norm = Math.sqrt(v.reduce((s, x) => s + x * x, 0)) || 1
  return v.map((x) => x / norm)
}

const server = http.createServer(async (req, res) => {
  const ts = () => new Date().toISOString().slice(11, 19)
  if (/embeddings/.test(req.url)) {
    let raw = ''
    for await (const c of req) raw += c
    let inReq
    try { inReq = JSON.parse(raw) } catch { return json(res, 400, { error: 'bad json' }) }
    const inputs = Array.isArray(inReq.input) ? inReq.input : [inReq.input || '']
    slog(`req ${req.url} embeddings model=${inReq.model} n=${inputs.length}`)
    return json(res, 200, {
      object: 'list',
      data: inputs.map((t, i) => ({ object: 'embedding', index: i, embedding: pseudoEmbedding(t) })),
      model: inReq.model || 'nomic-embed-text',
      usage: { prompt_tokens: 0, total_tokens: 0 },
    })
  }
  if (req.method !== 'POST' || !/\/api\/chat|\/chat/.test(req.url)) {
    // AI-Player 生成时会 ping 一次服务端(如 /models),回空 200 避免它报"无法连接"
    if (req.method === 'GET') { slog(`req GET ${req.url} -> 200 (ping)`); res.writeHead(200, { 'Content-Type': 'application/json' }); return res.end('{}') }
    return json(res, 404, { error: 'only /api/chat and /embeddings supported' })
  }
  let raw = ''
  for await (const c of req) raw += c
  let inReq
  try { inReq = JSON.parse(raw) } catch { return json(res, 400, { error: 'bad json' }) }

  const model = MODEL_MAP[inReq.model] || 'deepseek-flash'
  const key = readKey()
  if (!key) { console.error(`[${ts()}] no api key`); return json(res, 500, { error: 'no key' }) }

  const outReq = {
    model,
    // AI-Player 发大写 "SYSTEM" 等 role,DeepSeek 只认小写;只保留 role/content
    messages: (inReq.messages || []).map((m) => ({ role: String(m.role || 'user').toLowerCase(), content: m.content ?? '' })),
    stream: false,
    max_tokens: 2048,
  }
  const payload = JSON.stringify(outReq)
  slog(`req ${req.url} model=${inReq.model}->${model} msgs=${outReq.messages.length} stream=${!!inReq.stream} keys=${Object.keys(inReq)} body0=${JSON.stringify((inReq.messages||[])[0]||{}).slice(0,150)}`)

  try {
    const ac = new AbortController()
    const timer = setTimeout(() => ac.abort(), 180000)
    const r = await fetch(API_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: 'Bearer ' + key },
      body: payload,
      signal: ac.signal,
    })
    clearTimeout(timer)
    const data = await r.json()
    if (data.error) {
      slog(`API error: ${JSON.stringify(data.error).slice(0,300)}`)
      return json(res, 502, { error: String(data.error.message || data.error).slice(0, 200) })
    }
    const msg = data.choices?.[0]?.message || {}
    const content = msg.content || ''
    const thinking = msg.reasoning_content || ''

    if (inReq.stream) {
      // Ollama 流式 = ndjson 分块
      res.writeHead(200, { 'Content-Type': 'application/x-ndjson' })
      res.write(JSON.stringify({ model, created_at: new Date().toISOString(), message: { role: 'assistant', content: '' }, done: false }) + '\n')
      if (thinking) res.write(JSON.stringify({ model, created_at: new Date().toISOString(), message: { role: 'assistant', thinking }, done: false }) + '\n')
      for (const piece of content.match(/[\s\S]{1,120}/g) || []) {
        res.write(JSON.stringify({ model, created_at: new Date().toISOString(), message: { role: 'assistant', content: piece }, done: false }) + '\n')
      }
      res.end(JSON.stringify({ model, created_at: new Date().toISOString(), message: { role: 'assistant', content: '' }, done: true, total_duration: 1 }) + '\n')
    } else {
      json(res, 200, {
        model,
        created_at: new Date().toISOString(),
        message: { role: 'assistant', content, ...(thinking ? { thinking } : {}) },
        done: true,
        total_duration: 1,
        eval_count: data.usage?.completion_tokens || 0,
      })
    }
    slog(`-> 200 content=${content.length}ch`)
  } catch (e) {
    slog(`upstream fail: ${e.message}`)
    json(res, 502, { error: 'upstream: ' + e.message })
  }
})

server.listen(PORT, '127.0.0.1', () => console.log(`[ollama-shim] listening on 127.0.0.1:${PORT}`))
