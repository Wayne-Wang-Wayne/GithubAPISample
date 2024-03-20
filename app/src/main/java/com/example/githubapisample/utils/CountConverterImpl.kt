package com.example.githubapisample.utils

class CountConverterImpl : CountConverter {

    /**
     * Converts count to k-string.
     * @param count the count to be converted
     * @return the k-string
     * Examples:
     * 3300->3.3k
     * 3020->3k
     * 3000->3k
     * 33300->33.3k
     * 33000->33k
     * 33098->33k
     */
    override fun convertCountToKString(count: Int): String {
        return when {
            count >= 1000 -> {
                val kCount = count / 1000.0
                val formattedCount = if (kCount % 1 == 0.0) {
                    String.format("%.0fk", kCount)
                } else {
                    String.format("%.1fk", kCount)
                }
                if (formattedCount.endsWith(".0k")) formattedCount.dropLast(3) + "k" else formattedCount
            }
            else -> String.format("%d", count)
        }
    }
}