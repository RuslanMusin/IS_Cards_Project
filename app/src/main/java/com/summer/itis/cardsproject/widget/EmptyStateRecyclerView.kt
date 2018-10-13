package com.summer.itis.cardsproject.widget

import android.content.Context
import android.support.annotation.VisibleForTesting
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View

//СТАНДАРТНЫЙ RECYCLER
class EmptyStateRecyclerView : RecyclerView {

    private var mEmptyView: View? = null

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    fun checkIfEmpty() {
        adapter?.let {
            if (it.itemCount > 0) {
                showRecycler()
            } else {
                showEmptyView()
            }
        }
    }

    fun setEmptyView(view: View) {
        mEmptyView = view
    }

    @VisibleForTesting
    internal fun showRecycler() {
        mEmptyView?.visibility = View.GONE
        visibility = View.VISIBLE
    }

    @VisibleForTesting
    internal fun showEmptyView() {
        mEmptyView?.visibility = View.VISIBLE
        visibility = View.GONE
    }
}