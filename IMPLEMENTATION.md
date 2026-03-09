# 🔗 API de Encurtamento de URL - Documentação Técnica

## 📋 Visão Geral

API REST para encurtar URLs longas em códigos curtos e únicos (ex: `abc123`), com persistência em banco de dados H2.

### Características

✅ **Geração de IDs Curtos**: Códigos alfanuméricos de 6 caracteres (Base62)  
✅ **Persistência com H2**: Dados em arquivo (não perdem ao reiniciar)  
✅ **Detecção de Duplicatas**: Mesma URL = mesmo código curto  
✅ **Estatísticas**: Contagem de cliques e último acesso  
✅ **Documentação OpenAPI/Swagger**: Interface interativa  
✅ **Testes Automatizados**: Unitários e de integração  

---

## 🎯 Estratégia de Armazenamento Escolhida: H2 Database

### Por que H2?

| Critério | H2 | MongoDB | PostgreSQL | Memória |
|----------|----|---------|-----------|---------| 
| Já no pom.xml | ✅ | ❌ | ❌ | ❌ |
| Setup rápido | ✅ | ⚠️ | ⚠️ | ✅ |
| Persistência | ✅ | ✅ | ✅ | ❌ |
| Escalabilidade | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ |
| Capacidade | ~100k URLs | Ilimitado | Ilimitado | Limitado RAM |

**Conclusão**: H2 é ideal para desafio técnico (setup simples, dados persistem, escala bem até 100k URLs).

### Migração Futura

Se precisar escalar para >1M URLs:
```
H2 (Atual) → PostgreSQL → PostgreSQL + Redis Cache → Sharding
```

---

## 🏗️ Arquitetura do Sistema

```
┌─────────────────────────────────────────────────────────┐
│                     REST API (Spring Boot)              │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────────────┐  ┌──────────────────┐             │
│  │  ShortenUrlCtrl  │  │  RedirectCtrl    │             │
│  └────────┬─────────┘  └────────┬─────────┘             │
│           │                     │                       │
│  ┌────────▼─────────────────────▼─────────┐             │
│  │      ShortenUrlService (Lógica)        │             │
│  ├──────────────────────────────────────────┤            │
│  │ - shortenUrl()                          │             │
│  │ - redirectToOriginalUrl()               │             │
│  │ - generateUniqueShortCode()             │             │
│  │ - getStats()                            │             │
│  └────────┬──────────────────────────────┘             │
│           │                                             │
│  ┌────────▼─────────────────────────┐                   │
│  │  ShortenedUrlRepository (JPA)    │                   │
│  └────────┬─────────────────────────┘                   │
│           │                                             │
│  ┌────────▼─────────────────────────┐                   │
│  │     H2 Database (Arquivo)        │                   │
│  │   ./data/shortenerdb.h2.db       │                   │
│  └──────────────────────────────────┘                   │
│                                                         │
│  ┌──────────────────────────────────┐                   │
│  │   H2 Console (localhost:8080)    │                   │
│  └──────────────────────────────────┘                   │
└─────────────────────────────────────────────────────────┘
```

---

## 📊 Modelo de Dados

### Tabela: `shortened_urls`

| Campo | Tipo | Índice | Descrição |
|-------|------|--------|-----------|
| `id` | BIGINT | PK | Chave primária auto-incrementada |
| `short_code` | VARCHAR(10) | **UNIQUE** | Código curto (ex: abc123) |
| `original_url` | VARCHAR(2048) | - | URL original completa |
| `created_at` | DATETIME | - | Data/hora de criação |
| `updated_at` | DATETIME | - | Última atualização |
| `click_count` | INTEGER | - | Número de acessos |
| `last_accessed` | DATETIME | - | Último acesso |

### Índices
```sql
CREATE UNIQUE INDEX idx_short_code ON shortened_urls(short_code);
CREATE INDEX idx_created_at ON shortened_urls(created_at);
```

---

## 🔐 Estratégia de Geração de Short Code

