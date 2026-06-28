package com.badaabdulrahaman.calculatorapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvDisplay: TextView
    private lateinit var tvExpression: TextView
    private var firstNumber = 0.0
    private var secondNumber = 0.0
    private var currentOperator = ""
    private var isNewOperation = true
    private var isScientificMode = false
    private var isDegreeMode = true
    private var pendingOperation = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvDisplay = findViewById(R.id.tvDisplay)
        tvExpression = findViewById(R.id.tvExpression)

        // Number buttons
        val numIds = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3,
            R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7,
            R.id.btn8, R.id.btn9
        )
        for (id in numIds) {
            findViewById<Button>(id).setOnClickListener {
                val digit = (it as Button).text.toString()
                if (isNewOperation || tvDisplay.text.toString() == "0") {
                    tvDisplay.text = digit
                    isNewOperation = false
                } else {
                    tvDisplay.append(digit)
                }
            }
        }

        // Basic operations
        findViewById<Button>(R.id.btnDecimal).setOnClickListener {
            if (!tvDisplay.text.contains(".")) tvDisplay.append(".")
        }
        findViewById<Button>(R.id.btnAdd).setOnClickListener { setOperator("+") }
        findViewById<Button>(R.id.btnSubtract).setOnClickListener { setOperator("−") }
        findViewById<Button>(R.id.btnMultiply).setOnClickListener { setOperator("×") }
        findViewById<Button>(R.id.btnDivide).setOnClickListener { setOperator("÷") }

        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            if (pendingOperation.isNotEmpty()) {
                secondNumber = tvDisplay.text.toString().toDouble()
                val result = performPendingOp(firstNumber, secondNumber, pendingOperation)
                displayResult(result)
                pendingOperation = ""
                tvExpression.text = ""
            } else {
                secondNumber = tvDisplay.text.toString().toDouble()
                val result = when (currentOperator) {
                    "+" -> firstNumber + secondNumber
                    "−" -> firstNumber - secondNumber
                    "×" -> firstNumber * secondNumber
                    "÷" -> if (secondNumber != 0.0) firstNumber / secondNumber
                    else { showError("Cannot divide by zero"); return@setOnClickListener }
                    else -> return@setOnClickListener
                }
                tvExpression.text = "$firstNumber $currentOperator $secondNumber ="
                displayResult(result)
            }
            isNewOperation = true
        }

        findViewById<Button>(R.id.btnClear).setOnClickListener {
            tvDisplay.text = "0"
            tvExpression.text = ""
            firstNumber = 0.0; secondNumber = 0.0
            currentOperator = ""; pendingOperation = ""
            isNewOperation = true
        }

        findViewById<Button>(R.id.btnNegate).setOnClickListener {
            val value = tvDisplay.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            displayResult(value * -1)
        }

        findViewById<Button>(R.id.btnPercent).setOnClickListener {
            val value = tvDisplay.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            displayResult(value / 100)
        }

        // Mode toggle
        findViewById<Button>(R.id.btnBasicMode).setOnClickListener {
            isScientificMode = false
            findViewById<ScrollView>(R.id.scientificPanel).visibility = View.GONE
            it.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFFF9F0A.toInt())
            findViewById<Button>(R.id.btnSciMode).backgroundTintList =
                android.content.res.ColorStateList.valueOf(0xFF3A3A3C.toInt())
        }

        findViewById<Button>(R.id.btnSciMode).setOnClickListener {
            isScientificMode = true
            findViewById<ScrollView>(R.id.scientificPanel).visibility = View.VISIBLE
            it.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFFF9F0A.toInt())
            findViewById<Button>(R.id.btnBasicMode).backgroundTintList =
                android.content.res.ColorStateList.valueOf(0xFF3A3A3C.toInt())
        }

        // Degree/Radian toggle
        findViewById<Button>(R.id.btnDeg).setOnClickListener {
            isDegreeMode = true
            Toast.makeText(this, "Degree Mode", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnRad).setOnClickListener {
            isDegreeMode = false
            Toast.makeText(this, "Radian Mode", Toast.LENGTH_SHORT).show()
        }

        // Trig functions
        findViewById<Button>(R.id.btnSin).setOnClickListener {
            val v = getAngleInRadians()
            tvExpression.text = "sin(${tvDisplay.text}°)"
            displayResult(sin(v))
        }
        findViewById<Button>(R.id.btnCos).setOnClickListener {
            val v = getAngleInRadians()
            tvExpression.text = "cos(${tvDisplay.text})"
            displayResult(cos(v))
        }
        findViewById<Button>(R.id.btnTan).setOnClickListener {
            val v = getAngleInRadians()
            tvExpression.text = "tan(${tvDisplay.text})"
            displayResult(tan(v))
        }
        findViewById<Button>(R.id.btnAsin).setOnClickListener {
            val v = tvDisplay.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            val result = Math.toDegrees(asin(v))
            tvExpression.text = "sin⁻¹(${tvDisplay.text})"
            displayResult(result)
        }
        findViewById<Button>(R.id.btnAcos).setOnClickListener {
            val v = tvDisplay.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            val result = Math.toDegrees(acos(v))
            tvExpression.text = "cos⁻¹(${tvDisplay.text})"
            displayResult(result)
        }
        findViewById<Button>(R.id.btnAtan).setOnClickListener {
            val v = tvDisplay.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            val result = Math.toDegrees(atan(v))
            tvExpression.text = "tan⁻¹(${tvDisplay.text})"
            displayResult(result)
        }

        // Hyperbolic functions
        findViewById<Button>(R.id.btnSinh).setOnClickListener {
            val v = tvDisplay.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            tvExpression.text = "sinh(${tvDisplay.text})"
            displayResult(sinh(v))
        }
        findViewById<Button>(R.id.btnCosh).setOnClickListener {
            val v = tvDisplay.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            tvExpression.text = "cosh(${tvDisplay.text})"
            displayResult(cosh(v))
        }
        findViewById<Button>(R.id.btnTanh).setOnClickListener {
            val v = tvDisplay.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            tvExpression.text = "tanh(${tvDisplay.text})"
            displayResult(tanh(v))
        }

        // Math functions
        findViewById<Button>(R.id.btnLog).setOnClickListener {
            val v = tvDisplay.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            tvExpression.text = "log(${tvDisplay.text})"
            displayResult(log10(v))
        }
        findViewById<Button>(R.id.btnLn).setOnClickListener {
            val v = tvDisplay.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            tvExpression.text = "ln(${tvDisplay.text})"
            displayResult(ln(v))
        }
        findViewById<Button>(R.id.btnSqrt).setOnClickListener {
            val v = tvDisplay.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            tvExpression.text = "√(${tvDisplay.text})"
            displayResult(sqrt(v))
        }
        findViewById<Button>(R.id.btnSquare).setOnClickListener {
            val v = tvDisplay.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            tvExpression.text = "(${tvDisplay.text})²"
            displayResult(v.pow(2))
        }
        findViewById<Button>(R.id.btnPow).setOnClickListener {
            firstNumber = tvDisplay.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            pendingOperation = "pow"
            tvExpression.text = "${tvDisplay.text} ^"
            isNewOperation = true
        }
        findViewById<Button>(R.id.btnAbs).setOnClickListener {
            val v = tvDisplay.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            tvExpression.text = "|${tvDisplay.text}|"
            displayResult(abs(v))
        }
        findViewById<Button>(R.id.btnInverse).setOnClickListener {
            val v = tvDisplay.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            if (v == 0.0) { showError("Cannot divide by zero"); return@setOnClickListener }
            tvExpression.text = "1/${tvDisplay.text}"
            displayResult(1.0 / v)
        }

        // Constants
        findViewById<Button>(R.id.btnPi).setOnClickListener {
            tvDisplay.text = Math.PI.toString()
            isNewOperation = false
        }
        findViewById<Button>(R.id.btnE).setOnClickListener {
            tvDisplay.text = Math.E.toString()
            isNewOperation = false
        }

        // Factorial
        findViewById<Button>(R.id.btnFactorial).setOnClickListener {
            val v = tvDisplay.text.toString().toDoubleOrNull()?.toInt() ?: return@setOnClickListener
            tvExpression.text = "${v}!"
            displayResult(factorial(v).toDouble())
        }

        // Permutation nPr
        findViewById<Button>(R.id.btnPerm).setOnClickListener {
            firstNumber = tvDisplay.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            pendingOperation = "perm"
            tvExpression.text = "${tvDisplay.text} P"
            isNewOperation = true
            Toast.makeText(this, "Enter r value", Toast.LENGTH_SHORT).show()
        }

        // Combination nCr
        findViewById<Button>(R.id.btnComb).setOnClickListener {
            firstNumber = tvDisplay.text.toString().toDoubleOrNull() ?: return@setOnClickListener
            pendingOperation = "comb"
            tvExpression.text = "${tvDisplay.text} C"
            isNewOperation = true
            Toast.makeText(this, "Enter r value", Toast.LENGTH_SHORT).show()
        }

        // Matrix
        findViewById<Button>(R.id.btnMatrix).setOnClickListener {
            showMatrixDialog()
        }
    }

    private fun getAngleInRadians(): Double {
        val v = tvDisplay.text.toString().toDoubleOrNull() ?: 0.0
        return if (isDegreeMode) Math.toRadians(v) else v
    }

    private fun setOperator(op: String) {
        firstNumber = tvDisplay.text.toString().toDoubleOrNull() ?: 0.0
        currentOperator = op
        tvExpression.text = "$firstNumber $op"
        isNewOperation = true
    }

    private fun performPendingOp(n: Double, r: Double, op: String): Double {
        return when (op) {
            "pow" -> n.pow(r)
            "perm" -> permutation(n.toInt(), r.toInt()).toDouble()
            "comb" -> combination(n.toInt(), r.toInt()).toDouble()
            else -> 0.0
        }
    }

    private fun displayResult(result: Double) {
        tvDisplay.text = if (result == result.toLong().toDouble() && !result.isInfinite())
            result.toLong().toString()
        else
            "%.6f".format(result).trimEnd('0').trimEnd('.')
        isNewOperation = true
    }

    private fun factorial(n: Int): Long {
        if (n < 0) return -1
        if (n == 0 || n == 1) return 1
        return n * factorial(n - 1)
    }

    private fun permutation(n: Int, r: Int): Long {
        if (r > n) return 0
        return factorial(n) / factorial(n - r)
    }

    private fun combination(n: Int, r: Int): Long {
        if (r > n) return 0
        return factorial(n) / (factorial(r) * factorial(n - r))
    }

    private fun showError(msg: String) {
        tvDisplay.text = "Error"
        tvExpression.text = msg
        isNewOperation = true
    }

    private fun showMatrixDialog() {
        val sizes = arrayOf("2×2 Matrix", "3×3 Matrix", "2×2 Determinant", "3×3 Determinant")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Matrix Operations")
        builder.setItems(sizes) { _, which ->
            when (which) {
                0 -> show2x2MatrixInput()
                1 -> show3x3MatrixInput()
                2 -> show2x2DeterminantInput()
                3 -> show3x3DeterminantInput()
            }
        }
        builder.show()
    }

    private fun show2x2MatrixInput() {
        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)
        val label = TextView(this)
        label.text = "Enter 2×2 Matrix A elements (a,b,c,d):"
        layout.addView(label)
        val inputA = android.widget.EditText(this)
        inputA.hint = "a11,a12,a21,a22"
        inputA.inputType = android.text.InputType.TYPE_CLASS_TEXT
        layout.addView(inputA)
        val label2 = TextView(this)
        label2.text = "Enter 2×2 Matrix B elements:"
        layout.addView(label2)
        val inputB = android.widget.EditText(this)
        inputB.hint = "b11,b12,b21,b22"
        inputB.inputType = android.text.InputType.TYPE_CLASS_TEXT
        layout.addView(inputB)

        android.app.AlertDialog.Builder(this)
            .setTitle("2×2 Matrix Multiplication")
            .setView(layout)
            .setPositiveButton("Calculate") { _, _ ->
                try {
                    val a = inputA.text.toString().split(",").map { it.trim().toDouble() }
                    val b = inputB.text.toString().split(",").map { it.trim().toDouble() }
                    if (a.size == 4 && b.size == 4) {
                        val r00 = a[0]*b[0] + a[1]*b[2]
                        val r01 = a[0]*b[1] + a[1]*b[3]
                        val r10 = a[2]*b[0] + a[3]*b[2]
                        val r11 = a[2]*b[1] + a[3]*b[3]
                        val result = "Result:\n[$r00, $r01]\n[$r10, $r11]"
                        tvExpression.text = "2×2 Matrix A×B"
                        tvDisplay.text = r00.toString()
                        android.app.AlertDialog.Builder(this)
                            .setTitle("Matrix Result")
                            .setMessage(result)
                            .setPositiveButton("OK", null)
                            .show()
                    }
                } catch (e: Exception) { showError("Invalid input") }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun show3x3MatrixInput() {
        val layout = android.widget.LinearLayout(this)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 20, 50, 20)
        val inputA = android.widget.EditText(this)
        inputA.hint = "a11,a12,a13,a21,a22,a23,a31,a32,a33"
        layout.addView(TextView(this).apply { text = "Matrix A (9 values):" })
        layout.addView(inputA)
        val inputB = android.widget.EditText(this)
        inputB.hint = "b11,b12,b13,b21,b22,b23,b31,b32,b33"
        layout.addView(TextView(this).apply { text = "Matrix B (9 values):" })
        layout.addView(inputB)

        android.app.AlertDialog.Builder(this)
            .setTitle("3×3 Matrix Multiplication")
            .setView(layout)
            .setPositiveButton("Calculate") { _, _ ->
                try {
                    val a = inputA.text.toString().split(",").map { it.trim().toDouble() }
                    val b = inputB.text.toString().split(",").map { it.trim().toDouble() }
                    if (a.size == 9 && b.size == 9) {
                        val r = Array(3) { i -> DoubleArray(3) { j ->
                            (0..2).sumOf { k -> a[i*3+k] * b[k*3+j] }
                        }}
                        val result = "Result:\n[${r[0][0]}, ${r[0][1]}, ${r[0][2]}]\n" +
                                "[${r[1][0]}, ${r[1][1]}, ${r[1][2]}]\n" +
                                "[${r[2][0]}, ${r[2][1]}, ${r[2][2]}]"
                        android.app.AlertDialog.Builder(this)
                            .setTitle("3×3 Matrix Result")
                            .setMessage(result)
                            .setPositiveButton("OK", null)
                            .show()
                    }
                } catch (e: Exception) { showError("Invalid input") }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun show2x2DeterminantInput() {
        val input = android.widget.EditText(this)
        input.hint = "a,b,c,d"
        android.app.AlertDialog.Builder(this)
            .setTitle("2×2 Determinant\n[a,b]\n[c,d]")
            .setView(input)
            .setPositiveButton("Calculate") { _, _ ->
                try {
                    val v = input.text.toString().split(",").map { it.trim().toDouble() }
                    val det = v[0]*v[3] - v[1]*v[2]
                    tvExpression.text = "det(2×2)"
                    displayResult(det)
                } catch (e: Exception) { showError("Invalid input") }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun show3x3DeterminantInput() {
        val input = android.widget.EditText(this)
        input.hint = "a,b,c,d,e,f,g,h,i"
        android.app.AlertDialog.Builder(this)
            .setTitle("3×3 Determinant (9 values)")
            .setView(input)
            .setPositiveButton("Calculate") { _, _ ->
                try {
                    val v = input.text.toString().split(",").map { it.trim().toDouble() }
                    val det = v[0]*(v[4]*v[8]-v[5]*v[7]) -
                            v[1]*(v[3]*v[8]-v[5]*v[6]) +
                            v[2]*(v[3]*v[7]-v[4]*v[6])
                    tvExpression.text = "det(3×3)"
                    displayResult(det)
                } catch (e: Exception) { showError("Invalid input") }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}