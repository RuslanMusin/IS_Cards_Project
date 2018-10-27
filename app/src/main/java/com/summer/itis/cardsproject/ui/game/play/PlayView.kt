package com.summer.itis.cardsproject.ui.game.play

import com.arellomobile.mvp.MvpView
import com.summer.itis.cardsproject.ui.base.base_first.BaseView

interface PlayView: BaseView {

    fun onAnswer(isRight: Boolean)
}