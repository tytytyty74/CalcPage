package org.example.expression

import org.typemeta.funcj.control.Either


class Context {
    val map: MutableMap<String, N> = HashMap()
    operator fun get(i:String) : Either<String, N> {
        val x = map[i]
        return if (x != null) Either.right(x) else Either.left("Invalid Variable")
    }
    operator fun set(i:String, b: N) {
        map[i] = b
    }
}