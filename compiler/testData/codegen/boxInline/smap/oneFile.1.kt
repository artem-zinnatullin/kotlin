fun box(): String {

    return test {
        "K"
    }

}

inline fun test(p: () -> String): String {
    var pd = ""
    pd = "O"
    return pd + p()
}
