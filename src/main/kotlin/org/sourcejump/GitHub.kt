package org.sourcejump

import com.intellij.openapi.diagnostic.thisLogger
import org.kohsuke.github.*
import org.sourcejump.config.SJConfig

object GitHub {
  val logger = thisLogger()
  private val token get() = SJConfig.githubToken
  private val github get() = GitHubBuilder().withJwtToken(token).build()

  fun isTokenValid() = github.isCredentialValid

  fun fetchResults(
    selectedText: String,
    extension: String,
  ) = try {
    github.searchContent()
      .q(selectedText)
      .extension(extension)
      .list().take(SJConfig.numResults)
  } catch (exception: Exception) {
    logger.error(exception)
    emptyList<GHContent>()
  }
}