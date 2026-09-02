# Contexto do Projeto: Docker Runner

## Visão geral

O Docker Runner é um plugin para IDEs JetBrains que permite iniciar e parar o ambiente Docker do projeto atual diretamente da barra de navegação superior do IDE, sem precisar abrir um terminal.

A ideia central do projeto é reduzir a fricção de desenvolvimento quando o projeto usa Docker Compose. Com um único clique, o plugin executa:

- docker compose up --build -d quando os containers não estão ativos
- docker compose down quando os containers já estão em execução

## Objetivo do produto

O projeto foi pensado para desenvolvedores que querem um controle rápido do ambiente local de um projeto, especialmente em projetos com Docker Compose e múltiplos serviços.

Ele torna o processo mais natural dentro do fluxo do IDE, com:

- visibilidade do status do ambiente
- ação automática com base no estado atual
- feedback visual no próprio IntelliJ

## Stack principal

- Kotlin
- IntelliJ Platform Gradle Plugin
- Gradle
- Docker Compose
- SVG icons para UI do plugin

## Estrutura do repositório

- `src/main/kotlin/com/olezelelabs/dockerrunner/DockerComposeRunAction.kt`
  - Lógica principal da ação do plugin.
  - Detecta se o `docker compose` está rodando no projeto atual.
  - Alterna entre `up` e `down` a partir do estado atual.

- `src/main/resources/META-INF/plugin.xml`
  - Define o plugin, a ação registrada e a integração com a toolbar do IDE.
  - Adiciona o botão na `MainToolBar` e na `NavBarToolBar`.

- `src/main/resources/icons/`
  - Ícones do botão, como `play.svg` e `play_verde.svg`.

- `build.gradle.kts`
  - Configuração principal do módulo do plugin IntelliJ.

- `gradlew`, `gradlew.bat`, `gradle/`
  - Scripts e wrappers do Gradle.

## Como o plugin funciona

A ação principal é `DockerComposeRunAction`.

Fluxo:

1. Obtém o projeto ativo (`e.project`) e o caminho base do projeto (`project.basePath`).
2. Verifica se há containers em execução via:
   - `docker compose ps --status running --quiet`
3. Se houver containers em execução:
   - executa `docker compose down`
4. Se não houver:
   - executa `docker compose up --build -d`
5. Atualiza o texto e o ícone do botão conforme o estado do ambiente.

O código também usa `ProgressManager` e `Task.Backgroundable` para rodar os comandos em segundo plano e evitar congelamento da UI do IDE.

## Comandos de build e execução

### Build do projeto

```bash
./gradlew build
```

### Executar o IDE com o plugin em modo de desenvolvimento

```bash
./gradlew runIde
```

### Ver tarefas disponíveis do Gradle

```bash
./gradlew tasks --all
```

## Regras de contribuição

- Preferir alterações pequenas e focadas.
- Manter a compatibilidade com JetBrains IDEs e com a API do IntelliJ Platform.
- Não trocar a identidade do plugin (`com.olezelelabs.dockerrunner`) sem necessidade.
- Preservar a lógica de toggle Up/Down baseada no estado real do ambiente Docker.
- Manter o comportamento assíncrono para não bloquear a interface.
- Use ícones e recursos já presentes em `src/main/resources` quando possível.

## Observações importantes

- O projeto é um plugin, não uma aplicação standalone.
- Ele depende do ambiente local do desenvolvedor ter Docker e Docker Compose instalados.
- A ação assume que o projeto ativo contém um arquivo de configuração do Docker Compose na raiz do diretório do projeto.
- O plugin atualmente é orientado ao fluxo simples de "subir" e "parar" ambiente de desenvolvimento.

## Referências do projeto

- README principal: `README.md`
- Manifesto do plugin: `src/main/resources/META-INF/plugin.xml`
- Código principal: `src/main/kotlin/com/olezelelabs/dockerrunner/DockerComposeRunAction.kt`
