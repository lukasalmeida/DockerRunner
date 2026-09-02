# Agent Guide

## Propósito

Este repositório contém um plugin JetBrains para controlar containers Docker Compose diretamente da interface do IDE. O agente deve agir como um assistente de desenvolvimento do projeto e manter o comportamento do plugin consistente com a intenção original do produto.

## Contexto do projeto

- Nome: Docker Runner
- Tipo: plugin IntelliJ / Kotlin
- Objetivo: executar e interromper o ambiente Docker a partir da navbar do IDE
- Repositório principal: `olezelelabs/DockerRunner`

## Estrutura relevante

- `src/main/kotlin/com/olezelelabs/dockerrunner/DockerComposeRunAction.kt`
  - Lógica principal da ação do botão
- `src/main/resources/META-INF/plugin.xml`
  - Registro da action e integração com toolbar
- `src/main/resources/icons/`
  - Ícones do botão
- `build.gradle.kts`
  - Configuração do plugin com IntelliJ Platform

## Regras para alterações

1. Mantenha o plugin leve e focado em uma ação simples.
2. Preserve a lógica de toggle de estado do ambiente Docker.
3. Sempre trate a execução do comando como operação em background para não congelar a IDE.
4. Use `project.basePath` como base do diretório do projeto atual.
5. Quando necessário, mantenha o comportamento compatível com `docker compose up --build -d` e `docker compose down`.
6. Evite reestruturações grandes em um projeto pequeno e bem definido.
7. Não adicione dependências pesadas sem necessidade.

## Padrões de implementação

- Em Kotlin, priorize clareza e código pequeno.
- Use `ProgressManager` / `Task.Backgroundable` quando a operação pode bloquear a UI.
- Ao mudar textos, ícones ou IDs de ação, mantenha a integração com `plugin.xml` consistente.
- Siga o estilo atual do projeto, que é direto e pragmático.

## Validação recomendada

Antes de fechar qualquer mudança, execute:

```bash
./gradlew build
```

Se a mudança for visual ou de UI do plugin, considere também testar a execução local com:

```bash
./gradlew runIde
```

## Boas práticas para agentes

- Entenda o problema antes de editar: o plugin depende do projeto ativo e do ambiente Docker local.
- Faça mudanças pequenas e verificáveis.
- Não remova a funcionalidade principal sem justificar.
- Quando o plugin achar que o ambiente está rodando, a ação deve seguir o comportamento de parada e vice-versa.

## Comandos úteis

```bash
./gradlew tasks --all
./gradlew build
./gradlew runIde
```

## Observação final

O projeto é simples, mas seu valor está na experiência direta dentro do IDE. Mantenha a UX enxuta, rápida e previsível.
