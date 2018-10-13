package com.summer.itis.cardsproject.ui.base.base_first

import android.annotation.SuppressLint
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.utils.AppHelper
import com.summer.itis.cardsproject.utils.Const
import com.summer.itis.cardsproject.ui.base.base_first.BaseView


open class BasePresenter<View: BaseView>: MvpPresenter<View>() {

    var isStopped: Boolean = false

}