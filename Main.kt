package calculator

import java.math.BigInteger

fun main() {
    val variables: MutableMap<String, BigInteger> = mutableMapOf()
    while (true) {
        try {
            val st = readLine()
            when {
                st == null -> continue
                st == "/exit" -> {
                    println("Bye!")
                    return
                }
                st == "/help" -> {
                    println("The program calculates / * + - ^ of numbers")
                }
                st.isEmpty() -> continue
                st[0] == '/' -> throw Exception("Unknown command")
                st.contains("=") -> variables(st, variables)
                st.trim().matches("[a-zA-Z]*".toRegex()) -> println(replaceVariables(st.trim(), variables))
                else -> {
                    calculator(st, variables)
                }
            }
        }
        catch (e: Exception){
            println(e.message)
        }
    }


}

fun replaceVariables(st: String, variables: MutableMap<String, BigInteger>): BigInteger {
    return variables[st] ?: throw Exception("Unknown variable")
}// replace variable with it's value

private fun variables(st: String, variables: MutableMap<String, BigInteger>){
    when {
        st.trim().matches("[a-zA-Z]*\\s*?=\\s*?-?[0-9]*".toRegex()) -> {
            val assign: MutableList<String> = st.trim().split("=").toMutableList()
            variables[assign[0].trim()] = assign.last().trim().toBigInteger()
        }
        st.trim().matches("[a-zA-Z]*\\s*?=\\s*?[a-zA-Z]*\\s*?".toRegex()) -> {
            val assign: MutableList<String> = st.trim().split("=").toMutableList()
            variables[assign[0].trim()] =  replaceVariables(assign.last().trim(), variables)
        }
        st.trim().matches("[a-zA-Z]*\\s*?=.*?".toRegex()) -> throw Exception("Invalid assignment")
        else -> throw Exception("Invalid identifier")
    }
} // assigns variable

private fun calculator(st: String, variables: MutableMap<String, BigInteger>) { //gets string to calculate, can contain illegal things
    val inputList: MutableList<String> = mutableListOf()

    var chIndex = 0
    while (chIndex in st.indices){                                //creates input list and
        chIndex = readNextChar(chIndex, st, inputList, variables) //returns ind where it stoped
    }
        println(result(inputList.toPostfixNotation()))  //to postfix notation then calculating result

//TODO throw exception for 1+ 1-

}

private fun result(postfix: MutableList<String>): String {
    val tempStack: MutableList<String> = mutableListOf()
    for (ind in postfix.indices) {
        when {
            postfix[ind].matches("[0-9]*".toRegex()) -> {
                tempStack += postfix[ind]
            }
            postfix[ind].matches("[*/^+-]".toRegex()) -> {
                if (tempStack.isEmpty()) {
                    throw Exception("Illegal")
                }
                val num2 = tempStack.last()
                tempStack.removeLast()


                if (tempStack.isEmpty() && num2.toBigIntegerOrNull() != null && postfix[ind].matches("[+-]*".toRegex())) {
                    tempStack += (num2.toBigInteger() * (if (postfix[ind] == "-") - BigInteger.ONE else BigInteger.ONE)).toString()
                } else {
                    if (tempStack.isEmpty() && num2.toBigIntegerOrNull() == null) {
                        throw Exception("Illegal")
                    }
                    val num1 = tempStack.last()
                    tempStack.removeLast()
                    tempStack += operation(num1, num2, postfix[ind])
                }
            }

        }

    }
    return tempStack.last()
}

fun operation(num1: String, num2: String, operator: String): String {
    return when (operator){
        "/" -> (num1.toBigInteger() / num2.toBigInteger()).toString()
        "*" -> (num1.toBigInteger() * num2.toBigInteger()).toString()
        "+" -> (num1.toBigInteger() + num2.toBigInteger()).toString()
        "-" -> (num1.toBigInteger() - num2.toBigInteger()).toString()
        "^" -> num1.toBigDecimal().pow(num2.toInt()).toBigInteger().toString()
        else -> throw Exception("Something else")
    }
}

