package edu.mcgill.sourcejump.config

import com.intellij.openapi.options.Configurable
import edu.mcgill.sourcejump.config.SJConfig.Companion.settings

class SJConfigurable: Configurable {
  private val panel by lazy(::SJSettingsPanel)

  override fun getDisplayName() = "SourceJump"

  override fun createComponent() = panel.rootPanel

  override fun isModified() =
    panel.ghToken != settings.githubToken ||
      panel.numResults != settings.resultsToFetch


  override fun apply() {
    settings.githubToken = panel.ghToken
    settings.resultsToFetch = panel.numResults
  }

  override fun reset() = panel.reset(settings)
}