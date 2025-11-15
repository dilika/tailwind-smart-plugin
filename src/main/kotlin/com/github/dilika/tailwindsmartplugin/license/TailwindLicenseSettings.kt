package com.github.dilika.tailwindsmartplugin.license

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*

/**
 * Settings persistants pour la licence
 */
@State(
    name = "TailwindLicenseSettings",
    storages = [Storage("tailwind-license.xml")]
)
@Service(Service.Level.APP)
class TailwindLicenseSettings : PersistentStateComponent<TailwindLicenseSettings.State> {
    
    companion object {
        fun getInstance(): TailwindLicenseSettings {
            return ApplicationManager.getApplication().getService(TailwindLicenseSettings::class.java)
        }
    }
    
    data class State(
        var isPremium: Boolean = false,
        var isTrial: Boolean = false,
        var trialEndDate: String? = null,
        var lastCheckTime: Long = 0L
    )
    
    private var state = State()
    
    override fun getState(): State = state
    
    override fun loadState(state: State) {
        this.state = state
    }
    
    var isPremium: Boolean
        get() = state.isPremium
        set(value) {
            state.isPremium = value
        }
    
    var isTrial: Boolean
        get() = state.isTrial
        set(value) {
            state.isTrial = value
        }
    
    var trialEndDate: String?
        get() = state.trialEndDate
        set(value) {
            state.trialEndDate = value
        }
    
    var lastCheckTime: Long
        get() = state.lastCheckTime
        set(value) {
            state.lastCheckTime = value
        }
}

