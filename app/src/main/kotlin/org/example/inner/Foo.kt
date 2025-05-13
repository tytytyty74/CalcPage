package org.example.inner

sealed class Foo(s: String) {
    val s = s
    class Foo2(): Foo("hello")
}