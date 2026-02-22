# auth-service

OAuth2 Authorization Server for HeavyRent. Issues JWT tokens signed with RSA-256. Built on Spring Authorization Server 7.0.2.

## Responsibilities

- Authenticating users (form login)
- Issuing `access_token` and `id_token` (JWT)
- Exposing public keys via JWKS endpoint for other services to verify tokens

## Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| GET | `/oauth2/authorize` | Start authorization flow |
| POST | `/oauth2/token` | Exchange code for tokens |
| GET | `/oauth2/jwks` | Public RSA keys (used by resource servers) |
| GET | `/.well-known/openid-configuration` | OIDC discovery document |
| GET/POST | `/login` | Login form |

## OAuth2 Authorization Code Flow

### Step 1 — Request authorization code

Open in browser:

```
http://localhost:8080/oauth2/authorize
  ?response_type=code
  &client_id=heavyrent-client
  &redirect_uri=http://localhost:8080/authorized
  &scope=openid%20profile
```

Login with:
- **Username:** `user@heavyrent.com`
- **Password:** `password`

After login, browser redirects to:
```
http://localhost:8080/authorized?code=XXXXXX
```

Copy the full `code` from the URL — it expires in ~5 minutes and is single-use.

### Step 2 — Exchange code for tokens

```bash
curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -u "heavyrent-client:secret" \
  -d "grant_type=authorization_code" \
  -d "code=YOUR_CODE_HERE" \
  -d "redirect_uri=http://localhost:8080/authorized"
```

Response:

```json
{
  "access_token": "eyJraWQi...",
  "id_token": "eyJraWQi...",
  "token_type": "Bearer",
  "expires_in": 299
}
```

Use `access_token` in all subsequent requests to resource servers.

### Step 3 — Call a protected endpoint

```bash
curl http://localhost:8081/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## JWT Token Structure

The `access_token` is a signed JWT. Payload example (decoded from Base64):

```json
{
  "sub":   "user@heavyrent.com",
  "aud":   "heavyrent-client",
  "scope": ["openid", "profile"],
  "iss":   "http://localhost:8080",
  "exp":   1771536079,
  "iat":   1771535779
}
```

Token is **signed** (not encrypted) with RSA-256. Anyone can read the payload, but only auth-service can produce a valid signature.

## Configuration

```yaml
server:
  port: 8080

# Registered client (MVP - in-memory)
client-id:     heavyrent-client
client-secret: secret
redirect-uri:  http://localhost:8080/authorized
scopes:        openid, profile

# Test user (MVP - in-memory)  
username: user@heavyrent.com
password: password
role:     RENTER
```

## MVP Limitations (to fix before production)

| What | Now | Production |
|------|-----|------------|
| Users | InMemoryUserDetailsManager | JdbcUserDetailsManager + PostgreSQL |
| Clients | InMemoryRegisteredClientRepository | JdbcRegisteredClientRepository |
| Tokens | InMemoryOAuth2AuthorizationService | JdbcOAuth2AuthorizationService |
| RSA keys | Generated on startup (lost on restart) | Stored in KeyStore / Vault |
| Passwords | `{noop}` plain text | BCryptPasswordEncoder |
| PKCE | Disabled | `requireProofKey(true)` |

## Running

```bash
./gradlew :auth-service:bootRun
```

Service starts on `http://localhost:8080`