package org.connecttag.services.callblocking

import android.telephony.PhoneNumberUtils

public object BlockedNumberMatcher {
    public fun toComparableNumber(number: String): String = PhoneNumberUtils.stripSeparators(number)
        .filter { it.isDigit() || it == '+' }
        .trim()

    public fun isPattern(number: String): Boolean = number.contains('*')

    public fun matches(number: String, blockedNumber: BlockedNumber): Boolean {
        val comparable = toComparableNumber(number)
        return comparable == blockedNumber.numberToCompare ||
            comparable == toComparableNumber(blockedNumber.number) ||
            matchesPattern(number, blockedNumber.number)
    }

    public fun matchesAny(number: String, blockedNumbers: Collection<BlockedNumber>): Boolean =
        blockedNumbers.any { matches(number, it) }

    private fun matchesPattern(number: String, pattern: String): Boolean {
        if (!isPattern(pattern)) return false
        val regex = pattern
            .replace("+", "\\+")
            .replace("*", ".*")
            .toRegex()
        return number.matches(regex)
    }
}
