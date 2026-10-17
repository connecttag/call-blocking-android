package org.connecttag.services.callblocking

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.BlockedNumberContract.BlockedNumbers
import android.telephony.PhoneNumberUtils

public class AndroidCallBlockingRepository(
    private val context: Context,
) : CallBlockingRepository {

    override fun getBlockedNumbers(): CallBlockingResult<List<BlockedNumber>> {
        if (!isSupported()) return CallBlockingResult.Unsupported

        return runCatching {
            val blockedNumbers = mutableListOf<BlockedNumber>()
            val projection = arrayOf(
                BlockedNumbers.COLUMN_ID,
                BlockedNumbers.COLUMN_ORIGINAL_NUMBER,
                BlockedNumbers.COLUMN_E164_NUMBER,
            )

            context.contentResolver.query(BlockedNumbers.CONTENT_URI, projection, null, null, null)?.use { cursor ->
                val idIndex = cursor.getColumnIndexOrThrow(BlockedNumbers.COLUMN_ID)
                val numberIndex = cursor.getColumnIndexOrThrow(BlockedNumbers.COLUMN_ORIGINAL_NUMBER)
                val normalizedIndex = cursor.getColumnIndexOrThrow(BlockedNumbers.COLUMN_E164_NUMBER)
                while (cursor.moveToNext()) {
                    val number = cursor.getString(numberIndex).orEmpty()
                    val normalized = cursor.getString(normalizedIndex) ?: number
                    blockedNumbers += BlockedNumber(
                        id = cursor.getLong(idIndex),
                        number = number,
                        normalizedNumber = normalized,
                    )
                }
            }
            blockedNumbers.sortedBy { it.number }
        }.toCallBlockingResult()
    }

    override fun addBlockedNumber(number: String): CallBlockingResult<BlockedNumber> {
        if (!isSupported()) return CallBlockingResult.Unsupported

        return runCatching {
            val normalizedNumber = if (number.contains('*')) {
                number
            } else if (number.any(Char::isDigit)) {
                PhoneNumberUtils.normalizeNumber(number)
            } else {
                number
            }
            val values = ContentValues().apply {
                put(BlockedNumbers.COLUMN_ORIGINAL_NUMBER, number)
                put(BlockedNumbers.COLUMN_E164_NUMBER, normalizedNumber)
            }
            val uri = context.contentResolver.insert(BlockedNumbers.CONTENT_URI, values)
            val id = uri?.lastPathSegment?.toLongOrNull() ?: 0L
            BlockedNumber(id = id, number = number, normalizedNumber = normalizedNumber)
        }.toCallBlockingResult()
    }

    override fun deleteBlockedNumber(number: String): CallBlockingResult<Boolean> {
        if (!isSupported()) return CallBlockingResult.Unsupported

        return runCatching {
            val deleted = context.contentResolver.delete(
                BlockedNumbers.CONTENT_URI,
                "${BlockedNumbers.COLUMN_ORIGINAL_NUMBER} = ?",
                arrayOf(number),
            )
            deleted > 0
        }.toCallBlockingResult()
    }

    override fun isNumberBlocked(number: String): CallBlockingResult<Boolean> = when (val blockedNumbers = getBlockedNumbers()) {
        is CallBlockingResult.Success -> CallBlockingResult.Success(
            BlockedNumberMatcher.matchesAny(number, blockedNumbers.value),
        )
        is CallBlockingResult.Unsupported -> blockedNumbers
        is CallBlockingResult.PermissionDenied -> blockedNumbers
        is CallBlockingResult.Failure -> blockedNumbers
    }

    private fun isSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    private fun <T> Result<T>.toCallBlockingResult(): CallBlockingResult<T> = fold(
        onSuccess = { CallBlockingResult.Success(it) },
        onFailure = { throwable ->
            if (throwable is SecurityException) CallBlockingResult.PermissionDenied else CallBlockingResult.Failure(throwable)
        },
    )
}
