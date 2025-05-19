package org.example.expression

import org.typemeta.funcj.control.Either

class Variable(val name:String): Expression(){
    override val numOperands: Int = 1

    override fun evaluate(context:Context): Either<String, N> {
        return context[name]
    }
}