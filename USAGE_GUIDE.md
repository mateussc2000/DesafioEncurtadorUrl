# 🚀 Guia Prático de Uso

## Como Usar os Novos DTOs

### 1️⃣ ShortenUrlResponse (Resposta Simples)

Usado para endpoints que retornam informações básicas de uma URL encurtada.

#### Exemplo de Uso no Controller
```java
@PostMapping("/v1/urls")
public ResponseEntity<ShortenUrlResponse> shortenUrl(
    @RequestBody ShortenUrlRequest request) {
    
    ShortenUrlResponse response = service.shortenUrl(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

#### Response JSON
```json
{
  "id": "x7k9mq",
  "shortUrl": "http://localhost:8080/x7k9mq",
  "originalUrl": "https://www.exemplo.com/artigos/muito/longo",
  "createdAt": "2026-03-11T14:30:22.123456",
  "expirationDate": "2026-04-11T14:30:22.123456"
}
```

---

### 2️⃣ DetailsUrlResponse (Resposta com Detalhes)

Usado para endpoints que retornam estatísticas ou informações completas.

#### Exemplo de Uso no Controller
```java
@GetMapping("/stats/{shortCode}")
public ResponseEntity<DetailsUrlResponse> getStats(
    @PathVariable String shortCode) {
    
    Optional<DetailsUrlResponse> response = service.getStats(shortCode);
    return response.map(ResponseEntity::ok)
                  .orElseGet(() -> ResponseEntity.notFound().build());
}
```

#### Response JSON
```json
{
  "id": "x7k9mq",
  "shortUrl": "http://localhost:8080/x7k9mq",
  "originalUrl": "https://www.exemplo.com/artigos/muito/longo",
  "createdAt": "2026-03-11T14:30:22.123456",
  "expirationDate": "2026-04-11T14:30:22.123456",
  "clickCount": 127
}
```

---

### 3️⃣ Usando o ShortenUrlMapper

O Mapper está disponível como `@Component` e pode ser injetado em qualquer classe Spring.

#### Injetar o Mapper
```java
@Service
public class MinhaClasse {
    
    @Autowired
    private ShortenUrlMapper mapper;
    
    public void processar(ShortenedUrl url) {
        // Converter para response simples
        ShortenUrlResponse response = mapper.toResponse(url);
        
        // Converter para response detalhada
        DetailsUrlResponse details = mapper.toDetailsResponse(url);
    }
}
```

#### Usar em Streams
```java
// Stream de ShortenedUrl para ShortenUrlResponse
List<ShortenUrlResponse> responses = urls.stream()
    .map(mapper::toResponse)
    .collect(Collectors.toList());

// Stream de ShortenedUrl para DetailsUrlResponse
List<DetailsUrlResponse> details = urls.stream()
    .map(mapper::toDetailsResponse)
    .collect(Collectors.toList());
```

#### Usar com Optional
```java
// Converter Optional
Optional<ShortenUrlResponse> response = urlOptional
    .map(mapper::toResponse);

// Com orElseGet
ShortenUrlResponse response = urlOptional
    .map(mapper::toResponse)
    .orElseThrow(() -> new NotFoundException("URL não encontrada"));
