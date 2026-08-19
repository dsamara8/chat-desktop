# Groq Chat — JavaFX

Aplicação de chat desenvolvida em **Java 20**, utilizando **JavaFX** para a interface gráfica e a **API da Groq** para comunicação com um modelo de inteligência artificial.

O projeto permite enviar mensagens por uma interface de chat e receber respostas da IA em tempo real.

## Tecnologias utilizadas

* Java 20
* JavaFX
* API Groq
* HTTP Client do Java
* Maven
* IntelliJ IDEA

## Funcionalidades

* Interface gráfica de chat
* Campo para digitar mensagens
* Botão para enviar mensagens
* Envio de mensagens para a API da Groq
* Recebimento das respostas da IA
* Indicador de status enquanto a IA está processando
* Tratamento de erros da API
* Rolagem automática das mensagens

## Estrutura do projeto

```text
groq-chat/
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── example/
│                   └── chatdesktop/
│                       └── Main.java
│
├── pom.xml
└── README.md
```

## Requisitos

Antes de executar o projeto, é necessário ter instalado:

* JDK 20
* IntelliJ IDEA
* Maven
* JavaFX
* Uma conta na Groq com uma API Key

## Configuração da API Groq

No arquivo `Main.java`, existe uma configuração semelhante a:

```java
private static final String GROQ_URL =
        "https://api.groq.com/openai/v1/chat/completions";

private static final String GROQ_API_KEY =
        "SUA_CHAVE_AQUI";

private static final String MODEL =
        "openai/gpt-oss-20b";
```

Substitua:

```text
SUA_CHAVE_AQUI
```

pela sua chave da Groq.

### Importante

Nunca publique sua API Key no GitHub, README, prints ou redes sociais.

O ideal para um projeto real é utilizar uma variável de ambiente ou um arquivo de configuração que não seja enviado para o repositório.

## Como executar

### 1. Abrir o projeto

Abra a pasta do projeto no IntelliJ IDEA.

### 2. Verificar o JDK

Confirme que o projeto está utilizando **Java 20**.

No IntelliJ:

```text
File
→ Project Structure
→ Project
→ SDK
→ Java 20
```

### 3. Atualizar o Maven

Abra o painel do Maven e execute:

```text
Reload All Maven Projects
```

### 4. Executar

Abra:

```text
Main.java
```

e execute o método:

```java
public static void main(String[] args)
```

A janela do aplicativo será aberta.

## Como utilizar

Depois que o aplicativo abrir:

1. Digite uma mensagem no campo inferior.
2. Clique em **Enviar** ou pressione `Enter`.
3. A mensagem será enviada para a API da Groq.
4. A resposta da IA aparecerá na tela.

Exemplo:

```text
Usuário:
Oi, tudo bem?

IA:
Olá! Tudo bem! Como posso ajudar?
```

## Comunicação com a Groq

A aplicação utiliza uma requisição HTTP `POST` para:

```text
https://api.groq.com/openai/v1/chat/completions
```

A autenticação é feita através do cabeçalho:

```text
Authorization: Bearer SUA_API_KEY
```

O modelo utilizado atualmente no projeto é:

```text
openai/gpt-oss-20b
```

## Tratamento de erros

A aplicação possui tratamento para erros da API.

Por exemplo, se houver algum problema com a requisição, a interface exibirá uma mensagem como:

```text
Erro ao conversar com a Groq:
A Groq retornou HTTP ...
```

Isso facilita a identificação de problemas relacionados à API, modelo ou conexão.

## Personalização

A aparência da aplicação pode ser alterada diretamente pelo CSS do JavaFX dentro do `Main.java`.

Exemplos de elementos que podem ser personalizados:

```java
-fx-background-color
-fx-text-fill
-fx-font-size
-fx-background-radius
-fx-padding
```

Também é possível trocar:

* Cores da interface
* Tamanho da janela
* Nome da aplicação
* Texto das mensagens
* Modelo utilizado pela Groq
* Temperatura da IA
* Quantidade máxima de tokens

## Próximas melhorias

Algumas melhorias que podem ser adicionadas futuramente:

* Histórico de conversas
* Botão para limpar o chat
* Salvamento das conversas
* Suporte a múltiplos usuários
* Indicador de carregamento mais avançado
* Markdown nas respostas
* Código separado em classes
* Configuração da API por arquivo `.env`
* Interface mais moderna
* Tema claro e escuro
* Banco de dados para armazenar conversas

## Autor

Projeto desenvolvido como estudo de integração entre:

```text
Java 20
+
JavaFX
+
API Groq
```

## Licença

Este projeto é destinado para fins de estudo e aprendizado. Sinta-se livre para modificá-lo e evoluí-lo conforme suas necessidades.

