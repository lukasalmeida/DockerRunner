package com.olezelelabs.dockerrunner

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.IconLoader
import java.io.File
import java.util.concurrent.TimeUnit

class DockerComposeRunAction : AnAction() {

    private val normalIcon = IconLoader.getIcon("/icons/play.svg", javaClass)
    private val greenIcon = IconLoader.getIcon("/icons/play_verde.svg", javaClass)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val projectPath = project.basePath ?: return

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Docker Runner", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.text = "Verificando status do Docker..."

                // 1. Verifica se o daemon do Docker está rodando na máquina
                if (!isDockerRunning()) {
                    indicator.text = "Docker inativo. Tentando iniciar..."
                    val opened = tryOpenDocker()

                    if (!opened) {
                        var userWantsToOpen = false
                        ApplicationManager.getApplication().invokeAndWait {
                            val result = Messages.showYesNoDialog(
                                project,
                                "O Docker não está rodando e não foi possível iniciá-lo automaticamente. Deseja abrir o Docker agora?",
                                "Docker Inativo",
                                "Abrir Docker",
                                "Cancelar",
                                Messages.getWarningIcon()
                            )
                            userWantsToOpen = (result == Messages.YES)
                        }

                        if (userWantsToOpen) {
                            val opened2 = tryOpenDocker()
                            if (!opened2) {
                                showDockerManualStartAlert(project)
                                return
                            }
                            Thread.sleep(5000)
                        } else {
                            showDockerManualStartAlert(project)
                            return
                        }
                    } else {
                        Thread.sleep(5000)
                    }
                }

                // 2. Prossegue com a lógica inteligente de Up/Down se o Docker estiver ativo
                indicator.text = "Verificando status do Docker Compose..."
                val isRunning = checkDockerComposeRunning(projectPath)

                val command = if (isRunning) {
                    indicator.text = "Derrubando containers (docker compose down)..."
                    listOf("docker", "compose", "down")
                } else {
                    indicator.text = "Subindo containers (docker compose up --build -d)..."
                    listOf("docker", "compose", "up", "--build", "-d")
                }

                try {
                    val process = ProcessBuilder(command)
                        .directory(File(projectPath))
                        .redirectErrorStream(true)
                        .start()

                    process.waitFor()
                } catch (ex: Exception) {
                    // Tratamento de erro de execução
                }
            }
        })
    }

    private enum class DockerState { OFF, UP, DOWN }
    private var lastState = DockerState.OFF
    private val checking = java.util.concurrent.atomic.AtomicBoolean(false)

    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project == null || project.basePath == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        e.presentation.isEnabledAndVisible = true

        // Atualiza imediatamente com o último estado conhecido (rápido, não bloqueante)
        when (lastState) {
            DockerState.OFF -> {
                e.presentation.icon = normalIcon
                e.presentation.text = "Docker Off"
            }
            DockerState.UP -> {
                e.presentation.icon = normalIcon
                e.presentation.text = "Docker Up"
            }
            DockerState.DOWN -> {
                e.presentation.icon = greenIcon
                e.presentation.text = "Docker Down (Stop)"
            }
        }

        // Se já há uma checagem em andamento, não dispara outra
        if (!checking.compareAndSet(false, true)) return

        val pres = e.presentation
        val projectPath = project.basePath!!

        // Checagem em background para não bloquear a thread de update
        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                val running = isDockerRunning()
                val composeRunning = if (running) checkDockerComposeRunning(projectPath) else false

                lastState = when {
                    !running -> DockerState.OFF
                    composeRunning -> DockerState.DOWN
                    else -> DockerState.UP
                }

                ApplicationManager.getApplication().invokeLater {
                    if (project.isDisposed) return@invokeLater
                    when (lastState) {
                        DockerState.OFF -> {
                            pres.icon = normalIcon
                            pres.text = "Docker Off"
                        }
                        DockerState.UP -> {
                            pres.icon = normalIcon
                            pres.text = "Docker Up"
                        }
                        DockerState.DOWN -> {
                            pres.icon = greenIcon
                            pres.text = "Docker Down (Stop)"
                        }
                    }
                }
            } finally {
                checking.set(false)
            }
        }
    }

    private fun isDockerRunning(): Boolean {
        return try {
            val process = ProcessBuilder("docker", "info").start()
            process.waitFor(2, TimeUnit.SECONDS)
            process.exitValue() == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun tryOpenDocker(): Boolean {
        val os = System.getProperty("os.name").lowercase()

        return when {
            os.contains("win") -> launchWindowsDocker()
            os.contains("mac") -> launchMacDocker()
            os.contains("nix") || os.contains("nux") || os.contains("bsd") -> launchLinuxDocker()
            else -> false
        }
    }

    private fun launchWindowsDocker(): Boolean {
        val candidates = listOf(
            "C:\\Program Files\\Docker\\Docker\\Docker Desktop.exe",
            "C:\\Program Files (x86)\\Docker\\Docker\\Docker Desktop.exe"
        )

        val existing = candidates.firstOrNull { File(it).exists() }
        val command = if (existing != null) {
            listOf("cmd", "/c", "start", "", "\"$existing\"")
        } else {
            listOf("cmd", "/c", "start", "", "Docker Desktop")
        }

        return runProcess(command)
    }

    private fun launchMacDocker(): Boolean {
        val dockerAppCandidates = listOf(
            "/Applications/Docker.app",
            "/Applications/Docker Desktop.app"
        )

        val appName = if (dockerAppCandidates.any { File(it).exists() }) {
            "Docker"
        } else {
            "Docker Desktop"
        }

        return runProcess(listOf("open", "-a", appName))
    }

    private fun launchLinuxDocker(): Boolean {
        val commands = listOf(
            listOf("systemctl", "start", "docker"),
            listOf("systemctl", "--user", "start", "docker"),
            listOf("service", "docker", "start")
        )

        return commands.any { runProcess(it) }
    }

    private fun runProcess(command: List<String>): Boolean {
        return try {
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()

            val exited = process.waitFor(10, TimeUnit.SECONDS)
            exited && process.exitValue() == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun showDockerManualStartAlert(project: com.intellij.openapi.project.Project) {
        ApplicationManager.getApplication().invokeLater {
            Messages.showErrorDialog(
                project,
                "Não foi possível iniciar o Docker automaticamente. Abra o Docker Desktop (Windows/macOS) ou o serviço Docker (Linux) manualmente e tente novamente.",
                "Docker Inativo"
            )
        }
    }

    private fun checkDockerComposeRunning(projectPath: String): Boolean {
        return try {
            val process = ProcessBuilder("docker", "compose", "ps", "--status", "running", "--quiet")
                .directory(File(projectPath))
                .start()

            val output = process.inputStream.bufferedReader().readText()
            process.waitFor(1, TimeUnit.SECONDS)
            output.isNotBlank()
        } catch (e: Exception) {
            false
        }
    }

    override fun getActionUpdateThread() = com.intellij.openapi.actionSystem.ActionUpdateThread.BGT

}