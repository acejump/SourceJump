package org.sourcejump

import com.intellij.find.*
import com.intellij.notification.*
import com.intellij.notification.NotificationType.*
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR
import com.intellij.openapi.application.*
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.*
import com.intellij.openapi.util.NlsSafe
import org.jetbrains.annotations.Nullable
import java.io.File

class SourceJumpAction: DumbAwareAction() {
  fun Project.notify(
    content: String?,
    type: NotificationType = INFORMATION
  ) = NotificationGroupManager.getInstance()
    .getNotificationGroup("sourcejump")
    .createNotification(content!!, type)
    .notify(this)

  override fun actionPerformed(e: AnActionEvent) {
    val (project, editor) = e.project to e.getData(EDITOR)

    if (!GitHub.isTokenValid()) {
      project?.notify(
        """Your GitHub token is either invalid or unavailable.
          <a href="https://github.com/settings/tokens/new">Create a new</a>
          personal access token then add it to Settings | Tools | SourceJump.
        """.trimIndent(),
        ERROR
      )
      return
    }

    val (query, ext) = editor?.let {
      val ext = FileDocumentManager.getInstance()
        .getFile(it.document)?.extension
      if (it.selectionModel.selectedText.isNullOrEmpty())
        it.selectionModel.selectWordAtCaret(true)
      it.selectionModel.selectedText to ext
    } ?: return

    // Don't bother fetching short or empty queries
    if (query.isNullOrEmpty() || ext.isNullOrEmpty() || query.length < 2) {
      project?.notify("No query was selected.", WARNING)
      return
    }

    project?.notify(
      """Searching .$ext files on GitHub for: "$query"
        GitHub Rate limit: ${GitHub.remaining}/${GitHub.limit}
        """.trimMargin()
    )

    Thread { runSearch(query, ext, project) }.start()
  }

  fun runSearch(query: String, ext: String, project: Project?)  {
    val queryDir = File("$tempDir/${query.hashCode()}/")
    if (!queryDir.exists()) {

      val results = GitHub.searchCode(query, ext)
      //    results.sortedBy { it. } TODO
      results.store(queryDir, ext)

      if (results.isEmpty())
        project?.notify("No results found!\"$query\"", WARNING)
    }

    showResults(project, queryDir, query)
  }

  private fun showResults(
    project: Project?,
    tempDir: File,
    selectedText: String
  ) = runInEdt {
    FindManager.getInstance(project)
      .showFindDialog(FindModel().apply {
        stringToFind = selectedText
        isCaseSensitive = true
        directoryName = tempDir.absolutePath
        isProjectScope = false
        isSearchInProjectFiles = false
      }) {}
  }
}