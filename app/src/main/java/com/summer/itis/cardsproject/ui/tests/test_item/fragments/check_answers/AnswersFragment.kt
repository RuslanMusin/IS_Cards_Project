package com.summer.itis.cardsproject.ui.tests.test_item.fragments.check_answers

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.CompoundButtonCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.model.test.Answer
import com.summer.itis.cardsproject.model.test.Question
import com.summer.itis.cardsproject.model.test.Test
import com.summer.itis.cardsproject.ui.base.base_first.BaseBackActivity
import com.summer.itis.cardsproject.ui.base.custom_base_menu.OnFourActionListener
import com.summer.itis.cardsproject.ui.tests.test_item.ChangeToolbarListener
import com.summer.itis.cardsproject.ui.tests.test_item.TestActivity
import com.summer.itis.cardsproject.ui.tests.test_item.TestActivity.Companion.ANSWERS_FRAGMENT
import com.summer.itis.cardsproject.ui.tests.test_item.TestActivity.Companion.FINISH_FRAGMENT
import com.summer.itis.cardsproject.ui.tests.test_item.TestActivity.Companion.TEST_JSON
import com.summer.itis.cardsproject.utils.Const
import com.summer.itis.cardsproject.utils.Const.TAG_LOG
import com.summer.itis.cardsproject.utils.Const.gsonConverter
import kotlinx.android.synthetic.main.fragment_question.*
import java.util.*

class AnswersFragment : Fragment(), View.OnClickListener, OnFourActionListener {

    private lateinit var question: Question
    private lateinit var test: Test
    private lateinit var type: String
    private var listSize: Int = 0
    private var number: Int = 0

    private lateinit var colorStateList: ColorStateList
    private lateinit var rightStateList: ColorStateList

    private lateinit var textViews: MutableList<TextView>
    private lateinit var checkBoxes: MutableList<CheckBox>

    companion object {

        const val TAG_TEST_ANSWERS_FRAGMENT = "TAG_ANSWERS_FRAGMENT"

        const val QUESTION_NUMBER = "queston_number"

        const val RIGHT_ANSWERS = "right_answers"
        const val WRONG_ANSWERS = "wrong_answers"
        const val ANSWERS_TYPE = "type_answers"

        fun newInstance(args: Bundle): Fragment {
            val fragment = AnswersFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onBackPressed() {
        beforeQuestion()
    }

    override fun onCancel() {
        finishQuestions()
    }

    override fun onForward() {
        nextQuestion()
    }

    override fun onOk() {
        finishQuestions()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_question, container, false)
        setAnswersData()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews(view)
        setListeners()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setAnswersData() {
        arguments?.getString(ANSWERS_TYPE)?.let { type = it }
        arguments?.getString(TEST_JSON)?.let { testStr ->
            arguments?.getInt(QUESTION_NUMBER)?.let { num ->
                number = num
                test = gsonConverter.fromJson(testStr, Test::class.java)
                if (type.equals(RIGHT_ANSWERS)) {
                    question = test.rightQuestions[number]
                    listSize = test.rightQuestions.size
                } else {
                    question = test.wrongQuestions[number]
                    listSize = test.wrongQuestions.size
                }
                (activity as BaseBackActivity<*>).currentTag = TestActivity.ANSWERS_FRAGMENT + number
                (activity as ChangeToolbarListener).changeToolbar(ANSWERS_FRAGMENT, "Вопрос ${number + 1}/${listSize}")
            }
        }
    }

    private fun initViews(view: View) {
        textViews = ArrayList()
        checkBoxes = ArrayList()

        if(number == (listSize-1)) {
            btn_next_question.visibility = View.GONE
            btn_finish_questions.visibility = View.VISIBLE
            (activity as ChangeToolbarListener).showOk(true)
        }
        tv_question.text = question.question
        setStartAnswers()
    }

    private fun setStartAnswers() {
        colorStateList = ColorStateList(
                arrayOf(intArrayOf(-android.R.attr.state_checked), // unchecked
                        intArrayOf(android.R.attr.state_checked)),// checked
                        intArrayOf(Color.parseColor("#FFFFFF"), 
                                    Color.parseColor("#DC143C"))
        )

        rightStateList = ColorStateList(
                arrayOf(intArrayOf(-android.R.attr.state_checked), // unchecked
                        intArrayOf(android.R.attr.state_checked)),// checked
                        intArrayOf(Color.parseColor("#FFFFFF"), 
                                    Color.parseColor("#00cc00"))
        )

        for (answer in question.answers) {
            addAnswer(answer)
        }
        for(tv in textViews) {
            Log.d(Const.TAG_LOG,"text = " + tv.text)
        }
    }
    
    private fun setListeners() {
        btn_finish_questions.setOnClickListener(this)
        btn_next_question.setOnClickListener(this)
        btn_next_question.text = getString(R.string.next_question)
    }

    private fun beforeQuestion() {
        if(number > 0) {
            val args: Bundle = Bundle()
            args.putString(TEST_JSON, gsonConverter.toJson(test))
            args.putString(ANSWERS_TYPE, type)
            args.putInt(QUESTION_NUMBER, --number)
            val fragment = AnswersFragment.newInstance(args)
            (activity as BaseBackActivity<*>).changeFragment(fragment, ANSWERS_FRAGMENT + number)
        } else {
            finishQuestions()
        }
    }

    private fun finishQuestions() {
        val args: Bundle = Bundle()
        args.putString(TEST_JSON, gsonConverter.toJson(test))
        val fragment = FinishFragment.newInstance(args)
        (activity as BaseBackActivity<*>).changeFragment(fragment, FINISH_FRAGMENT)    }

    private fun nextQuestion() {
        val args: Bundle = Bundle()
        args.putString(TEST_JSON, gsonConverter.toJson(test))
        args.putString(ANSWERS_TYPE,type)
        args.putInt(QUESTION_NUMBER, ++number)
        val fragment = AnswersFragment.newInstance(args)
        (activity as BaseBackActivity<*>).changeFragment(fragment, ANSWERS_FRAGMENT + number)
    }

    override fun onClick(v: View) {
        
        when (v.id) {

            R.id.btn_finish_questions -> {
                finishQuestions()
            }

            R.id.btn_next_question -> {
                nextQuestion()
            }
        }
    }

    private fun addAnswer(answer: Answer) {
        val view: LinearLayout = layoutInflater.inflate(R.layout.layout_item_question,li_answers,false) as LinearLayout
        val tvAnswer: TextView = view.findViewWithTag("tv_answer")
        tvAnswer.text = answer.text
        textViews.add(tvAnswer)
        val checkBox: CheckBox = view.findViewWithTag("checkbox")
        if(answer.isRight) {
            CompoundButtonCompat.setButtonTintList(checkBox, rightStateList)
            checkBox.isChecked = true
        }
        checkBoxes.add(checkBox)
        li_answers.addView(view)
        if(type.equals(WRONG_ANSWERS) && !answer.isRight && answer.userClicked != answer.isRight) {
            Log.d(TAG_LOG,"change checkbox color")
            Log.d(Const.TAG_LOG,"text tv = ${tvAnswer.text}")
            Log.d(TAG_LOG,"answer.isRight = ${answer.isRight} and userClick = ${answer.userClicked}")
            CompoundButtonCompat.setButtonTintList(checkBox, colorStateList)
            checkBox.isChecked = true
        }
        checkBox.isEnabled = false
    }
}
