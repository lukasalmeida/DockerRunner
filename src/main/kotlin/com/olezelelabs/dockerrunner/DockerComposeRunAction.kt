package com.olezelerunner.dockerruner

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import java.io.File
import java.util.concurrent.TimeUnit

class DockerComposeRunAction : AnAction() {

    // Carrega os ícones dinamicamente
    private val normalIcon = IconLoader.getIcon("/icons/play.svg", javaClass)
    private val greenIcon = IconLoader.getIcon("/icons/play_verde.svg", javaClass)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val projectPath = project.basePath ?: return

        try {
            val process = ProcessBuilder("docker", "compose", "up", "--build", "-d")
                .directory(File(projectPath))
                .redirectErrorStream(true)
                .start()
            process.waitFor(2, TimeUnit.SECONDS)
        } catch (ex: Exception) {
            // Tratar erro se necessário
        }
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        if (project == null || project.basePath == null) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        e.presentation.isEnabledAndVisible = true

        // Verifica de forma leve se o Docker Compose está ativo neste projeto
        val isRunning = checkDockerComposeRunning(project.basePath!!)

        // Altera o ícone dinamicamente com base no estado
        if (isRunning) {
            e.presentation.icon = greenIcon
            e.presentation.text = "Docker Running (Up)"
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

            // Se houver saída, significa que há containers rodando para este compose
            output.isNotBlank()
        } catch (e: Exception) {
            false
        }
    }

    override fun getActionUpdateThread() = com.intellij.openapi.actionSystem.ActionUpdateThread.BGT
}