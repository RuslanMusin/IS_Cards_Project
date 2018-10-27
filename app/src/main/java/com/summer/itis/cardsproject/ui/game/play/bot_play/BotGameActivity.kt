package com.summer.itis.cardsproject.ui.game.play.bot_play

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.GravityEnum
import com.afollestad.materialdialogs.MaterialDialog
import com.arellomobile.mvp.presenter.InjectPresenter
import com.bumptech.glide.Glide
import com.ms.square.android.expandabletextview.ExpandableTextView
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.R.id.tv_game_enemy_name
import com.summer.itis.cardsproject.model.card.Card
import com.summer.itis.cardsproject.model.test.Question
import com.summer.itis.cardsproject.model.user.User
import com.summer.itis.cardsproject.repository.database.game.GamesRepository
import com.summer.itis.cardsproject.ui.base.base_first.BaseActivity
import com.summer.itis.cardsproject.ui.game.play.change_list.GameChangeListAdapter
import com.summer.itis.cardsproject.ui.game.play.list.GameCardsListAdapter
import com.summer.itis.cardsproject.ui.member.member_item.PersonalActivity
import com.summer.itis.cardsproject.utils.AppHelper
import com.summer.itis.cardsproject.utils.Const
import com.summer.itis.cardsproject.utils.getRandom
import com.summer.itis.cardsproject.widget.CenterZoomLayoutManager
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.dialog_end_game.view.*
import kotlinx.android.synthetic.main.game_toolbar.*
import kotlinx.android.synthetic.main.item_game_card_medium.view.*
import kotlinx.android.synthetic.main.layout_change_card.*
import java.util.*

class BotGameActivity : BaseActivity<BotGamePresenter>(), BotGameView {

    var mode: String = MODE_PLAY_GAME

    @InjectPresenter
    override lateinit var presenter: BotGamePresenter

    lateinit var myCard: Card
    lateinit var enemyCard: Card

    var enemyChoosed: Boolean = false
    var myChoosed: Boolean = false
    var enemyAnswered: Boolean = false
    var myAnswered: Boolean = false
    var isQuestionMode: Boolean = false

    lateinit var myCards: MutableList<Card>

    var choosingEnabled = false

    var round: Int = 1

    lateinit var timer: CountDownTimer

    lateinit var adapter: GameCardsListAdapter

