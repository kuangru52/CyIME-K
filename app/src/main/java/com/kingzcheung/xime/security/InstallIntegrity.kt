package com.kingzcheung.xime.security

import android.content.Context

object InstallIntegrity {

    internal val OFFICIAL_CERTS: Set<String> = setOf(
        "e0d419cbfc7715e0be5bd98e7e5a26ab7d147a67b15d0961797197e6c76c05e2",
    )

    const val DOWNLOAD_URL = "https://github.com/Cyletix/CyIME/releases"

    enum class Result {
        OFFICIAL,
        FOREIGN,
        UNKNOWN,
        SKIPPED,
    }

    @Suppress("UNUSED_PARAMETER")
    fun check(context: Context): Result = Result.OFFICIAL

    @Suppress("UNUSED_PARAMETER")
    internal fun classify(current: List<String>, history: List<String>): Result = Result.OFFICIAL
}
