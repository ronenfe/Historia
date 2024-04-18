package net.festinger.historia

import android.content.Context
import android.location.Geocoder
import android.location.Location
import java.io.IOException
import java.util.*

class Call(
    number: String?,
    name: String?,
    time: Long,
    duration: Long,
    type: Int,
    location: Location?,
    context: Context?
) {
    lateinit var name: String
        private set
    lateinit var number: String
        private set
    lateinit var type: Types
        private set
    var duration: Long
        private set
    var time: Long
        private set
    lateinit var location: String
        private set

    init {
        this.name = name?.replace("\"".toRegex(), "\"\"") ?: "Unknown"
        this.number =
            if (number.isNullOrEmpty() || number.startsWith("-")) "Unknown" else number
        this.type = Types.fromInt(type)
        this.time = time
        this.duration = duration.toLong()
        val geocoder = context?.let { Geocoder(it, Locale.getDefault()) }
        var loc = "Not Available"
        if (location != null) {
            try {
                val addresses = geocoder?.getFromLocation(location.latitude, location.longitude, 1)
                val loc2 =
                    (if (addresses!![0].getAddressLine(0) == null) "" else addresses[0].getAddressLine(
                        0
                    ) + ", ") + if (addresses[0].locality == null) "" else addresses[0].locality + ", " + if (addresses[0].adminArea == null) "" else addresses[0].adminArea + ", " + if (addresses[0].countryName == null) "" else addresses[0].countryName + ", " + if (addresses[0].postalCode == null) "" else addresses[0].postalCode + ", "
                var i = loc2.length - 1
                while (i >= 0 && (loc2[i] == ' ' || loc2[i] == ',')) {
                    i--
                }
                loc = loc2.substring(0, i)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        this.location = loc
    }
    enum class Types(val value: Int) {
        Incoming(1),
        Outgoing(2),
        Missed(3);

        companion object {
            fun fromInt(value: Int): Types {
                return if (entries.any() { it.value == value }) {
                    entries.first { it.value == value }
                } else {
                    Outgoing
                }
            }
        }
    }
}