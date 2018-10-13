import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.model.test.Answer
import com.summer.itis.cardsproject.model.test.Question
import com.summer.itis.cardsproject.model.test.Test
import com.summer.itis.cardsproject.ui.base.base_first.BaseBackActivity
import com.summer.itis.cardsproject.ui.base.custom_base_menu.OnFourActionListener
import com.summer.itis.cardsproject.ui.tests.test_item.ChangeToolbarListener
import com.summer.itis.cardsproject.ui.tests.test_item.TestActivity
import com.summer.itis.cardsproject.ui.tests.test_item.TestActivity.Companion.FINISH_FRAGMENT
import com.summer.itis.cardsproject.ui.tests.test_item.TestActivity.Companion.QUESTION_FRAGMENT
import com.summer.itis.cardsproject.ui.tests.test_item.TestActivity.Companion.TEST_JSON
import com.summer.itis.cardsproject.utils.Const.TAG_LOG
import com.summer.itis.cardsproject.utils.Const.gsonConverter
import kotlinx.android.synthetic.main.fragment_question.*


class QuestionFragment : Fragment(), View.OnClickListener, OnFourActionListener {

    private lateinit var question: Question
    private lateinit var test: Test
    private var number: Int = 0

    private var textViews: MutableList<TextView> = ArrayList()
    private var checkBoxes: MutableList<CheckBox> = ArrayList()

    companion object {

        const val TAG_TEST_QUESTION_FRAG = "TAG_TEST_QUESTION_FRAG"

        const val QUESTION_NUMBER = "queston_number"

        const val RIGHT_ANSWERS = "right_answers"
        const val WRONG_ANSWERS = "wrong_answers"
        const val ANSWERS_TYPE = "type_answers"
        const val CARD_JSON = "card_json"

        fun newInstance(args: Bundle): Fragment {
            val fragment = QuestionFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onBackPressed() {
        shouldCancel()
    }

    override fun onCancel() {
        shouldCancel()
    }

    override fun onForward() {
        nextQuestion()
    }

    override fun onOk() {
        finishQuestions()
    }

    fun shouldCancel() {
        MaterialDialog.Builder(activity as Context)
                .title(R.string.question_dialog_title)
                .content(R.string.question_dialog_content)
                .positiveText(R.string.agree)
                .negativeText(R.string.disagree)
                .onPositive(object :MaterialDialog.SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                       quitFromTest()
                    }

                })
                .show()
    }

    private fun quitFromTest() {
        for(question in test.questions) {
            question.userRight = false
            for(answer in question.answers) {
                answer.userClicked = false
            }
        }
        TestActivity.start(activity as Activity,test)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_question, container, false)

        arguments?.getString(TEST_JSON)?.let { testStr ->
            arguments?.getInt(QUESTION_NUMBER)?.let { num ->
                number = num
                test = gsonConverter.fromJson(testStr, Test::class.java)
                question = test.questions[number]

                (activity as BaseBackActivity<*>).currentTag = TestActivity.QUESTION_FRAGMENT + number
                (activity as ChangeToolbarListener).changeToolbar(QUESTION_FRAGMENT, "Вопрос ${number + 1}/${test.questions.size}")
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setQuestionData()
        setListeners()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setQuestionData() {
        tv_question.text = question.question
        setStartAnswers()
    }

    private fun setStartAnswers() {
        for (answer in question.answers) {
            addAnswer(answer)
        }
        for(tv in textViews) {
            Log.d(TAG_LOG,"text = " + tv.text)
        }

        if(number == (test.questions.size-1)) {
            btn_next_question.visibility = View.GONE
            btn_finish_questions.visibility = View.VISIBLE
            (activity as ChangeToolbarListener).showOk(true)
        }
    }

    private fun setListeners() {
        btn_finish_questions.setOnClickListener(this)
        btn_next_question.setOnClickListener(this)
    }

    private fun finishQuestions() {
        checkAnswers()
        val args: Bundle = Bundle()
        args.putString(TEST_JSON, gsonConverter.toJson(test))
        val fragment = FinishFragment.newInstance(args)
        (activity as BaseBackActivity<*>).changeFragment(fragment, FINISH_FRAGMENT)
    }

    private fun nextQuestion() {
        checkAnswers()
        val args: Bundle = Bundle()
        args.putString(TEST_JSON, gsonConverter.toJson(test))
        args.putInt(QUESTION_NUMBER, ++number)
        val fragment = QuestionFragment.newInstance(args)
        (activity as BaseBackActivity<*>).changeFragment(fragment, QUESTION_FRAGMENT + number)
    }
    override fun onClick(v: View) {

        when (v.id) {

            R.id.btn_finish_questions -> {
               finishQuestions()
               }

            R.id.btn_next_question -> {
                Log.d(TAG_LOG, "next")
                nextQuestion()
            }

        }
    }

    private fun checkAnswers() {
        Log.d(TAG_LOG,"questioin = ${question.question}")
        question.userRight = true
        for(i in question.answers.indices) {
            val answer: Answer = question.answers[i]
                if(checkBoxes.get(i).isChecked) {
                    answer.userClicked = true
                    Log.d(TAG_LOG,"checked answer = ${answer.text}")
                }
            if(answer.userClicked != answer.isRight) {
                question.userRight = false
                Log.d(TAG_LOG, "wrong i = $i and answer = " + question.answers[i])
            }
            Log.d(TAG_LOG,"userclick = ${answer.userClicked} and q right = ${answer.isRight} and text = ${answer.text}")
        }
    }

    private fun addAnswer(answer: Answer) {
        val view: LinearLayout = layoutInflater.inflate(R.layout.layout_item_question,li_answers,false) as LinearLayout
        val tvAnswer: TextView = view.findViewWithTag("tv_answer")
        tvAnswer.text = answer.text
        textViews.add(tvAnswer)
        Log.d(TAG_LOG,"text tv = ${tvAnswer.text}")
        val checkBox: CheckBox = view.findViewWithTag("checkbox")
        checkBoxes.add(checkBox)
        Log.d(TAG_LOG,"checkboxes size = ${checkBoxes.size}")
        li_answers.addView(view)
    }
}
