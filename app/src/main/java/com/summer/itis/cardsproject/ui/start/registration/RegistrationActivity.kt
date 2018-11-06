package com.summer.itis.cardsproject.ui.start.registration


import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.view.View
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.arellomobile.mvp.presenter.InjectPresenter
import com.bumptech.glide.Glide
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.model.test.Test
import com.summer.itis.cardsproject.model.user.User
import com.summer.itis.cardsproject.ui.base.base_first.BaseActivity
import com.summer.itis.cardsproject.ui.start.login.LoginActivity
import com.summer.itis.cardsproject.ui.tests.test_item.TestActivity
import com.summer.itis.cardsproject.utils.AppHelper
import com.summer.itis.cardsproject.utils.Const
import com.summer.itis.cardsproject.utils.Const.STUB_PATH
import kotlinx.android.synthetic.main.activity_registration.*
import kotlinx.android.synthetic.main.dialog_pick_image.*

import java.io.InputStream


class RegistrationActivity : BaseActivity<RegistrationPresenter>(), RegistrationView, View.OnClickListener {

    var imageUri: Uri? = null
    var photoUrl: String = STUB_PATH
    var isStandartPhoto: Boolean = true

    lateinit var photoDialog: MaterialDialog

    @InjectPresenter
    override lateinit var presenter: RegistrationPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)
        setListeners()
    }

    override fun setStartStatus() {

    }

    private fun setListeners() {
        btn_sign_up.setOnClickListener(this)
        li_add_photo.setOnClickListener(this)
        link_login.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_sign_up -> createAccount()

            R.id.li_add_photo -> addPhoto()

            R.id.link_login -> goToLogin()
        }
    }

    private fun goToLogin() {
        LoginActivity.start(this)
    }

    private fun createAccount() {
        val username = et_email.text.toString()
        val password =et_password.text.toString()
        presenter.createAccount(username, password)
    }

    private fun addPhoto() {
        photoDialog = MaterialDialog.Builder(this)
                .customView(R.layout.dialog_pick_image, false).build()

        photoDialog.btn_choose_gallery.setOnClickListener{ showGallery() }

        photoDialog.show()

    }

    override fun onActivityResult(reqCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(reqCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if(reqCode == GALLERY_PHOTO) {
                data?.let { setGalleryPhoto(it) }
            }
            if(reqCode == STANDART_PHOTO) {
                data?.let { setStandartPhoto(it) }
            }
        } else {
            imageUri = null
            Toast.makeText(this@RegistrationActivity, "You haven't picked Image", Toast.LENGTH_LONG).show()
        }
    }

    private fun setGalleryPhoto(data: Intent) {
        imageUri = data.data
        isStandartPhoto = false
        val imageStream: InputStream = getContentResolver().openInputStream(imageUri)
        val selectedImage: Bitmap = BitmapFactory.decodeStream(imageStream)
        iv_cover.setImageBitmap(selectedImage)
    }

    private fun setStandartPhoto(data: Intent) {
    }

    private fun showGallery() {
        photoDialog.hide()
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), GALLERY_PHOTO)
    }

    override fun goToProfile(user: User) {
        TestActivity.start(this, Test())
//        PersonalActivity.start(this, user)
    }

    override fun showEmailError(hasError: Boolean) {
        if(hasError) {
            ti_email.error = getString(R.string.enter_correct_name)
        } else {
            ti_email.error = null
        }

    }

    override fun showPasswordError(hasError: Boolean) {
        if(hasError) {
            ti_password.error = getString(R.string.enter_correct_password)
        } else {
            ti_password.error = null
        }

    }

    override fun prepareUserBeforeCreate() {
        val user = User()

        user.email = et_email.text.toString()
        user.username = et_username.text.toString()
        user.lowerUsername = user.username?.toLowerCase()
        user.isStandartPhoto = isStandartPhoto
        user.photoUrl = photoUrl

//        presenter.createUserInDatabase(user,imageUri)
        TestActivity.start(this, Test())
    }

    companion object {

        private const val GALLERY_PHOTO = 0
        private const val STANDART_PHOTO = 1

        const val TAG_REGISTRATION = "TAG_REGISTR"

        fun start(activity: Activity) {
            val intent = Intent(activity, RegistrationActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            activity.startActivity(intent)
        }
    }
}
