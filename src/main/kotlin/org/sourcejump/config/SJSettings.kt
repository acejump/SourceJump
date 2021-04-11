package org.sourcejump.config

data class SJSettings(
  var githubToken: String = "",
  var resultsToFetch: Int = 10
)