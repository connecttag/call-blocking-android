package org.connecttag.services.callblocking

public interface CallBlockingRepository {
    public fun getBlockedNumbers(): CallBlockingResult<List<BlockedNumber>>
    public fun addBlockedNumber(number: String): CallBlockingResult<BlockedNumber>
    public fun addBlockedNumbers(numbers: Collection<String>): CallBlockingResult<List<BlockedNumber>> {
        val added = mutableListOf<BlockedNumber>()
        numbers.distinct().forEach { number ->
            when (val result = addBlockedNumber(number)) {
                is CallBlockingResult.Success -> added += result.value
                is CallBlockingResult.Unsupported -> return result
                is CallBlockingResult.PermissionDenied -> return result
                is CallBlockingResult.Failure -> return result
            }
        }
        return CallBlockingResult.Success(added)
    }
    public fun deleteBlockedNumber(number: String): CallBlockingResult<Boolean>
    public fun isNumberBlocked(number: String): CallBlockingResult<Boolean>
}
