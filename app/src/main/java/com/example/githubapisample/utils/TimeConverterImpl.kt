package com.example.githubapisample.utils

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class TimeConverterImpl : TimeConverter {

    override fun convertUtcToUtcPlus8(utcTime: String?): String {
        if (utcTime == null) return ""
        val dateTime = ZonedDateTime.parse(utcTime)
        val utcPlus8Time = dateTime.withZoneSameInstant(ZoneId.of("UTC+8"))
        val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
        return utcPlus8Time.format(formatter)
    }

}