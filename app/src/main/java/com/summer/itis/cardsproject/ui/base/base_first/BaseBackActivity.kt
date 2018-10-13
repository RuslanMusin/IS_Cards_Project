package com.summer.itis.cardsproject.ui.base.base_first

import android.support.v4.app.Fragment
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.ui.base.custom_base_menu.OnBackPressedListener

abstract class BaseBackActivity<Presenter: BasePresenter<*>>: BaseActivity<Presenter>() {

    lateinit var currentTag: String

    override fun onBackPressed() {
        (getCurrentFragment() as OnBackPressedListener).onBackPressed()
        super.onBackPressed()
    }

    fun changeFragment(fragment: Fragment, tag: String) {
        getCurrentFragment()?.let {
            supportFragmentManager
                .beginTransaction()
                .remove(it)
                .add(R.id.fragment_container, fragment,tag)
                .commit()
        }
    }

    fun getCurrentFragment(): Fragment? {
        return supportFragmentManager.findFragmentByTag(currentTag)
    }

}