    companion object {

        const val MODE_CHANGE_CARDS = "change_cards"
        const val MODE_PLAY_GAME = "play_game"
        const val GAME_MODE = "game_mode"

        fun start(context: Context, gameMode: String) {
            val intent = Intent(context, BotGameActivity::class.java)
            intent.putExtra(GAME_MODE,gameMode)
            context.startActivity(intent)
        }

        fun start(context: Context) {
            val intent = Intent(context, BotGameActivity::class.java)
            context.startActivity(intent)
        }


        fun setWeight(view: View, w: Float) {
            val params = view.layoutParams as LinearLayout.LayoutParams
            params.weight = w
            view.layoutParams = params
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBotGameData()
        setContentView(R.layout.activity_game)
        setStartCardInvisible()
        setListeners()
        setToolbarData()
        showProgressDialog(R.string.progress_message)
    }

    private fun setBotGameData() {
        AppHelper.currentUser.gameLobby?.let { presenter.setInitState(it) }
    }

    override fun onBackPressed() {
        quitGame()
    }

    fun quitGame() {
        PersonalActivity.start(this, AppHelper.currentUser)
    }

    override fun setEnemyUserData(user: User) {
        tv_game_enemy_name.text = user.username
    }

    override fun setCardsList(cards: ArrayList<Card>) {
        myCards = cards
        Log.d(Const.TAG_LOG,"set cards")

        initRecycler(cards)
        hideProgressDialog()
        startTimer()

    }

    private fun setStartCardInvisible() {
        enemy_selected_card.visibility = View.INVISIBLE
        my_selected_card.visibility = View.INVISIBLE
        game_questions_container.visibility = View.GONE
    }

    private fun setToolbarData() {
        setSupportActionBar(game_toolbar)
        changeRound(round)
    }

    private fun initRecycler(cards: ArrayList<Card>) {
        Log.d(Const.TAG_LOG,"set game adapter")
        rv_game_my_cards.adapter = GameCardsListAdapter(
                cards,
                this,
                {
                    if (choosingEnabled) {
                        presenter.chooseCard(it)
                    }
                }
        )
        adapter = rv_game_my_cards.adapter as GameCardsListAdapter
        rv_game_my_cards.layoutManager = CenterZoomLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
    }

    private fun setListeners() {
        val listener: View.OnClickListener = View.OnClickListener {

            when(it.id) {

                R.id.enemy_selected_card -> {
                    if(enemy_selected_card.visibility == View.VISIBLE) {
                        showDialogCard(enemyCard)
                    }
                }

                R.id.my_selected_card -> {
                    if(my_selected_card.visibility == View.VISIBLE) {
                        showDialogCard(myCard)
                    }
                }
            }

        }
        enemy_selected_card.setOnClickListener(listener)
        my_selected_card.setOnClickListener(listener)
        btn_cancel.setOnClickListener{quitGame()}
    }

    private fun showDialogCard(card: Card) {
        val dialog: MaterialDialog = MaterialDialog.Builder(this)
                .customView(R.layout.fragment_test_card, false)
                .build()

        val view: View? = dialog.customView
        view?.findViewById<ExpandableTextView>(R.id.expand_text_view)?.text = card.abstractCard.description
        view?.findViewById<TextView>(R.id.tv_name)?.text = card.abstractCard.name

        view?.findViewById<ImageView>(R.id.iv_portrait)?.let { it1 ->
            Glide.with(it1.context)
                    .load(card.abstractCard.photoUrl)
                    .into(it1)
        }
        dialog.show()
    }


    override fun setCardChooseEnabled(enabled: Boolean) {
        choosingEnabled = enabled
        rv_game_my_cards.isEnabled = enabled
        if (enabled) {
            rv_game_my_cards.alpha = 1f
        } else {
            rv_game_my_cards.alpha = 0.5f
        }
    }

    override fun onAnswer(isRight: Boolean) {
        presenter.answer(isRight)
    }

    override fun showEnemyCardChoose(card: Card) {
        enemyCard = card
        enemyChoosed = true
        updateTime()
        setCard(enemy_selected_card, card)
        enemy_selected_card.visibility = View.VISIBLE

        val a_anim = AlphaAnimation(0f, 1f)
        a_anim.duration = 700;
        a_anim.fillAfter = true
        enemy_selected_card.startAnimation(a_anim)
    }
    
    private fun changeRound(round: Int) {
        toolbar_title.text = getString(R.string.game_round,round)
    }

    private fun updateTime() {
        Log.d(Const.TAG_LOG,"updateTime")
        checkIsAnsweredBoth()
        checkIsChoosedBoth()
    }
    
    private fun checkIsAnsweredBoth() {
        if(enemyAnswered and myAnswered) {
            Log.d(Const.TAG_LOG,"choose card mode")
            enemyAnswered = false
            myAnswered = false
            isQuestionMode = false
            adapter.isClickable = true
            changeRound(++round)
            timer.cancel()
            startTimer()
        }
    }
    
    private fun checkIsChoosedBoth() {
        if(enemyChoosed and myChoosed) {
            Log.d(Const.TAG_LOG,"show question mode")
            enemyChoosed = false
            myChoosed = false
            isQuestionMode = true
            startViewTime()
        }
    }
    
    private fun startViewTime() {
        timer.cancel()
        tv_time.text = getString(R.string.view_time)
        timer = object : CountDownTimer(10000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                timer.cancel()
                presenter.showQuestion()
                startTimer()
            }
        }.start()
    }

