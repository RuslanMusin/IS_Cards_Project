package com.summer.itis.cardsproject.ui.tests.test_item

import QuestionFragment.Companion.QUESTION_NUMBER
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.model.card.AbstractCard
import com.summer.itis.cardsproject.model.card.Card
import com.summer.itis.cardsproject.model.test.Answer
import com.summer.itis.cardsproject.model.test.Question
import com.summer.itis.cardsproject.model.test.Test
import com.summer.itis.cardsproject.ui.base.base_first.BaseBackActivity
import com.summer.itis.cardsproject.ui.base.custom_base_menu.OnCancelListener
import com.summer.itis.cardsproject.ui.base.custom_base_menu.OnForwardListener
import com.summer.itis.cardsproject.ui.base.custom_base_menu.OnOkListener
import com.summer.itis.cardsproject.utils.Const.EDIT_STATUS
import com.summer.itis.cardsproject.utils.Const.STUB_PATH
import com.summer.itis.cardsproject.utils.Const.gsonConverter
import kotlinx.android.synthetic.main.back_forward_toolbar.*


class TestActivity : BaseBackActivity<TestPresenter>(), TestView, ChangeToolbarListener {

    @InjectPresenter
    override lateinit var presenter: TestPresenter

    lateinit var test: Test

    private val containerId: Int = R.id.fragment_container

    companion object {

        const val TAG_TEST_ACT = "TAG_TEST_ACT"

        const val TEST_JSON: String = "test_json"

        const val QUESTION_FRAGMENT: String = "question_fragment"
        const val TEST_FRAGMENT: String = "test_fragment"
        const val ANSWERS_FRAGMENT: String = "answers_fragment"
        const val FINISH_FRAGMENT: String = "finish_fragment"
        const val WINNED_FRAGMENT: String = "winned_fragment"

        fun start(activity: Activity, test: Test) {
            val intent = Intent(activity, TestActivity::class.java)
            val testStr: String = gsonConverter.toJson(test)
            intent.putExtra(TEST_JSON,testStr)
            activity.startActivity(intent)
        }
    }

    override fun setStartStatus() {
        setStatus(EDIT_STATUS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_with_frame_and_toolbar)
        setSupportActionBar(test_toolbar)
        createTest()
        setListeners()
        setStartFragment()
    }

    private fun createTest() {
        test = Test()
        test.title = "Достоевский"
        test.type = "user_type"
        test.imageUrl = "https://upload.wikimedia.org/wikipedia/commons/7/78/Vasily_Perov_-_%D0%9F%D0%BE%D1%80%D1%82%D1%80%D0%B5%D1%82_%D0%A4.%D0%9C.%D0%94%D0%BE%D1%81%D1%82%D0%BE%D0%B5%D0%B2%D1%81%D0%BA%D0%BE%D0%B3%D0%BE_-_Google_Art_Project.jpg"
        test.id = "1"
        test.authorId = "1"
        test.authorName = "Ruslan"
        test.cardId = "1"
        test.desc = "something"

        val questionOne: Question = Question()
        questionOne.id = "0"
        questionOne.question = "В каком году родился Фёдор Михайлович Достоевский?"
        val answersOne = questionOne.answers
        var answer: Answer = Answer()
        answer.isRight = false
        answer.text = "В 1799"
        answersOne.add(answer)

        answer = Answer()
        answer.isRight = true
        answer.text = "В 1821"
        answersOne.add(answer)

        answer = Answer()
        answer.isRight = false
        answer.text = "В 1881"
        answersOne.add(answer)


        val questionTwo: Question = Question()
        questionTwo.id = "1"
        questionTwo.question = "Каким неприятным событием была омрачена юность Достоевского?"
        val answersTwo = questionTwo.answers
        var answerTwo: Answer = Answer()
        answerTwo.isRight = false
        answerTwo.text = "Достоевский проиграл в карты свое имение"
        answersTwo.add(answerTwo)

        answerTwo = Answer()
        answerTwo.isRight = false
        answerTwo.text = "Часто болел"
        answersTwo.add(answerTwo)

        answerTwo = Answer()
        answerTwo.isRight = true
        answerTwo.text = "Отца писателя убили крепостные крестьяне"
        answersTwo.add(answerTwo)

        test.questions.add(questionOne)
        test.questions.add(questionTwo)

        val card: Card = Card()
        val absCard = AbstractCard()
        absCard.name = "Достоевский"
        absCard.description = "desc"
        absCard.extract = "desc"
        absCard.photoUrl = STUB_PATH
        card.abstractCard = absCard
        test.card = card
    }

    private fun setListeners() {
        btn_back.setOnClickListener(this)
        btn_forward.setOnClickListener(this)
        btn_cancel.setOnClickListener(this)
        btn_ok.setOnClickListener(this)
    }

    private fun setStartFragment() {
        val args: Bundle = Bundle()
        args.putString(TEST_JSON, gsonConverter.toJson(test))
        args.putInt(QUESTION_NUMBER, 0)
        val fragment = QuestionFragment.newInstance(args)
        if (supportFragmentManager.findFragmentById(containerId) == null) {
            supportFragmentManager.beginTransaction()
                    .add(containerId, fragment, QUESTION_FRAGMENT + 0)
                    .commit()
        }
    }

    override fun changeToolbar(tag: String, title: String) {
        setToolbarTitle(title)
        when {
            TEST_FRAGMENT.equals(tag) -> {
                btn_ok.visibility = View.GONE
                btn_cancel.visibility = View.GONE
                btn_forward.visibility = View.GONE
            }

            QUESTION_FRAGMENT.equals(tag) -> {
                btn_back.visibility = View.GONE
                btn_cancel.visibility = View.VISIBLE
                btn_forward.visibility = View.VISIBLE
            }

            FINISH_FRAGMENT.equals(tag) -> {
                btn_ok.visibility = View.VISIBLE
                btn_cancel.visibility = View.GONE
                btn_back.visibility = View.GONE
                btn_forward.visibility = View.GONE
            }

            ANSWERS_FRAGMENT.equals(tag) -> {
                btn_ok.visibility = View.GONE
                btn_cancel.visibility = View.VISIBLE
                btn_back.visibility = View.VISIBLE
                btn_forward.visibility = View.VISIBLE
            }

            WINNED_FRAGMENT.equals(tag) -> {
                btn_back.visibility = View.VISIBLE
                btn_cancel.visibility = View.GONE
                btn_forward.visibility = View.GONE
                btn_ok.visibility = View.GONE
            }
        }
    }

    override fun setToolbarTitle(title: String) {
        toolbar_title.text = title
    }

    override fun showOk(boolean: Boolean) {
        if(boolean == true) {
            btn_ok.visibility = View.VISIBLE
        } else {
            btn_ok.visibility = View.GONE
        }
    }

    override fun onClick(v: View?) {
        when(v?.id) {

            R.id.btn_back -> {
                onBackPressed()
            }

            R.id.btn_forward -> {
                (getCurrentFragment() as OnForwardListener).onForward()
            }

            R.id.btn_ok -> {
                (getCurrentFragment() as OnOkListener).onOk()
            }

            R.id.btn_cancel -> {
                (getCurrentFragment() as OnCancelListener).onCancel()
            }
        }
    }
}
