package com.summer.itis.cardsproject.ui.base.base_first

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.arellomobile.mvp.MvpAppCompatActivity
import com.arellomobile.mvp.presenter.InjectPresenter
import com.google.android.gms.flags.impl.SharedPreferencesFactory.getSharedPreferences
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.model.game.Lobby
import com.summer.itis.cardsproject.ui.game.play.user_play.PlayGameActivity
import com.summer.itis.cardsproject.utils.AppHelper
import com.summer.itis.cardsproject.utils.Const
import com.summer.itis.cardsproject.utils.Const.STOP_STATUS
import kotlinx.android.synthetic.main.activity_base.*


//СТАНДАРТНЫЙ БАЗОВЫЙ АКТИВИТИ ДЛЯ ТЕХ,КОМУ НЕ НУЖНА НАВИГАЦИЯ, НО ЕСТЬ СТРЕЛКА НАЗАД И ПРОГРЕСС БАР.
abstract class BaseActivity<Presenter: BasePresenter<*>> : MvpAppCompatActivity(), BaseView {

    var progressDialog: ProgressDialog? = null
    var gameRequestDialog: MaterialDialog? = null

    var currentStatus: String = Const.OFFLINE_STATUS

    abstract var presenter: Presenter

    companion object {

        const val TAG_BASE_ACT = "TAG_BASE_ACT"
    }

    abstract fun setStartStatus()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStartStatus()
        setOfflineChecking()
    }


    override fun onStop() {
        presenter.isStopped = true
        hideProgressDialog()
        super.onStop()
    }

    override fun onResume() {
        Log.d(Const.TAG_LOG,"onResume")
        setStatus(currentStatus)
        super.onResume()
    }

    override fun onPause() {
        Log.d(Const.TAG_LOG,"on stop status = ${Const.OFFLINE_STATUS}")
        presenter.changeUserStatus(STOP_STATUS)
        super.onPause()
    }

    protected fun setOfflineChecking() {
        presenter.checkUserConnection { offlineMode() }
    }

    override fun showProgressDialog(message: String) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
            progressDialog?.let{
                it.setMessage(message)
                it.isIndeterminate = true
                it.setCancelable(false)
            }
        }
        progressDialog?.show()
    }

    override fun showProgressDialog(messageId: Int) {
        showProgressDialog(getString(messageId))
    }

    override fun hideProgressDialog() {
        progressDialog?.let {
            if (it.isShowing) {
                progressDialog!!.dismiss()
            }
        }
    }

    override fun showSnackBar(message: String) {
        val snackbar: Snackbar = Snackbar.make(findViewById(android.R.id.content),
                message, Snackbar.LENGTH_LONG)
        snackbar.getView().setBackgroundColor(Color.BLACK)
        val textView = snackbar.view.findViewById(android.support.design.R.id.snackbar_text) as TextView;
        textView.setTextColor(Color.WHITE);
        snackbar.show()
    }

    override fun showSnackBar(messageId: Int) {
        showSnackBar(getString(messageId))
    }

    fun showWarningDialog(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(message)
        builder.setPositiveButton(R.string.button_ok, null)
        builder.show()
    }


    fun showWarningDialog(messageId: Int) {
        showWarningDialog(getString(messageId))
    }


    fun hasInternetConnection(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting
    }

    fun checkInternetConnection(): Boolean {
        val hasInternetConnection = hasInternetConnection()
        if (!hasInternetConnection) {
            showWarningDialog(R.string.internet_connection_failed)
        }
        return hasInternetConnection
    }

    fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    protected fun setBackArrow(toolbar: Toolbar) {
        val actionBar = supportActionBar
        if(supportActionBar == null) {
            setSupportActionBar(toolbar)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener { v -> onBackPressed() }
    }

    protected open fun setToolbarTitle(title: String) {
        supportActionBar?.title = title
    }

    protected fun deleteUserPrefs() {
        val sharedPreferences: SharedPreferences = getSharedPreferences(Const.USER_DATA_PREFERENCES,Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.remove(Const.USER_USERNAME)
        editor.remove(Const.USER_PASSWORD)
        editor.apply()
    }

    protected fun waitEnemy() {
        Log.d(TAG_BASE_ACT,"wait enemy")
        presenter.waitEnemy()

    }

    override fun showGameRequestDialog(lobby: Lobby) {
        gameRequestDialog = MaterialDialog.Builder(this)
                .title(R.string.game_request_dialog_title)
                .content(R.string.game_request_dialog_content)
                .positiveText(R.string.agree)
                .negativeText(R.string.disagree)
                .onPositive(object : MaterialDialog.SingleButtonCallback {
                    override fun onClick(dialog: MaterialDialog, which: DialogAction) {
                        presenter.agreeGameRequest(lobby)
                    }

                })
                .onNegative { dialog: MaterialDialog, which: DialogAction ->
                    presenter.refuseAndWait(lobby) }
                .build()

        gameRequestDialog?.setCancelable(false)
        gameRequestDialog?.show()
    }

    override fun hideGameRequestDialog() {
        gameRequestDialog?.hide()
    }

    override fun goToGame() {
        Log.d(TAG_BASE_ACT, "play game after wait")
        PlayGameActivity.start(this)
    }

    fun setStatus(status: String) {
        Log.d(TAG_BASE_ACT,"current status = $status")
        currentStatus = status
        AppHelper.userStatus = status
        AppHelper.onlineFunction = onlineMode()
        AppHelper.offlineFunction = offlineMode()
        presenter.changeUserStatus(status)
    }

    fun offlineMode(): () -> (Unit) {
        return {
            Log.d(Const.TAG_LOG,"offline mode")
            if(li_offline != null && container != null) {
                li_offline.visibility = View.VISIBLE
                container.visibility = View.GONE
            }
        }
    }

    fun onlineMode(): () -> (Unit) {
        return {
            Log.d(Const.TAG_LOG,"online mode")
            if(li_offline != null && container != null) {
                container.visibility = View.VISIBLE
                li_offline.visibility = View.GONE
            }
        }
    }

    protected fun setStartWaiting(status: String) {
        setStatus(status)
        waitEnemy()
    }
}