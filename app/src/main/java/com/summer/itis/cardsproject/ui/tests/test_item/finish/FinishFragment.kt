
import QuestionFragment.Companion.ANSWERS_TYPE
import QuestionFragment.Companion.RIGHT_ANSWERS
import QuestionFragment.Companion.WRONG_ANSWERS
import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.model.test.Question
import com.summer.itis.cardsproject.model.test.Test
import com.summer.itis.cardsproject.ui.base.base_first.BaseBackActivity
import com.summer.itis.cardsproject.ui.base.custom_base_menu.OnBackPressedListener
import com.summer.itis.cardsproject.ui.base.custom_base_menu.OnOkListener
import com.summer.itis.cardsproject.ui.tests.test_item.ChangeToolbarListener
import com.summer.itis.cardsproject.ui.tests.test_item.TestActivity
import com.summer.itis.cardsproject.ui.tests.test_item.TestActivity.Companion.ANSWERS_FRAGMENT
import com.summer.itis.cardsproject.ui.tests.test_item.TestActivity.Companion.FINISH_FRAGMENT
import com.summer.itis.cardsproject.ui.tests.test_item.TestActivity.Companion.TEST_JSON
import com.summer.itis.cardsproject.ui.tests.test_item.TestActivity.Companion.WINNED_FRAGMENT

import com.summer.itis.cardsproject.utils.AppHelper
import com.summer.itis.cardsproject.utils.Const.gsonConverter
import kotlinx.android.synthetic.main.fragment_finish_test.*

class FinishFragment : Fragment(), View.OnClickListener, OnBackPressedListener, OnOkListener {

    lateinit var test: Test
    var rightQuestions: MutableList<Question> = ArrayList()
    var wrongQuestions: MutableList<Question> = ArrayList()
    var procent: Long = 0

    companion object {

        const val TAG_TEST_FINISH = "TAG_TEST_FINISH"

        fun newInstance(args: Bundle): Fragment {
            val fragment = FinishFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onBackPressed() {
    }

    override fun onOk() {
        btn_finish_test.performClick()
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_finish_test, container, false)
        setFinishData()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tv_right_answers.text = rightQuestions.size.toString()
        tv_wrong_answers.text = wrongQuestions.size.toString()
        setCardText()
        setListeners()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setFinishData() {
        (activity as BaseBackActivity<*>).currentTag = TestActivity.FINISH_FRAGMENT
        (activity as ChangeToolbarListener).changeToolbar(FINISH_FRAGMENT,getString(R.string.test_result))

        test = gsonConverter.fromJson(arguments?.getString(TEST_JSON), Test::class.java)
        for(question in test.questions) {
            if(question.userRight) {
                rightQuestions.add(question)
            } else {
                wrongQuestions.add(question)
            }
        }
        test.rightQuestions = rightQuestions
        test.wrongQuestions = wrongQuestions
    }

    private fun setListeners() {
        btn_finish_test.setOnClickListener(this)
        li_wrong_answers.setOnClickListener(this)
        li_right_answers.setOnClickListener(this)
        li_winned_card.setOnClickListener(this)
    }

    fun setCardText() {
        procent = Math.round((test.rightQuestions.size.toDouble() / test.questions.size.toDouble()) * 100)
        Log.d(TAG_TEST_FINISH, "procent = $procent")
        if (procent >= 80) {
            Log.d(TAG_TEST_FINISH, "finish it")
            tv_winned_card.text = test.card?.abstractCard?.name
            test.testDone = true

        } else {
            tv_winned_card.text = getText(R.string.test_failed)
        }
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.btn_finish_test -> {
                finishTest()
            }

            R.id.li_wrong_answers -> {
                showWrongAnswers()
            }

            R.id.li_right_answers -> {
                showRightAnswers()
            }

            R.id.li_winned_card -> {
                showWinnedCard()
            }
        }
    }

    private fun showWrongAnswers() {
        if(wrongQuestions.size > 0) {
            prepareAnswers(WRONG_ANSWERS)
        }
    }

    private fun showRightAnswers() {
        if(rightQuestions.size > 0) {
            prepareAnswers(RIGHT_ANSWERS)
        }
    }

    private fun showWinnedCard() {
    }

    private fun finishTest() {
        for(question in test.questions) {
            question.userRight = false
            for(answer in question.answers) {
                answer.userClicked = false
            }
        }
        TestActivity.start(activity as Activity,test)
    }

    fun prepareAnswers(type: String) {
    }
}