package zed.rainxch.core.data.utils

import java.security.MessageDigest

fun hashQuery(query: String): String {
    val normalized = query.trim().lowercase()
    if (normalized.isEmpty()) return ""
    val digest = MessageDigest.getInstance("SHA-256").digest(normalized.encodeToByteArray())
    val hex = buildString(digest.size * 2) {
        for (byte in digest) {
            val v = byte.toInt() and 0xff
            if (v < 0x10) append('0')
            append(v.toString(16))
        }
    }
    return hex.take(16)
}
