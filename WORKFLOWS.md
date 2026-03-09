# 🎬 Fluxos de Operação

## Fluxo 1: Encurtar uma URL

```
┌─────────┐                                           ┌────────┐
│ Cliente │                                           │ Banco  │
└────┬────┘                                           └───┬────┘
     │                                                    │
     │ POST /api/v1/urls/shorten                        │
     │ { originalUrl: "https://example.com/..." }      │
     ├──────────────────────────────────────────────────>│
     │                                                    │
     │ 1. Recebe request                               │
     │ 2. Valida URL                                   │
     │ 3. Busca URL existente                         │
     │    (SELECT * WHERE originalUrl = ?)             │
     │    ├─────────────────────────────────────────────>│
     │    │                                              │
     │    │ Já existe? Retorna código existente        │
     │    │<─────────────────────────────────────────────┤
     │    │                                              │
     │ 4. Se novo: Gera shortCode único                │
     │    - Aleatório (6 chars)                         │
     │    - Verifica se existe                         │
     │    - Retry se houver colisão                    │
     │                                                    │
     │ 5. Salva no banco                               │
     │    (INSERT INTO shortened_urls ...)             │
     │    ├──────────────────────────────────────────────>│
     │    │ ✓ Inserido                                  │
     │    │<─────────────────────────────────────────────┤
     │                                                    │
     │ 6. Retorna resposta                             │
     │<───────────────────────────────────────────────────┤
     │ {                                                │
     │   shortCode: "abc123",                           │
     │   shortUrl: "http://localhost:8080/abc123"      │
     │   ...                                            │
     │ }                                                │
```

---

## Fluxo 2: Redirecionar para URL Original

```
┌─────────┐                                           ┌────────┐
│ Cliente │                                           │ Banco  │
└────┬────┘                                           └───┬────┘
     │                                                    │
     │ GET /abc123 (ou click em link curto)            │
     ├──────────────────────────────────────────────────>│
     │                                                    │
     │ 1. Valida formato do shortCode                  │
     │ 2. Busca URL no banco                           │
     │    (SELECT * WHERE shortCode = 'abc123')        │
     │    ├──────────────────────────────────────────────>│
     │    │                                              │
     │    │ Encontrado: ShortenedUrl                    │
     │    │<─────────────────────────────────────────────┤
     │                                                    │
     │ 3. Incrementa clickCount                         │
     │    (UPDATE ... SET clickCount = clickCount + 1)  │
     │    ├──────────────────────────────────────────────>│
     │    │ ✓ Atualizado                                │
     │    │<─────────────────────────────────────────────┤
     │                                                    │
     │ 4. Retorna 302 Found                            │
     │    com Location header                           │
     │<───────────────────────────────────────────────────┤
     │ HTTP/1.1 302 Found                              │
     │ Location: https://example.com/...              │
     │                                                    │
     │ (Browser segue redirecionamento automaticamente)│
     ├─ https://example.com/...
```

---

## Fluxo 3: Obter Estatísticas

```
┌─────────┐                                           ┌────────┐
│ Cliente │                                           │ Banco  │
└────┬────┘                                           └───┬────┘
     │                                                    │
     │ GET /api/v1/urls/stats/abc123                   │
     ├──────────────────────────────────────────────────>│
     │                                                    │
     │ 1. Busca URL no banco                           │
     │    (SELECT * WHERE shortCode = 'abc123')        │
     │    ├──────────────────────────────────────────────>│
     │    │                                              │
     │    │ Encontrado: ShortenedUrl                    │
     │    │<─────────────────────────────────────────────┤
     │                                                    │
     │ 2. Retorna JSON com:                            │
     │    - clickCount                                  │
     │    - lastAccessed                               │
     │    - createdAt                                  │
     │<───────────────────────────────────────────────────┤
     │ {                                                │
     │   shortCode: "abc123",                           │
     │   clickCount: 42,                                │
     │   lastAccessed: "2024-03-08T15:22:11"           │
     │   ...                                            │
     │ }                                                │
```

---

## Geração de Short Code (Algoritmo Detalhado)

