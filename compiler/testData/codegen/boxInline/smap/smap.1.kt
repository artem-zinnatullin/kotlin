import builders.*
import kotlin.InlineOption.*

inline fun testAllInline(): String {
    var res = "Fail"

    html {
        head {
            res = "12"
        }
    }

    return res
}


fun box(): String {
    var expected = testAllInline();

    if (expected != "O") return "fail 1: "

    return "OK"
}