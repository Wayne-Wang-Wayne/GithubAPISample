package com.example.githubapisample.fakeclass

import com.example.githubapisample.utils.CountConverter

class FakeCountConverter : CountConverter {
    val fakeCount = "1k"
    override fun convertCountToKString(count: Int): String {
        return fakeCount
    }
}