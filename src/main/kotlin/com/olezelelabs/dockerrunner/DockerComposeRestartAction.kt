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

class DockerComposeRestartAction : AnAction() {

    private val icon = IconLoader.getIcon("/icons/restart.svg", javaClass)

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val projectPath = project.basePath ?: return

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Docker Runner - Restart", true) {
            override fun run(indicator: ProgressIndicator) {
                indicator.isIndeterminate = true
                indicator.text = "Reiniciando containers..."

                val composeRunning = checkDockerComposeRunning(projectPath)
                if (!composeRunning) {
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showInfoMessage(project, "Nenhum container em execução para reiniciar.", "Docker Compose")
                    }
                    return
                }

                try {
                    // 1) docker compose down
                    indicator.text = "Executando: docker compose down"
                    val down = ProcessBuilder(listOf("docker", "compose", "down"))
                        .directory(File(projectPath))
                        .redirectErrorStream(true)
                        .start()
                    down.waitFor()

                    // 2) aguardar até que não haja containers em execução (timeout 60s)
                    indicator.text = "Aguardando containers encerrarem..."
                    val startWait = System.currentTimeMillis()
                    while (checkDockerComposeRunning(projectPath)) {
                        if (System.currentTimeMillis() - startWait > 60_000) break
                        Thread.sleep(1000)
                    }

                    // 3) docker compose up --build -d
                    indicator.text = "Executando: docker compose up --build -d"
                    val up = ProcessBuilder(listOf("docker", "compose", "up", "--build", "-d"))
                        .directory(File(projectPath))
                        .redirectErrorStream(true)
                        .start()
                    up.waitFor()

                    ApplicationManager.getApplication().invokeLater {
                        Messages.showInfoMessage(project, "Reinício concluído.", "Docker Compose")
                    }
                } catch (ex: Exception) {
                    ApplicationManager.getApplication().invokeLater {
                        Messages.showErrorDialog(project, "Erro ao reiniciar containers: ${ex.message}", "Docker Compose")
                    }
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
        e.presentation.isEnabled = isRunning
        e.presentation.icon = icon
        e.presentation.text = "Restart"
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
