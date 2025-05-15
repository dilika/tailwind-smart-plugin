package com.github.dilika.tailwindsmartplugin.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Persistent settings for the Tailwind CSS Smart Plugin
 */
@Service
@State(
    name = "TailwindPluginSettings",
    storages = [Storage("tailwindSmartPlugin.xml")]
)
class TailwindPluginSettings : PersistentStateComponent<TailwindPluginSettings> {
    // Icon display settings
    var showIcons: Boolean = true
    var useRoundedColorIcons: Boolean = true
    
    // Override to provide the state to persist
    override fun getState(): TailwindPluginSettings = this

    // Override to load the state
    override fun loadState(state: TailwindPluginSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }
    
    companion object {
        // Helper to get the service instance
        fun getInstance(): TailwindPluginSettings {
            return com.intellij.openapi.components.service<TailwindPluginSettings>()
        }
    }
}
