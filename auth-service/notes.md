Участники:
[BROWSER]  = браузер пользователя (Resource Owner user)
[CLIENT]   = приложение-клиент (heavyrent-client) на http://localhost:8080
[AS]       = Authorization Server (твой Spring Authorization Server)
[RS]       = Resource Server (API; может быть отдельным или тот же AS, если защищаешь свои endpoints)

----------------------------------------------------------------------------------------------------

(0) Старт AS
[AS] генерирует RSA KeyPair:
- PrivateKey (секретный) -> хранится только на AS
- PublicKey  (открытый)  -> будет опубликован в JWKS
[AS] публикует JWKS endpoint: GET /oauth2/jwks  (там PublicKey + kid)

----------------------------------------------------------------------------------------------------

(1) Пользователь начинает логин/авторизацию
[BROWSER] ---- GET  /oauth2/authorize?response_type=code
&client_id=heavyrent-client
&redirect_uri=http://localhost:8080/authorized
&scope=openid%20profile
&state=xyz
-----------------------------------------------> [AS]

[AS] видит: user не залогинен
[AS] ---- 302 Redirect Location: /login -------------------> [BROWSER]


(2) Логин пользователя (formLogin)
[BROWSER] ---- GET /login --------------------------------> [AS]
[BROWSER] ---- POST /login (username/password) -----------> [AS]

[AS] аутентифицирует через InMemoryUserDetailsService
[AS] ---- 302 Redirect обратно на /oauth2/authorize ------> [BROWSER]


(3) AS выдаёт authorization code и редиректит на redirect_uri клиента
[BROWSER] ---- GET /oauth2/authorize (повтор) ------------> [AS]

[AS] создаёт authorization_code (одноразовый, короткоживущий)
[AS] ---- 302 Redirect Location:
http://localhost:8080/authorized?code=abc&state=xyz ---> [BROWSER]

[BROWSER] ---- GET http://localhost:8080/authorized?code=abc&state=xyz ---> [CLIENT]

----------------------------------------------------------------------------------------------------

(4) CLIENT меняет code на tokens (server-to-server)
[CLIENT] ---- POST /oauth2/token -------------------------> [AS]
Content-Type: application/x-www-form-urlencoded
grant_type=authorization_code
code=abc
redirect_uri=http://localhost:8080/authorized

           + Client Authentication (обычно):
             Authorization: Basic base64(client_id:client_secret)
             где client_secret = "secret" (у тебя {noop}secret)

[AS] проверяет:
- code валиден?
- client_id/secret валиден?
- redirect_uri совпадает?

(5) Выпуск токенов: тут включаются RSA ключи
[AS] подписывает JWT приватным RSA PrivateKey
- access_token: JWT (часто)
- id_token: JWT (OIDC, точно JWT)
JWT header содержит "kid" (идентификатор ключа)

[AS] ---- 200 OK ----------------------------------------> [CLIENT]
{
"access_token": "eyJ... (JWT, подписан PrivateKey)",
"id_token":     "eyJ... (JWT, подписан PrivateKey)",
"token_type": "Bearer",
"expires_in": 3600,
...
}

----------------------------------------------------------------------------------------------------

(6) Проверка подписи JWT получателем через PublicKey (JWKS)
Вариант A: CLIENT хочет проверить id_token (OIDC best practice)
[CLIENT] читает JWT header -> видит kid
[CLIENT] ---- GET /oauth2/jwks ---------------------------> [AS]
[AS] ---- 200 OK (JWKS JSON: PUBLIC KEY + kid) ----------> [CLIENT]
[CLIENT] достаёт PublicKey по kid и проверяет подпись JWT


(7) Доступ к API с access_token
[CLIENT] ---- GET /api/something -------------------------> [RS]
Authorization: Bearer <access_token(JWT)>

[RS] валидирует JWT:
- берёт kid из header
- получает PublicKey из JWKS (или кеширует)
- проверяет подпись и claims (exp/iss/aud/scope)

[RS] ---- 200 OK / 401 Unauthorized ---------------------> [CLIENT]

---------------------------------------------------------------------------------------