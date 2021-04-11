package org.sourcejump

import com.intellij.find.*
import com.intellij.notification.*
import com.intellij.notification.NotificationType.*
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.*
import java.io.File

class SourceJumpAction: DumbAwareAction() {
  fun notify(
    project: Project?,
    content: String?,
    type: NotificationType = INFORMATION
  ) = NotificationGroupManager.getInstance()
    .getNotificationGroup("sourcejump")
    .createNotification(content!!, type)
    .notify(project)

  override fun actionPerformed(e: AnActionEvent) {
    if (!GitHub.isTokenValid()) {
      notify(
        e.project,
        """Your GitHub token is either invalid or unavailable.
          <a href="https://github.com/settings/tokens/new">Create a new</a>
          personal access token then add it to Settings | Tools | SourceJump.
        """.trimIndent(),
        ERROR
      )
      return
    }

    val (query, ext) = e.getData(EDITOR)?.let {
      val ext = FileDocumentManager.getInstance()
        .getFile(it.document)?.extension
      if (it.selectionModel.selectedText.isNullOrEmpty())
        it.selectionModel.selectWordAtCaret(true)
      it.selectionModel.selectedText to ext
    } ?: return

    // Don't bother fetching short or empty queries
    if (query.isNullOrEmpty() || ext.isNullOrEmpty() || query.length < 2) {
      notify(e.project, "No query was selected.", WARNING)
      return
    }

    val queryDir = File("$tempDir/${query.hashCode()}/")

    if (!queryDir.exists()) {
      notify(e.project, "Searching .$ext files on GitHub for: \"$query\"")

      val results = GitHub.fetchResults(query, ext)
//    results.sortedBy { it. } TODO
      results.store(queryDir, ext)

      if (results.isEmpty()) {
        notify(e.project, "No results found!\"$query\"", WARNING)
        return
      }
    }

    showResults(e.project, queryDir, query)
  }

  private fun showResults(
    project: Project?,
    tempDir: File,
    selectedText: String
  ) = FindManager.getInstance(project).showFindDialog(FindModel().apply {
    stringToFind = selectedText
    isCaseSensitive = true
    directoryName = tempDir.absolutePath
    isProjectScope = false
    isSearchInProjectFiles = false
  }) {}
}