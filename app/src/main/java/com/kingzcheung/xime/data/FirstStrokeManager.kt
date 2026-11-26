package com.kingzcheung.xime.data

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object FirstStrokeManager {
    private const val TAG = "FirstStrokeManager"
    private const val ASSET_FILE_NAME = "hanzi_stroke_8105.tsv"

    // Map Hanzi Char to first stroke code (1: 横, 2: 竖, 3: 撇, 4: 点/捺, 5: 折)
    @Volatile
    private var strokeMap: Map<Char, Int>? = null

    /**
     * Ensure the 8105 Hanzi first-stroke table is loaded in memory.
     */
    fun ensureLoaded(context: Context) {
        if (strokeMap != null) return
        synchronized(this) {
            if (strokeMap != null) return
            val map = HashMap<Char, Int>(8200)
            try {
                context.assets.open(ASSET_FILE_NAME).use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                        var line: String?
                        var isHeader = true
                        while (reader.readLine().also { line = it } != null) {
                            val l = line ?: continue
                            if (isHeader) {
                                isHeader = false
                                continue
                            }
                            val parts = l.split('\t')
                            if (parts.size >= 7) {
                                val hanziStr = parts[3].trim()
                                val strokeCodeStr = parts[6].trim()
                                if (hanziStr.isNotEmpty() && strokeCodeStr.isNotEmpty()) {
                                    val char = hanziStr[0]
                                    val firstCodeChar = strokeCodeStr[0]
                                    val strokeCode = firstCodeChar.digitToIntOrNull()
                                    if (strokeCode != null && strokeCode in 1..5) {
                                        map[char] = strokeCode
                                    }
                                }
                            }
                        }
                    }
                }
                strokeMap = map
                Log.d(TAG, "Loaded ${map.size} Hanzi first-stroke records.")
            } catch (e: Exception) {
                Log.e(TAG, "Error loading $ASSET_FILE_NAME", e)
                strokeMap = map
            }
        }
    }

    /**
     * Get the first stroke code for a character (1..5), or null if not found.
     */
    fun getFirstStroke(context: Context, char: Char): Int? {
        ensureLoaded(context)
        return strokeMap?.get(char)
    }

    /**
     * Filter candidates by selected stroke code (1..5).
     */
    fun filterCandidates(context: Context, candidates: List<String>, strokeCode: Int): List<String> {
        ensureLoaded(context)
        if (strokeCode !in 1..5 || candidates.isEmpty()) return candidates
        return candidates.filter { candidate ->
            val firstHanzi = candidate.firstOrNull { isChineseChar(it) }
            if (firstHanzi != null) {
                strokeMap?.get(firstHanzi) == strokeCode
            } else {
                false
            }
        }
    }

    /**
     * Basic stroke symbols corresponding to stroke codes 1..5.
     * 1: 横 (一), 2: 竖 (丨), 3: 撇 (丿), 4: 点/捺 (丶), 5: 折 (乛)
     */
    val STROKE_SYMBOLS = listOf("一", "丨", "丿", "丶", "乛")

    /**
     * Default punctuation marks for idle state.
     */
    val DEFAULT_PUNCTUATIONS = listOf("，", "。", "？", "！", "～")

    private fun isChineseChar(c: Char): Boolean {
        val ub = Character.UnicodeBlock.of(c)
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
                ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B ||
                ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
    }
}
