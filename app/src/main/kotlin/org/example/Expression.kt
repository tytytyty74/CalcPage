package org.example
import org.example.Expression.N
import org.typemeta.funcj.data.Chr
import org.typemeta.funcj.data.IList
import org.typemeta.funcj.parser.Parser
import org.typemeta.funcj.parser.Parser.choice
import org.typemeta.funcj.parser.Text.*
import java.lang.Double.NaN
import java.util.*


sealed class Expression {

    class Calculation( val op: Operator) : Expression()
    {
        lateinit var operands:Array<out Expression>
        override val numOperands: Int
            get() {
                return operands.size
            }

        constructor(op: Operator, vararg operands : Expression) : this(op) {
            if (operands.size == 2 && operands[1].numOperands == 2) {
                var newOps:Array<Expression> = Array(2) { n -> N(n) }
                var a = operands[0]
                var b = operands[1]
                if (a is N && b is Calculation && b.op.level == this.op.level) {
                    var b1 = b.operands[0]
                    var b2 = b.operands[1]
                    newOps[0] = Calculation(this.op, a, b1)
                    newOps[1] = b2
                    this.operands = newOps
                }
                else {
                    this.operands = operands
                }
            }
            else this.operands = operands
        }
        override fun evaluate(): N {
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

    open class N(var value: Double) : Expression() {
        var name: Optional<String> = Optional.empty()
        constructor(v:Double, name:String ) : this(v) {
            named(name)
        }
        open fun named(name:String) : N {
            this.name = Optional.of(name)
            variables.put(name, this)
            return this
        }
        class NWithUnit(v:Double, var u: U): N(v) {
            constructor(v:Double, u:U, s:String):this( v, u) {
                this.named(s)
            }
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
            override fun named(name:String) : NWithUnit {
                this.name = Optional.of(name)
                variables.put(name, this)
                return this
            }

            override fun toString(): String {
                return super.toString() + " $u"
            }



        }
        constructor(value:Int) : this(value.toDouble())

        override val numOperands: Int = 1

        override fun evaluate(): N {
            return this
        }
        override fun toString():String = name.map{a -> "$a: "}.orElse("")+"$value"
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

        open operator fun minus(a:N):N = N(value - a.value)
        open operator fun minus(a:Int):N = this - N(a)
        open operator fun minus(a:Double):N = this - N(a)

        open operator fun times(a:N):N = N(value * a.value)
        open operator fun times(a:Int):N = this * N(a)
        open operator fun times(a:Double):N = this * N(a)


        open operator fun div(a:N):N = N(value/a.value)
        open operator fun div(a:Int):N = this / N(a)
        open operator fun div(a:Double):N = this / N(a)

        open operator fun rem(a:N):N = N(value%a.value)
        open operator fun rem(a:Int):N = this % N(a)
        open operator fun rem(a:Double):N = this % N(a)

    }

    abstract val numOperands: Int
    abstract fun evaluate(): N


    companion object {
        var variables: MutableMap<String, N> = Hashtable()
        val d = dble.andL(ws.many())!!
        val du = d.and(U.parser.andL(ws.many()).optional())!!
        val duExpr = du.map { a, b -> if (b.isEmpty) N(a) else N.NWithUnit(a, b.get()) }!!
        fun lookup(name:String):Optional<N> = Optional.ofNullable(variables[name])
        fun addVariable(name:String, value:N) {
            variables.put(name, value)
        }
        fun dbleExpr() : Parser<Chr, N> {
            return ws.many().andR(duExpr.or(vLookup()))
        }
        fun vLookup() : Parser<Chr, N> {
            val v = mainParse.stringChoice(variables.keys.toList()).andL(ws.many())
            return v.map{a -> lookup(a).get()}!!
        }
        fun resetVars() {
            variables.clear()
        }


    }


}

abstract class Operator {
    class Add : Operator() {
        override fun toString():String {
            return "+"
        }
        override fun evaluate(vararg operands: Expression): N {
            var sum = if (operands.isNotEmpty()) operands[0].evaluate() else N(0.0)
            for (t in operands.drop(1)) {
                sum+= t.evaluate()
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
        override fun evaluate(vararg operands: Expression): N {
            return if (operands.isEmpty()) N(0.0) else operands[0].evaluate() - Add().evaluate(*operands.drop(1).toTypedArray())
        }
        override val parse: Parser<Chr, Operator> = chr('-').map { _ -> this }

        override val level: Int = 1
    }
    class Mul : Operator() {
        override fun toString():String {
            return "*"
        }
        override fun evaluate(vararg operands: Expression): N {
            var product = if (operands.isNotEmpty()) operands[0].evaluate() else N(0.0)
            for (t in operands.drop(1)) {
                product *= t.evaluate()
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
        override fun evaluate(vararg operands: Expression): N {
            return if (operands.isEmpty()) N(0.0) else operands[0].evaluate() / Mul().evaluate(*operands.drop(1).toTypedArray())
        }

        override val parse: Parser<Chr, Operator> = chr('/').map { _ -> this }
        override val level: Int = 2
    }

    abstract fun evaluate(vararg operands: Expression): N
    abstract val parse:Parser<Chr, Operator>
    abstract val level:Int
}

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
        var allUnits:ArrayList<U> = arrayListOf(Length(1.0, "Meters"), Length(1000.0, "Km"))
        val a = allUnits.map { a -> string(a.name).map { _ -> a } }
        val m = IList.ofIterable(a).nonEmptyOpt().get()
        val parser : Parser<Chr, U> = choice(m)

    }

}