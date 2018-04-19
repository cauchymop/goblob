import org.mockito.Mockito

fun <T> kAny(): T {
    Mockito.any<T>()
    return uninitialized()
}

private fun <T> uninitialized(): T = null as T