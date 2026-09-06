# Changelog

Todas as alterações relevantes do plugin Docker Runner serão documentadas aqui.

## v1.2.0

### Adicionado
- Toggle inteligente para subir ou derrubar o ambiente Docker Compose do projeto ativo.
- Ação de reinício do ambiente com `docker compose down` seguido de `docker compose up --build -d`.
- Ação de limpeza com confirmação para `docker compose down -v`.
- Integração do botão na barra principal e na barra de navegação do IDE.

### Melhorado
- Detecção mais robusta do estado do Docker e do Docker Compose.
- Execução em segundo plano para manter a interface do IDE responsiva.
- Mensagens e feedback mais claros durante ações em andamento.

### Corrigido
- Ajustes na experiência de uso para projetos com Docker Compose configurado na raiz do projeto.
- Melhor tratamento quando o Docker não está disponível localmente.

## v1.1.0

### Melhorado
- Adicionada a barra de progresso ao executar comandos do Docker Compose.
- A interface do IDE agora informa visualmente o estado da operação em andamento.
- Melhor feedback para o usuário durante ações de startup e shutdown do ambiente.

## v1.0.2

### Corrigido
- Ajustado o comportamento de início do Docker Runner para aguardar o ambiente iniciar corretamente.
- Corrigido o problema em que o container era iniciado e encerrado rapidamente.
- Melhor estabilidade na execução do comando principal do plugin.
