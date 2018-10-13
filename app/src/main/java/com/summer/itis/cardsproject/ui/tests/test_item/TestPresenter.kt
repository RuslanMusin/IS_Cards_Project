package com.summer.itis.cardsproject.ui.tests.test_item

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import com.summer.itis.cardsproject.ui.base.base_first.BasePresenter
import com.summer.itis.cardsproject.utils.Const.TAG_LOG

@InjectViewState
class TestPresenter : BasePresenter<TestView>() {

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        Log.d(TAG_LOG, "attach presenter")
    }
}
