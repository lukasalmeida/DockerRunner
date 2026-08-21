package com.olezelerunner.dockerruner

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.util.IconLoader
import java.io.File
import java.util.concurrent.TimeUnit

class DockerComposeRunAction : AnAction() {

    private val normalIcon = IconLoader.getIcon("/icons/play.svg", javaClass)
    private val greenIcon = IconLoader.getIcon("/icons/play_verde.svg", javaClass)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val projectPath = project.basePath ?: return

        // Executa em background para evitar travamentos e fornecer feedback de carregamento
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Docker Runner", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
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

                    // Aguarda o processo terminar em background
                    process.waitFor()
                } catch (ex: Exception) {
                    // Tratamento de erro silencioso por enquanto
                }
            }
        })
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project == null || project.basePath == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        e.presentation.isEnabledAndVisible = true
        val isRunning = checkDockerComposeRunning(project.basePath!!)

        if (isRunning) {
            e.presentation.icon = greenIcon
            e.presentation.text = "Docker Down (Stop)"
        } else {
            e.presentation.icon = normalIcon
            e.presentation.text = "Docker Up"
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