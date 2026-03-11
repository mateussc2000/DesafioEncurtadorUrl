# Comparação: Antes vs Depois

## ANTES - ShortenUrlResponse (Antigo)

```java
public record ShortenUrlResponse(
    String shortCode,          // ❌ Removido
    String customAlias,        // ❌ Removido
    String originalUrl,        // ✅ Mantido
    String shortUrl,           // ✅ Mantido
    LocalDateTime createdAt,   // ✅ Mantido
    LocalDateTime expirationDate,     // ✅ Mantido
    Integer clickCount,        // ❌ Movido para DetailsUrlResponse
    LocalDateTime lastAccessed // ❌ Removido
) {}
```

**Problemas:**
- Muitos campos desnecessários misturados
- ClickCount deveria estar apenas em DetailsUrlResponse
- Campo `lastAccessed` nunca era usado nas respostas

---

## DEPOIS - ShortenUrlResponse (Novo)

```java
public record ShortenUrlResponse(
    String id,                 // ✅ Nome melhorado (era shortCode)
    String shortUrl,           // ✅ Mantido
    String originalUrl,        // ✅ Mantido
    LocalDateTime createdAt,   // ✅ Mantido
    LocalDateTime expirationDate // ✅ Mantido
) {}
```

**Benefícios:**
- ✅ Simples e objetivo
- ✅ Campo `id` semanticamente melhor
- ✅ Apenas campos essenciais
- ✅ Reutilizável em múltiplos endpoints

---

## NOVO - DetailsUrlResponse

```java
public record DetailsUrlResponse(
    String id,                 // ✅ Código curto
    String shortUrl,           // ✅ URL completa
    String originalUrl,        // ✅ URL original
    LocalDateTime createdAt,   // ✅ Data de criação
    LocalDateTime expirationDate,  // ✅ Data de expiração
    Integer clickCount         // ✅ Estatísticas
) {}
```

**Propósito:**
- Endpoint de estatísticas e detalhes
- Inclui `clickCount` para análise
- Enriquece dados sem contaminar ShortenUrlResponse

---

## Mapeamento no Serviço

### ANTES - Método Manual Interno

```java
// Dentro de ShortenUrlServiceImpl
@Value("${app.base-url:http://localhost:8080}")
private String baseUrl;

private ShortenUrlResponse mapToResponse(ShortenedUrl shortenedUrl) {
    String shortUrl = baseUrl + "/" + shortenedUrl.getShortCode();

    return new ShortenUrlResponse(
        shortenedUrl.getShortCode(),
        shortenedUrl.getCustomAlias(),    // ❌ Campo não utilizado
        shortenedUrl.getOriginalUrl(),
        shortUrl,
        shortenedUrl.getCreatedAt(),
        shortenedUrl.getExpirationDate(),
        shortenedUrl.getClickCount(),     // ❌ Sempre presente
        shortenedUrl.getLastAccessed()    // ❌ Nunca usado
    );
}
```

**Problemas:**
- Mistura responsabilidades
- Código junto ao serviço
- Difícil de reutilizar
- Não tem método para DetailsUrlResponse

---

### DEPOIS - Mapper Separado

```java
// ShortenUrlMapper.java - Component independente
@Component
public class ShortenUrlMapper {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public ShortenUrlResponse toResponse(ShortenedUrl shortenedUrl) {
        String shortUrl = baseUrl + "/" + shortenedUrl.getShortCode();
        return new ShortenUrlResponse(
            shortenedUrl.getShortCode(),
            shortUrl,
            shortenedUrl.getOriginalUrl(),
            shortenedUrl.getCreatedAt(),
            shortenedUrl.getExpirationDate()
        );
    }

    public DetailsUrlResponse toDetailsResponse(ShortenedUrl shortenedUrl) {
        String shortUrl = baseUrl + "/" + shortenedUrl.getShortCode();
        return new DetailsUrlResponse(
            shortenedUrl.getShortCode(),
            shortUrl,
            shortenedUrl.getOriginalUrl(),
            shortenedUrl.getCreatedAt(),
            shortenedUrl.getExpirationDate(),
            shortenedUrl.getClickCount()
        );
    }
}
```

**Benefícios:**
- ✅ Responsabilidade única
- ✅ Reutilizável em todo o projeto
- ✅ Fácil de testar
- ✅ Suporta dois DTOs diferentes

---

## Uso no Serviço

### ANTES
```java
public ShortenUrlResponse shortenUrl(ShortenUrlRequest request) {
    // ... lógica ...
    return mapToResponse(saved);  // Método privado
}

public Optional<ShortenUrlResponse> getShortenedUrl(String shortCode) {
    return repository.findByShortCode(shortCode)
                     .map(this::mapToResponse);  // Só tem uma opção
}
```

### DEPOIS
```java
@Autowired
private ShortenUrlMapper mapper;

public ShortenUrlResponse shortenUrl(ShortenUrlRequest request) {
    // ... lógica ...
    return mapper.toResponse(saved);  // Usa mapper
}

public Optional<ShortenUrlResponse> getShortenedUrl(String shortCode) {
    return repository.findByShortCode(shortCode)
                     .map(mapper::toResponse);  // Injeta mapper
}

public Optional<ShortenUrlResponse> getStats(String shortCode) {
    return repository.findByShortCode(shortCode)
                     .map(mapper::toDetailsResponse);  // Opção B disponível
}
```

---

## Endpoints Afetados

### 1. POST `/v1/urls` - Encurtar URL
```
Response: ShortenUrlResponse
```
✅ **Benefício:** Retorna apenas dados essenciais

### 2. GET `/api/urls/{shortCode}` - Obter informações
```
Response: ShortenUrlResponse
```
✅ **Benefício:** Leve e rápido

### 3. GET `/stats/{shortCode}` - Obter estatísticas
```
Response: ShortenUrlResponse (ANTES)
Response: DetailsUrlResponse (DEPOIS - sugestão)
```
💡 **Melhoria:** Poderia retornar DetailsUrlResponse para incluir clickCount sem change breaking

---

## Exemplo de JSON Responses

### ShortenUrlResponse
```json
{
  "id": "x7k9mq",
  "shortUrl": "http://localhost:8080/x7k9mq",
  "originalUrl": "https://www.exemplo.com/artigos/muito/longo/titulo",
  "createdAt": "2026-03-11T14:30:22.123456",
  "expirationDate": "2026-04-11T14:30:22.123456"
}
```

### DetailsUrlResponse
```json
{
  "id": "x7k9mq",
  "shortUrl": "http://localhost:8080/x7k9mq",
  "originalUrl": "https://www.exemplo.com/artigos/muito/longo/titulo",
  "createdAt": "2026-03-11T14:30:22.123456",
  "expirationDate": "2026-04-11T14:30:22.123456",
  "clickCount": 127
}
```

---

## Resumo das Melhorias

| Aspecto | Antes | Depois |
|---------|-------|--------|
| **Campos DTO** | 8 campos misturados | 5 campos (+ 6 em Details) |
| **Mapeamento** | Método privado no Service | Component separado |
| **Reusabilidade** | Baixa | Alta |
| **Testabilidade** | Difícil | Fácil |
| **Clareza** | Confusa | Cristalina |
| **DTOs** | 1 (genérico) | 2 (específicos) |
| **Dependências** | Nenhuma | Nenhuma |


