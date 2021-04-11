package org.sourcejump.config

data class SJSettings(
  var githubToken: String = "",
  var maxResults: Int = 100
)