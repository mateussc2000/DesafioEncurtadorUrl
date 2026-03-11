# Sugestão de Melhoria - Controller (OPCIONAL)

## Contexto
O endpoint `/stats/{shortCode}` atualmente retorna `ShortenUrlResponse`, mas deveria retornar `DetailsUrlResponse` para incluir o `clickCount`.

---

## Proposta de Melhoria

### Arquivo: `ShortenUrlController.java`

#### Adicionar import:
```java
import com.encurtador_url.SuperApp.dto.DetailsUrlResponse;
```

#### Atualizar interface do Service:
```java
// ShortenUrlService.java
Optional<DetailsUrlResponse> getStats(String shortCode);  // ← Mudança de tipo
```

#### Atualizar implementação do Service:
```java
// ShortenUrlServiceImpl.java
@Override
@Transactional(readOnly = true)
public Optional<DetailsUrlResponse> getStats(String shortCode) {
    Optional<ShortenedUrl> url = repository.findByShortCode(shortCode);
    return url.map(mapper::toDetailsResponse);  // ← Usar toDetailsResponse
}
```

#### Atualizar o Controller:
```java
@GetMapping("/stats/{shortCode}")
@Operation(summary = "Obter estatísticas", description = "Retorna números de cliques e último acesso")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Estatísticas obtidas"),
    @ApiResponse(responseCode = "404", description = "URL não encontrada"),
})
public ResponseEntity<DetailsUrlResponse> getStats(@PathVariable String shortCode) {
    Optional<DetailsUrlResponse> response = service.getStats(shortCode);  // ← Tipo mudado
    return response.map(ResponseEntity::ok)
                  .orElseGet(() -> ResponseEntity.notFound().build());
}
```

---

## Benefícios

✅ **Coesão Semântica**
- Endpoint `/stats/` retorna estatísticas (inclui clickCount)
- Faz sentido incluir o número de cliques

✅ **Utiliza o Novo DTO**
- `DetailsUrlResponse` foi criado para exatamente este caso

✅ **Consistência**
- `GET /api/urls/{shortCode}` → ShortenUrlResponse (básico)
- `GET /stats/{shortCode}` → DetailsUrlResponse (com detalhes)

✅ **API Rest Mais Limpa**
- Respostas semanticamente corretas
- Cada endpoint retorna o DTO apropriado

---

## Possível Impacto

### Breaking Change ⚠️
- Clientes que consomem `/stats/{shortCode}` receberão `clickCount`
- Se já esperavam este campo, é upgrade automático
- Se não esperavam, é apenas um campo novo (não quebra parsing JSON)

### Mitigação
- Documentação clara no Swagger
- Versionamento de API (ex: `/v1/stats/`, `/v2/stats/`)

---

## Alternativa: Manter Atual (NÃO Recomendado)

Se quiser manter `ShortenUrlResponse` em `/stats/`:
- DTO não fica inútil
- Mas `DetailsUrlResponse` perde seu propósito
- Melhor implementar a mudança acima

---

## Checklist de Implementação

Se decidir implementar:

- [ ] Atualizar `ShortenUrlService.java` interface
- [ ] Atualizar `ShortenUrlServiceImpl.getStats()` 
- [ ] Atualizar `ShortenUrlController.getStats()`
- [ ] Adicionar import de `DetailsUrlResponse`
- [ ] Compilar e testar
- [ ] Atualizar documentação/testes

---

## Resumo

**Decisão:** Esta é uma sugestão de melhoria, não é obrigatória.

Se quer aproveitar o novo `DetailsUrlResponse`:
→ Implemente a mudança acima

Se quer manter tudo como está:
→ `DetailsUrlResponse` fica disponível para uso futuro em outros endpoints

