package com.summer.itis.cardsproject.repository

import android.util.Log
import com.summer.itis.cardsproject.repository.api.WikiApiRepository
import com.summer.itis.cardsproject.repository.api.WikiApiRepositoryImpl
import com.summer.itis.cardsproject.repository.database.card.AbstractCardRepository
import com.summer.itis.cardsproject.repository.database.card.CardRepository
import com.summer.itis.cardsproject.repository.database.game.GamesRepository
import com.summer.itis.cardsproject.repository.database.test.TestRepository
import com.summer.itis.cardsproject.repository.database.user.UserRepository
import com.summer.itis.cardsproject.utils.Const.TAG_LOG



class RepositoryProvider {

    companion object {

        //table_names
        const val USERS = "users"
        const val USER_FRIENDS = "user_friends"
        const val USERS_ABSTRACT_CARDS = "users_abstract_cards"
        const val USERS_TESTS = "users_tests"
        const val USERS_CARDS = "users_cards"
        const val ABSTRACT_CARDS = "abstract_cards"
        const val CARDS = "test_cards"
        const val TESTS = "tests"
        const val TEST_COMMENTS = "test_comments"
        const val CARD_COMMENTS = "card_comments"
        const val LOBBIES = "lobbies"
        const val USERS_LOBBIES = "users_lobbies"

        val testRepository: TestRepository by lazy {
            TestRepository()
        }

        val cardRepository: CardRepository by lazy {
            CardRepository()
        }

        val userRepository: UserRepository by lazy {
            UserRepository()
        }


        val gamesRepository: GamesRepository by lazy {
            GamesRepository()
        }

        val abstractCardRepository: AbstractCardRepository by lazy {
            AbstractCardRepository()
        }

        val wikiApiRepository: WikiApiRepository by lazy {
            Log.d(TAG_LOG,"wikiRepo")
            WikiApiRepositoryImpl()
        }


    }
}