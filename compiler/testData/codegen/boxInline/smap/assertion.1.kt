import test.*

fun box(): String {
    assert(true)
    assert(true) {
        "test"
    }

    return "OK"
}