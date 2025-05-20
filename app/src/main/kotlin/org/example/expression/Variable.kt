package org.example.expression

import org.typemeta.funcj.control.Either

class Variable(val name:String): Expression(){
    override val numOperands: Int = 1
    var value: Either<String, N> = Either<String, N>.left("?")

    override fun evaluate(context:Context): Either<String, N> {
        value = context[name]
        return value
    }

    override fun toString(): String {
        return "$name: $value"
    }
}