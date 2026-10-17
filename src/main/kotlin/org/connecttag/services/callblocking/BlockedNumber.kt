package org.connecttag.services.callblocking

public data class BlockedNumber(
    public val id: Long,
    public val number: String,
    public val normalizedNumber: String = number,
    public val numberToCompare: String = BlockedNumberMatcher.toComparableNumber(normalizedNumber),
    public val contactName: String? = null,
)

public sealed interface CallBlockingResult<out T> {
    public data class Success<T>(public val value: T) : CallBlockingResult<T>
    public data object Unsupported : CallBlockingResult<Nothing>
    public data object PermissionDenied : CallBlockingResult<Nothing>
    public data class Failure(public val throwable: Throwable) : CallBlockingResult<Nothing>
}
