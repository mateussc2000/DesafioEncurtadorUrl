# 📚 Exemplos de Uso da API

## 1️⃣ Encurtar uma URL (POST)

### Request
```bash
curl -X POST http://localhost:8080/api/v1/urls/shorten \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://www.github.com/usuario/projeto/very/long/path?param1=value1&param2=value2"
  }'
```

### Response (201 Created)
```json
{
  "id": 1,
  "shortCode": "abc123",
  "originalUrl": "https://www.github.com/usuario/projeto/very/long/path?param1=value1&param2=value2",
  "shortUrl": "http://localhost:8080/abc123",
  "createdAt": "2024-03-08T10:30:00",
  "clickCount": 0,
  "lastAccessed": null
}
```

---

## 2️⃣ Obter Informações da URL (GET)

### Request
```bash
curl http://localhost:8080/api/v1/urls/abc123
```

### Response (200 OK)
```json
{
  "id": 1,
  "shortCode": "abc123",
  "originalUrl": "https://www.github.com/usuario/projeto/very/long/path?param1=value1&param2=value2",
  "shortUrl": "http://localhost:8080/abc123",
  "createdAt": "2024-03-08T10:30:00",
  "clickCount": 5,
  "lastAccessed": "2024-03-08T11:45:22"
}
```

---

## 3️⃣ Redirecionar para URL Original (GET na raiz)

### Request
```bash
curl -L http://localhost:8080/abc123
```

### Response
```
HTTP/1.1 302 Found
Location: https://www.github.com/usuario/projeto/very/long/path?param1=value1&param2=value2
```

**Resultado**: O cliente será redirecionado automaticamente para a URL original  
**Efeito colateral**: `clickCount` será incrementado em 1

---

## 4️⃣ Obter Estatísticas (GET)

### Request
```bash
curl http://localhost:8080/api/v1/urls/stats/abc123
```

### Response (200 OK)
```json
{
  "id": 1,
  "shortCode": "abc123",
  "originalUrl": "https://www.github.com/usuario/projeto/very/long/path?param1=value1&param2=value2",
  "createdAt": "2024-03-08T10:30:00",
  "clickCount": 42,
  "lastAccessed": "2024-03-08T15:22:11"
}
```

---

## 5️⃣ Deletar uma URL (DELETE)

### Request
```bash
curl -X DELETE http://localhost:8080/api/v1/urls/abc123
```

### Response (204 No Content)
```
(vazio - status 204)
```

---

## 🔍 Exemplos com cURL em Lote

### Criar 5 URLs Encurtadas

```bash
#!/bin/bash

URLS=(
  "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
  "https://en.wikipedia.org/wiki/URL_shortening"
  "https://www.google.com/search?q=url+shortener"
  "https://github.com/trending"
  "https://stackoverflow.com/questions/tagged/java"
)

for url in "${URLS[@]}"; do
  echo "Encurtando: $url"
  curl -X POST http://localhost:8080/api/v1/urls/shorten \
    -H "Content-Type: application/json" \
    -d "{\"originalUrl\": \"$url\"}" \
    -s | jq '.'
  echo ""
done
```

### Simular Acessos

```bash
#!/bin/bash

SHORT_CODE="abc123"

for i in {1..10}; do
  echo "Acesso $i..."
  curl -L -s http://localhost:8080/$SHORT_CODE > /dev/null
  sleep 1
done

echo "Verificando estatísticas:"
curl http://localhost:8080/api/v1/urls/stats/$SHORT_CODE | jq '.'
```

---

## 🧪 Testes com Postman

### Importar para Postman

```json
{
  "info": {
    "name": "URL Shortener API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Encurtar URL",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"originalUrl\": \"https://example.com/very/long/url\"}"
        },
        "url": {
          "raw": "http://localhost:8080/api/v1/urls/shorten",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "v1", "urls", "shorten"]
        }
      }
    },
    {
      "name": "Redirecionar",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8080/abc123",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["abc123"]
        }
      }
    },
    {
      "name": "Obter Estatísticas",
      "request": {
        "method": "GET",
        "url": {
          "raw": "http://localhost:8080/api/v1/urls/stats/abc123",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "v1", "urls", "stats", "abc123"]
        }
      }
    }
  ]
}
```

---

## 🔐 Tratamento de Erros

### URL Não Encontrada

**Request**
```bash
curl http://localhost:8080/api/v1/urls/invalid123
```

**Response (404 Not Found)**
```
(corpo vazio)
```

---

### URL Vazia

**Request**
```bash
curl -X POST http://localhost:8080/api/v1/urls/shorten \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": ""}'
```

**Response (400 Bad Request)**
```
(corpo vazio)
```

---

## 💡 Dicas de Teste

1. **Use jq para formatar JSON**
   ```bash
   curl ... | jq '.'
   ```

2. **Teste em paralelo**
   ```bash
   for i in {1..100}; do
     curl http://localhost:8080/$SHORT_CODE &
   done
   wait
   ```

3. **Monitore o banco H2**
   - Acesse: http://localhost:8080/h2-console
   - Query: `SELECT * FROM shortened_urls ORDER BY created_at DESC;`

4. **Verifique logs em tempo real**
   ```bash
   tail -f target/spring.log
   ```

---

## 📊 Teste de Carga

```bash
#!/bin/bash
# Teste com Apache Bench

# 1. Criar uma URL encurtada
SHORT_URL=$(curl -s -X POST http://localhost:8080/api/v1/urls/shorten \
  -H "Content-Type: application/json" \
  -d '{"originalUrl": "https://example.com"}' | jq -r '.shortCode')

echo "Testing short code: $SHORT_URL"

# 2. Fazer 1000 requisições, 10 concorrentes
ab -n 1000 -c 10 http://localhost:8080/$SHORT_URL

# 3. Verificar estatísticas
curl http://localhost:8080/api/v1/urls/stats/$SHORT_URL | jq '.'
```

**Esperado**: 1000 cliques registrados

---

## 🚀 Performance

### Benchmark Típico

- **Criar URL**: ~50ms
- **Redirecionar**: ~20ms
- **Obter stats**: ~10ms
- **Taxa de throughput**: ~1000 URLs/segundo (em uma máquina moderna)