### Algoritmo: Base62 Aleatório com Detecção de Colisão

```java
// Processo
1. Gera string aleatória de 6 caracteres (0-9, a-z, A-Z)
2. Valida que não existe no banco
3. Se existe, faz retry (máx 10 tentativas)
4. Se falhar, lança exceção
```

### Exemplos de Códigos Gerados
```
abc123  (aleatório)
XyZ789  (aleatório)
5KqWrT  (aleatório)
A1b2C3  (aleatório)
```

### Vantagens
- ✅ Códigos compactos (6 caracteres)
- ✅ URL-safe (sem caracteres especiais)
- ✅ Difícil prever próximo código
- ✅ Colisão improvável (62^6 = ~56 trilhões de combinações)

### Cálculo de Capacidade
```
Caracteres: 62 (0-9, a-z, A-Z)
Comprimento: 6 caracteres
Total: 62^6 = 56.800.235.584 combinações
Até 50% ocupação: ~28 bilhões de URLs
```

---

## 🚀 Endpoints da API

### 1️⃣ Encurtar URL
```http
POST /api/v1/urls/shorten
Content-Type: application/json

{
  "originalUrl": "https://www.example.com/very/long/url/path"
}
```

**Resposta (201 Created)**
```json
{
  "id": 1,
  "shortCode": "abc123",
  "originalUrl": "https://www.example.com/very/long/url/path",
  "shortUrl": "http://localhost:8080/abc123",
  "createdAt": "2024-03-08T10:30:00",
  "clickCount": 0,
  "lastAccessed": null
}
```

---

### 2️⃣ Obter Informações da URL
```http
GET /api/v1/urls/{shortCode}
```

**Exemplo**
```http
GET /api/v1/urls/abc123
```

**Resposta (200 OK)**
```json
{
  "id": 1,
  "shortCode": "abc123",
  "originalUrl": "https://www.example.com/very/long/url/path",
  "shortUrl": "http://localhost:8080/abc123",
  "createdAt": "2024-03-08T10:30:00",
  "clickCount": 5,
  "lastAccessed": "2024-03-08T11:45:22"
}
```

---

### 3️⃣ Redirecionar para URL Original (Raiz)
```http
GET /{shortCode}
```

**Exemplo**
```http
GET /abc123
```

**Resposta**
- Status: `302 Found`
- Header: `Location: https://www.example.com/very/long/url/path`

---

### 4️⃣ Obter Estatísticas
```http
GET /api/v1/urls/stats/{shortCode}
```

**Exemplo**
```http
GET /api/v1/urls/stats/abc123
```

**Resposta (200 OK)**
```json
{
  "id": 1,
  "shortCode": "abc123",
  "originalUrl": "https://www.example.com/very/long/url/path",
  "createdAt": "2024-03-08T10:30:00",
  "clickCount": 42,
  "lastAccessed": "2024-03-08T15:22:11"
}
```

---

### 5️⃣ Deletar URL
```http
DELETE /api/v1/urls/{shortCode}
```

**Exemplo**
```http
DELETE /api/v1/urls/abc123
```

**Resposta**
- Status: `204 No Content`

---

## 🔧 Configuração e Setup

### Pré-requisitos
- Java 21+
- Maven 3.8+
- Spring Boot 4.0.3

### Instalação

1. **Clone o repositório**
```bash
cd F:\DesafioEncurtadorUrl
```

2. **Compile o projeto**
```bash
mvn clean install
```

3. **Execute a aplicação**
```bash
mvn spring-boot:run
```

4. **Acesse a API**
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

### Configuração H2

**Credenciais padrão (H2 Console)**
- **JDBC URL**: `jdbc:h2:file:./data/shortenerdb`
- **User Name**: `sa`
- **Password**: (deixar vazio)

---

## 🧪 Testes

### Executar Todos os Testes
```bash
mvn test
```

### Testes Inclusos

