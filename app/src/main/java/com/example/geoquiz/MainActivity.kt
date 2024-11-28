package com.example.geoquiz

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProviders


private const val TAG = "MainActivity"
private const val REQUEST_CODE_CHEAT = 0
private const val KEY_INDEX = "index"       //Сохранение состояния интерфейса


class MainActivity : AppCompatActivity() {

    private lateinit var trueButton: Button
    private lateinit var falseButton: Button
    private lateinit var nextButton: ImageButton
    private lateinit var questionTextView: TextView
    private lateinit var previewButton: ImageButton
    private lateinit var cheatButton: Button

    private val quizViewModel: QuizViewModel by lazy {
        ViewModelProviders.of(this).get(QuizViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate(Bundle?) called")
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val currentIndex = savedInstanceState?.getInt(KEY_INDEX, 0) ?: 0
        quizViewModel.currentIndex = currentIndex


        //val provider: ViewModelProvider = ViewModelProviders.of(this)        //Цепляемся к MainActivity
        //val quizViewModel = provider.get(QuizViewModel::class.java)          // С другой стороны наш ViewModel
        //Log.d(TAG, "Got a QuizViewModel: ${quizViewModel}")


        trueButton = findViewById(R.id.true_button)
        falseButton = findViewById(R.id.false_button)
        nextButton = findViewById(R.id.next_button)
        previewButton = findViewById(R.id.preview_button)
        questionTextView = findViewById(R.id.questionTextView)
        cheatButton = findViewById(R.id.cheat_button)

        updateQuestion()
        if(quizViewModel.sessionUser.answers[quizViewModel.currentIndex] != null)   // Кнопки вспоминают состояние после поворота
            blockButtons(true)

        trueButton.setOnClickListener{ view: View ->
            checkAnswer(true)
            blockButtons(true)
        }

        falseButton.setOnClickListener{ view: View ->
            checkAnswer(false)
            blockButtons(true)
        }

        nextButton.setOnClickListener{

            if(quizViewModel.currentIndex == quizViewModel.currentQuestionSize - 1 ) {
                var counterCheat = 0

                for(everyone in quizViewModel.answersWithCheat){
                    if(everyone == true)
                        counterCheat++
                }


                Toast.makeText(this, "Result of test:\n" +
                        "${quizViewModel.sessionUser.trueAnswer} / ${quizViewModel.currentQuestionSize}\n" +
                        "Cheating ${counterCheat} / ${quizViewModel.currentQuestionSize}", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            quizViewModel.moveToNext()
            updateQuestion()

            if(quizViewModel.sessionUser.answers[quizViewModel.currentIndex] != null) //Если на вопрос отвечали, то блокируем кнопки
                blockButtons(true)
            else
                blockButtons(false)
        }

        previewButton.setOnClickListener{

            quizViewModel.currentIndex = if(quizViewModel.currentIndex != 0)
                quizViewModel.currentIndex-1
            else
                quizViewModel.currentQuestionSize - 1

            updateQuestion()

            if(quizViewModel.sessionUser.answers[quizViewModel.currentIndex] != null) //Если на вопрос отвечали, то блокируем кнопки
                blockButtons(true)
            else
                blockButtons(false)
        }

        cheatButton.setOnClickListener { view ->
            val answerIsTrue = quizViewModel.currentQuestionAnswer
            val intent = CheatActivity.newIntent(this@MainActivity, answerIsTrue, quizViewModel.attempsQVM)

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val options =
                    ActivityOptions.makeClipRevealAnimation(view, 0, 0, view.width, view.height)

                startActivityForResult(intent, REQUEST_CODE_CHEAT, options.toBundle())
            }
            else {
                startActivityForResult(intent, REQUEST_CODE_CHEAT)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode != Activity.RESULT_OK)
            return

        //TODO если уже читерил, то не перезапишется
        if(requestCode == REQUEST_CODE_CHEAT && quizViewModel.answersWithCheat[quizViewModel.currentIndex] != true){
            quizViewModel.answersWithCheat[quizViewModel.currentIndex] =
                data?.getBooleanExtra(EXTRA_ANSWER_SHOW, false) ?: false
            quizViewModel.attempsQVM = data?.getIntExtra(ATTEMP_COUNT, 3) ?: 3
        }
    }


    private fun updateQuestion(){
        val questionTextResId = quizViewModel.currentQuestionText      //Сохраняем текущий идентификатор вопроса
        questionTextView.setText(questionTextResId)                    //Устанавливаем текст вопроса в TV
    }


    private fun checkAnswer(userAnswer: Boolean) {
        if(userAnswer)
            quizViewModel.sessionUser.answers.add(quizViewModel.currentIndex, true) // Пользователь ответил "true"
        else
            quizViewModel.sessionUser.answers.add(quizViewModel.currentIndex, false) // Пользователь ответил "false"

        val correctAnswer = quizViewModel.questionBank[quizViewModel.currentIndex].answer

        val messageResId = when {
            quizViewModel.answersWithCheat[quizViewModel.currentIndex] == true -> R.string.judgment_toast
            userAnswer == correctAnswer -> R.string.correct_toast
            else -> R.string.incorrect_toast
        }

        if(userAnswer == correctAnswer){
            quizViewModel.sessionUser.trueAnswer++       // Увеличиваем счетчик правильных ответов
        }

        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()
    }


    private fun blockButtons(block: Boolean){
        if(block){
            trueButton.isEnabled = false
            falseButton.isEnabled = false

            trueButton.setBackgroundColor(Color.WHITE)
            falseButton.setBackgroundColor(Color.WHITE)
        }
        else{
            trueButton.isEnabled = true
            falseButton.isEnabled = true

            trueButton.setBackgroundColor(Color.parseColor("#6750A3"))
            falseButton.setBackgroundColor(Color.parseColor("#6750A3"))
        }

    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.i(TAG, "onSavedInstanceState")
        outState.putInt(KEY_INDEX,  quizViewModel.currentIndex)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }
}





