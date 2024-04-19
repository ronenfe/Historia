package net.festinger.historia

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.READ_CALENDAR
import android.Manifest.permission.READ_CALL_LOG
import android.Manifest.permission.RECEIVE_BOOT_COMPLETED
import android.Manifest.permission.WRITE_CALENDAR
import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

private const val REQUEST_CODE = 1
class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private val mLastLocation: Location? = null

    // android.app.Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onStart() {
        super.onStart()
        requestPermissions()
    }
    private fun requestPermissions() {
        val perms = arrayOf(
            ACCESS_FINE_LOCATION,
            ACCESS_COARSE_LOCATION,
            WRITE_CALENDAR,
            READ_CALENDAR,
            READ_CALL_LOG,
            RECEIVE_BOOT_COMPLETED,
        )
        if (!EasyPermissions.hasPermissions(this, *perms)) {
            EasyPermissions.requestPermissions(
                host = this,
                rationale =getString(R.string.permissions_rationale),
                requestCode = REQUEST_CODE,
                perms = perms)
        }
        else {
            startService(Intent(this, HistoriaService::class.java))

            supportFragmentManager
                .beginTransaction()
                .replace(android.R.id.content, Preferences())
                .commit()
        }
    }
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }
    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        startService(Intent(this, HistoriaService::class.java))

        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, Preferences())
            .commit()
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

}