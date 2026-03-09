# ⚡ Quick Start Guide - 5 Minutos

## 1️⃣ Compilar (2 minutos)

```bash
cd F:\DesafioEncurtadorUrl
mvn clean install
```

✅ Esperado: `BUILD SUCCESS`

---

## 2️⃣ Executar (1 minuto)

```bash
mvn spring-boot:run
```

✅ Esperado: 
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___  '_  '_  '_ \/ _`  \ \ \ \
 \\/  ___) _)      (_   ) ) ) )
  '  ____ .___ __ _\__,  / / / /
 =========_==============___/=/_/_/_/

 :: Spring Boot :: (v4.0.3)

Tomcat started on port(s): 8080
```

---

## 3️⃣ Testar API (2 minutos)

### Terminal 1: Criar URL Curta

```bash
curl -X POST http://localhost:8080/api/v1/urls/shorten \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://www.github.com/usuario/repositorio/issues?page=5&status=open"
  }'
```

**Resposta esperada:**
```json
{
  "id": 1,
  "shortCode": "abc123",
  "originalUrl": "https://www.github.com/...",
  "shortUrl": "http://localhost:8080/abc123",
  "createdAt": "2024-03-08T10:30:00",
  "clickCount": 0,
  "lastAccessed": null
}
```

✅ **Copie o `shortCode`** (ex: `abc123`)

---

### Testar Redirecionamento

```bash
curl -L http://localhost:8080/abc123
```

**O que acontece:**
1. GET /abc123 retorna `302 Found`
2. Browser redireciona para URL original automaticamente
3. Click count é incrementado

✅ **Verificar estatísticas:**

```bash
curl http://localhost:8080/api/v1/urls/stats/abc123
```

**Resposta:**
```json
{
  "id": 1,
  "shortCode": "abc123",
  "originalUrl": "https://www.github.com/...",
  "createdAt": "2024-03-08T10:30:00",
  "clickCount": 1,
  "lastAccessed": "2024-03-08T10:31:15"
}
```

---

## 🎯 Interfaces Web

### Swagger UI (Documentação Interativa)
```
http://localhost:8080/swagger-ui/index.html
```

Aqui você pode:
- ✓ Ver todos os endpoints
- ✓ Testar a API diretamente
- ✓ Ver schemas de requisição/resposta

### H2 Console (Banco de Dados)
```
http://localhost:8080/h2-console
```

**Credenciais:**
- JDBC URL: `jdbc:h2:file:./data/shortenerdb`
- User: `sa`
- Password: (deixar vazio)

**Comandos SQL úteis:**
```sql
-- Ver todas as URLs
SELECT * FROM shortened_urls;

-- Ver URL específica
SELECT * FROM shortened_urls WHERE short_code = 'abc123';

-- Ver mais acessadas
SELECT * FROM shortened_urls ORDER BY click_count DESC LIMIT 10;

-- Ver criadas hoje
SELECT * FROM shortened_urls WHERE DATE(created_at) = CURDATE();

-- Contar total
SELECT COUNT(*) FROM shortened_urls;
```

---

## 📊 Teste Completo em 5 Passos

```bash
#!/bin/bash

echo "1. Criando primeira URL..."
RESP1=$(curl -s -X POST http://localhost:8080/api/v1/urls/shorten \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"https://example.com/1"}')
CODE1=$(echo $RESP1 | grep -o '"shortCode":"[^"]*' | cut -d'"' -f4)
echo "   Short code: $CODE1"

echo ""
echo "2. Criando segunda URL..."
RESP2=$(curl -s -X POST http://localhost:8080/api/v1/urls/shorten \
  -H "Content-Type: application/json" \
  -d '{"originalUrl":"https://example.com/2"}')
CODE2=$(echo $RESP2 | grep -o '"shortCode":"[^"]*' | cut -d'"' -f4)
echo "   Short code: $CODE2"

echo ""
echo "3. Simulando 5 cliques na primeira URL..."
for i in {1..5}; do
  curl -s -L http://localhost:8080/$CODE1 > /dev/null
  echo "   Click $i..."
done

echo ""
echo "4. Verificando estatísticas..."
curl -s http://localhost:8080/api/v1/urls/stats/$CODE1 | grep clickCount

echo ""
echo "5. Deletando primeira URL..."
curl -s -X DELETE http://localhost:8080/api/v1/urls/$CODE1
echo "   Deletado!"

echo ""
echo "6. Tentando acessar URL deletada (deve retornar 404)..."
curl -s -o /dev/null -w "Status: %{http_code}\n" http://localhost:8080/$CODE1
```

---

## 🧪 Executar Testes

```bash
mvn test
```

**Resultado esperado:**
```
Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

---

## 🔍 Inspecionar Dados

### Ver todas as URLs criadas
```bash
curl http://localhost:8080/h2-console
# Executar: SELECT * FROM shortened_urls;
```

### Ver URLs mais populares
```bash
# No H2 Console
SELECT * FROM shortened_urls 
ORDER BY click_count DESC 
LIMIT 5;
```

### Ver tamanho do banco
```bash
# No H2 Console
SELECT COUNT(*) as total FROM shortened_urls;
```

---

## 🛑 Parar a Aplicação

```bash
# Terminal onde está rodando:
Ctrl + C
```

---

## ⚠️ Troubleshooting Rápido

### Erro: Port 8080 já em uso
```bash
# Use outra porta
vim src/main/resources/application.properties
# Adicione: server.port=8081
```

### Erro: H2 locked
```bash
# Delete o lock file
rm ./data/shortenerdb.lock
```

### Testes falhando
```bash
mvn clean test
```

---

## 📚 Próximos Passos

1. **Entender a arquitetura**: Leia [IMPLEMENTATION.md](IMPLEMENTATION.md)
2. **Ver exemplos avançados**: Consulte [EXAMPLES.md](EXAMPLES.md)
3. **Conhecer os fluxos**: Veja [WORKFLOWS.md](WORKFLOWS.md)
4. **Ler sobre a decisão**: Confira [analise_estrategias.md](analise_estrategias.md)

---

## ✨ Resumo do Que Você Tem

✅ API REST completa para encurtar URLs  
✅ Banco de dados H2 com persistência  
✅ Gerador de IDs curtos (Base62)  
✅ Detecção de duplicatas  
✅ Rastreamento de cliques  
✅ 14 testes automatizados  
✅ Documentação completa  
✅ Console H2 Web  
✅ Swagger UI  

---

**Tudo pronto para usar! 🚀**

Dúvidas? Consulte os arquivos de documentação no repositório.

