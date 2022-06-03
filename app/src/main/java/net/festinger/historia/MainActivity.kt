package net.festinger.historia

import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat


class MainActivity : AppCompatActivity() {
    private val mLastLocation: Location? = null

    // android.app.Activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setPermissions()
        startService(Intent(this, HistoriaService::class.java))
        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, Preferences())
            .commit()
    }

    // android.app.Activity
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        for (perm in grantResults) {
            if (perm == -1) {
                setPermissions()
                setContentView(R.layout.activity_main)
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions!!, grantResults)
    }

    private fun setPermissions(): Boolean? {
        val arraylist: ArrayList<String> = ArrayList()
        if (ContextCompat.checkSelfPermission(this, "android.permission.WRITE_CALENDAR") != 0) {
            arraylist.add("android.permission.WRITE_CALENDAR")
        }
        if (ContextCompat.checkSelfPermission(this, "android.permission.READ_CALENDAR") != 0) {
            arraylist.add("android.permission.READ_CALENDAR")
        }
        if (ContextCompat.checkSelfPermission(this, "android.permission.READ_CALL_LOG") != 0) {
            arraylist.add("android.permission.READ_CALL_LOG")
        }
        if (ContextCompat.checkSelfPermission(
                this,
                "android.permission.ACCESS_COARSE_LOCATION"
            ) != 0
        ) {
            arraylist.add("android.permission.ACCESS_COARSE_LOCATION")
        }
        if (ContextCompat.checkSelfPermission(
                this,
                "android.permission.ACCESS_FINE_LOCATION"
            ) != 0
        ) {
            arraylist.add("android.permission.ACCESS_FINE_LOCATION")
        }
        if (arraylist.size <= 0) {
            return true
        }
        ActivityCompat.requestPermissions(
            this,
            (arraylist.toArray(arrayOfNulls<String>(0)) as Array<String?>), 1
        )
        return false
    }

}