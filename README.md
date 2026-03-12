# 🔗 URL Shortener — API de Encurtamento de URLs

> API REST para encurtar URLs longas em códigos curtos e únicos (ex: `abc123`).  
> Construída com **Spring Boot 4.0.3**, **Java 21** e **H2 Database** com persistência em arquivo.

---

## 📑 Índice

- [Stack](#-stack)
- [Execução Local](#-execução-local)
- [Docker](#-docker)
- [Endpoints](#-endpoints)
- [Autenticação](#-autenticação-x-api-key)
- [Swagger UI](#-swagger-ui)
- [H2 Console](#-h2-console)
- [Testes](#-testes)
- [Arquitetura](#-arquitetura)
- [Algoritmo Base62](#-algoritmo-base62)
- [Estrutura do Projeto](#-estrutura-do-projeto)
- [Troubleshooting](#-troubleshooting)

---

## 🛠️ Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.0.3 |
| Web | Spring Web MVC |
| Persistência | Spring Data JPA + Hibernate 7 |
| Banco de Dados | H2 2.4 (arquivo) |
| Mapeamento | MapStruct |
| Documentação | SpringDoc OpenAPI 3 (Swagger) |
| Testes | JUnit 5 + Mockito |
| Build | Maven 3.8+ |
| Container | Docker + Docker Compose |

---

## 🚀 Execução Local

### Pré-requisitos
- Java 21+
- Maven 3.8+ (ou use o `mvnw` incluso)

### 1. Clonar e compilar
```bash
git clone <repo-url>
cd DesafioEncurtadorUrl
./mvnw clean package -DskipTests
```

### 2. Executar
```bash
./mvnw spring-boot:run
```

A aplicação sobe em **http://localhost:8080**

---

## 🐳 Docker

### Pré-requisitos
- Docker Desktop instalado e rodando

### Subir com Docker Compose (recomendado)
```bash
# Build + start em foreground
docker compose up --build

# Build + start em background
docker compose up -d --build
```

### Parar
```bash
# Parar containers
docker compose down

# Parar e remover volumes
docker compose down -v
```

### Comandos úteis
```bash
# Ver logs em tempo real
docker compose logs -f app

# Ver status dos containers
docker compose ps

# Acessar shell do container
docker compose exec app sh

# Limpar imagens não utilizadas
docker system prune -f
```

### Build manual da imagem
```bash
./mvnw clean package -DskipTests
docker build -t encurtador-url .
docker run -p 8080:8080 -v ./data:/app/data encurtador-url
```

> **Dados persistidos**: o diretório `./data` do host é montado como volume no container — os dados do H2 sobrevivem ao restart.

---

## 🔌 Endpoints

Base URL: `http://localhost:8080`

| Método | Rota | Descrição | Auth |
|---|---|---|---|
| `POST` | `/v1/urls/` | Encurtar URL | ✅ X-API-Key |
| `GET` | `/v1/urls/{shortCode}` | Consultar URL | ❌ Público |
| `GET` | `/v1/urls/{shortCode}/stats` | Estatísticas de acesso | ❌ Público |
| `GET` | `/v1/urls/list` | Listar todas (paginado) | ❌ Público |
| `DELETE` | `/v1/urls/{shortCode}` | Remover URL | ✅ X-API-Key |
| `GET` | `/{shortCode}` | Redirecionar para URL original | ❌ Público |

---

### POST `/v1/urls/` — Encurtar URL

```bash
curl -X POST http://localhost:8080/v1/urls/ \
  -H "Content-Type: application/json" \
  -H "X-API-Key: minha-chave-secreta-123" \
  -d '{
    "originalUrl": "https://www.github.com/usuario/projeto/issues?status=open",
    "customAlias": "meu-link",
    "expirationDate": "2026-12-31T23:59:59"
  }'
```

> `customAlias` e `expirationDate` são opcionais. Sem `expirationDate`, expira em 1 ano.

**Resposta** `201 Created`:
```json
{
  "id": "meu-link",
  "shortUrl": "http://localhost:8080/meu-link",
  "originalUrl": "https://www.github.com/usuario/projeto/issues?status=open",
  "createdAt": "2026-03-11T10:30:00",
  "expirationDate": "2026-12-31T23:59:59"
}
```

---

### GET `/v1/urls/{shortCode}` — Consultar URL

```bash
curl http://localhost:8080/v1/urls/meu-link
```

**Resposta** `200 OK`:
```json
{
  "id": "meu-link",
  "shortUrl": "http://localhost:8080/meu-link",
  "originalUrl": "https://www.github.com/...",
  "createdAt": "2026-03-11T10:30:00",
  "expirationDate": "2026-12-31T23:59:59"
}
```

---

### GET `/v1/urls/{shortCode}/stats` — Estatísticas

```bash
curl http://localhost:8080/v1/urls/meu-link/stats
```

**Resposta** `200 OK`:
```json
{
  "id": "meu-link",
  "shortUrl": "http://localhost:8080/meu-link",
  "originalUrl": "https://www.github.com/...",
  "createdAt": "2026-03-11T10:30:00",
  "expirationDate": "2026-12-31T23:59:59",
  "clickCount": 42
}
```

---

### GET `/v1/urls/list` — Listar URLs (paginado)

```bash
curl "http://localhost:8080/v1/urls/list?page=0&size=10&sort=createdAt&direction=DESC"
```

| Param | Default | Descrição |
|---|---|---|
| `page` | `0` | Número da página |
| `size` | `10` | Itens por página |
| `sort` | `createdAt` | Campo de ordenação |
| `direction` | `DESC` | `ASC` ou `DESC` |

**Resposta** `200 OK` (ou `204 No Content` se vazio):
```json
{
  "shortenUrlList": [ ... ],
  "page": 0,
  "size": 10,
  "totalElements": 42,
  "totalPages": 5,
  "last": false
}
```

---

### DELETE `/v1/urls/{shortCode}` — Remover URL

```bash
curl -X DELETE http://localhost:8080/v1/urls/meu-link \
  -H "X-API-Key: minha-chave-secreta-123"
```

**Resposta** `204 No Content`

---

### GET `/{shortCode}` — Redirecionar

```bash
curl -L http://localhost:8080/meu-link
```

**Resposta** `302 Found` → redireciona para a URL original e incrementa `clickCount`.

---

## 🔐 Autenticação X-API-Key

Os endpoints `POST /v1/urls/` e `DELETE /v1/urls/{shortCode}` exigem o header `X-API-Key`.

```http
X-API-Key: minha-chave-secreta-123
```

A chave é configurada em `application.properties`:
```properties
app.api-key=minha-chave-secreta-123
```

**Respostas de erro de autenticação:**

| Situação | Status | Código |
|---|---|---|
| Header ausente | `401` | `BFF00501` |
| Chave inválida | `401` | `BFF00502` |

---

## 📋 Formato de Erros

Todas as respostas de erro seguem o padrão:

```json
{
  "status": 400,
  "codigo": "BFF00403",
  "message": "URL inválida",
  "path": "/v1/urls/",
  "timestamp": 1773280331443
}
```

| Código | Significado | HTTP |
|---|---|---|
| `BFF00002` | Parâmetro inválido | 400 |
| `BFF00003` | Erro de query | 503 |
| `BFF00100` | Erro de banco de dados | 503 |
| `BFF00200` | Erro de mapeamento | 500 |
| `BFF00401` | URL expirada | 410 |
| `BFF00402` | URL não encontrada | 404 |
| `BFF00403` | URL inválida | 400 |
| `BFF00501` | X-API-Key ausente | 401 |
| `BFF00502` | X-API-Key inválida | 401 |
| `BFF99999` | Erro interno | 500 |

---

## 📖 Swagger UI

Acesse a documentação interativa da API:

```
http://localhost:8080/swagger-ui/index.html
```

### Como usar o Swagger com autenticação

1. Abra **http://localhost:8080/swagger-ui/index.html**
2. Clique no botão **Authorize 🔒** (canto superior direito)
3. No campo `X-API-Key`, insira: `minha-chave-secreta-123`
4. Clique em **Authorize** → **Close**
5. Agora todos os endpoints protegidos usarão a chave automaticamente

> **Observação**: O endpoint de redirecionamento `GET /{shortCode}` está oculto do Swagger UI intencionalmente — o Swagger segue o `302` automaticamente, o que causaria erro CORS. Use `GET /v1/urls/{shortCode}` para consultar via Swagger.

---

## 🗄️ H2 Console

Interface web para inspecionar o banco de dados em tempo real:

```
http://localhost:8080/h2-console
```

**Credenciais de conexão:**

| Campo | Valor |
|---|---|
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:file:./data/shortenerdb` |
| User Name | `sa` |
| Password | *(deixar vazio)* |

**Consultas úteis:**
```sql
-- Ver todas as URLs
SELECT * FROM shortened_urls;

-- Ver as mais acessadas
SELECT short_code, original_url, click_count
FROM shortened_urls
ORDER BY click_count DESC
LIMIT 10;

-- Contar total
SELECT COUNT(*) AS total FROM shortened_urls;

-- Ver criadas hoje
SELECT * FROM shortened_urls
WHERE DATE(created_at) = CURDATE();

-- URL específica
SELECT * FROM shortened_urls WHERE short_code = 'abc123';
```

---

## 🧪 Testes

### Rodar todos os testes
```bash
./mvnw test
```

**Resultado esperado:**
```
Tests run: 234, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Rodar um grupo específico
```bash
# Apenas testes de integração (TAAC)
./mvnw test -Dtest=ShortenUrlControllerIntegrationTest

# Apenas testes unitários do serviço
./mvnw test -Dtest=ShortenUrlServiceTest

# Apenas testes de validação
./mvnw test -Dtest=UrlRequestValidatorTest
```

### Cobertura de testes

| Classe | Tipo | Testes |
|---|---|---|
| `ShortenUrlControllerIntegrationTest` | Integração (TAAC) | 14 |
| `ShortenUrlServiceTest` | Unitário | 17 |
| `UrlRequestValidatorTest` | Unitário | 41 |
| `UrlBusinessValidatorTest` | Unitário | 8 |
| `ShortCodeGeneratorTest` | Unitário | 77 |
| `IdGeneratorTest` | Unitário | 71 |
| `ShortenUrlMapperTest` | Unitário | 5 |
| `SuperAppApplicationTests` | Contexto Spring | 1 |

---

## 🏗️ Arquitetura

```
┌─────────────────────────────────────────────────────────────┐
│                    CLIENTE / INTERNET                       │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTP :8080
                            ▼
            ┌───────────────────────────────┐
            │     ApiKeyFilter              │  ← Intercepta POST/DELETE
            │  (X-API-Key obrigatório)      │    antes do Spring MVC
            └───────────────┬───────────────┘
                            │
            ┌───────────────┴───────────────┐
            │                               │
            ▼                               ▼
   ┌─────────────────┐             ┌─────────────────┐
   │ ShortenUrl      │             │ Redirect        │
   │ Controller      │             │ Controller      │
   │ /v1/urls/*      │             │ /{shortCode}    │
   └────────┬────────┘             └────────┬────────┘
            │                               │
            └──────────────┬────────────────┘
                           │
                           ▼
              ┌────────────────────────┐
              │   UrlRequestValidator  │  ← Valida formato/protocolo
              │   UrlBusinessValidator │  ← Valida unicidade no banco
              └────────────┬───────────┘
                           │
                           ▼
              ┌────────────────────────┐
              │  ShortenUrlServiceImpl │
              │  - shortenUrl()        │
              │  - getShortenedUrl()   │
              │  - redirectToUrl()     │
              │  - getStats()          │
              │  - deleteShortenedUrl()│
              │  - listAll()           │
              └────────────┬───────────┘
                           │
                           ▼
              ┌────────────────────────┐
              │  ShortenedUrlRepository│  ← Spring Data JPA
              └────────────┬───────────┘
                           │
                           ▼
              ┌────────────────────────────────────┐
              │  H2 DATABASE (./data/shortenerdb)  │
              ├────────────────────────────────────┤
              │  shortened_urls                    │
              │  ├─ short_code (PK, UNIQUE)        │
              │  ├─ short_url (UNIQUE)             │
              │  ├─ original_url                   │
              │  ├─ custom_alias (UNIQUE)          │
              │  ├─ created_at / updated_at        │
              │  ├─ expiration_date                │
              │  ├─ click_count                    │
              │  └─ last_accessed                  │
              └────────────────────────────────────┘
```

### Fluxo de encurtamento

```
POST /v1/urls/
     │
     ├─ Valida URL (protocolo HTTP/HTTPS, tamanho, formato)
     ├─ Valida alias customizado (se fornecido)
     ├─ Valida data de expiração (se fornecida)
     │
     ├─ URL já existe no banco?
     │   ├─ SIM  → retorna shortCode existente (idempotência)
     │   └─ NÃO  → continua
     │
     ├─ Alias customizado fornecido?
     │   ├─ SIM  → valida unicidade no banco
     │   └─ NÃO  → gera Base62 aleatório (6 chars, max 10 tentativas)
     │
     └─ Salva no banco → retorna 201 Created
```

### Fluxo de redirecionamento

```
GET /{shortCode}
     │
     ├─ Valida formato do shortCode
     ├─ Busca no banco
     ├─ URL expirada? → 410 Gone
     ├─ Não encontrada? → 404 Not Found
     │
     └─ Incrementa clickCount + atualiza lastAccessed
         └─ Retorna 302 Found com Location: <originalUrl>
```

---

## 🔢 Algoritmo Base62

Gera códigos alfanuméricos curtos e URL-safe:

```
Charset: 0-9 A-Z a-z  (62 caracteres)
Tamanho: 6 caracteres

Combinações possíveis: 62^6 = 56.800.235.584 (~56 bilhões)
Seguro sem colisão até: ~100 milhões de URLs (< 0,001% colisão)

Exemplo: "abc123", "XyZ7Q9", "5KqWrT"
```

**Estratégia anti-colisão:**
1. Gera código aleatório
2. Verifica se já existe no banco
3. Se existe → retry (máximo 10 tentativas)
4. Se não existe → persiste e retorna

---

## 📁 Estrutura do Projeto

```
src/main/java/com/encurtador_url/SuperApp/
├── config/
│   ├── CorsConfig.java          # CORS global
│   ├── JacksonConfig.java       # ObjectMapper bean
│   └── OpenApiConfig.java       # Swagger / X-API-Key scheme
├── controller/
│   ├── ShortenUrlController.java  # /v1/urls/*
│   └── RedirectController.java    # /{shortCode}
├── dto/
│   ├── request/
│   │   └── ShortenUrlRequest.java
│   └── response/
│       ├── ShortenUrlResponse.java
│       ├── ShortenUrlListResponse.java
│       ├── DetailsUrlResponse.java
│       └── ErrorResponse.java     # Genérico com T para details
├── enums/
│   └── ErrorCodeEnum.java         # Todos os códigos BFF0xxxx
├── exception/
│   ├── AbstractException.java
│   ├── MapperException.java
│   ├── RepositoryException.java
│   ├── UrlExpiredException.java
│   ├── UrlInvalidaExceptionException.java
│   ├── UrlNotFoundException.java
│   └── ValidationException.java
├── filter/
│   └── ApiKeyFilter.java          # Autenticação X-API-Key
├── handler/
│   └── GlobalExceptionHandler.java # @ControllerAdvice
├── mapper/
│   └── ShortenUrlMapper.java       # MapStruct
├── model/
│   └── ShortenedUrl.java
├── repository/
│   └── ShortenedUrlRepository.java
├── service/
│   ├── ShortenUrlService.java
│   └── ShortenUrlServiceImpl.java
├── util/
│   ├── ShortCodeGenerator.java     # Base62 + validação
│   └── IdGenerator.java
└── validations/
    ├── UrlRequestValidator.java    # Valida formato/protocolo/tamanho
    └── UrlBusinessValidator.java   # Valida unicidade no banco

src/test/java/com/encurtador_url/SuperApp/
├── controller/
│   └── ShortenUrlControllerIntegrationTest.java  # TAAC (14 testes)
├── service/
│   └── ShortenUrlServiceTest.java                # Unitário (17 testes)
├── util/
│   ├── ShortCodeGeneratorTest.java
│   ├── IdGeneratorTest.java
│   └── ShortenUrlMapperTest.java
├── validations/
│   ├── UrlRequestValidatorTest.java
│   └── UrlBusinessValidatorTest.java
└── SuperAppApplicationTests.java

src/main/resources/
├── application.properties          # Config padrão (H2 arquivo)
└── application-docker.properties   # Config para container Docker

src/test/resources/
└── application-test.properties     # Config de testes (H2 em memória)
```

---

## 🐛 Troubleshooting

### Porta 8080 já em uso
```properties
# src/main/resources/application.properties
server.port=8081
```

### H2 database file is locked
```bash
# Feche outras instâncias da aplicação e delete o lock file
rm ./data/shortenerdb.lock
```

### Swagger não carrega
```
# Certifique-se de acessar com /index.html no final:
http://localhost:8080/swagger-ui/index.html
```

### Testes falhando
```bash
./mvnw clean test
```

### Docker: imagem desatualizada
```bash
# Force rebuild sem cache
docker compose up --build --force-recreate
```

### Logs da aplicação
```bash
# Local
./mvnw spring-boot:run | tee app.log

# Docker
docker compose logs -f app
```

---

## 💾 Persistência dos Dados

Os dados são gravados em arquivo na pasta `./data/`:

```
./data/shortenerdb.mv.db   ← banco principal
./data/shortenerdb.lock    ← lock temporário (excluir se travar)
```

Tanto em execução local quanto Docker, os dados sobrevivem ao restart da aplicação.

---

## 📄 Licença

MIT License — consulte [LICENSE](LICENSE) para detalhes.

---

**Spring Boot 4.0.3 · Java 21 · H2 · MapStruct · JUnit 5**
