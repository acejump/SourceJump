package org.sourcejump.config

import com.intellij.openapi.components.*

@State(
  name = "SJConfig",
  storages = [(Storage("\$APP_CONFIG\$/SourceJump.xml"))]
)
class SJConfig: PersistentStateComponent<SJSettings> {
  private var settings = SJSettings()

  companion object {
    val settings
      get() = ServiceManager.getService(SJConfig::class.java).settings

    val githubToken get() = settings.githubToken
    val maxResults get() = settings.maxResults
  }

  override fun getState() = settings

  override fun loadState(state: SJSettings) = state.let { settings = it }
}