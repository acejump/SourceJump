package edu.mcgill.sourcejump

import com.intellij.find.*
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.*
import edu.mcgill.sourcejump.config.SJConfig
import org.kohsuke.github.*
import java.io.File

class FetchResultsFromGithubAction: DumbAwareAction() {
  val token by lazy { SJConfig.githubToken }
  val github by lazy { GitHubBuilder().withJwtToken(token).build() }

  val logger = thisLogger()

  override fun actionPerformed(e: AnActionEvent) {
    val (selectedText, extension) = e.getData(EDITOR)?.let {
      val ext = FileDocumentManager.getInstance()
        .getFile(it.document)?.extension
      if (it.selectionModel.selectedText.isNullOrEmpty())
        it.selectionModel.selectWordAtCaret(true)
      it.selectionModel.selectedText to ext
    } ?: return

    // Don't bother fetching short or empty queries
    if (selectedText.isNullOrEmpty() ||
      extension.isNullOrEmpty() ||
      selectedText.length < 2
    ) return

    val queryDir = File("$tempDir/${selectedText.hashCode()}/")

    if (!queryDir.exists()) {
      val results = github.fetchResults(selectedText, extension)
//    results.sortedBy { it. } TODO
      results.store(queryDir, extension)

      if (results.isEmpty()) return
    }

    showResults(e.project, queryDir, selectedText)
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