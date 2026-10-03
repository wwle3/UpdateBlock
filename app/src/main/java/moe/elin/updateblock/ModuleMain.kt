package moe.elin.updateblock

import android.util.Log
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface.HotReloadedParam
import io.github.libxposed.api.XposedModuleInterface.HotReloadingParam
import io.github.libxposed.api.XposedModuleInterface.ModuleLoadedParam
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

class ModuleMain : XposedModule() {
    private val engine by lazy { HookEngine(this) }
    private val nagramHooks by lazy { NagramUpdateHooks(this, engine) }

    override fun onModuleLoaded(param: ModuleLoadedParam) {
        log(
            Log.INFO,
            HookEngine.TAG,
            "loaded in ${param.processName} system=${param.isSystemServer} " +
                "fw=$frameworkName $frameworkVersion api=$apiVersion",
        )
    }

    override fun onPackageReady(param: PackageReadyParam) {
        if (!param.isFirstPackage) {
            return
        }
        install(param.packageName, param.classLoader)
    }

    override fun onHotReloading(param: HotReloadingParam): Boolean {
        param.setSavedInstanceState("reload")
        return true
    }

    override fun onHotReloaded(param: HotReloadedParam) {
        param.oldHookHandles.forEach { it.unhook() }
        val classLoader = Thread.currentThread().contextClassLoader ?: javaClass.classLoader
        install(Targets.packageNameOf(param.processName), classLoader)
    }

    private fun install(packageName: String, classLoader: ClassLoader) {
        if (packageName !in Targets.PACKAGES) {
            log(Log.INFO, HookEngine.TAG, "skip $packageName")
            return
        }
        log(Log.INFO, HookEngine.TAG, "hook in-app update for $packageName")
        nagramHooks.install(classLoader)
    }
}
