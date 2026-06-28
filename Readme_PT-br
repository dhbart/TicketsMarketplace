# 🎟️ Marketplace — Estudo de Persistência Multi-banco com Lock e Cache

Projeto de estudo desenvolvido durante o Bootcamp do Santander pela DIO para explorar na prática como uma aplicação Spring Boot pode gerenciar múltiplos bancos de dados ao mesmo tempo, com estratégias de **cache com Redis**, **lock distribuído** e **comunicação entre módulos por eventos internos**.

---

## 🧭 Visão Geral

A aplicação simula um marketplace de venda de ingressos dividido em três contextos bem delimitados:

| Módulo | Responsabilidade | Banco de Dados |
|---|---|---|
| **Registration** | Cadastro de clientes | MySQL (porta 3307) |
| **Catalog** | Vitrine de eventos com metadados enriquecidos | MySQL (porta 3308) + MongoDB + Redis (cache) |
| **Ticketing** | Reserva de assentos com controle de concorrência | PostgreSQL + Redis (lock) |

Cada módulo possui seu próprio `DataSource`, `EntityManagerFactory` e `TransactionManager`, configurados manualmente — sem depender do autoconfig do Spring Boot.

---

## 🏗️ Arquitetura

O projeto segue os princípios da **Arquitetura Hexagonal** (Ports & Adapters), separando claramente:

- **Domain** — entidades e interfaces de repositório (sem dependência de frameworks)
- **Application** — casos de uso orquestrando regras de negócio
- **Infrastructure** — implementações concretas: JPA, MongoDB, Redis, controllers HTTP, event listeners

A comunicação entre módulos acontece via **eventos de aplicação do Spring** (`ApplicationEventPublisher`), mantendo o desacoplamento sem a necessidade de um broker externo.

```
src/main/java/com/estudojava/marketplace/
├── common/              # DTOs de eventos compartilhados entre módulos
├── registration/        # Contexto de cadastro de clientes
├── catalog/             # Contexto de vitrine de eventos
└── ticketing/           # Contexto de reserva de assentos
```

---

## 🔧 Tecnologias

- **Java 25** com Virtual Threads (`spring.threads.virtual.enabled=true`)
- **Spring Boot 4.1**
- **Spring Data JPA** — Hibernate com múltiplos `EntityManager`
- **Spring Data MongoDB** — metadados de eventos
- **Spring Data Redis** — cache de vitrine e lock de assentos (via Jedis)
- **Spring Data REST** — CRUD REST automático para entidades JPA
- **HikariCP** — pool de conexões por DataSource
- **MySQL 9.6** (dois bancos: registration e catalog)
- **PostgreSQL 18** (banco ticketing)
- **MongoDB 8.2** (metadados do catalog)
- **Redis 8.6** (duas instâncias separadas: cache e lock)
- **Docker Compose** para subir toda a infraestrutura localmente
- **Lombok** para reduzir boilerplate

---

## 📦 Módulos em Detalhe

### Registration

Gerencia o cadastro de clientes. Usa o **Spring Data REST** para expor automaticamente endpoints CRUD sobre o repositório JPA.

Quando um cliente é criado ou atualizado, um `@RepositoryEventHandler` intercepta o evento do Spring Data REST e publica um evento interno `CustomerCreated` para os outros módulos.

**Fluxo:**
```
POST /customers (via Spring Data REST)
  → CustomerEventHandler.afterCreate()
  → publisher.publishEvent(CustomerCreated)
  → [Ticketing recebe e replica o cliente no seu banco]
```

---

### Catalog

Exibe a vitrine de eventos combinando dados de duas fontes diferentes:

- **MySQL** — dados estruturados do evento (título, data)
- **MongoDB** — metadados ricos (descrição, requisitos técnicos, mapa de assentos por setor)

O caso de uso `BrowseShowcaseUsecase` busca os eventos do MySQL e, para cada um, enriquece os dados com os metadados do MongoDB de forma **assíncrona** (`@Async` + `CompletableFuture`). O resultado final é **cacheado no Redis** com `@Cacheable`.

