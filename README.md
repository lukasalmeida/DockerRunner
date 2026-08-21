# Docker Runner 🐳

> Uma extensão simples, elegante e eficiente para os IDEs da JetBrains que permite controlar containers Docker diretamente da barra de navegação superior.

## 🚀 O Problema
Sempre que abrimos um novo projeto, precisamos recorrer ao terminal para rodar `docker compose up --build -d`. O **Docker Runner** elimina essa fricção, colocando um botão de execução dedicado diretamente na sua *Navbar*.

---

## ✨ Recursos

* **Execução Rápida:** Roda o comando `docker compose up --build -d` com um único clique no diretório raiz do projeto aberto.
* **Compatibilidade Universal:** Funciona perfeitamente em toda a suíte de IDEs da JetBrains (IntelliJ IDEA, WebStorm, PyCharm, GoLand, PhpStorm, etc.).
* **Isolamento por Projeto:** Detecta automaticamente o caminho do projeto ativo através da API do IntelliJ.

---

## 🛠️ Tecnologias Utilizadas

* **Kotlin** (Linguagem principal)
* **IntelliJ Platform Gradle Plugin** (Moderno, versão 2.x)
* **ProcessBuilder** (Para automação e execução nativa de comandos do sistema)

---

## 📦 Como Instalar (Uso Local)

1. Baixe o arquivo `.zip` mais recente na aba de [Releases](https://github.com/lukasalmeida/docker-runner/releases) deste repositório.
2. Abra o seu IDE JetBrains.
3. Vá em **Settings > Plugins**.
4. Clique na engrenagem (Configurações) e selecione **Install Plugin from Disk...**
5. Escolha o arquivo `.zip` baixado e reinicie o IDE.

---

## ⚙️ Como Compilar o Projeto do Zero

Se você quiser compilar o código fonte por conta própria:

1. Clone o repositório:
   ```bash
   git clone [https://github.com/lukasalmeida/docker-runner.git](https://github.com/lukasalmeida/docker-runner.git)