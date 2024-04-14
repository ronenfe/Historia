package net.festinger.historia

import android.content.Context
import android.location.Geocoder
import android.location.Location
import java.io.IOException
import java.text.DateFormat
import java.util.*

/* loaded from: classes.dex */
class CallStrings {
    var duration: Long
        private set
    val strings: Array<String?>

    /* JADX INFO: Access modifiers changed from: package-private */
    constructor(
        number: String?,
        name: String?,
        time: Long,
        duration: String,
        type: Int,
        location: Location?,
        context: Context?
    ) {
        val str: String
        val str2: String
        this.duration = 0L
        strings = arrayOfNulls(8)
        val strArr = strings
        str = name?.replace("\"".toRegex(), "\"\"") ?: "Unknown"
        strArr[1] = str
        val strArr2 = strings
        strArr2[0] =
            if (number == null || number.isEmpty() || number.startsWith("-")) "Unknown" else number
        when (type) {
            1 -> strings[4] = "Incoming"
            2 -> strings[4] = "Outgoing"
            3 -> strings[4] = "Missed"
            else -> strings[4] = "Outgoing"
        }
        strings[2] = DateFormat.getDateTimeInstance().format(Date(time))
        val intDuration = duration.toInt()
        this.duration = intDuration.toLong()
        val hours = intDuration / 3600
        strings[3] = if (hours == 0) "" else Integer.toString(hours) + ":"
        val intDuration2 = intDuration % 3600
        val minutes = intDuration2 / 60
        val strArr3 = strings
        strArr3[3] =
            strArr3[3].toString() + (if (Integer.toString(minutes).length == 2) Integer.toString(
                minutes
            ) else "0" + Integer.toString(minutes)) + ":"
        val intDuration3 = intDuration2 % 60
        val strArr4 = strings
        val stringBuilder = StringBuilder(strArr4[3].toString())
        str2 = if (Integer.toString(intDuration3).length == 2) {
            Integer.toString(intDuration3)
        } else {
            "0" + Integer.toString(intDuration3)
        }
        strArr4[3] = stringBuilder.append(str2).toString()
        strings[6] = java.lang.Long.toString(time)
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
        strings[5] = loc
    }

    internal constructor() {
        duration = 0L
        strings = arrayOfNulls(8)
    }

    internal constructor(
        number: String?,
        name: String?,
        time: String?,
        duration: String?,
        type: String?,
        location: String?,
        sn: String?
    ) {
        this.duration = 0L
        strings = arrayOfNulls(8)
        strings[0] = number
        strings[1] = name
        strings[2] = time
        strings[3] = duration
        strings[4] = type
        strings[5] = location
        strings[6] = sn
    }

    override fun toString(): String {
        strings[2] = DateFormat.getDateTimeInstance().format(
            java.lang.Long.valueOf(
                strings[6]!!.toLong()
            )
        )
        return """
            "${strings[0]}","${strings[1]}","${strings[2]}","${strings[3]}","${strings[4]}","","${strings[5]}","${strings[6]}"
            
            """.trimIndent()
    }

    companion object {
        const val DURATION = 3
        const val LOCATION = 5
        const val NAME = 1
        const val NUMBER = 0
        const val SN = 6
        const val TIME = 2
        const val TYPE = 4
    }
}