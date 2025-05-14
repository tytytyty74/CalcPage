package org.example
import org.typemeta.funcj.data.Chr
import org.typemeta.funcj.parser.Parser
import org.typemeta.funcj.parser.Text.chr
import org.typemeta.funcj.parser.Text.dble
import org.typemeta.funcj.parser.Text.ws

sealed class Expression {
    class Calculation(val op: Operator, vararg val operands : Expression) : Expression()
    {
        override fun evaluate(): Double {
            return op.evaluate(*operands)
        }

        override fun toString():String {
            var retVal = "("
            for (i in operands) {
                retVal += "$i $op "
            }
            if (operands.size > 0) {
                retVal = retVal.substring(0, (retVal.length-op.toString().length - 2))
            }
            retVal += ")"
            return retVal
        }

    }

    class N(val value: Double) : Expression() {
        constructor(value:Int) : this(value.toDouble())
        override fun evaluate(): Double {
            return value
        }
        override fun toString():String {
            return value.toString()
        }

    }

    abstract fun evaluate(): Double


    companion object {
        val dbleExpr : Parser<Chr, Expression> = ws.many().andR(dble).andL(ws.many()).map { a -> N(a) }
    }


}

sealed class Operator {
    class Add : Operator() {
        override fun toString():String {
            return "+"
        }
        override fun evaluate(vararg operands: Expression): Double {
            var sum = 0.0
            for (t in operands) {
                sum+= t.evaluate()
            }
            return sum
        }

        override val parse: Parser<Chr, Operator> = chr('+').map { _ -> this }
    }
    class Sub : Operator() {
        override fun toString():String {
            return "-"
        }
        override fun evaluate(vararg operands: Expression): Double {
            return if (operands.isEmpty()) 0.0 else operands[0].evaluate() - Add().evaluate(*operands.drop(1).toTypedArray())
        }
        override val parse: Parser<Chr, Operator> = chr('-').map { _ -> this }

    }
    class Mul : Operator() {
        override fun toString():String {
            return "*"
        }
        override fun evaluate(vararg operands: Expression): Double {
            var product = 1.0
            for (t in operands) {
                product *= t.evaluate()
            }
            return product
        }
        override val parse: Parser<Chr, Operator> = chr('*').map { _ -> this }

    }
    class Div : Operator() {
        override fun toString():String {
            return "/"
        }
        override fun evaluate(vararg operands: Expression): Double {
            return if (operands.isEmpty()) 0.0 else operands[0].evaluate() - Mul().evaluate(*operands.drop(1).toTypedArray())
        }

        override val parse: Parser<Chr, Operator> = chr('/').map { _ -> this }

    }
    abstract fun evaluate(vararg operands: Expression): Double
    abstract val parse:Parser<Chr, Operator>
}