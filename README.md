# API Challenge — CoteFácil

Duas APIs REST que se comunicam entre si, implementando autenticação via JWT e operações CRUD de pedidos.

## Arquitetura

```
Cliente → [API Gateway :8080] → autentica e valida JWT
                ↓  (proxy com token no header)
         [API Orders :8081] → CRUD no banco (H2)
```

A API Orders também valida o JWT independentemente — defesa em profundidade.

## Tecnologias

- Java 21 + Spring Boot 3.3.4
- Spring Security + JWT (jjwt 0.12.6)
- Spring Data JPA + H2 (desenvolvimento) / PostgreSQL (produção via Docker)
- Spring WebFlux (WebClient para proxy no Gateway)
- springdoc-openapi 2.5.0 (Swagger UI)
- Docker + Docker Compose

---

## Como executar

### Com Docker (recomendado)

```bash
# Na raiz do projeto
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970 \
docker-compose up --build
```

Aguarde a API Orders passar no healthcheck antes de o Gateway iniciar.

### Localmente (sem Docker)

Requer Java 21 e Maven 3.9+.

**Terminal 1 — API Orders (porta 8081):**
```bash
cd api-orders
mvn spring-boot:run
```

**Terminal 2 — API Gateway (porta 8080):**
```bash
cd api-gateway
mvn spring-boot:run
```

---

## Credenciais de teste

| Campo    | Valor      |
|----------|------------|
| username | `usuario`  |
| password | `senha123` |

---

## Exemplos de uso (curl)

### 1. Login — obter token JWT

```bash
curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"usuario","password":"senha123"}' | jq .
```

Resposta:
```json
{
  "token": "<JWT>",
  "type": "Bearer",
  "expiresIn": 3600
}
```

Exporte o token para os próximos comandos:
```bash
TOKEN="<cole o token aqui>"
```

### 2. Criar pedido

```bash
curl -s -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "João Silva",
    "customerEmail": "joao@email.com"
  }' | jq .
```

### 3. Listar pedidos (paginado)

```bash
curl -s "http://localhost:8080/api/orders?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN" | jq .
```

### 4. Buscar pedido por ID

```bash
curl -s http://localhost:8080/api/orders/1 \
  -H "Authorization: Bearer $TOKEN" | jq .
```

### 5. Adicionar item ao pedido

```bash
curl -s -X POST http://localhost:8080/api/orders/1/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "Produto A",
    "quantity": 3,
    "unitPrice": 15.00
  }' | jq .
```

### 6. Atualizar pedido

```bash
curl -s -X PUT http://localhost:8080/api/orders/1 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "João Silva",
    "customerEmail": "joao@email.com",
    "status": "CONFIRMED"
  }' | jq .
```

### 7. Deletar pedido

```bash
curl -s -X DELETE http://localhost:8080/api/orders/1 \
  -H "Authorization: Bearer $TOKEN"
```

---

## Swagger UI

| API      | URL                                    |
|----------|----------------------------------------|
| Gateway  | http://localhost:8080/swagger-ui.html  |
| Orders   | http://localhost:8081/swagger-ui.html  |

No Swagger UI, clique em **Authorize** e informe o token no formato `Bearer <token>`.

---

## Endpoints

### API Gateway (porta 8080)

| Método | Path             | Auth       | Descrição                       |
|--------|------------------|------------|---------------------------------|
| POST   | `/auth/login`    | Público    | Autentica e retorna JWT         |
| GET    | `/api/orders/**` | Bearer JWT | Proxy para API Orders           |
| POST   | `/api/orders/**` | Bearer JWT | Proxy para API Orders           |
| PUT    | `/api/orders/**` | Bearer JWT | Proxy para API Orders           |
| DELETE | `/api/orders/**` | Bearer JWT | Proxy para API Orders           |

### API Orders (porta 8081)

| Método | Path                       | Descrição                          |
|--------|----------------------------|------------------------------------|
| GET    | `/api/orders`              | Lista paginada de pedidos          |
| GET    | `/api/orders/{id}`         | Busca pedido por ID                |
| POST   | `/api/orders`              | Cria pedido                        |
| PUT    | `/api/orders/{id}`         | Atualiza pedido                    |
| DELETE | `/api/orders/{id}`         | Remove pedido                      |
| GET    | `/api/orders/{id}/items`   | Lista itens do pedido              |
| POST   | `/api/orders/{id}/items`   | Adiciona item (recalcula total)    |

---

## Decisões arquiteturais

| Decisão | Justificativa |
|---------|---------------|
| WebClient (não RestTemplate) | Non-blocking; RestTemplate em modo de manutenção desde Spring 5 |
| API Orders também valida JWT | Defesa em profundidade — acesso direto à porta 8081 ainda exige token válido |
| Interfaces para Services | Respeita DIP (SOLID); facilita mock nos testes e troca de implementação |
| DTOs como `record` | Imutáveis, sem boilerplate, aprovados pelo Sonar |
| `Optional` nos repositories | Elimina NPE e evita retornar `null` — alerta do Sonar |
| `orphanRemoval = true` | Item removido da lista do pedido é deletado automaticamente do banco |
| Docker multi-stage | Imagem final só com JRE Alpine — menor, sem ferramentas de build expostas |
| H2 em memória | Facilita execução local sem dependência externa |
