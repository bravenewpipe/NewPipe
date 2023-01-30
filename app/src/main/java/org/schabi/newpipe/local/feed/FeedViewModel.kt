package org.schabi.newpipe.local.feed

import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.preference.PreferenceManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.functions.Function5
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.schedulers.Schedulers
import org.schabi.newpipe.App
import org.schabi.newpipe.R
import org.schabi.newpipe.database.feed.model.FeedGroupEntity
import org.schabi.newpipe.database.stream.StreamWithState
import org.schabi.newpipe.local.feed.item.StreamItem
import org.schabi.newpipe.local.feed.service.FeedEventManager
import org.schabi.newpipe.local.feed.service.FeedEventManager.Event.ErrorResultEvent
import org.schabi.newpipe.local.feed.service.FeedEventManager.Event.IdleEvent
import org.schabi.newpipe.local.feed.service.FeedEventManager.Event.ProgressEvent
import org.schabi.newpipe.local.feed.service.FeedEventManager.Event.SuccessResultEvent
import org.schabi.newpipe.util.DEFAULT_THROTTLE_TIMEOUT
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

enum class ShowItems {
    WATCHED, PARTIALLY_WATCHED, DEFAULT
}
class FeedViewModel(
    private val application: Application,
    groupId: Long = FeedGroupEntity.GROUP_ALL_ID,
    initialShowPlayedItems: ShowItems = ShowItems.DEFAULT,
    initialShowFutureItems: Boolean = true
) : ViewModel() {
    private val feedDatabaseManager = FeedDatabaseManager(application)

    private val toggleShowPlayedItems = BehaviorProcessor.create<ShowItems>()
    private val toggleShowPlayedItemsFlowable = toggleShowPlayedItems
        .startWithItem(initialShowPlayedItems)
        .distinctUntilChanged()

    private val toggleShowFutureItems = BehaviorProcessor.create<Boolean>()
    private val toggleShowFutureItemsFlowable = toggleShowFutureItems
        .startWithItem(initialShowFutureItems)
        .distinctUntilChanged()

    private val mutableStateLiveData = MutableLiveData<FeedState>()
    val stateLiveData: LiveData<FeedState> = mutableStateLiveData

    private var combineDisposable = Flowable
        .combineLatest(
            FeedEventManager.events(),
            toggleShowPlayedItemsFlowable,
            toggleShowFutureItemsFlowable,
            feedDatabaseManager.notLoadedCount(groupId),
            feedDatabaseManager.oldestSubscriptionUpdate(groupId),

            Function5 { t1: FeedEventManager.Event, t2: ShowItems, t3: Boolean,
                t4: Long, t5: List<OffsetDateTime> ->
                return@Function5 CombineResultEventHolder(t1, t2, t3, t4, t5.firstOrNull())
            }
        )
        .throttleLatest(DEFAULT_THROTTLE_TIMEOUT, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .map { (event, showPlayedItems, showFutureItems, notLoadedCount, oldestUpdate) ->
            val streamItems = if (event is SuccessResultEvent || event is IdleEvent) {
                feedDatabaseManager
                    .getStreams(
                        groupId,
                        !(
                            showPlayedItems == ShowItems.WATCHED ||
                                showPlayedItems == ShowItems.PARTIALLY_WATCHED
                            ),
                        showPlayedItems != ShowItems.PARTIALLY_WATCHED,
                        showFutureItems
                    )
                    .blockingGet(arrayListOf())
            } else {
                arrayListOf()
            }

            CombineResultDataHolder(event, streamItems, notLoadedCount, oldestUpdate)
        }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe { (event, listFromDB, notLoadedCount, oldestUpdate) ->
            mutableStateLiveData.postValue(
                when (event) {
                    is IdleEvent -> FeedState.LoadedState(listFromDB.map { e -> StreamItem(e) }, oldestUpdate, notLoadedCount)
                    is ProgressEvent -> FeedState.ProgressState(event.currentProgress, event.maxProgress, event.progressMessage)
                    is SuccessResultEvent -> FeedState.LoadedState(listFromDB.map { e -> StreamItem(e) }, oldestUpdate, notLoadedCount, event.itemsErrors)
                    is ErrorResultEvent -> FeedState.ErrorState(event.error)
                }
            )

            if (event is ErrorResultEvent || event is SuccessResultEvent) {
                FeedEventManager.reset()
            }
        }

    override fun onCleared() {
        super.onCleared()
        combineDisposable.dispose()
    }

    private data class CombineResultEventHolder(
        val t1: FeedEventManager.Event,
        val t2: ShowItems,
        val t3: Boolean,
        val t4: Long,
        val t5: OffsetDateTime?
    )

    private data class CombineResultDataHolder(
        val t1: FeedEventManager.Event,
        val t2: List<StreamWithState>,
        val t3: Long,
        val t4: OffsetDateTime?
    )

    fun togglePlayedItems(showItems: ShowItems) {
        toggleShowPlayedItems.onNext(showItems)
    }

    fun saveShowPlayedItemsToPreferences(showItems: ShowItems) =
        PreferenceManager.getDefaultSharedPreferences(application).edit {
            this.putString(
                application.getString(R.string.feed_show_played_items_key),
                showItems.toString()
            )
            this.apply()
        }

    fun getItemsVisibilityFromPreferences() = getItemsVisibilityFromPreferences(application)

    fun toggleFutureItems(showFutureItems: Boolean) {
        toggleShowFutureItems.onNext(showFutureItems)
    }

    fun saveShowFutureItemsToPreferences(showFutureItems: Boolean) =
        PreferenceManager.getDefaultSharedPreferences(application).edit {
            this.putBoolean(application.getString(R.string.feed_show_future_items_key), showFutureItems)
            this.apply()
        }

    fun getShowFutureItemsFromPreferences() = getShowFutureItemsFromPreferences(application)

    companion object {

        private fun getItemsVisibilityFromPreferences(context: Context): ShowItems {
            val s = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(
                    context.getString(R.string.feed_show_played_items_key),
                    ShowItems.DEFAULT.toString()
                ) ?: ShowItems.DEFAULT.toString()
            return ShowItems.valueOf(s)
        }

        private fun getShowFutureItemsFromPreferences(context: Context) =
            PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getString(R.string.feed_show_future_items_key), true)
        fun getFactory(context: Context, groupId: Long) = viewModelFactory {
            initializer {
                FeedViewModel(
                    App.getApp(),
                    groupId,
                    // Read initial value from preferences
                    getItemsVisibilityFromPreferences(context.applicationContext),
                    getShowFutureItemsFromPreferences(context.applicationContext)
                )
            }
        }
    }
}
