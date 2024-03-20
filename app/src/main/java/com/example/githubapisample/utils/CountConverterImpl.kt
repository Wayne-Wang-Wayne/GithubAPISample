package com.example.githubapisample.utils

class CountConverterImpl : CountConverter {
    override fun convertCountToKString(count: Int): String {
        return when {
            count >= 1000 -> String.format("%.1fk", count / 1000.0)
            else -> count.toString()
        }
    }
}