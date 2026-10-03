package moe.elin.updateblock

import android.app.Application
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper
import java.util.concurrent.CopyOnWriteArraySet

class App : Application(), XposedServiceHelper.OnServiceListener {

    companion object {
        @Volatile
        var service: XposedService? = null
            private set

        private val listeners = CopyOnWriteArraySet<ServiceStateListener>()

        fun addServiceStateListener(listener: ServiceStateListener, notifyImmediately: Boolean) {
            listeners.add(listener)
            if (notifyImmediately) {
                listener.onServiceStateChanged(service)
            }
        }

        fun removeServiceStateListener(listener: ServiceStateListener) {
            listeners.remove(listener)
        }
    }

    override fun onCreate() {
        super.onCreate()
        XposedServiceHelper.registerListener(this)
    }

    override fun onServiceBind(bound: XposedService) {
        service = bound
        listeners.forEach { it.onServiceStateChanged(bound) }
    }

    override fun onServiceDied(dead: XposedService) {
        service = null
        listeners.forEach { it.onServiceStateChanged(null) }
    }

    interface ServiceStateListener {
        fun onServiceStateChanged(service: XposedService?)
    }
}
