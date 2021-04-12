package org.sourcejump.config

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.ColorPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.*
import com.intellij.ui.layout.GrowPolicy.*
import java.awt.Color
import javax.swing.*
import javax.swing.text.JTextComponent
import kotlin.reflect.KProperty

class SJSettingsPanel {
  private val ghTokenField = JBTextField("")
  private val maxResultsCB =
    ComboBox((1..10).map { it * 20 }.toList().toTypedArray())

  internal val rootPanel: JPanel = panel {
    fun Cell.short(component: JComponent) = component(growPolicy = SHORT_TEXT)
    fun Cell.medium(component: JComponent) = component(growPolicy = MEDIUM_TEXT)

    titledRow("GitHub Settings") {
      row("GitHub Token") { medium(ghTokenField) }
      row("Max search results") { short(maxResultsCB) }
    }
  }

  internal var ghToken by ghTokenField
  internal var maxResults by maxResultsCB

  fun reset(settings: SJSettings) {
    ghToken = settings.githubToken
    maxResults = settings.maxResults
  }

  private operator fun JTextComponent.getValue(
    a: SJSettingsPanel,
    p: KProperty<*>
  ) = text

  private operator fun JTextComponent.setValue(
    a: SJSettingsPanel,
    p: KProperty<*>,
    s: String
  ) = setText(s)

  private operator fun ColorPanel.getValue(
    a: SJSettingsPanel,
    p: KProperty<*>
  ) = selectedColor

  private operator fun ColorPanel.setValue(
    a: SJSettingsPanel,
    p: KProperty<*>,
    c: Color?
  ) = setSelectedColor(c)

  private operator fun JCheckBox.getValue(a: SJSettingsPanel, p: KProperty<*>) =
    isSelected

  private operator fun JCheckBox.setValue(
    a: SJSettingsPanel,
    p: KProperty<*>,
    selected: Boolean
  ) = setSelected(selected)

  private operator fun <T> ComboBox<T>.getValue(
    a: SJSettingsPanel,
    p: KProperty<*>
  ) = selectedItem as T

  private operator fun <T> ComboBox<T>.setValue(
    a: SJSettingsPanel,
    p: KProperty<*>,
    item: T
  ) = setSelectedItem(item)

  private inline fun <reified T: Enum<T>> ComboBox<T>.setupEnumItems(crossinline onChanged: (T) -> Unit) {
    T::class.java.enumConstants.forEach(this::addItem)
    addActionListener { onChanged(selectedItem as T) }
  }
}