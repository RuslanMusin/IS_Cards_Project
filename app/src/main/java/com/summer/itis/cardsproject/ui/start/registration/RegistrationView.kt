package com.summer.itis.cardsproject.ui.start.registration

import com.summer.itis.cardsproject.model.user.User
import com.summer.itis.cardsproject.ui.base.base_first.BaseView


interface RegistrationView: BaseView {

    fun showEmailError(hasError: Boolean)

    fun showPasswordError(hasError: Boolean)

    fun prepareUserBeforeCreate()

    fun goToProfile(user: User)
}