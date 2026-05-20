# API Changes — PR #43

Documentação das mudanças de API para implementação no front-end.

**Base URL:** `http://localhost:8080` (desenvolvimento) / `${APP_BASE_URL}` (produção)

---

## Endpoints Novos

### Confirmação de E-mail

```
GET /usuario/confirmar?token={token}
```



**Autenticação:** Nenhuma (público)  
**Rate limit:** 10 requisições por minuto por IP

Confirma o e-mail do usuário a partir do token recebido no e-mail de cadastro. O token expira em **24 horas**.

**Query params:**

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `token` | string | sim | Token UUID recebido no e-mail |

**Respostas:**

| Status | Corpo | Situação |
|---|---|---|
| `204 No Content` | — | E-mail confirmado com sucesso |
| `400 Bad Request` | `"Token inválido"` | Token não existe na base |
| `400 Bad Request` | `"Token expirado"` | Token ultrapassou 24h |
| `400 Bad Request` | `"Usuário não encontrado"` | Usuário removido após geração do token |

**Exemplo de uso (link enviado por e-mail):**
```
GET /usuario/confirmar?token=550e8400-e29b-41d4-a716-446655440000
```

---

### Reenvio de E-mail de Confirmação

```
POST /usuario/reenviar-confirmacao?email={email}
```

**Autenticação:** Nenhuma (público)  
**Rate limit:** 10 requisições por minuto por IP

Invalida o token anterior (se existir) e gera um novo token de confirmação com validade de **24 horas**, reenviando o e-mail.

**Query params:**

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `email` | string | sim | E-mail do usuário cadastrado |

**Respostas:**

| Status | Corpo | Situação |
|---|---|---|
| `204 No Content` | — | Novo e-mail de confirmação enviado |
| `400 Bad Request` | `"Usuário não encontrado"` | E-mail não cadastrado na base |
| `400 Bad Request` | `"E-mail já confirmado"` | Usuário já completou a verificação |
| `429 Too Many Requests` | `"Too many requests"` | Rate limit atingido |

**Exemplo:**
```
POST /usuario/reenviar-confirmacao?email=usuario@exemplo.com
```

**Fluxo esperado no front-end:**
1. Exibir botão "Reenviar e-mail de confirmação" na tela de aviso pós-cadastro
2. Chamar este endpoint ao clicar
3. Exibir feedback de sucesso e, idealmente, desabilitar o botão por alguns segundos para evitar cliques repetidos

---

## Endpoints Alterados

### Cadastro de Usuário

```
POST /usuario
```

**Autenticação:** Nenhuma (público)  
**Rate limit:** 10 requisições por minuto por IP  
**Content-Type:** `application/json`

> **Mudança de comportamento:** Além de criar o usuário, agora também cria automaticamente um **plano gratuito mensal** e **envia um e-mail de confirmação** para o endereço cadastrado. O usuário começa com `emailValidado: false`.

**Corpo da requisição:**
```json
{
  "email": "usuario@exemplo.com",
  "senha": "minhasenha123"
}
```

| Campo | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `email` | string | sim | E-mail do usuário |
| `senha` | string | sim | Senha em texto puro (hash feito no servidor) |

**Respostas:**

| Status | Corpo | Situação |
|---|---|---|
| `201 Created` | — | Usuário criado, e-mail de confirmação enviado |
| `400 Bad Request` | `"Email já cadastrado"` | E-mail já existe na base |
| `400 Bad Request` | `"Usuário já possui um plano ativo"` | Plano ativo já existe para este e-mail |
| `429 Too Many Requests` | `"Too many requests"` | Rate limit atingido |

**Fluxo esperado no front-end:**
1. Chamar `POST /usuario`
2. Exibir mensagem pedindo para o usuário verificar o e-mail
3. Quando o usuário clicar no link do e-mail, ele será redirecionado para `GET /usuario/confirmar?token=...`
4. Após confirmação bem-sucedida, redirecionar para login

---

### Plano Vigente

```
GET /plano/vigente?email={email}
```

**Autenticação:** Nenhuma (público)

> **Mudança:** O campo `gratuito` foi adicionado à resposta.

**Query params:**

| Parâmetro | Tipo | Obrigatório | Descrição |
|---|---|---|---|
| `email` | string | sim | E-mail do usuário |
| `tenant` | string | não | Identificador do tenant (usar apenas se necessário) |

**Resposta `200 OK`:**
```json
{
  "email": "usuario@exemplo.com",
  "vigenteAte": "2026-06-19",
  "gratuito": true
}
```

| Campo | Tipo | Descrição |
|---|---|---|
| `email` | string | E-mail do dono do plano |
| `vigenteAte` | string (`yyyy-MM-dd`) | Data de expiração do plano |
| `gratuito` | boolean | `true` = plano gratuito, `false` = plano pago |

---

### Criar Plano (admin)

```
POST /plano
```

**Autenticação:** Requerida (Bearer JWT)  
**Content-Type:** `application/json`

> **Mudança:** O campo `gratuito` foi adicionado ao corpo da requisição.

**Corpo da requisição:**
```json
{
  "email": "usuario@exemplo.com",
  "periodoPlano": "MENSAL",
  "gratuito": false
}
```

| Campo | Tipo | Obrigatório | Valores aceitos | Descrição |
|---|---|---|---|---|
| `email` | string | sim | — | E-mail do usuário |
| `periodoPlano` | string | sim | `"MENSAL"`, `"ANUAL"` | Período de vigência |
| `gratuito` | boolean | não | `true`, `false` | Se `null`, assume `false` |

**Resposta `200 OK`:**

Sem corpo. O ID do plano criado é retornado no header:
```
id: <id-do-plano>
```

---

## Rate Limiting

Os endpoints públicos de usuário possuem proteção contra abuso. O controle é feito **por IP**.

| Endpoint | Limite |
|---|---|
| `POST /usuario` | 10 req / 60 seg |
| `GET /usuario/confirmar` | 10 req / 60 seg |
| `POST /usuario/reenviar-confirmacao` | 10 req / 60 seg |

Quando o limite é ultrapassado:
```
HTTP 429 Too Many Requests

Too many requests
```

O front-end deve tratar esse status e exibir uma mensagem adequada ao usuário (ex: "Muitas tentativas. Aguarde alguns instantes.").

---

## Notas de Implementação

- O link de confirmação no e-mail aponta para o **back-end** (`/usuario/confirmar?token=...`). Se o front-end precisar interceptar esse redirecionamento, é necessário ajustar a variável `APP_BASE_URL` na configuração do servidor para apontar para a URL do front-end e então redirecionar internamente para a API.
- O campo `emailValidado` **não é retornado** em nenhum endpoint atual. Se o front-end precisar checar esse estado, solicitar um novo endpoint.
- Datas são retornadas em formato ISO-8601: `vigenteAte` no padrão `yyyy-MM-dd`.
