package com.olezelelabs.dockerrunner

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.IconLoader
import java.io.File
import java.util.concurrent.TimeUnit

class DockerComposeCleanAction : AnAction() {

    private val icon = IconLoader.getIcon("/icons/clean.svg", javaClass)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val projectPath = project.basePath ?: return

        val confirmed = Messages.showYesNoDialog(
            project,
            "Executar 'docker compose down -v' irá remover volumes do container. Tem certeza?",
            "Confirmar limpeza do volume",
            Messages.getQuestionIcon()
        ) == Messages.YES

        if (!confirmed) return

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Docker Runner - Clean", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.text = "Executando docker compose down -v..."

                try {
                    val process = ProcessBuilder(listOf("docker", "compose", "down", "-v"))
                        .directory(File(projectPath))
                        .redirectErrorStream(true)
                        .start()

                    process.waitFor()
                } catch (ex: Exception) {
                    // silencioso por enquanto
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

        // Sempre mostrar o botão na toolbar, mas habilitar somente quando o compose estiver rodando
        e.presentation.isEnabledAndVisible = true
        val isRunning = checkDockerComposeRunning(project.basePath!!)
        e.presentation.isEnabled = isRunning
        e.presentation.icon = icon
        e.presentation.text = "Clean (Down -v)"
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
