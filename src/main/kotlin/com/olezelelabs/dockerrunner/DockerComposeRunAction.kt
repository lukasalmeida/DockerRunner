package com.olezelelabs.dockerrunner

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import java.io.File
import java.util.concurrent.TimeUnit

class DockerComposeRunAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val projectPath = project.basePath ?: return

        // Verifica se há um docker-compose
        val dockerComposeFile = File(projectPath, "docker-compose.yml")
        val dockerComposeYaml = File(projectPath, "docker-compose.yaml")

        if (!dockerComposeFile.exists() && !dockerComposeYaml.exists()) {
            Messages.showErrorDialog(project, "Nenhum arquivo docker-compose encontrado.", "Erro")
            return
        }

        try {
            // Executa o comando
            val process = ProcessBuilder("docker", "compose", "up", "--build", "-d")
                .directory(File(projectPath))
                .redirectErrorStream(true)
                .start()

            Messages.showInfoMessage(project, "Docker Compose disparado com sucesso!", "Docker Runner")
        } catch (ex: Exception) {
            Messages.showErrorDialog(project, "Falha ao rodar Docker: ${ex.message}", "Erro")
        }
    }

    override fun update(e: AnActionEvent) {
        // O botão só fica habilitado se houver um projeto aberto
        e.presentation.isEnabledAndVisible = e.project != null
    }
}