package edu.mcgill.sourcejump.config

data class SJSettings(
  var githubToken: String = "",
  var resultsToFetch: Int = 10
)