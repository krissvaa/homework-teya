# Wallet

A simple multi-currency wallet API. It lets you:

- create **accounts**
- open **balances** in different currencies for an account (an account can only have one balance per currency)
- record **transactions** (deposits and withdrawals) against a balance, with validation for currency mismatches and insufficient funds

Built with Java 25, Spring Boot 4, Spring Data JPA / Hibernate, Flyway and an in-memory H2 database, documented with springdoc-openapi (Swagger UI).

## Prerequisites

- Java 25
- No local Maven install needed — use the bundled `./mvnw` wrapper

## Running the application

```bash
./mvnw spring-boot:run
```

The app starts on **http://localhost:8080**.

It uses an in-memory H2 database (`jdbc:h2:mem:walletdb`) that Flyway seeds on startup with a few example accounts, balances and transactions (see `src/main/resources/db/migration/V2__wallet_test_data.sql`), so there's data to query right away.

### Swagger UI

Once the app is running, open the interactive API docs at:

**http://localhost:8080/swagger-ui.html**

(raw OpenAPI spec at `http://localhost:8080/v3/api-docs`)

You can try out every endpoint directly from that page.

### H2 console

To inspect the database directly, open **http://localhost:8080/h2-console** and connect with:

- JDBC URL: `jdbc:h2:mem:walletdb`
- User: `sa`
- Password: *(empty)*

## Running the tests

```bash
./mvnw test
```

This runs the unit tests plus the full `@SpringBootTest`/MockMvc integration test suite covering account creation, balance creation and deposit/withdrawal transactions.

## API examples

The examples below use `curl` and the account/balance IDs seeded by Flyway (Alice Johnson's account and her EUR balance). Swap in your own IDs, or use the ones returned by the `POST` calls, when trying this against your own data.

### Create an account

```bash
curl -i -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"name": "Dana Prince"}'
```

```json
{
  "id": "2f6a1e2e-...",
  "name": "Dana Prince",
  "createdAt": "2026-09-27T10:00:00Z",
  "updatedAt": "2026-09-27T10:00:00Z"
}
```

### List all accounts

```bash
curl http://localhost:8080/accounts
```

### Get account detail (with balances and transaction history)

```bash
curl http://localhost:8080/accounts/11111111-1111-1111-1111-111111111111
```

### Add a new currency balance to an account

An account can hold at most one balance per currency; new balances start at `0`.

```bash
curl -i -X POST http://localhost:8080/balances \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "11111111-1111-1111-1111-111111111111",
    "currency": "USD"
  }'
```

Trying to add a currency the account already has returns `409 Conflict`:

```bash
curl -i -X POST http://localhost:8080/balances \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "11111111-1111-1111-1111-111111111111",
    "currency": "EUR"
  }'
```

### List balances for an account

```bash
curl "http://localhost:8080/balances?accountId=11111111-1111-1111-1111-111111111111"
```

### Deposit funds into a balance

```bash
curl -i -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "balanceId": "aaaaaaaa-1111-1111-1111-111111111111",
    "amount": 50.00,
    "currency": "EUR",
    "type": "DEPOSIT"
  }'
```

### Withdraw funds from a balance

```bash
curl -i -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "balanceId": "aaaaaaaa-1111-1111-1111-111111111111",
    "amount": 25.00,
    "currency": "EUR",
    "type": "WITHDRAWAL"
  }'
```

A withdrawal larger than the current balance returns `400 Bad Request` ("Insufficient funds"), and submitting a currency that doesn't match the balance's currency also returns `400 Bad Request` ("Currency mismatch").

### List transactions for a balance

```bash
curl "http://localhost:8080/transactions?balanceId=aaaaaaaa-1111-1111-1111-111111111111"
```
