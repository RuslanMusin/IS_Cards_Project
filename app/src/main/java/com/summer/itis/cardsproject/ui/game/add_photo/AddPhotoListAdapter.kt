package com.summer.itis.cardsproject.ui.game.add_photo

import android.view.ViewGroup
import com.summer.itis.cardsproject.model.common.PhotoItem
import com.summer.itis.cardsproject.ui.base.base_first.BaseAdapter
import com.summer.itis.cardsproject.ui.game.add_photo.AddPhotoListHolder


class AddPhotoListAdapter(items: MutableList<PhotoItem>) : BaseAdapter<PhotoItem, AddPhotoListHolder>(items) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddPhotoListHolder {
        return AddPhotoListHolder.create(parent.context)
    }

    override fun onBindViewHolder(holder: AddPhotoListHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val item = getItem(position)
        holder.bind(item)
    }
}
