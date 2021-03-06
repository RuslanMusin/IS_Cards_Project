package com.summer.itis.cardsproject.ui.tests.test_item.fragments.winned_card

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.summer.itis.cardsproject.R
import com.summer.itis.cardsproject.model.card.Card
import com.summer.itis.cardsproject.model.test.Test
import com.summer.itis.cardsproject.ui.base.base_first.BaseBackActivity
import com.summer.itis.cardsproject.ui.base.custom_base_menu.OnBackPressedListener
import com.summer.itis.cardsproject.ui.tests.test_item.ChangeToolbarListener
import com.summer.itis.cardsproject.ui.tests.test_item.TestActivity
import com.summer.itis.cardsproject.ui.tests.test_item.TestActivity.Companion.FINISH_FRAGMENT
import com.summer.itis.cardsproject.ui.tests.test_item.TestActivity.Companion.TEST_JSON
import com.summer.itis.cardsproject.ui.tests.test_item.TestActivity.Companion.WINNED_FRAGMENT
import com.summer.itis.cardsproject.utils.Const.gsonConverter
import kotlinx.android.synthetic.main.fragment_test_card.*
import kotlinx.android.synthetic.main.layout_expandable_text_view.*


class TestCardFragment: Fragment(), OnBackPressedListener {

    lateinit var test: Test
    lateinit var card: Card

    companion object {

        const val TAG_TEST_CARD_FRAG = "TAG_TEST_CARD_FRAG"

        fun newInstance(args: Bundle): Fragment {
            val fragment = TestCardFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onBackPressed() {
        val args: Bundle = Bundle()
        args.putString(TEST_JSON, gsonConverter.toJson(test))
        val fragment = FinishFragment.newInstance(args)
        (activity as BaseBackActivity<*>).changeFragment(fragment, FINISH_FRAGMENT)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_test_card, container, false)

        val testStr: String? = arguments?.getString(TEST_JSON)
        test = gsonConverter.fromJson(testStr, Test::class.java)
        test.card?.let {
            card = it
            (activity as BaseBackActivity<*>).currentTag = TestActivity.WINNED_FRAGMENT
            (activity as ChangeToolbarListener).changeToolbar(WINNED_FRAGMENT, "Карта ${card.abstractCard.name}")
        }
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        expand_text_view.text = card.abstractCard.description
        tv_name.text = card.abstractCard.name
        tv_support.text = card.support.toString()
        tv_hp.text = card.hp.toString()
        tv_strength.text = card.strength.toString()
        tv_prestige.text = card.prestige.toString()
        tv_intelligence.text = card.intelligence.toString()

        card.abstractCard.photoUrl?.let {
            Glide.with(iv_portrait.context)
                    .load(it)
                    .into(iv_portrait)
        }

        super.onViewCreated(view, savedInstanceState)
    }
}