package org.example.expression
import org.typemeta.funcj.control.Either
import java.lang.Double.NaN

open class N(var value: Double) : Expression() {
    class NWithUnit(v:Double, var u: U): N(v) {
        override fun plus(a: N):N {
            return when (a) {
                is NWithUnit -> if (a.u eq u) {
                    NWithUnit(value + a.value, u)

                } else {
                    if (a.u.javaClass == u.javaClass) {
                        NWithUnit(value + u.convert(a).value, u)

                    } else N(NaN)
                }
                else -> NWithUnit(value + a.value, u)
            }
        }

        override fun toString(): String {
            return super.toString() + " $u"
        }



    }
    constructor(value:Int) : this(value.toDouble())

    override val numOperands: Int = 1

    override fun evaluate(context: Context): Either<String, N> {
        return Either.right(this)
    }
    override fun toString():String = "$value"
    operator fun unaryPlus() {
        value = +value
    }

    operator fun unaryMinus() {
        value = -value
    }

    operator fun inc():N {
        return N(value++)
    }
    operator fun dec():N {
        return N(value--)
    }

    open operator fun plus(a:N):N = N(value+a.value)
    open operator fun plus(a:Int):N = this + N(a)
    open operator fun plus(a:Double):N = this + N(a)
    open operator fun<T> plus(a: Either<T, N>):Either<T, N> = a.map{x -> this + x}

    open operator fun minus(a:N):N = N(value - a.value)
    open operator fun minus(a:Int):N = this - N(a)
    open operator fun minus(a:Double):N = this - N(a)
    open operator fun<T> minus(a: Either<T, N>):Either<T, N> = a.map{x -> this - x}

    open operator fun times(a:N):N = N(value * a.value)
    open operator fun times(a:Int):N = this * N(a)
    open operator fun times(a:Double):N = this * N(a)
    open operator fun<T> times(a: Either<T, N>):Either<T, N> = a.map{x -> this - x}


    open operator fun div(a:N):N = N(value/a.value)
    open operator fun div(a:Int):N = this / N(a)
    open operator fun div(a:Double):N = this / N(a)
    open operator fun<T> div(a: Either<T, N>):Either<T, N> = a.map{x -> this - x}

    open operator fun rem(a:N):N = N(value%a.value)
    open operator fun rem(a:Int):N = this % N(a)
    open operator fun rem(a:Double):N = this % N(a)

}
