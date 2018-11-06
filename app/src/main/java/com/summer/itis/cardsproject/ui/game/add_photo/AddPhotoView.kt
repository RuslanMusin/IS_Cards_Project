package com.summer.itis.cardsproject.ui.game.add_photo

import com.arellomobile.mvp.MvpView
import com.summer.itis.cardsproject.model.common.PhotoItem
import com.summer.itis.cardsproject.ui.base.base_first.BaseView
import io.reactivex.disposables.Disposable

interface AddPhotoView : BaseView {

    fun handleError(throwable: Throwable)

    fun showLoading(disposable: Disposable)

    fun hideLoading()

    fun changeDataSet(photos: List<PhotoItem>)
}