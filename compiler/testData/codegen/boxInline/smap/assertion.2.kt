package test

public val MASSERTIONS_ENABLED: Boolean = true

public inline fun massert(value: Boolean, lazyMessage: () -> String) {
    if (MASSERTIONS_ENABLED) {
        if (!value) {
            val message = lazyMessage()
            throw AssertionError(message)
        }
    }
}


public fun dassert(value: Boolean, message: Any = "Assertion failed") {
    if (ASSERTIONS_ENABLED) {
        if (!value) {
            throw AssertionError(message)
        }
    }
}
