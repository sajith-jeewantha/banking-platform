# 🏦 Digital Banking Platform
**WSO2 Identity Server + WSO2 API Manager + Spring Boot + MySQL + Docker**

---

## Development Status

This project is actively being developed.  
Some features may be incomplete or unstable.

---

## 🏗️ Architecture

```bash
Postman (Client)
      │
      ├──[OAuth2 Token]──► WSO2 Identity Server :9443
      │                        (SSO, OAuth2, JWT Issuer)
      │
      └──[Bearer Token]──► WSO2 API Manager :8243 (Gateway)
                               │  (validates token with IS)
                               │
                    ┌──────────┴──────────┐
                    ▼                     ▼
          Account Service :8081   Transaction Service :8082
                    │                     │
                    └────────┬────────────┘
                             ▼
                       MySQL :3306
                  ┌─────────────────────┐
                  │ banking_account_db  │
                  │ banking_txn_db      │
                  └─────────────────────┘
```

---

## 📋 Prerequisites
- Docker Desktop installed and running
- Postman installed
- 8GB+ RAM (WSO2 products are heavy)

---

## 🚀 STEP 1 — Start All Services

```bash
# Clone or place this project, then:
cd banking-platform

# Start everything
docker compose up --build

# Wait about 3-4 minutes for WSO2 IS and APIM to fully start
# Watch for: "WSO2 Identity Server started" in the logs
```

**Check services are running:**
| Service | URL |
|---|---|
| WSO2 IS Console | https://localhost:9443/console |
| WSO2 APIM Publisher | https://localhost:9444/publisher |
| WSO2 APIM DevPortal | https://localhost:9444/devportal |
| Account Service | http://localhost:8081 |
| Transaction Service | http://localhost:8082 |

> All WSO2 URLs use self-signed SSL — click "Advanced → Proceed" in your browser.

---

## 🔐 STEP 2 — Configure WSO2 Identity Server

### 2.1 — Login to IS Console
- URL: https://localhost:9443/console
- Username: `admin`
- Password: `admin`

### 2.2 — Create Users
Go to **User Management → Users → Add User**

Create two users:
| Username | Password | Role |
|---|---|---|
| alice | Alice@123 | CUSTOMER |
| bob | Bob@123 | CUSTOMER |

### 2.3 — Create OAuth2 Application (Service Provider)
Go to **Applications → New Application → Standard Based → OAuth2/OpenID Connect**

Settings:
- Name: `BankingApp`
- Grant Types: ✅ Password, ✅ Client Credentials, ✅ Refresh Token
- Callback URL: `http://localhost` (dummy for now)
- Click **Register**

**Copy the Client ID and Client Secret** — you'll need them in Postman.

### 2.4 — Allow Password Grant for Users
Go to the application → **API Authorization → Authorize** the `openid` scope.

---

## 🌐 STEP 3 — Configure WSO2 API Manager

### 3.1 — Login to Publisher
- URL: https://localhost:9444/publisher
- Username: `admin` / Password: `admin`

### 3.2 — Connect APIM to WSO2 IS as Key Manager
Go to **Admin Portal** (https://localhost:9444/admin) → **Key Managers → Add Key Manager**

| Field | Value |
|---|---|
| Name | WSO2IS |
| Type | WSO2 Identity Server |
| Well-known URL | https://wso2is:9443/oauth2/oidcdiscovery/.well-known/openid-configuration |
| Username | admin |
| Password | admin |

Click **Import** then **Save**.

### 3.3 — Create Account API
Go to **Publisher → Create API → REST API → Design New**

| Field | Value |
|---|---|
| Name | AccountAPI |
| Context | /account-api |
| Version | 1.0 |
| Endpoint | http://account-service:8081 |

Add these resources:
- `POST /api/accounts`
- `GET /api/accounts`
- `GET /api/accounts/{id}`
- `GET /api/accounts/{id}/balance`

Click **Deploy → Publish**

### 3.4 — Create Transaction API
Same steps, different values:

| Field | Value |
|---|---|
| Name | TransactionAPI |
| Context | /transaction-api |
| Version | 1.0 |
| Endpoint | http://transaction-service:8082 |

Resources:
- `POST /api/transactions/transfer`
- `POST /api/transactions/deposit`
- `POST /api/transactions/withdraw`
- `GET /api/transactions/my`
- `GET /api/transactions/account/{accountId}`
- `GET /api/transactions/{ref}`

Click **Deploy → Publish**

### 3.5 — Subscribe in Developer Portal
Go to **DevPortal** (https://localhost:9444/devportal)
- Create an Application: `BankingApp`
- Subscribe to both `AccountAPI` and `TransactionAPI`
- Generate keys (use the **WSO2IS** key manager you configured)

---

## 📬 STEP 4 — Setup Postman

1. Import `postman/Banking-Platform.postman_collection.json`
2. Edit Collection Variables:
    - `CLIENT_ID` → paste from WSO2 IS app
    - `CLIENT_SECRET` → paste from WSO2 IS app

### 4.1 — Get Access Token
Run: **"0. WSO2 IS - Get Access Token → Get Token (Password Grant)"**

This will automatically save the token to `ACCESS_TOKEN` variable.

---

## 🧪 STEP 5 — Test the Flow

Run these in order:

### Phase 1: Create Accounts
1. `POST /api/accounts` → Create Savings account (save the `id`)
2. `POST /api/accounts` → Create Current account (save the `id`)
3. `GET /api/accounts` → See both accounts

### Phase 2: Transactions
4. `POST /api/transactions/deposit` → Deposit to account 1
5. `GET /api/accounts/1/balance` → Verify balance increased
6. `POST /api/transactions/withdraw` → Withdraw from account 1
7. `POST /api/transactions/transfer` → Transfer account 1 → account 2
8. `GET /api/transactions/my` → See full transaction history

### Phase 3: Via APIM Gateway
9. Same requests but using `{{APIM_GW_URL}}` — token is now validated by APIM too

---

## 📊 API Reference

### Account Service (port 8081)
| Method | Endpoint | Description |
|---|---|---|
| POST | /api/accounts | Create new account |
| GET | /api/accounts | Get all my accounts |
| GET | /api/accounts/{id} | Get account by ID |
| GET | /api/accounts/{id}/balance | Get balance |

### Transaction Service (port 8082)
| Method | Endpoint | Description |
|---|---|---|
| POST | /api/transactions/deposit | Deposit money |
| POST | /api/transactions/withdraw | Withdraw money |
| POST | /api/transactions/transfer | Transfer between accounts |
| GET | /api/transactions/my | My transaction history |
| GET | /api/transactions/account/{id} | Transactions for account |
| GET | /api/transactions/{ref} | Get by transaction reference |

---

## 🗄️ Database

Connect with any MySQL client:
- Host: `localhost:3306`
- Username: `root`
- Password: `{password}`
- Databases: `banking_account_db`, `banking_transaction_db`

---

## 🛑 Stop Everything

```bash
docker compose down

# To also delete all data:
docker compose down -v
```

---

## 🐛 Troubleshooting

**WSO2 IS not starting?**
→ Give it more time (3-5 minutes). Check: `docker logs wso2-identity-server`

**Spring Boot can't connect to MySQL?**
→ MySQL health check must pass first. Wait and retry: `docker compose restart account-service`

**Token validation failing?**
→ Make sure APIM Key Manager is pointing to `wso2is` (Docker hostname), not `localhost`

**SSL errors in Postman?**
→ Go to Postman Settings → disable SSL certificate verification
