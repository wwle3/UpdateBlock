package moe.elin.updateblock

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import moe.elin.updateblock.databinding.ActivityMainBinding
import io.github.libxposed.service.XposedService

class MainActivity : Activity(), App.ServiceStateListener {
    private lateinit var binding: ActivityMainBinding
    private var bound: XposedService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnScope.setOnClickListener { requestRecommendedScope() }
        render(null)
    }

    override fun onStart() {
        super.onStart()
        App.addServiceStateListener(this, true)
    }

    override fun onStop() {
        App.removeServiceStateListener(this)
        super.onStop()
    }

    override fun onServiceStateChanged(service: XposedService?) {
        bound = service
        runOnUiThread { render(service) }
    }

    private fun render(service: XposedService?) {
        if (service == null) {
            binding.status.text = getString(R.string.status_disconnected)
            binding.framework.text = getString(R.string.framework_unknown)
            binding.btnScope.isEnabled = false
            return
        }
        binding.status.text = getString(R.string.status_connected)
        binding.framework.text = getString(
            R.string.framework_info,
            service.frameworkName,
            service.frameworkVersion,
            service.apiVersion,
        )
        binding.btnScope.isEnabled = true
    }

    private fun requestRecommendedScope() {
        val service = bound ?: return
        service.requestScope(
            Targets.RECOMMENDED_SCOPE,
            object : XposedService.OnScopeEventListener {
                override fun onScopeRequestApproved(approved: List<String>) {
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.scope_ok, approved.size),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }

                override fun onScopeRequestFailed(message: String) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                    }
                }
            },
        )
    }
}