**Fluxo:**
```
GET /showcase
  → BrowseShowcaseUsecase.execute()
  → [Cache Redis hit? Retorna imediatamente]
  → [Cache miss?]
      → JpaEventRepository.findAll() (MySQL)
      → EventEnricher.enrich() para cada evento (async, MongoDB)
      → CompletableFuture.join() para consolidar
      → Resultado serializado e salvo no Redis
```

Quando um `EventMetadata` é salvo no MongoDB, o listener `EventMetadataEventListener` publica um `EventUpdated`, que replica os dados estruturados para o módulo Ticketing.

---

### Ticketing

Gerencia a reserva de assentos com controle de concorrência distribuído usando Redis.

**Fluxo de reserva:**
```
POST /ticketing/events/{eventId}/seats/select
  + Header: X-CUSTOMER-ID
  + Body: { seatId }
  → SelectSeatUseCase.execute()
  → Verifica se o assento existe no PostgreSQL
  → Tenta adquirir lock no Redis (SET NX, TTL de 30s)
  → Sucesso: 201 Created
  → Lock já existe: SeatAlreadyReservedException (conflito)
```

O lock é representado pela entidade `SeatLock` anotada com `@RedisHash(timeToLive = 30)`, ou seja, expira automaticamente em 30 segundos — evitando locks eternos em caso de falha.

O `WorkOfUnitEventRepository` atua como um repositório composto, coordenando as operações entre o `EventCrudRepository` (PostgreSQL/JPA) e o `RedisSeatLockRepository`.

---

## ⚙️ Configuração Multi-DataSource

Um dos principais aprendizados deste projeto é como configurar múltiplos bancos de dados na mesma aplicação Spring Boot sem autoconfig. Cada módulo tem sua própria classe `@Configuration` que define:

- `DataSourceProperties` — lê as propriedades do `application.properties`
- `HikariDataSource` — pool de conexões dedicado
- `LocalContainerEntityManagerFactoryBean` — EntityManager isolado por módulo
- `JpaTransactionManager` — transações independentes por contexto

O módulo `registration` é marcado como `@Primary` para que o Spring saiba qual usar como padrão. Os outros módulos usam `@Qualifier("catalog")` e `@Qualifier("ticketing")` para isolar seus beans.

---

## 🚀 Como rodar

**Pré-requisitos:** Docker, Java 25, Gradle

```bash
# Clonar o projeto
git clone <url-do-repo>
cd marketplace

# Subir a infraestrutura (bancos e Redis)
docker compose up -d

# Rodar a aplicação
./gradlew bootRun
```

O Spring Boot detecta automaticamente o `compose.yml` e aguarda os healthchecks dos containers antes de iniciar (`spring.docker.compose.lifecycle-management=start_only`).

---

## 🌐 Endpoints

| Método | Endpoint | Módulo | Descrição |
|---|---|---|---|
| `GET` | `/showcase` | Catalog | Lista eventos com metadados (com cache Redis) |
| `POST` | `/ticketing/events/{eventId}/seats/select` | Ticketing | Reserva um assento (com lock Redis) |
| `*` | `/customers/**` | Registration | CRUD de clientes via Spring Data REST |
| `GET` | `/actuator/health` | — | Health check com detalhes de todos os datasources |

> O HAL Explorer está disponível em `/` para navegar nos recursos Spring Data REST.

---

## 📚 Conceitos Explorados

- Configuração manual de múltiplos `DataSource` com HikariCP no Spring Boot
- Múltiplos `EntityManagerFactory` e `TransactionManager` coexistindo na mesma JVM
- Cache distribuído com Redis e `@Cacheable` (resultado precisa implementar `Serializable`)
- Lock distribuído com Redis usando `@RedisHash` com TTL automático
- Comunicação assíncrona entre módulos via `ApplicationEventPublisher` sem broker externo
- Enriquecimento paralelo de dados com `@Async` e `CompletableFuture`
- Arquitetura Hexagonal com separação de domain, application e infrastructure
- Spring Data REST + `@RepositoryEventHandler` para lifecycle hooks no CRUD
- Virtual Threads do Java 25 habilitadas para melhor throughput em I/O