```
┌─────────────────────────────────────────────────────┐
│ Requisição: Gerar shortCode único                  │
└─────────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────┐
│ Iteração 1 (até MAX_RETRIES=10)                    │
└─────────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────┐
│ 1. Gera aleatório:                                 │
│    charset = "0-9a-zA-Z" (62 caracteres)           │
│    length = 6                                       │
│    código = random sample de 6 caracteres          │
│    Exemplo: "abc123"                               │
└─────────────────────────────────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────────────────┐
│ 2. Valida formato:                                 │
│    matches("^[0-9A-Za-z]{3,10}$") ?               │
└─────────────────────────────────────────────────────┘
              │
         ┌────┴────┐
         ▼         ▼
       SIM       NÃO (retry)
         │         │
         ▼         ▼
┌──────────────┐  (volta à iteração)
│ 3. Verifica  │
│  no banco:   │
│ EXISTS BY    │
│ SHORT_CODE   │
└──────────────┘
         │
    ┌────┴────┐
    ▼         ▼
  EXISTE    NÃO EXISTE
    │         │
    ▼         ▼
  RETRY   ✓ RETORNA
  (i++)   ("abc123")
    │
    ▼
 i < 10?
 ├─ SIM → volta à iteração 1
 └─ NÃO → ERRO (exceção)
```

---

## Fluxo de Detecção de Duplicata

```
Usuario 1 → POST /shorten                  Usuario 2 → POST /shorten
            URL: "https://example.com"                 URL: "https://example.com"
                      │                                        │
                      ▼                                        ▼
            [Service: shortenUrl]                   [Service: shortenUrl]
                      │                                        │
        ┌─────────────┴──────────────────────────────────────┐
        │ SELECT * FROM shortened_urls                       │
        │ WHERE original_url = "https://example.com"         │
        └─────────────┬──────────────────────────────────────┘
                      ▼
        ┌──────────────────────────────────────┐
        │ Encontrado? (SIM)                    │
        │ shortCode: "abc123"                  │
        │ clickCount: 5                        │
        └──────────────────────────────────────┘
           │                        │
    User 1 │                        │ User 2
      ▼                            ▼
    Retorna                    Retorna
    mesmo código               mesmo código
    "abc123"                   "abc123"
    
    ✓ Não cria duplicata!
    ✓ Reutiliza código existente
    ✓ Economia de armazenamento
```

---

## Estrutura de Dados em Memória (Cache)

```
ShortenedUrl Entity (JPA)
│
├─ id: 1 (PK)
├─ shortCode: "abc123" (UNIQUE INDEX)
├─ originalUrl: "https://example.com/path"
├─ createdAt: 2024-03-08T10:30:00
├─ updatedAt: 2024-03-08T10:30:00
├─ clickCount: 42
└─ lastAccessed: 2024-03-08T15:22:11
```

---

## Operações do Banco de Dados

### CREATE (Insert)
```sql
INSERT INTO shortened_urls 
  (short_code, original_url, created_at, updated_at, click_count)
VALUES 
  ('abc123', 'https://example.com/...', '2024-03-08T10:30:00', '2024-03-08T10:30:00', 0)
```

### READ (Select por shortCode)
```sql
SELECT * FROM shortened_urls 
WHERE short_code = 'abc123'
```

### READ (Select por originalUrl)
```sql
SELECT * FROM shortened_urls 
WHERE original_url = 'https://example.com/...'
```

### UPDATE (Incrementar cliques)
```sql
UPDATE shortened_urls 
SET click_count = click_count + 1, last_accessed = NOW()
WHERE short_code = 'abc123'
```

### DELETE
```sql
DELETE FROM shortened_urls 
WHERE short_code = 'abc123'
```

### INDEX (para performance)
```sql
CREATE UNIQUE INDEX idx_short_code ON shortened_urls(short_code);
CREATE INDEX idx_created_at ON shortened_urls(created_at);
```

---

## Cronograma de Evolução

```
Mês 1: Implementação Atual (H2)
├─ ✓ Criar entidade JPA
├─ ✓ Gerar short codes únicos
├─ ✓ Endpoints REST CRUD
├─ ✓ Detecção de duplicata
└─ ✓ Estatísticas básicas

Mês 2: Otimizações (H2 com Melhorias)
├─ Adicionar paginação
├─ Cache de resultados frequentes
├─ Validação de URLs
└─ Rate limiting

Mês 3: Migração para PostgreSQL
├─ Trocar datasource H2 → PostgreSQL
├─ Indices otimizados
├─ Connection pooling
└─ Testes de carga

Mês 4: Cache Distribuído (Redis)
├─ Redis para short_code → originalUrl
├─ Cache invalidation
├─ Métricas de hit/miss
└─ Scalability horizontal

Mês 6: Analytics Avançado
├─ Elasticsearch para análise
├─ Geolocalização de cliques
├─ Device tracking
└─ Dashboard de estatísticas
```


