package org.connecttag.services.callblocking

import java.io.InputStream
import java.io.OutputStream

public object BlockedNumbersTextCodec {
    private const val DELIMITER = ","

    public fun encode(blockedNumbers: Collection<BlockedNumber>): String =
        blockedNumbers.joinToString(DELIMITER) { it.number }

    public fun decode(text: String): List<String> = text
        .split(DELIMITER)
        .map(String::trim)
        .filter(String::isNotEmpty)
        .distinct()

    public fun write(blockedNumbers: Collection<BlockedNumber>, outputStream: OutputStream) {
        outputStream.bufferedWriter().use { writer -> writer.write(encode(blockedNumbers)) }
    }

    public fun read(inputStream: InputStream): List<String> =
        inputStream.bufferedReader().use { reader -> decode(reader.readText()) }
}