```

---

## 📋 Casos de Uso Comuns

### Cenário 1: Encurtar uma URL (POST /v1/urls)

**Entrada:**
```json
{
  "originalUrl": "https://www.google.com/search?q=encurtador+de+url",
  "expirationDate": "2026-04-11T14:30:00"
}
```

**Saída (ShortenUrlResponse):**
```json
{
  "id": "abc123",
  "shortUrl": "http://localhost:8080/abc123",
  "originalUrl": "https://www.google.com/search?q=encurtador+de+url",
  "createdAt": "2026-03-11T14:30:22",
  "expirationDate": "2026-04-11T14:30:00"
}
```

**Código:**
```java
@PostMapping("/v1/urls")
public ResponseEntity<ShortenUrlResponse> shortenUrl(
    @RequestBody ShortenUrlRequest request) {
    ShortenUrlResponse response = service.shortenUrl(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

---

### Cenário 2: Obter Informações (GET /api/urls/{shortCode})

**Entrada:** 
- Path: `/api/urls/abc123`

**Saída (ShortenUrlResponse):**
```json
{
  "id": "abc123",
  "shortUrl": "http://localhost:8080/abc123",
  "originalUrl": "https://www.google.com/search?q=encurtador+de+url",
  "createdAt": "2026-03-11T14:30:22",
  "expirationDate": "2026-04-11T14:30:00"
}
```

**Código:**
```java
@GetMapping("/api/urls/{shortCode}")
public ResponseEntity<ShortenUrlResponse> getUrl(
    @PathVariable String shortCode) {
    Optional<ShortenUrlResponse> response = service.getShortenedUrl(shortCode);
    return response.map(ResponseEntity::ok)
                  .orElseGet(() -> ResponseEntity.notFound().build());
}
```

---

### Cenário 3: Obter Estatísticas (GET /stats/{shortCode})

**Entrada:**
- Path: `/stats/abc123`

**Saída (DetailsUrlResponse - COM clickCount):**
```json
{
  "id": "abc123",
  "shortUrl": "http://localhost:8080/abc123",
  "originalUrl": "https://www.google.com/search?q=encurtador+de+url",
  "createdAt": "2026-03-11T14:30:22",
  "expirationDate": "2026-04-11T14:30:00",
  "clickCount": 42
}
```

**Código Sugerido:**
```java
@GetMapping("/stats/{shortCode}")
public ResponseEntity<DetailsUrlResponse> getStats(
    @PathVariable String shortCode) {
    Optional<DetailsUrlResponse> response = service.getStats(shortCode);
    return response.map(ResponseEntity::ok)
                  .orElseGet(() -> ResponseEntity.notFound().build());
}

// Em ShortenUrlServiceImpl
public Optional<DetailsUrlResponse> getStats(String shortCode) {
    return repository.findByShortCode(shortCode)
                    .map(mapper::toDetailsResponse);  // ← Use toDetailsResponse
}
```

---

## 🔧 Configuração de Properties

### application.properties

```properties
# URL base para construir shortUrl
app.base-url=http://localhost:8080

# Para produção:
# app.base-url=https://sho.rt

# Para desenvolvimento:
# app.base-url=http://localhost:8080
```

### application-test.properties

```properties
# URL base para testes
app.base-url=http://test.local
```

---

## 📝 Exemplo Completo de Service

```java
@Service
@Transactional
public class ShortenUrlServiceImpl implements ShortenUrlService {

    @Autowired
    private ShortenedUrlRepository repository;

    @Autowired
    private ShortenUrlMapper mapper;  // ← Injetar mapper

    @Override
    public ShortenUrlResponse shortenUrl(ShortenUrlRequest request) {
        // ... validações ...
        
        ShortenedUrl saved = repository.save(shortenedUrl);
        
        // Usar mapper para converter
        return mapper.toResponse(saved);  // ← Simples e limpo
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ShortenUrlResponse> getShortenedUrl(String shortCode) {
        return repository.findByShortCode(shortCode)
                        .map(mapper::toResponse);  // ← Method reference
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<DetailsUrlResponse> getStats(String shortCode) {
        return repository.findByShortCode(shortCode)
                        .map(mapper::toDetailsResponse);  // ← Com detalhes
    }
}
```

---

## 🧪 Testando com Postman

### 1. Encurtar URL
```
POST http://localhost:8080/v1/urls
Content-Type: application/json

{
  "originalUrl": "https://www.exemplo.com/pagina/muito/longa",
  "expirationDate": "2026-04-11T14:30:00"
}
```

Esperado: `201 Created` com `ShortenUrlResponse`

---

### 2. Obter Informações
```
GET http://localhost:8080/api/urls/abc123
```

Esperado: `200 OK` com `ShortenUrlResponse`

---

### 3. Obter Estatísticas
```
GET http://localhost:8080/stats/abc123
```

Esperado: `200 OK` com `DetailsUrlResponse`

---

## 🎯 Resumo de Boas Práticas

### ✅ Faça
```java
// Usar mapper para conversão
ShortenUrlResponse response = mapper.toResponse(entity);

// Usar method reference
return entity.map(mapper::toResponse);

// Injetar mapper via @Autowired
@Autowired private ShortenUrlMapper mapper;
```

### ❌ Não Faça
```java
// Não converter manualmente
new ShortenUrlResponse(
    entity.getId(),
    // ... 5 conversões ...
);

// Não duplicar lógica de conversão
String shortUrl = baseUrl + "/" + entity.getCode();

// Não criar novos mappers em cada classe
private ShortenUrlMapper mapper = new ShortenUrlMapper();
```

---

## 📚 Documentação Relacionada

- `REFACTORING_DTOS.md` - Detalhes técnicos
- `BEFORE_AFTER_COMPARISON.md` - Comparação de mudanças
- `IMPLEMENTATION_CHECKLIST.md` - Checklist completo
- `OPTIONAL_IMPROVEMENT.md` - Melhorias futuras

---

## 💡 Dicas e Truques

### Converter Lista
```java
List<ShortenUrlResponse> responses = urls.stream()
    .map(mapper::toResponse)
    .collect(Collectors.toList());
```

### Converter com Filtro
```java
List<DetailsUrlResponse> populares = urls.stream()
    .filter(url -> url.getClickCount() > 10)
    .map(mapper::toDetailsResponse)
    .collect(Collectors.toList());
```

### Página de Resultados
```java
Page<ShortenedUrl> page = repository.findAll(pageable);
Page<ShortenUrlResponse> responses = page.map(mapper::toResponse);
return ResponseEntity.ok(responses);
```

---

**Última atualização:** 11/03/2026

