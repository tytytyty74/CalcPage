package org.example.expression
import org.typemeta.funcj.control.Either
import org.typemeta.funcj.data.Chr
import org.typemeta.funcj.parser.Parser
import org.typemeta.funcj.parser.Text.*


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
        override fun evaluate(context:Context): Either<String, N> {
            return op.evaluate(context, *operands)
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



    abstract val numOperands: Int
    abstract fun evaluate(context: Context): Either<String, N>


    companion object {
        val d = dble.andL(ws.many())!!
        val du = d.and(U.parser.andL(ws.many()).optional())!!
        val varname = alpha.and(alphaNum.many()).map { a, b -> b.fold(a.toString()) { i, j -> i + j.toString() } }.andL(ws.many())
        val varExpr = varname.map { a -> Variable(a) }
        val duExpr = du.map { a, b -> if (b.isEmpty) N(a) else N.NWithUnit(a, b.get()) as Expression}!!

        val dbleExpr : Parser<Chr, Expression> = ws.many().andR(duExpr.or(varExpr))



    }


}



