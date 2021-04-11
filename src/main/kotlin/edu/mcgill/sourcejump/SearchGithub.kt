package edu.mcgill.sourcejump

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys.EDITOR
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.vfs.LocalFileSystem
import edu.mcgill.sourcejump.config.SJConfig
import org.kohsuke.github.*
import java.nio.file.Files

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

    val results = github.fetchResults(selectedText, extension)

    if (results.isEmpty()) return

    val display =
      results.joinToString("\n\n") { it.htmlUrl + "\n" + it.content + "\n" }

    logger.warn("Found ${results.size} results on GitHub")

    val ioFile = Files.createTempFile(selectedText, ".txt").toFile()
      .apply { writeText(display) }

    logger.warn("Wrote file to ${ioFile.path}")

    val file = LocalFileSystem.getInstance().findFileByIoFile(ioFile)!!

    FileEditorManager.getInstance(e.project!!)
      .openTextEditor(OpenFileDescriptor(e.project!!, file), true)
  }

  private fun GitHub.fetchResults(
    selectedText: String,
    extension: String
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