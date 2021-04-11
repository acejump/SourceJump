package edu.mcgill.sourcejump

import com.intellij.find.*
import com.intellij.notification.*
import com.intellij.notification.NotificationType.*
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.*
import edu.mcgill.sourcejump.config.SJConfig
import org.kohsuke.github.*
import java.io.File

class FetchResultsFromGithubAction: DumbAwareAction() {
  val token get() = SJConfig.githubToken
  val github get() = GitHubBuilder().withJwtToken(token).build()

  val logger = thisLogger()

  fun notify(
    project: Project?,
    content: String?,
    type: NotificationType = INFORMATION
  ) = NotificationGroupManager.getInstance()
    .getNotificationGroup("sourcejump")
    .createNotification(content!!, type)
    .notify(project)

  override fun actionPerformed(e: AnActionEvent) {
    if (token.isEmpty()) {
      notify(
        e.project,
        "SourceJump needs a GitHub Personal Access token. Create one " +
          "<a href=\"https://github.com/settings/tokens/new\">here</a>.\n" +
          "Then paste it in Settings | Tools | SourceJump.",
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
    if (query.isNullOrEmpty() ||
      ext.isNullOrEmpty() ||
      query.length < 2
    ) {
      notify(e.project, "No query was selected.", WARNING)
      return
    }

    val queryDir = File("$tempDir/${query.hashCode()}/")

    if (!queryDir.exists()) {
      notify(e.project, "Searching .$ext files on GitHub for: \"$query\"")

      val results = github.fetchResults(query, ext)
//    results.sortedBy { it. } TODO
      results.store(queryDir, ext)

      if (results.isEmpty()) {
        notify(e.project, "No results found!\"$query\"", WARNING)
        return
      }
    }

    showResults(e.project, queryDir, query)
  }

  private fun List<GHContent>.store(tempDir: File, extension: String) {
    tempDir.mkdirs()
    val tempPath = tempDir.absolutePath
    forEachIndexed { i, result ->
      if (result.isFile) return@forEachIndexed
      result.read().use {
        File("$tempPath/${i}_" + result.name)
          .apply { writeOrigin(result, extension) }
          .appendBytes(it.readAllBytes())
      }
    }
  }

  private fun File.writeOrigin(result: GHContent, extension: String) =
    writeText(when (extension) {
      "java", "kt" -> "//"
      "py" -> "#"
      // TODO: Others?
      else -> null
    }?.let { "$it ${result.htmlUrl}\n\n" } ?: "")

  private fun showResults(
    project: Project?,
    tempDir: File,
    selectedText: String
  ) = FindManager.getInstance(project).showFindDialog(FindModel().apply {
    stringToFind = selectedText
    directoryName = tempDir.absolutePath
    isProjectScope = false
    isSearchInProjectFiles = false
  }) {}

  private fun GitHub.fetchResults(
    selectedText: String,
    extension: String,
  ) = try {
    searchContent()
      .q(selectedText)
      .extension(extension)
      .list().take(SJConfig.numResults)
  } catch (exception: Exception) {
    logger.error(exception)
    emptyList<GHContent>()
  }
}