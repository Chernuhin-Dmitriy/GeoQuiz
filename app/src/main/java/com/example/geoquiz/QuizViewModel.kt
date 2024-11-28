package com.example.geoquiz

import androidx.lifecycle.ViewModel

private const val TAG = "QuizViewModel"

class QuizViewModel : ViewModel(){
//    init {
//        Log.d(TAG, "ViewModel instanse created")
//    }

    var currentIndex = 0
    var isCheater = false
    var attempsQVM = 3

    val questionBank = listOf(
        Question(R.string.question_asia, true),
        Question(R.string.question_africa, false),
        Question(R.string.question_americas, true),
        Question(R.string.question_oceans, true),
        Question(R.string.question_australia, true),
        Question(R.string.question_mideast, false)
    )

    var answersWithCheat = Array<Boolean?>(questionBank.size) {null}
    var sessionUser: UserAnswer = UserAnswer(MutableList(currentQuestionSize){null}, 0)


    val currentQuestionText: Int
        get() = questionBank[currentIndex].textResId

    val currentQuestionSize: Int
        get() = questionBank.size

    val currentQuestionAnswer: Boolean
        get() = questionBank[currentIndex].answer



    fun moveToNext(){
        currentIndex = (currentIndex + 1) % questionBank.size
    }

}



//    override fun onCleared(){
//        super.onCleared()
//        Log.d(TAG, "ViewModel instance about to be destroyed")
//    }
