package com.summer.itis.cardsproject.utils

import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

//КЛАСС RX JAVA
class RxUtils {

    companion object {

        fun <T> async() = ObservableTransformer<T,T> {
            observable ->  observable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())

        }

        fun <T> async(background: Scheduler,
                      main: Scheduler) = ObservableTransformer<T,T>{
            observable ->
                observable
                        .subscribeOn(background)
                        .observeOn(main)

        }

        fun <T> asyncSingle() = SingleTransformer<T, T> {
            single ->
                    single.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())

        }

        fun <T> asyncMaybe() = MaybeTransformer<T,T> {
            maybe ->
                    maybe
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
            }
        }

        fun asyncCompletable() = CompletableTransformer {
            completable ->
                completable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
        }

        fun <T> asyncFlowable() = FlowableTransformer<T,T> {
            flowable ->
                flowable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())

        }
}
