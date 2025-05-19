package org.example.expression

import org.typemeta.funcj.data.Chr
import org.typemeta.funcj.data.IList
import org.typemeta.funcj.parser.Parser
import org.typemeta.funcj.parser.Parser.choice
import org.typemeta.funcj.parser.Text.string
import java.lang.Double.NaN
import java.util.ArrayList

abstract class U {
    class Length(override val conversion: Double, override val name: String) : U() {
        override fun convert(v: Double, b: U) : Double {
            return when (b) {
                // v * conversion -> A * m/A / m/B -> B from this to b
                is Length -> v * b.conversion / conversion
                else -> NaN
            }
        }

    }

    abstract val conversion: Double
    abstract val name: String
    abstract fun convert(v:Double, b:U): Double
    fun convert(v:N, b:U): N = N(convert(v.value, b))
    fun convert(v: N.NWithUnit) : N.NWithUnit = N.NWithUnit(convert(v.value, v.u), v.u)
    override fun toString(): String = name

    infix fun eq(b: U): Boolean = name == b.name

    companion object {
        var allUnits: ArrayList<U> = arrayListOf(Length(1.0, "Meters"), Length(1000.0, "Km"))
        val a = allUnits.map { a -> string(a.name).map { _ -> a } }
        val m = IList.ofIterable(a).nonEmptyOpt().get()
        val parser : Parser<Chr, U> = choice(m)

    }

}