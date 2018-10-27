package com.summer.itis.cardsproject.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.summer.itis.cardsproject.model.user.User

import com.summer.itis.cardsproject.utils.Const.MAX_LENGTH
import com.summer.itis.cardsproject.utils.Const.MORE_TEXT
import com.summer.itis.cardsproject.utils.Const.OFFLINE_STATUS
import com.summer.itis.cardsproject.utils.Const.TAG_LOG
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import com.summer.itis.cardsproject.R

//ОСНОВНОЙ КЛАСС HELPER приложения. ОТСЮДА БЕРЕМ ТЕКУЩЕГО ЮЗЕРА ИЗ БД, ГРУЗИМ ФОТКУ ЮЗЕРА В ПРОФИЛЬ,
//ПОЛУЧАЕМ ССЫЛКУ НА ПУТЬ ФАЙЛОГО ХРАНИЛИЩА И СОЗДАЕМ СЕССИЮ. ПОКА ТАК ПУСТЬ БУДЕТ
class AppHelper {

    companion object {

        lateinit var currentUser: User

        var userInSession: Boolean = false

        val dataReference: DatabaseReference = FirebaseDatabase.getInstance().reference

        fun setUserPhoto(photoView: ImageView, user: User, context: Context) {
            if (user.isStandartPhoto) {
                Glide.with(context)
                        .load(R.drawable.pers)
                        .into(photoView)
            } else {
                Glide.with(context)
                        .load(user.photoUrl)
                        .into(photoView)
            }
        }

        fun setPhotoAndListener(photoView: ImageView, user: User, context: Context) {
            setUserPhoto(photoView,user,context)
        }


        fun readFileFromAssets(fileName: String, context: Context): List<String> {
            var reader: BufferedReader? = null
            var names: MutableList<String> = ArrayList()
            try {
                reader = BufferedReader(
                        InputStreamReader(context.assets.open(fileName), "UTF-8"))
                var mLine: String? = reader.readLine()
                while (mLine != null && !"".equals(mLine)) {
                    names.add(mLine)
                    mLine = reader.readLine()
                }
                return names
            } catch (e: IOException) {
                //log the exception
            } finally {
                if (reader != null) {
                    try {
                        reader.close()
                    } catch (e: IOException) {
                        //log the exception
                    }

                }
            }
            return names
        }

        fun convertDpToPx(dp: Float, context: Context): Int {
            val r = context.getResources()
            val px = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    dp,
                    r.getDisplayMetrics()
            ).toInt()
            return px
        }

        fun hideKeyboardFrom(context: Context, view: View) {
            Log.d(TAG_LOG,"hide keyboard")
            val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
        }

        fun showKeyboard(context: Context, editText: EditText) {
            val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
        }

        fun cutLongDescription(description: String, maxLength: Int): String {
            return if (description.length < MAX_LENGTH) {
                description
            } else {
                description.substring(0, MAX_LENGTH - MORE_TEXT.length) + MORE_TEXT
            }
        }
    }
}
