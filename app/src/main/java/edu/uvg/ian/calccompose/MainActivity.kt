package edu.uvg.ian.calccompose
import edu.uvg.ian.calccompose.ui.theme.CalccomposeTheme
import kotlin.math.pow
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalccomposeTheme {
                CalculatorScreen()
            }
        }
    }
}

@Composable
fun CalculatorScreen() {
    var expression by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = expression,
            color = Color.White,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = result,
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        val buttons = listOf(
            listOf("AC", "r", "^", "/"),
            listOf("7", "8", "9", "*"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("0", ".", "(", ")", "=")
        )

        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                row.forEach { text ->
                    CalculatorButton(text) {
                        when (text) {
                            "=" -> {
                                try {
                                    result = Calculator.evaluate(expression).toString()
                                    expression = result
                                } catch (e: Exception) {
                                    result = "Error"
                                    expression = ""
                                }
                            }
                            "AC" -> {
                                expression = ""
                                result = "0"
                            }
                            else -> {
                                val lastChar = expression.lastOrNull()
                                if (text == "." && (lastChar == null || lastChar.isOperator() || expression.endsWith("."))) {
                                    // Do nothing, avoid multiple dots in the same number.
                                } else if (text == "0" && (expression.isEmpty() || Regex("[^0-9]").containsMatchIn(expression.takeLast(1)) || (lastChar == '0' && expression.dropLast(1).lastOrNull()?.isOperator() == true))) {

                                    // Do nothing, avoid leading zeros.
                                } else if (text != "." && text != "0") {
                                    // Remove leading zero if another digit or operator is added
                                    if (expression.endsWith('0') && expression.length > 1 && expression[expression.length - 2].isOperator()) {
                                        expression = expression.dropLast(1)
                                    }
                                    expression += text
                                } else {
                                    expression += text
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CalculatorButton(symbol: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .size(80.dp)
            .padding(4.dp)
    ) {
        Text(text = symbol, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

object Calculator {
    private fun precedence(op: Char): Int {
        return when (op) {
            '+', '-' -> 1
            '*', '/' -> 2
            '^', 'r' -> 3
            else -> -1
        }
    }

    private fun applyOp(a: Double, b: Double, op: Char): Double {
        return when (op) {
            '+' -> a + b
            '-' -> a - b
            '*' -> a * b
            '/' -> a / b
            '^' -> a.pow(b)
            'r' -> b.pow(1 / a)
            else -> throw UnsupportedOperationException("Operator not supported")
        }
    }

    fun infixToPostfix(expression: String): String {
        val result = StringBuilder()
        val stack = java.util.Stack<Char>()
        var i = 0
        while (i < expression.length) {
            val c = expression[i]
            if (c.isDigit() || c == '.') {
                while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
                    result.append(expression[i++])
                }
                result.append(' ')
                i--
            } else if (c == '(') {
                stack.push(c)
            } else if (c == ')') {
                while (stack.isNotEmpty() && stack.peek() != '(') {
                    result.append(stack.pop()).append(' ')
                }
                stack.pop()
            } else if (c == '+' || c == '-' || c == '*' || c == '/' || c == '^' || c == 'r') {
                while (stack.isNotEmpty() && precedence(stack.peek()) >= precedence(c)) {
                    result.append(stack.pop()).append(' ')
                }
                stack.push(c)
            }
            i++
        }
        while (stack.isNotEmpty()) {
            result.append(stack.pop()).append(' ')
        }
        return result.toString()
    }

    fun evaluatePostfix(expression: String): Double {
        val stack = java.util.Stack<Double>()
        val tokens = expression.split(" ")
        for (token in tokens) {
            if (token.isEmpty()) continue
            if (token[0].isDigit() || token[0] == '.') {
                stack.push(token.toDouble())
            } else {
                val b = stack.pop()
                val a = if (stack.isNotEmpty() && token[0] != 'e') stack.pop() else 0.0
                stack.push(applyOp(a, b, token[0]))
            }
        }
        return stack.pop()
    }

    fun evaluate(expression: String): Double {
        return evaluatePostfix(infixToPostfix(expression))
    }
}

private fun Char?.isOperator(): Boolean {
    return this == '+' || this == '-' || this == '*' || this == '/' || this == '^' || this == 'r'
}
