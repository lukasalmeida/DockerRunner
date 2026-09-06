# Docker Runner 🐳

Plugin para IDEs JetBrains que simplifica o controle do ambiente Docker Compose do projeto ativo diretamente da barra de navegação do IDE.

## Visão geral

O Docker Runner elimina a necessidade de abrir o terminal para subir ou derrubar o ambiente local do projeto. Com um único clique, o plugin detecta o estado atual do Docker Compose e executa a ação correta.

## Funcionalidades

- Toggle inteligente do ambiente Docker Compose
  - Se os containers estiverem parados, executa `docker compose up --build -d`
  - Se estiverem em execução, executa `docker compose down`
- Reinício do ambiente com ação dedicada
  - `docker compose down` + `docker compose up --build -d`
- Limpeza completa com confirmação
  - `docker compose down -v` para remover volumes
- Detecção automática do projeto ativo
- Início automático do Docker quando ele não está disponível
- Execução em segundo plano para não bloquear a interface do IDE
- Integração direta na barra principal e na barra de navegação do projeto

## Ações incluídas

- Docker Up
- Restart
- Clean (Down -v)

## Instalação

1. Acesse a aba de releases do repositório: https://github.com/olezelelabs/DockerRunner/releases
2. Baixe o arquivo `.zip` da versão mais recente
3. Abra o IDE JetBrains
4. Vá em `Settings > Plugins`
5. Clique na engrenagem e selecione `Install Plugin from Disk...`
6. Escolha o arquivo `.zip` e reinicie o IDE

## Compilação local

```bash
git clone https://github.com/olezelelabs/DockerRunner.git
cd DockerRunner
./gradlew build
```

Para testar o plugin em execução no IDE:

```bash
./gradlew runIde
```

## Changelog

Consulte o arquivo [CHANGELOG.md](CHANGELOG.md) para acompanhar as mudanças por versão.

## Repositório

- GitHub: https://github.com/olezelelabs/DockerRunner

## Compatibilidade

Compatível com IntelliJ IDEA, WebStorm, PyCharm, GoLand, PhpStorm e demais IDEs baseados na IntelliJ Platform.
