package moe.elin.updateblock

import android.util.Log
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import java.lang.reflect.Method

class HookEngine(private val module: XposedModule) {
    fun load(classLoader: ClassLoader, name: String): Class<*>? {
        return try {
            Class.forName(name, false, classLoader)
        } catch (_: ClassNotFoundException) {
            null
        } catch (t: Throwable) {
            module.log(Log.WARN, TAG, "load $name failed", t)
            null
        }
    }

    fun hookNamed(
        clazz: Class<*>,
        methodName: String,
        idPrefix: String,
        hooker: XposedInterface.Hooker,
        walkSuper: Boolean = false,
        required: Boolean = true,
    ): Int {
        val methods = findMethods(clazz, methodName, walkSuper)
        var hooked = 0
        methods.forEachIndexed { index, method ->
            if (hookMethod(method, "$idPrefix#$index", hooker)) {
                hooked += 1
            }
        }
        if (hooked == 0 && required) {
            module.log(Log.WARN, TAG, "missing ${clazz.name}.$methodName")
        }
        return hooked
    }

    private fun findMethods(clazz: Class<*>, methodName: String, walkSuper: Boolean): List<Method> {
        if (!walkSuper) {
            return clazz.declaredMethods.filter { it.name == methodName }
        }
        val out = ArrayList<Method>()
        var current: Class<*>? = clazz
        while (current != null && current != Any::class.java) {
            out.addAll(current.declaredMethods.filter { it.name == methodName })
            current = current.superclass
        }
        return out
    }

    fun hookMethod(
        method: Method,
        id: String,
        hooker: XposedInterface.Hooker,
    ): Boolean {
        return try {
            method.isAccessible = true
            module.hook(method)
                .setId(id)
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept(hooker)
            module.log(
                Log.INFO,
                TAG,
                "hooked ${method.declaringClass.name}.${method.name}(${method.parameterTypes.joinToString { it.simpleName }})",
            )
            true
        } catch (t: Throwable) {
            module.log(
                Log.WARN,
                TAG,
                "hook failed ${method.declaringClass.name}.${method.name}",
                t,
            )
            false
        }
    }

    fun skipVoid(): XposedInterface.Hooker {
        return XposedInterface.Hooker { chain ->
            module.log(
                Log.INFO,
                TAG,
                "blocked ${chain.executable.declaringClass.simpleName}.${chain.executable.name}",
            )
            null
        }
    }

    companion object {
        const val TAG = "UpdateBlock"
    }
}
