# Microservices

## Dev Ports (localhost)

| Service | Port |
|---|---|
| user-service | 8081 |
| shop-service | 8082 |
| product-service | 8083 |
| order-service | 8084 |
| agent-service | 8085 |

## Local database

It creates the `cartesian` user (password `cartesian`) and the databases
`user_db`, `shop_db`, `product_db`, `order_db`, and `agent_db`.
Each service connects to `localhost:5432` by default and applies
`schema.sql` + `data.sql` on startup.

To start a service against it:

```bash
./mvnw -pl user-service spring-boot:run
```