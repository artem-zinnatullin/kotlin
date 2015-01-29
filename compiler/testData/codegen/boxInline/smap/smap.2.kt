package builders

inline fun init(init: () -> Unit) {
    init()
}

inline fun initTag2(init: () -> Unit) {
    val p = 1;
    init()
}

inline fun head(init: () -> Unit) = initTag2(init)


inline fun html(init: () -> Unit) {
    return init(init)
}