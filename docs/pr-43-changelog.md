# PR #43 — Develop → Main

**Autor:** Jeanluca  
**Status:** Aberto  
**Data de criação:** 19/05/2026  
**Branch:** `develop` → `main`  
**Adições:** 924 linhas | **Remoções:** 32 linhas

---

## Resumo

Este PR introduz três funcionalidades principais: **confirmação de e-mail no cadastro de usuários**, **rate limiting nos endpoints públicos** e o **campo `gratuito` no modelo de Plano**. Além disso, o fluxo de registro de usuário foi refatorado para criar automaticamente um plano gratuito e disparar o e-mail de confirmação de forma assíncrona.

---

## Funcionalidades Implementadas

### 1. Confirmação de E-mail

Fluxo completo de verificação de e-mail ao cadastrar um novo usuário.

**Entidade `TokenConfirmacao`**
- Documento MongoDB na coleção `token_confirmacao`
- Token gerado via `UUID.randomUUID()`
- Expiração de **24 horas** após a criação
- Método `estaExpirado()` para validação de validade

**Serviço `EnviarEmailConfirmacaoService`**
- Execução **assíncrona** (`@Async`) para não bloquear o cadastro
- Consome a API externa de e-mail via `RestClient`
- Template utilizado: `confirmacao-cadastro`
- Link de confirmação gerado no formato: `{APP_BASE_URL}/usuario/confirmar?token={token}`
- Erros de envio são logados sem propagar exceção

**Use Case `ConfirmarEmailUseCase`**
- Valida existência e expiração do token
- Marca o campo `emailValidado = true` no documento `Usuario`
- Remove o token da base após confirmação bem-sucedida

**Novo endpoint:**
```
GET /usuario/confirmar?token={token}
```
- Resposta `204 No Content` em caso de sucesso
- Resposta `400` com mensagem de erro para token inválido ou expirado

---

### 2. Rate Limiting

Proteção contra abuso nos endpoints públicos de usuário usando cache Caffeine em memória.

**`RateLimitInterceptor`** (`HandlerInterceptor`)
- Chave de controle: `IP + HTTP_METHOD + URI`
- Retorna `429 Too Many Requests` ao ultrapassar o limite configurado
- Cache com expiração automática baseada na janela de tempo

**`RateLimitProperties`**
- Configuração via `application.yaml` com prefixo `rate-limit`
- Defaults: `maxRequests = 10`, `windowSeconds = 60`

**Configuração em `application.yaml`:**
```yaml
rate-limit:
  max-requests: 10
  window-seconds: 60
```

**Endpoints protegidos** (configurado em `WebMvcConfig`):
- `POST /usuario` — cadastro
- `GET /usuario/confirmar` — confirmação de e-mail

**Dependência adicionada (`pom.xml`):**
```xml
com.github.ben-manes.caffeine:caffeine
```

---

### 3. Flag `gratuito` no Plano

Diferenciação entre planos gratuitos e pagos no modelo `Plano`.

**Mudanças no domínio:**
- Campo `gratuito` alterado de `Boolean` (wrapper) para `boolean` (primitivo)
- Novo factory method: `Plano.criarPlano(email, periodo, gratuito)`
- Factory method existente `criarPlanoPadrao` passa a delegar para `criarPlano(..., true)`
- Método `processarReembolso()` agora marca o plano como `gratuito = true`

**Propagação do campo:**
- `PlanoAtivoOutput` atualizado para expor `gratuito`
- `CriarPlanoInputDto` recebe o campo `gratuito` na criação via API
- `CriarPlanoUseCase` repassa o campo ao criar um plano

---

### 4. Refatoração do Registro de Usuário

O use case `RegistrarUsuarioUseCase` foi expandido para:

1. Validar e-mail não cadastrado anteriormente
2. Criar o `Usuario` com `emailValidado = false`
3. Criar automaticamente um **plano gratuito mensal** para o novo usuário
4. Gerar e persistir um `TokenConfirmacao`
5. Disparar o envio do e-mail de confirmação de forma assíncrona

```
POST /usuario  →  Cria usuário + Plano gratuito + Envia e-mail de confirmação
```

---

### 5. Novas Configurações

| Classe | Responsabilidade |
|---|---|
| `PasswordEncoderConfig` | Bean `BCryptPasswordEncoder` para hash de senhas |
| `RestClientConfig` | Bean `RestClient` configurado para a API de e-mail |
| `EmailApiProperties` | Propriedades `email-api.base-url` do `application.yaml` |
| `WebMvcConfig` | Registro do `RateLimitInterceptor` nos paths protegidos |

**Variáveis de ambiente adicionadas:**
| Variável | Default | Descrição |
|---|---|---|
| `EMAIL_API_URL` | `https://email-api-placeholder.com` | URL base da API de e-mail |
| `APP_BASE_URL` | `http://localhost:8080` | URL base da aplicação (usada no link de confirmação) |

**`SecurityConfig`** atualizado para liberar o endpoint `GET /usuario/confirmar` sem autenticação.

---

## Testes Adicionados

| Arquivo de Teste | Cobertura |
|---|---|
| `RegistrarUsuarioControllerTest` | Cadastro com sucesso, e-mail duplicado, plano já existente |
| `ConfirmarEmailControllerTest` | Confirmação com token válido, token inválido, token expirado |
| `RegistrarUsuarioUseCaseTest` | Fluxo completo com mock do serviço de e-mail |
| `TokenConfirmacaoTestHelper` | Helper para criação de tokens em testes |
| `AuthUtilsTest` | Cobertura dos utilitários de autenticação |
| `PlanoControllerTest` | Atualizado para incluir o campo `gratuito` |
| `PlanoTest` | Atualizado para novos factory methods |

---

## Arquivos Modificados

### Novos arquivos
- `configs/PasswordEncoderConfig.java`
- `configs/RestClientConfig.java`
- `configs/WebMvcConfig.java`
- `configs/properties/EmailApiProperties.java`
- `configs/properties/RateLimitProperties.java`
- `filters/RateLimitInterceptor.java`
- `usuario/TokenConfirmacao.java`
- `usuario/service/EnviarEmailConfirmacaoService.java`
- `usuario/useCase/ConfirmarEmailUseCase.java`
- `test/.../usuario/api/ConfirmarEmailControllerTest.java`
- `test/.../usuario/api/RegistrarUsuarioControllerTest.java`
- `test/.../usuario/api/TokenConfirmacaoTestHelper.java`
- `test/.../usuario/useCase/RegistrarUsuarioUseCaseTest.java`
- `test/.../shared/AuthUtilsTest.java`

### Arquivos alterados
- `Usuario.java` — campo `emailValidado` + método `confirmarEmail()`
- `Plano.java` — campo `gratuito` primitivo + novo factory method
- `UsuarioController.java` — endpoint de confirmação de e-mail
- `RegistrarUsuarioUseCase.java` — integração com confirmação e plano gratuito
- `SecurityConfig.java` — liberação do endpoint de confirmação
- `application.yaml` — novas propriedades de configuração
- `pom.xml` — dependência Caffeine
