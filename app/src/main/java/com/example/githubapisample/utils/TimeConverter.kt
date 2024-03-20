package com.example.githubapisample.utils

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

interface TimeConverter {

    fun convertUtcToUtcPlus8(utcTime: String?): String

}