    private fun startTimer() {
        timer = object : CountDownTimer(30000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                tv_time.text =  "${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                Log.d(Const.TAG_LOG,"onFinish")
                answerIfNoAnswer()
                chooseIfNoChoice()
            }
        }
        timer.start()
    }
    
    private fun answerIfNoAnswer() {
        if(isQuestionMode && !myAnswered) {
            Log.d(Const.TAG_LOG,"notAnswered")
            myAnswered = true
            onAnswer(false)
            updateTime()

        }
    }
    
    private fun chooseIfNoChoice() {
        if(!isQuestionMode && !myChoosed) {
            Log.d(Const.TAG_LOG,"notChoosed")
            myChoosed = true
            val card: Card? = myCards.getRandom()
            card?.let { adapter.removeElement(it) }
            updateTime()
        }
    }

    override fun showQuestionForYou(question: Question) {
        game_questions_container.visibility = View.VISIBLE

        supportFragmentManager
                .beginTransaction()
                .replace(
                        R.id.game_questions_container,
                        GameQuestionFragment.newInstance(question)
                )
                .commit()

    }

    override fun hideQuestionForYou() {
        game_questions_container.visibility = View.GONE
    }

    override fun showYouCardChoose(card: Card) {
        myCard = card
        myChoosed = true
        updateTime()
        setCard(my_selected_card, card)
        my_selected_card.visibility = View.VISIBLE

        val a_anim = AlphaAnimation(0f, 1f)
        a_anim.duration = 200;
        a_anim.fillAfter = true
        my_selected_card.startAnimation(a_anim)
    }

    override fun hideEnemyCardChoose() {
        enemy_selected_card.clearAnimation()
        enemy_selected_card.visibility = View.INVISIBLE
    }

    override fun hideYouCardChoose() {
        my_selected_card.clearAnimation()
        my_selected_card.visibility = View.INVISIBLE
    }

    override fun showEnemyAnswer(correct: Boolean) {
        enemyAnswered = true
        updateTime()
        if (correct) {
            tv_enemy_score.text = (tv_enemy_score.text.toString().toInt() + 1).toString()
        }
    }

    override fun showYourAnswer(correct: Boolean) {
        myAnswered = true
        updateTime()
        if (correct) {
            tv_my_score.text = (tv_my_score.text.toString().toInt() + 1).toString()
        }
    }

    fun setCard(view: View, card: Card) {
        view.tv_card_person_name.text = card.abstractCard.name

        Glide.with(this)
                .load(card.abstractCard.photoUrl)
                .into(view.iv_card)

        setWeight(view.ll_card_params.view_card_intelligence, card.intelligence.toFloat())
        setWeight(view.ll_card_params.view_card_support, card.support.toFloat())
        setWeight(view.ll_card_params.view_card_prestige, card.prestige.toFloat())
        setWeight(view.ll_card_params.view_card_hp, card.hp.toFloat())
        setWeight(view.ll_card_params.view_card_strength, card.strength.toFloat())
    }




    override fun showGameEnd(type: GamesRepository.GameEndType, card: Card) {
        timer.cancel()

        if (type == GamesRepository.GameEndType.DRAW) {
            MaterialDialog.Builder(this)
                    .title(getString(R.string.draw))
                    .titleGravity(GravityEnum.CENTER)
                    .neutralText(getString(R.string.button_ok))
                    .buttonsGravity(GravityEnum.END)
                    .onNeutral { dialog, which ->
                        goToFindGameActivity()
                    }
                    .canceledOnTouchOutside(false)
                    .cancelable(false)
                    .show()
        } else {
            val dialog = MaterialDialog.Builder(this)
                    .title(when (type) {
                        GamesRepository.GameEndType.YOU_WIN,
                        GamesRepository.GameEndType.ENEMY_DISCONNECTED_AND_YOU_WIN -> "You win"

                        GamesRepository.GameEndType.YOU_LOSE,
                        GamesRepository.GameEndType.YOU_DISCONNECTED_AND_LOSE -> "You lose"

                        GamesRepository.GameEndType.DRAW -> "Draw"//never
                    })
                    .titleGravity(GravityEnum.CENTER)
                    .customView(R.layout.dialog_end_game, false)

                    .neutralText(getString(R.string.button_ok))
                    .buttonsGravity(GravityEnum.END)
                    .onNeutral { dialog, which ->
                        goToFindGameActivity()
                    }
                    .canceledOnTouchOutside(false)
                    .cancelable(false)
                    .show()

            setCard(dialog.view.card_in_end_dialog, card)

            dialog.view.tv_get_lose_card.text = when (type) {
                GamesRepository.GameEndType.YOU_WIN,
                GamesRepository.GameEndType.ENEMY_DISCONNECTED_AND_YOU_WIN -> "You get card:"

                GamesRepository.GameEndType.YOU_LOSE,
                GamesRepository.GameEndType.YOU_DISCONNECTED_AND_LOSE -> "You lose card:"

                GamesRepository.GameEndType.DRAW -> "Draw"//never
            }

            if (type == GamesRepository.GameEndType.ENEMY_DISCONNECTED_AND_YOU_WIN) {
                dialog.view.tv_game_end_reason.text = getString(R.string.enemy_disconnected)
                dialog.view.tv_game_end_reason.visibility = View.VISIBLE
            }

            if (type == GamesRepository.GameEndType.YOU_DISCONNECTED_AND_LOSE) {
                dialog.view.tv_game_end_reason.text = getString(R.string.you_disconnected)
                dialog.view.tv_game_end_reason.visibility = View.VISIBLE
            }

        }

    }

    private fun goToFindGameActivity() {
        quitGame()
    }
}
