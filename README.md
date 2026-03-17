# API Challenge

Duas APIs REST que se comunicam entre si, implementando controle de acesso via JWT e operações CRUD para pedidos.

## Arquitetura

```
Cliente → [API 1: Gateway :8080] → JWT gerado/validado
                    ↓  (proxy com token no header)
             [API 2: Orders :8081] → CRUD no banco
```

## Tecnologias

- Java 21
- Spring Boot 3.3.4
- Spring Security + JWT (jjwt 0.12.6)
- JPA/Hibernate + H2 (desenvolvimento)
- Docker + Docker Compose

## Como executar

### Com Docker

```bash
# TODO: instruções de execução com Docker
```

### Localmente (sem Docker)

```bash
# TODO: instruções de execução local
```

## Credenciais de teste

```
# TODO: credenciais padrão
```

## Endpoints

### API Gateway (porta 8080)

```
# TODO: lista de endpoints
```

### API Orders (porta 8081)

```
# TODO: lista de endpoints
```

## Swagger UI

- Gateway: http://localhost:8080/swagger-ui.html
- Orders: http://localhost:8081/swagger-ui.html
