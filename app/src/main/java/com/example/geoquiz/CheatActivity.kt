package com.example.geoquiz

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

private const val EXTRA_ANSWER_IS_TRUE = "com.example.geoquiz.answer_is_true"
private const val KEY_IS_ANSWER_SHOWN = "isAnswerShow"
const val EXTRA_ANSWER_SHOW = "com.example.geoquiz.answer_shown"
const val ATTEMP_COUNT = "attempCount"

class CheatActivity : AppCompatActivity() {

    private var answerIsTrue = false
    private var isAnswerShow = false
    private var attempCount = 3
    private lateinit var answerTextView: TextView
    private lateinit var showAnswerButton: Button
    private lateinit var apiTextView: TextView
    private lateinit var attempTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_cheat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Сохранить значение из дополнения (при переходе)
        answerIsTrue = intent.getBooleanExtra(EXTRA_ANSWER_IS_TRUE, false)
        attempCount = intent.getIntExtra(ATTEMP_COUNT, 3)


        answerTextView = findViewById(R.id.answer_text_view)
        showAnswerButton = findViewById(R.id.show_answer_button)

        apiTextView = findViewById(R.id.api_text_view)
        apiTextView.text = getString(R.string.api_info, Build.VERSION.SDK_INT)

        attempTextView = findViewById(R.id.attemps_text_view)
        attempTextView.text = getString(R.string.attemps_left, attempCount)

        if(attempCount < 1)
            showAnswerButton.isEnabled = false


        // Состояние было сохранено, то восстанавливаем  его в переменную "показанЛиОтвет"
        if(savedInstanceState != null) {
            isAnswerShow = savedInstanceState.getBoolean(KEY_IS_ANSWER_SHOWN, false) //intent.getBooleanExtra
            if(isAnswerShow) {
                setAnswerShownResult(true)
                Toast.makeText(this@CheatActivity,  "Answer was shown", Toast.LENGTH_SHORT).show()
            }
            else {
                Toast.makeText(this@CheatActivity,  "Answer wasn`t shown", Toast.LENGTH_SHORT).show()
            }

            attempCount = savedInstanceState.getInt(ATTEMP_COUNT, 3)
            attempTextView.text = getString(R.string.attemps_left, attempCount)
        }


        showAnswerButton.setOnClickListener {
            val answerText = when {
                answerIsTrue -> R.string.true_button
                else -> R.string.false_button
            }

            attempCount--
            attempTextView.text = getString(R.string.attemps_left, attempCount)

            if(attempCount == 0)
                showAnswerButton.isEnabled = false


            answerTextView.setText(answerText)
            setAnswerShownResult(true)
            isAnswerShow = true
        }
    }


    // Передаем константу и ключ дополнения в Main activity
    private fun setAnswerShownResult(isAnswerShown: Boolean){
        val data = Intent().apply{
            putExtra(EXTRA_ANSWER_SHOW, isAnswerShown)
            putExtra(ATTEMP_COUNT, attempCount)
        }
        setResult(Activity.RESULT_OK, data)
    }


    // Для добавки других данных - дополни число аргументов newIntent)
    companion object{
        fun newIntent(packageContext: Context, answerIsTrue: Boolean, attemps: Int): Intent {
            return Intent(packageContext, CheatActivity::class.java).apply {
                putExtra(EXTRA_ANSWER_IS_TRUE, answerIsTrue)
                putExtra(ATTEMP_COUNT, attemps)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_IS_ANSWER_SHOWN, isAnswerShow)
        outState.putInt(ATTEMP_COUNT, attempCount)
    }

//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putInt(RESULT_CODE)
//
//    }

}