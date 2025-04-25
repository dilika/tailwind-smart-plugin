package com.github.dilika.tailwindsmartplugin.startup

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.github.dilika.tailwindsmartplugin.services.TailwindConfigService

class TailwindStartupActivity : StartupActivity {
    override fun runActivity(project: Project) {
        val service = project.getService(TailwindConfigService::class.java)
        thisLogger().info("[Tailwind] TailwindStartupActivity started: $service")
    }
}
