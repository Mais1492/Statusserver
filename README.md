# Statusserver

A distributed, fault-tolerant system for geo-tagged **status messages** with a live
map dashboard. Final project for *Technologien verteilter Systeme* (SS26).

Several identical Spring Boot nodes each keep a full copy of the data and stay
consistent through a custom replication mechanism (cluster-wide UUIDs +
last-writer-wins + tombstones), with bootstrap and anti-entropy for fault
tolerance. See **[ARCHITECTURE.md](ARCHITECTURE.md)** for the full design.

## Prerequisites

- Docker Desktop (running)

## 1. Generate the TLS certificate (required, one-time)

The private key is intentionally **not** committed. Create a self-signed
`haproxy.pem` in the project root before the first run.

**Windows (PowerShell):**

```powershell
docker run --rm -v ${PWD}:/work -w /work alpine/openssl req -x509 -newkey rsa:2048 -nodes -keyout key.pem -out cert.pem -days 365 -subj "/CN=localhost"
Get-Content cert.pem, key.pem | Set-Content -Encoding ascii haproxy.pem
Remove-Item cert.pem, key.pem
```

**Linux / macOS:**

```bash
openssl req -x509 -newkey rsa:2048 -nodes -keyout key.pem -out cert.pem -days 365 -subj "/CN=localhost"
cat cert.pem key.pem > haproxy.pem && rm cert.pem key.pem
```

## 2. Run

```bash
docker compose up --build --scale statusserver=3
```

This starts RabbitMQ, two application nodes, and the HAProxy load balancer. Use a
higher number to run more nodes (e.g. `--scale statusserver=5`).

## 3. Open

| URL | What |
| --- | --- |
| https://localhost:8443 | Dashboard (HTTPS — accept the self-signed certificate warning) |
| http://localhost:8080 | Same dashboard over plain HTTP (testing & to display HTTP-setup) |
| http://localhost:8404 | HAProxy statistics |
| http://localhost:15672 | RabbitMQ management (`guest` / `guest`) |

## REST API

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/messages` | List messages (excludes deleted) |
| `POST` | `/messages` | Create a message |
| `PUT` | `/messages/{id}` | Update a message |
| `DELETE` | `/messages/{id}` | Delete a message (tombstone) |
| `GET` | `/messages/sync` | Full state incl. tombstones (node-to-node sync via proxy) |
| `GET` | `/status` | Cluster node health |

A status message has `username`, `statusText`, `latitude`, `longitude`; the server
fills in `id`, `timestamp`, and `updatedAt`.

## Technology stack

Spring Boot 4 · Java 21 · H2 (in-memory, per node) · RabbitMQ (fanout) ·
WebSocket/STOMP · Leaflet · HAProxy · Docker Compose.
