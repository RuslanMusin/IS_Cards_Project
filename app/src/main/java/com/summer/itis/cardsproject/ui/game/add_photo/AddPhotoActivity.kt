package com.summer.itis.cardsproject.ui.game.add_photo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.model.common.PhotoItem
import com.summer.itis.cardsproject.ui.base.base_first.BaseActivity
import com.summer.itis.cardsproject.ui.base.base_first.BaseAdapter
import com.summer.itis.cardsproject.utils.Const.EDIT_STATUS
import com.summer.itis.cardsproject.utils.Const.ITEM_JSON
import com.summer.itis.cardsproject.utils.Const.USER_ID
import com.summer.itis.cardsproject.utils.Const.gsonConverter
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_add_list.*
import kotlinx.android.synthetic.main.fragment_recycler_list.*

class AddPhotoActivity : BaseActivity<AddPhotoPresenter>(), AddPhotoView, BaseAdapter.OnItemClickListener<PhotoItem> {

    @InjectPresenter
    override lateinit var presenter: AddPhotoPresenter

    private lateinit var adapter: AddPhotoListAdapter

    lateinit var userId: String

    companion object {

        fun start(activity: Activity) {
            val intent = Intent(activity, AddPhotoActivity::class.java)
            activity.startActivity(intent)
        }
    }

    override fun setStartStatus() {
        setStatus(EDIT_STATUS)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_list)
        userId = intent.getStringExtra(USER_ID)
        initViews()
        initRecycler()
        presenter.loadPhotos(userId)
    }

    private fun initViews() {
        setBackArrow(tb_add_list)
        setToolbarTitle(getString(R.string.select_avatar))
    }

    override fun handleError(throwable: Throwable) {

    }

    override fun onBackPressed() {
        val intent = Intent()
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }

    private fun initRecycler() {
        adapter = AddPhotoListAdapter(ArrayList())
        val manager = GridLayoutManager(this,3)
        rv_list.layoutManager = manager
        rv_list.setEmptyView(tv_empty)
        adapter.attachToRecyclerView(rv_list)
        adapter.setOnItemClickListener(this)
        rv_list.setHasFixedSize(true)
    }

    override fun onItemClick(item: PhotoItem) {
        val intent = Intent()
        val itemJson = gsonConverter.toJson(item)
        intent.putExtra(ITEM_JSON, itemJson)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun changeDataSet(photos: List<PhotoItem>) {
        adapter.changeDataSet(photos)
    }

    override fun showLoading(disposable: Disposable) {

    }

    override fun hideLoading() {
    }

}