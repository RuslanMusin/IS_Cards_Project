package com.summer.itis.cardsproject.ui.game.play.change_list

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import com.bumptech.glide.Glide
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.model.card.Card
import com.summer.itis.cardsproject.ui.game.play.bot_play.BotGameActivity.Companion.setWeight
import com.summer.itis.cardsproject.utils.Const.TAG_LOG
import kotlinx.android.synthetic.main.item_game_card_medium.view.*


class GameChangeListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bind(card: Card) {
        itemView.tv_card_person_name.text = card.abstractCard.name

        card.abstractCard.photoUrl?.let {
            Glide.with(itemView.iv_card.context)
                    .load(it)
                    .into(itemView.iv_card)

        }
        setWeight(itemView.ll_card_params.view_card_intelligence, card.intelligence.toFloat())
        setWeight(itemView.ll_card_params.view_card_support, card.support.toFloat())
        setWeight(itemView.ll_card_params.view_card_prestige, card.prestige.toFloat())
        setWeight(itemView.ll_card_params.view_card_hp, card.hp.toFloat())
        setWeight(itemView.ll_card_params.view_card_strength, card.strength.toFloat())
    }

    companion object  {

        private val MAX_LENGTH = 80
        private val MORE_TEXT = "..."

        fun create(parent: ViewGroup): GameChangeListViewHolder {
            val view =  LayoutInflater.from(parent.context).inflate(R.layout.item_game_card_medium, parent, false);
            val holder = GameChangeListViewHolder(view)

            setFocusOnView(view)

            return holder
        }

        private fun setFocusOnView(view: View?) {
            view?.setOnFocusChangeListener({ v, hasFocus ->
                Log.d(TAG_LOG,"changed anim focus = $hasFocus")
                if (hasFocus) {
                    val anim = AnimationUtils.loadAnimation(view.context, R.anim.scale_in_tv)
                    view.startAnimation(anim)
                    anim.fillAfter = true
                } else {
                    val anim = AnimationUtils.loadAnimation(view.context, R.anim.scale_out_tv)
                    view.startAnimation(anim)
                    anim.fillAfter = true
                }
            })
        }
    }
}
