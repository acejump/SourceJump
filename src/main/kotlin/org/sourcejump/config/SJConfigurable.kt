package org.sourcejump.config

import com.intellij.openapi.options.Configurable
import org.sourcejump.config.SJConfig.Companion.settings

class SJConfigurable: Configurable {
  private val panel by lazy(::SJSettingsPanel)

  override fun getDisplayName() = "SourceJump"

  override fun createComponent() = panel.rootPanel

  override fun isModified() =
    panel.ghToken != settings.githubToken ||
      panel.maxResults != settings.maxResults


  override fun apply() {
    settings.githubToken = panel.ghToken
    settings.maxResults = panel.maxResults
  }

  override fun reset() = panel.reset(settings)
}