param(
  [string]$cmd,
  [string]$RconPass = $env:MAIDTECH_RCON_PASS
)
$ErrorActionPreference = 'Stop'
if (-not $RconPass) { Write-Error '请先设置环境变量 MAIDTECH_RCON_PASS(RCON 密码)'; exit 1 }
$rhost = '127.0.0.1'; $rport = 25575
$tcp = [System.Net.Sockets.TcpClient]::new(); $tcp.Connect($rhost, $rport); $s = $tcp.GetStream()
function SendPkt($id, $type, [string]$body) {
  $b = [System.Text.Encoding]::UTF8.GetBytes($body)
  $ms = [System.IO.MemoryStream]::new(); $w = [System.IO.BinaryWriter]::new($ms)
  $w.Write([int]($b.Length + 10)); $w.Write([int]$id); $w.Write([int]$type)
  $w.Write($b); $w.Write([byte]0); $w.Write([byte]0); $w.Flush()
  return $ms.ToArray()
}
function ReadResp() {
  $ib = [byte[]]::new(4); [void]$s.Read($ib, 0, 4)
  $len = [BitConverter]::ToInt32($ib, 0)
  $rb = [byte[]]::new($len); [void]$s.Read($rb, 0, $len)
  return [System.Text.Encoding]::UTF8.GetString($rb, 8, $len - 10).TrimEnd([char]0)
}
$p = SendPkt 1 3 $RconPass; $s.Write($p, 0, $p.Length); $s.Flush(); [void](ReadResp)
$pp = SendPkt 2 2 $cmd; $s.Write($pp, 0, $pp.Length); $s.Flush(); Start-Sleep -Milliseconds 500
Write-Output (ReadResp)
$tcp.Close()
