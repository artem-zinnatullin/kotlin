package builders

inline fun call(inlineOptions(InlineOption.ONLY_LOCAL_RETURN) init: () -> Unit) {
    return object {
        fun run () {
            init()
        }
    }.run()
}