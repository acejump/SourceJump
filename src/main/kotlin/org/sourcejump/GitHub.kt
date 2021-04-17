package org.sourcejump

import com.intellij.openapi.diagnostic.thisLogger
import org.kohsuke.github.*
import org.kohsuke.github.GHIssueSearchBuilder.Sort.COMMENTS
import org.sourcejump.config.SJConfig

object GitHub {
  private val logger = thisLogger()
  private val token get() = SJConfig.githubToken
  private val github get() = GitHubBuilder().withJwtToken(token).build()

  fun isTokenValid() = github.isCredentialValid

  fun searchIssues(query: String) = try {
    github.searchIssues().q(query).sort(COMMENTS)
      .list().take(SJConfig.maxResults).toList()
  } catch (exception: Exception) {
    logger.error(exception)
    emptyList<GHIssue>()
  }

  val remaining get() = github.rateLimit.search.remaining

  val limit get() = github.rateLimit.search.limit

  fun searchCode(query: String, extension: String) = try {
    github.searchContent()
      .q(query).extension(extension)
      .list().take(SJConfig.maxResults).toList()
  } catch (exception: Exception) {
    logger.error(exception)
    emptyList<GHContent>()
  }
}