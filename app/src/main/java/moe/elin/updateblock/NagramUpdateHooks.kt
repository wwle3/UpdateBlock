package moe.elin.updateblock

import android.util.Log
import android.view.View
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule

/**
 * Live path in NextAlone/Nagram:
 * LaunchActivity.checkAppUpdate -> UpdateHelper.checkNewVersionAvailable
 * (R8 inlines the wrappers, leftover is BaseRemoteHelper.load)
 */
class NagramUpdateHooks(
    private val module: XposedModule,
    private val engine: HookEngine,
) {
    fun install(classLoader: ClassLoader) {
        hookUpdateHelper(classLoader)
        hookCheckAppUpdate(classLoader)
        hookShowUpdateActivity(classLoader)
        hookUpdatePopup(classLoader)
        hookSharedConfig(classLoader)
        hookBlockingUpdateView(classLoader)
        hookUpdateLayout(classLoader)
    }

    private fun hookUpdateHelper(classLoader: ClassLoader) {
        val helper = engine.load(classLoader, "tw.nekomimi.nekogram.helpers.remote.UpdateHelper")
        val base = engine.load(classLoader, "tw.nekomimi.nekogram.helpers.remote.BaseRemoteHelper")
        val noUpdate = XposedInterface.Hooker { chain ->
            if (!isUpdateHelper(chain.thisObject)) {
                return@Hooker chain.proceed()
            }
            module.log(Log.INFO, HookEngine.TAG, "blocked UpdateHelper.${chain.executable.name}")
            val delegate = chain.args.firstOrNull { it != null && hasOnTLResponse(it) }
            completeNoUpdate(delegate)
            null
        }
        if (helper != null) {
            engine.hookNamed(
                helper,
                "checkNewVersionAvailable",
                "nagram.UpdateHelper.checkNewVersionAvailable",
                noUpdate,
                required = false,
            )
            engine.hookNamed(
                helper,
                "getShouldUpdateVersion",
                "nagram.UpdateHelper.getShouldUpdateVersion",
                noUpdate,
                required = false,
            )
            engine.hookNamed(helper, "onLoadSuccess", "nagram.UpdateHelper.onLoadSuccess", noUpdate)
        }
        if (base != null) {
            engine.hookNamed(base, "load", "nagram.BaseRemoteHelper.load", noUpdate)
        }
    }

    private fun hookCheckAppUpdate(classLoader: ClassLoader) {
        val clazz = engine.load(classLoader, "org.telegram.ui.LaunchActivity") ?: return
        engine.hookNamed(
            clazz,
            "checkAppUpdate",
            "nagram.LaunchActivity.checkAppUpdate",
            XposedInterface.Hooker { chain ->
                module.log(Log.INFO, HookEngine.TAG, "blocked LaunchActivity.checkAppUpdate")
                if (chain.args.size >= 2) {
                    endProgress(chain.getArg(1))
                }
                null
            },
        )
    }

    private fun hookShowUpdateActivity(classLoader: ClassLoader) {
        val clazz = engine.load(classLoader, "org.telegram.ui.LaunchActivity") ?: return
        engine.hookNamed(
            clazz,
            "showUpdateActivity",
            "nagram.LaunchActivity.showUpdateActivity",
            engine.skipVoid(),
        )
    }

    private fun hookUpdatePopup(classLoader: ClassLoader) {
        val loader = engine.load(classLoader, "org.telegram.messenger.ApplicationLoader")
        if (loader != null) {
            engine.hookNamed(
                loader,
                "showUpdateAppPopup",
                "nagram.ApplicationLoader.showUpdateAppPopup",
                XposedInterface.Hooker { chain ->
                    module.log(Log.INFO, HookEngine.TAG, "blocked ApplicationLoader.showUpdateAppPopup")
                    false
                },
                required = false,
            )
        }
    }

    private fun hookSharedConfig(classLoader: ClassLoader) {
        val clazz = engine.load(classLoader, "org.telegram.messenger.SharedConfig") ?: return
        engine.hookNamed(
            clazz,
            "isAppUpdateAvailable",
            "nagram.SharedConfig.isAppUpdateAvailable",
            XposedInterface.Hooker { false },
        )
        engine.hookNamed(
            clazz,
            "setNewAppVersionAvailable",
            "nagram.SharedConfig.setNewAppVersionAvailable",
            XposedInterface.Hooker { chain ->
                module.log(Log.INFO, HookEngine.TAG, "blocked SharedConfig.setNewAppVersionAvailable")
                false
            },
        )
    }

    private fun hookBlockingUpdateView(classLoader: ClassLoader) {
        val clazz = engine.load(classLoader, "org.telegram.ui.Components.BlockingUpdateView")
            ?: return
        engine.hookNamed(
            clazz,
            "show",
            "nagram.BlockingUpdateView.show",
            XposedInterface.Hooker { chain ->
                module.log(Log.INFO, HookEngine.TAG, "blocked BlockingUpdateView.show")
                hideView(chain.thisObject)
                null
            },
        )
    }

    private fun hookUpdateLayout(classLoader: ClassLoader) {
        val clazz = engine.load(classLoader, "org.telegram.ui.Components.UpdateLayout") ?: return
        engine.hookNamed(
            clazz,
            "updateAppUpdateViews",
            "nagram.UpdateLayout.updateAppUpdateViews",
            engine.skipVoid(),
        )
    }

    private fun isUpdateHelper(self: Any?): Boolean {
        return self?.javaClass?.name == "tw.nekomimi.nekogram.helpers.remote.UpdateHelper"
    }

    private fun hasOnTLResponse(obj: Any): Boolean {
        return obj.javaClass.methods.any { it.name == "onTLResponse" }
    }

    private fun completeNoUpdate(delegate: Any?) {
        if (delegate == null) {
            return
        }
        try {
            val method = delegate.javaClass.methods.firstOrNull { it.name == "onTLResponse" }
                ?: return
            method.isAccessible = true
            method.invoke(delegate, null, null)
        } catch (t: Throwable) {
            module.log(Log.WARN, HookEngine.TAG, "delegate onTLResponse failed", t)
        }
    }

    private fun endProgress(progress: Any?) {
        if (progress == null) {
            return
        }
        try {
            progress.javaClass.methods
                .firstOrNull { it.name == "end" && it.parameterCount == 0 }
                ?.invoke(progress)
        } catch (_: Throwable) {
        }
    }

    private fun hideView(target: Any?) {
        if (target is View) {
            try {
                target.visibility = View.GONE
            } catch (_: Throwable) {
            }
        }
    }
}