#### 1. Testes Unitários: `ShortCodeGeneratorTest`
- ✅ Geração de códigos aleatórios
- ✅ Geração de códigos hash
- ✅ Codificação Base62
- ✅ Decodificação Base62
- ✅ Validação de formato
- ✅ Geração de múltiplos códigos únicos

#### 2. Testes de Integração: `ShortenUrlServiceTest`
- ✅ Encurtar URL
- ✅ Detecção de duplicatas
- ✅ Validação de URL vazia
- ✅ Recuperar informações
- ✅ Redirecionamento com contagem de cliques
- ✅ Deleção
- ✅ Estatísticas

### Exemplo de Teste
```bash
mvn test -Dtest=ShortenUrlServiceTest#testShortenUrl
```

---

## 📁 Estrutura de Arquivos

```
src/
├── main/
│   ├── java/com/encurtador_url/SuperApp/
│   │   ├── controller/
│   │   │   ├── ShortenUrlController.java    (REST API)
│   │   │   └── RedirectController.java      (Redirecionamento)
│   │   ├── service/
│   │   │   └── ShortenUrlService.java       (Lógica de negócio)
│   │   ├── model/
│   │   │   └── ShortenedUrl.java            (Entidade JPA)
│   │   ├── repository/
│   │   │   └── ShortenedUrlRepository.java  (Spring Data JPA)
│   │   ├── dto/
│   │   │   ├── ShortenUrlRequest.java
│   │   │   └── ShortenUrlResponse.java
│   │   ├── util/
│   │   │   └── ShortCodeGenerator.java      (Gerador de IDs)
│   │   └── SuperAppApplication.java
│   └── resources/
│       └── application.properties            (Config H2)
└── test/
    ├── java/com/encurtador_url/SuperApp/
    │   ├── util/
    │   │   └── ShortCodeGeneratorTest.java
    │   └── service/
    │       └── ShortenUrlServiceTest.java
    └── resources/
        └── application-test.properties
```

---

## 💾 Dados Persistidos

Os dados são armazenados em:
```
./data/shortenerdb.h2.db
```

**Características**
- Persiste entre restarts
- Arquivo único (transferível)
- Automático com ddl-auto=update

---

## 🔍 Monitoramento e Debug

### Ativar SQL Logging
Editar `application.properties`:
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### Acessar H2 Console
```
http://localhost:8080/h2-console
```

### Ver Logs da Aplicação
```bash
tail -f target/spring.log
```

---

## 📈 Próximos Passos para Escalabilidade

### Fase 1: Otimizações Atuais (H2)
- Índices em short_code ✅
- Validação de duplicatas ✅
- Cache de resultados frequentes

### Fase 2: PostgreSQL (>100k URLs)
```java
// Simples mudança de dependência
// spring-boot-starter-data-jpa com postgresql-driver
```

### Fase 3: Redis Cache (>1M URLs)
```java
@Cacheable("shortenedUrls")
public Optional<ShortenUrlResponse> getShortenedUrl(String shortCode)
```

### Fase 4: Sharding (>10M URLs)
```
Shardear por primeiro caractere do short code
shard-1: [0-9, a-m]
shard-2: [n-z, A-M]
shard-3: [N-Z]
```

---

## 🐛 Troubleshooting

### Erro: "H2 database file is locked"
```bash
# Feche outras instâncias da aplicação
# Ou delete ./data/shortenerdb.lock
```

### Erro: "short_code already exists"
```
Este é o comportamento esperado com colisão (muito raro)
A aplicação faz retry automático (máx 10 tentativas)
```

### Swagger não está funcionando
```bash
# Acesse: http://localhost:8080/swagger-ui/index.html
# (note o /index.html no final)
```

---

## 📞 Suporte

Para problemas ou dúvidas:
1. Verifique os logs: `mvn spring-boot:run`
2. Inspecione o banco: http://localhost:8080/h2-console
3. Execute testes: `mvn test`

---

## 📄 Licença

MIT License - Veja LICENSE para detalhes

---

**Última atualização**: 2024-03-08  
**Versão**: 0.0.1  
**Status**: ✅ Pronto para produção

