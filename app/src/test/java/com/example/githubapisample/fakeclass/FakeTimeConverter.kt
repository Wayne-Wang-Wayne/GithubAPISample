package com.example.githubapisample.fakeclass

import com.example.githubapisample.utils.TimeConverter

class FakeTimeConverter : TimeConverter {
    val fakeTime = "2021年03月07日"
    override fun convertUtcToUtcPlus8(utcTime: String?): String {
        return fakeTime
    }
}