package org.example.expression

import org.typemeta.funcj.control.Either
import org.typemeta.funcj.data.Chr
import org.typemeta.funcj.parser.Parser
import org.typemeta.funcj.parser.Text.chr
import java.util.Optional
import javax.swing.text.html.Option

abstract class Operator {
    class Add : Operator() {
        override fun toString():String {
            return "+"
        }
        override fun evaluate(context: Context, vararg operands: Expression): Either<String, N> {
            var sum = if (operands.isNotEmpty()) operands[0].evaluate(context) else Either.left("Empty Addition")
            for (t in operands.drop(1)) {
                sum += t.evaluate(context)
            }
            return sum
        }

        override val parse: Parser<Chr, Operator> = chr('+').map { _ -> this }
        override val level: Int = 1
    }
    class Sub : Operator() {
        override fun toString():String {
            return "-"
        }
        override fun evaluate(context: Context, vararg operands: Expression): Either<String, N>  {
            return if (operands.isEmpty()) Either.left("Empty Subtraction") else operands[0].evaluate(context) - Add().evaluate(context, *operands.drop(1).toTypedArray())
        }
        override val parse: Parser<Chr, Operator> = chr('-').map { _ -> this }

        override val level: Int = 1
    }
    class Mul : Operator() {
        override fun toString():String {
            return "*"
        }
        override fun evaluate(context: Context, vararg operands: Expression): Either<String, N> {
            var product = if (operands.isNotEmpty()) operands[0].evaluate(context) else Either.left("Empty Multiplication")
            for (t in operands.drop(1)) {
                product *= t.evaluate(context)
            }
            return product
        }
        override val parse: Parser<Chr, Operator> = chr('*').map { _ -> this }

        override val level: Int = 2
    }
    class Div : Operator() {
        override fun toString():String {
            return "/"
        }
        override fun evaluate(context: Context, vararg operands: Expression): Either<String, N> {
            return if (operands.isEmpty()) Either.left("Empty Division") else operands[0].evaluate(context) / Mul().evaluate(context, *operands.drop(1).toTypedArray())
        }

        override val parse: Parser<Chr, Operator> = chr('/').map { _ -> this }
        override val level: Int = 2
    }

    abstract fun evaluate(context: Context, vararg operands: Expression): Either<String, N>
    abstract val parse:Parser<Chr, Operator>
    abstract val level:Int
    operator fun <A> Either<A, N>.plus(b: Either<A, N>):Either<A, N> = this.flatMap { a -> b.map { b2 -> a+b2}}
    operator fun <A> Either<A, N>.minus(b: Either<A, N>):Either<A, N> = this.flatMap { a -> b.map { b2 -> a-b2}}
    operator fun <A> Either<A, N>.times(b: Either<A, N>):Either<A, N> = this.flatMap { a -> b.map { b2 -> a*b2}}
    operator fun <A> Either<A, N>.div(b: Either<A, N>):Either<A, N> = this.flatMap { a -> b.map { b2 -> a/b2}}
}