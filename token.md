All done. Here's the complete answer based on the actual code (`SecurityConfig.java`) and verified against the running app.

---

## 1️⃣ `X-Balance-Token` (para `POST /api/v1/pesagens`)

__Formato__: é um __texto simples estático__ — __NÃO__ é JWT e __NÃO__ leva o prefixo `Bearer `. A validação é uma comparação exata de string (`tokenRecebido.equals(tokenEsperado)`).

__Valor padrão__: `balanca-dev-token`

- Definido em `application.yaml`: `app.balance.token: ${BALANCE_TOKEN:balanca-dev-token}`
- Sobrescrevível pela variável de ambiente `BALANCE_TOKEN` (no `docker-compose.yml`: `BALANCE_TOKEN: "balanca-dev-token"`)

__Como enviar no header__:

```javascript
X-Balance-Token: balanca-dev-token
```

__Exemplos__:

```bash
curl -X POST http://localhost:8080/api/v1/pesagens \
  -H "Content-Type: application/json" \
  -H "X-Balance-Token: balanca-dev-token" \
  -d '{"id":"balanca-01","plate":"ABC1234","weight":10500.0}'
```

```powershell
# PowerShell
$body = '{"id":"balanca-01","plate":"ABC1234","weight":10500.0}'
Invoke-WebRequest -Uri "http://localhost:8080/api/v1/pesagens" -Method Post `
  -ContentType "application/json" `
  -Headers @{"X-Balance-Token"="balanca-dev-token"} `
  -Body $body
```

> ⚠️ __Você recebeu o erro 401 porque__: o header estava ausente, tinha valor diferente de `balanca-dev-token`, ou foi enviado com `Bearer `(errado). Se você mudou `BALANCE_TOKEN` no compose, use o valor que você definiu.

---

## 2️⃣ JWT (para os endpoints administrativos)

__O app NÃO tem endpoint de login__ — não existe `/api/auth/login`. O token é um __JWT HS256__ assinado com uma chave secreta, que você gera externamente.

__Configuração atual__:

- Algoritmo: `HS256` (HMAC-SHA256)

- Segredo: env `app.security.jwt.secret`; __se vazio__ (caso default do docker-compose), o fallback dev é a string:

  > `chave-desenvolvimento-transporte-2026`

__Como enviar__:

```javascript
Authorization: Bearer <SEU_JWT>
```

### Como gerar o JWT

__Opção A — Python:__

```python
import base64, hashlib, hmac, json, time, urllib.parse

secret = "chave-desenvolvimento-transporte-2026"
def b64url(data):  # bytes -> str
    return urllib.parse.quote(base64.urlsafe_b64encode(data).decode().rstrip("="))
def b64(obj):      # dict -> str
    return b64url(json.dumps(obj, separators=(",", ":")).encode())

header  = b64({"alg": "HS256", "typ": "JWT"})
payload = b64({"sub": "admin", "iat": int(time.time()), "exp": int(time.time()) + 3600})
sig = hmac.new(secret.encode(), f"{header}.{payload}".encode(), hashlib.sha256).digest()
print(f"{header}.{payload}.{b64url(sig)}")
```

__Opção B — Node.js:__

```javascript
const crypto = require("crypto");
const secret = "chave-desenvolvimento-transporte-2026";
const b64 = (o) => Buffer.from(JSON.stringify(o)).toString("base64url");
const header  = b64({ alg: "HS256", typ: "JWT" });
const payload = b64({ sub: "admin", iat: Math.floor(Date.now()/1000), exp: Math.floor(Date.now()/1000)+3600 });
const sig = crypto.createHmac("sha256", secret).update(header+"."+payload).digest("base64url");
console.log(`${header}.${payload}.${sig}`);
```

__Opção C — manual (jwt.io):__ acesse [](https://jwt.io)<https://jwt.io>, use:

- Header: `{"alg":"HS256","typ":"JWT"}`
- Payload: `{"sub":"admin","exp":<epoch futuro>}`
- Assinatura (HMAC-SHA256): o segredo `chave-desenvolvimento-transporte-2026`

__Opção D — PowerShell__ (gerado e testado agora contra a API em execução — retornou `HTTP 200`):

```powershell
$secret = "chave-desenvolvimento-transporte-2026"
function To-B64Url([byte[]]$b){ [Convert]::ToBase64String($b).TrimEnd('=').Replace('+','-').Replace('/','_') }
function J($o){ To-B64Url ([Text.Encoding]::UTF8.GetBytes(($o|ConvertTo-Json -Compress))) }
$h  = J @{alg="HS256";typ="JWT"}
$n  = [int][DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
$p  = J @{sub="admin";iat=$n;exp=$n+3600}
$hm = New-Object System.Security.Cryptography.HMACSHA256
$hm.Key = [Text.Encoding]::UTF8.GetBytes($secret)
$jwt = "$h.$p." + (To-B64Url ($hm.ComputeHash([Text.Encoding]::UTF8.GetBytes("$h.$p"))))
Write-Host $jwt   # use como "Bearer $jwt"
```

__Exemplo de uso:__

```bash
curl -H "Authorization: Bearer <SEU_JWT>" http://localhost:8080/api/v1/relatorios/eficiencia
```

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/api/v1/relatorios/eficiencia" `
  -Headers @{"Authorization"="Bearer <SEU_JWT>"}
```

---

## Nota sobre produção 🔐

Para uso fora de dev, defina um secret forte na configuração (≥ 32 bytes):

```yaml
# application.yaml
app:
  security:
    jwt:
      secret: ${JWT_SECRET:}
```

e exporte `JWT_SECRET` no ambiente/`docker-compose.yml` — assim o fallback hardcoded não é usado.