fun readNextChar(chIndex: Int, st: String, inputList: MutableList<String>, variables: MutableMap<String, BigInteger>): Int {
    var tempString =""
    var currentIndex = chIndex
    when {
        st[currentIndex] in '0'..'9' -> {
            while (currentIndex <= st.lastIndex && st[currentIndex] in '0'..'9'){
                tempString += st[currentIndex]
                currentIndex++
            }
            inputList += tempString
            return currentIndex
        }

        st[currentIndex] in 'a'..'z' || st[currentIndex] in 'A'..'Z'  -> {
            while (currentIndex <= st.lastIndex && (st[currentIndex] in 'a'..'z' || st[currentIndex] in 'A'..'Z')){
                tempString += st[currentIndex]
                currentIndex++
            }
            val temp = replaceVariables(tempString, variables)
            if (temp < BigInteger.ZERO) {
                inputList += "-"
                inputList += ((-BigInteger.ONE) * temp).toString()
            }
            else {
                inputList += temp.toString()
            }
            return currentIndex
        }

        st[currentIndex] == '*' || st[currentIndex] == '/'|| st[currentIndex] == '^' -> {
            while (currentIndex <= st.lastIndex) {
                if (st[currentIndex + 1] == ' '|| st[currentIndex + 1] in 'a'..'z'|| st[currentIndex + 1] in '0'..'9' || st[currentIndex + 1] == '(' ||st[currentIndex + 1] == ')'){
                    tempString += st[currentIndex]
                    currentIndex++
                    inputList += tempString
                    return currentIndex
                }
                if (st[currentIndex + 1] == '*' || st[currentIndex + 1] == '/'|| st[currentIndex + 1] == '^' ){
                    throw java.lang.Exception("Invalid expression")
                }

            }
            return currentIndex
        }

        st[currentIndex] == '+' || st[currentIndex] == '-' -> {
            var tempSign = 1
            while (currentIndex <= st.lastIndex &&( st[currentIndex] == '+' || st[currentIndex] == '-' )){
                if (st[currentIndex] == '-'){
                    tempSign *= (-1)
                }
                currentIndex++
            }
            tempString += if (tempSign > 0) "+" else "-"
            inputList += tempString
            return currentIndex
        }

        st[currentIndex] == '(' || st[currentIndex] == ')' -> {
            inputList += st[currentIndex].toString()
            return currentIndex + 1
        }
        st[chIndex] == ' ' -> {
            return chIndex + 1
        }
    }
    return chIndex + 1
}

fun MutableList<String>.toPostfixNotation(): MutableList<String> {
    val inputList = this
    val queue: MutableList<String> = mutableListOf()
    val stack: MutableList<String> = mutableListOf()
    for (elementInd in inputList.indices){
        when {

            (inputList[elementInd].toBigIntegerOrNull() != null) -> {
                queue += inputList[elementInd]
            }
            (inputList[elementInd].matches("[*/^+-]".toRegex())) -> {
                while (stack.isNotEmpty() && stack.last().matches("[*/^+-]".toRegex()) && (Operators(stack.last()) >= Operators(inputList[elementInd]))){
                    queue += stack.last()
                    stack.removeLast()
                }
                stack += inputList[elementInd]
            }
            (inputList[elementInd] == "(") -> {
                stack += inputList[elementInd]
            }
            (inputList[elementInd] == ")") -> {
                while (stack.last() != "("){
                    queue += stack.last()
                    stack.removeLast()
                    if (stack.isEmpty()) throw Exception("Invalid expression")
                }
                stack.removeLast()
            }
        }
    }
    while (stack.isNotEmpty()) {
        queue += stack.last()
        stack.removeLast()
    }
    if (queue.contains("(")) throw Exception("Invalid expression")
//    println(queue)
    return queue
}

class Operators(private val st: String): Comparable<Operators>{
    override fun compareTo(other: Operators): Int {
        when {
            st == other.st -> return 0
//            (st == "+" || st == "-" ) && (other.st == "+" || other.st == "-" ) -> return 0
            (st == "*" || st == "/" ) && (other.st == "/" || other.st == "*" ) -> return 0
            st == "^" -> return 1
            other.st == "^" -> return -1
            st == "*" -> return 1
            other.st == "*" -> return -1
            st == "/" -> return 1
            other.st == "/" -> return -1
            st == "-" -> return 1
            other.st == "-" -> return -1
            st == "+" -> return 1
            other.st == "+" -> return -1
            else -> throw Exception("WHAT?")
        }
    }
}