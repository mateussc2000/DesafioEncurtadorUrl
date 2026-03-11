# Refatoração dos DTOs de Response

## Resumo das Mudanças

Foram realizadas refatorações nos DTOs de resposta da API de encurtamento de URLs, conforme solicitado. A solução implementada é **simples, prática e sem dependências externas** (sem MapStruct).

---

## 📋 Mudanças Realizadas

### 1. **ShortenUrlResponse** (Atualizado)
**Localização:** `src/main/java/com/encurtador_url/SuperApp/dto/ShortenUrlResponse.java`

#### Campos obrigatórios:
- `id` (String) - Código curto da URL
- `shortUrl` (String) - URL encurtada completa
- `originalUrl` (String) - URL original
- `createdAt` (LocalDateTime) - Data/hora de criação
- `expirationDate` (LocalDateTime) - Data/hora de expiração

**Implementação:** Java Record (Java 21+)

```java
public record ShortenUrlResponse(
    String id,
    String shortUrl,
    String originalUrl,
    LocalDateTime createdAt,
    LocalDateTime expirationDate
) {}
```

---

### 2. **DetailsUrlResponse** (Novo)
**Localização:** `src/main/java/com/encurtador_url/SuperApp/dto/DetailsUrlResponse.java`

#### Campos:
- `id` (String) - Código curto da URL
- `shortUrl` (String) - URL encurtada completa
- `originalUrl` (String) - URL original
- `createdAt` (LocalDateTime) - Data/hora de criação
- `expirationDate` (LocalDateTime) - Data/hora de expiração
- `clickCount` (Integer) - Número de cliques

**Implementação:** Java Record (Java 21+)

```java
public record DetailsUrlResponse(
    String id,
    String shortUrl,
    String originalUrl,
    LocalDateTime createdAt,
    LocalDateTime expirationDate,
    Integer clickCount
) {}
```

---

### 3. **ShortenUrlMapper** (Novo)
**Localização:** `src/main/java/com/encurtador_url/SuperApp/util/ShortenUrlMapper.java`

#### Responsabilidades:
- Converter entidade `ShortenedUrl` para `ShortenUrlResponse`
- Converter entidade `ShortenedUrl` para `DetailsUrlResponse`
- Gerenciar a construção da URL encurtada usando a property `app.base-url`

#### Métodos:
```java
public ShortenUrlResponse toResponse(ShortenedUrl shortenedUrl)
public DetailsUrlResponse toDetailsResponse(ShortenedUrl shortenedUrl)
```

#### Anotações:
- `@Component` - Spring gerencia o bean
- `@Value("${app.base-url:http://localhost:8080}")` - Injeção de propriedade configurável

---

### 4. **ShortenUrlServiceImpl** (Atualizado)
**Localização:** `src/main/java/com/encurtador_url/SuperApp/service/ShortenUrlServiceImpl.java`

#### Mudanças:
- ✅ Removido campo `@Value baseUrl` (agora no Mapper)
- ✅ Removido método `mapToResponse()` privado (lógica migrada para Mapper)
- ✅ Adicionada injeção do `ShortenUrlMapper` via `@Autowired`
- ✅ Atualização de todas as chamadas para usar `mapper.toResponse()`

#### Métodos afetados:
- `shortenUrl()` - Usa `mapper.toResponse()`
- `getShortenedUrl()` - Usa `mapper::toResponse` (method reference)
- `getStats()` - Usa `mapper::toResponse` (method reference)

---

## 🎯 Decisão: Por que NÃO usar MapStruct?

### ✅ Vantagens da Solução Manual (Mapper)

1. **Simplicidade**
   - Apenas 58 linhas de código
   - Sem dependências externas
   - Sem anotações complexas
   - Sem geração de código

2. **Praticidade para o Desafio**
   - A lógica é trivial (apenas construção de objetos)
   - MapStruct seria overhead desnecessário
   - Fácil de entender e manter

3. **Transparência**
   - Código explícito e óbvio
   - Sem "magia" de anotações
   - Fácil debug

4. **Performance**
   - Sem geração de código em tempo de compilação
   - Sem reflexão desnecessária
   - Método simples = mais rápido

### ❌ Por que não MapStruct?

- **Overhead:** Dependência extra no projeto
- **Overkill:** Lógica muito simples para usar framework
- **Complexidade:** Anotações e configuração desnecessárias
- **Tamanho:** Aumentaria o JAR sem benefício real

---

## 🔄 Fluxo de Dados

```
HTTP Request
    ↓
ShortenUrlController
    ↓
ShortenUrlServiceImpl.shortenUrl()
    ↓
ShortenedUrl (entidade JPA)
    ↓
ShortenUrlMapper.toResponse() ou toDetailsResponse()
    ↓
ShortenUrlResponse ou DetailsUrlResponse
    ↓
HTTP Response (JSON)
```

---

## 📝 Exemplo de Uso

### Resposta de Encurtamento (ShortenUrlResponse)
```json
{
  "id": "abc123",
  "shortUrl": "http://localhost:8080/abc123",
  "originalUrl": "https://www.exemplo.com/pagina/muito/longa",
  "createdAt": "2026-03-11T10:30:00",
  "expirationDate": "2026-03-18T10:30:00"
}
```

### Resposta com Estatísticas (DetailsUrlResponse)
```json
{
  "id": "abc123",
  "shortUrl": "http://localhost:8080/abc123",
  "originalUrl": "https://www.exemplo.com/pagina/muito/longa",
  "createdAt": "2026-03-11T10:30:00",
  "expirationDate": "2026-03-18T10:30:00",
  "clickCount": 42
}
```

---

## ✅ Validação

### Arquivos Criados/Modificados:
- ✅ `ShortenUrlResponse.java` - Refatorado
- ✅ `DetailsUrlResponse.java` - Novo
- ✅ `ShortenUrlMapper.java` - Novo
- ✅ `ShortenUrlServiceImpl.java` - Atualizado

### Compatibilidade:
- ✅ Sem breaking changes no controller
- ✅ Compatível com Spring Boot 4.0.3
- ✅ Compatível com Java 21
- ✅ Usa Records (suporte nativo em Java 21)

---

## 🚀 Próximos Passos (Opcional)

Se no futuro a lógica de mapeamento crescer significativamente, migrar para MapStruct seria uma opção viável. Por enquanto, a solução manual é a mais prática.

---

**Data:** 11/03/2026  
**Versão:** 1.0

