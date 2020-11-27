import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.merge

fun <State> combineEpics(vararg epics: Epic<State>): Epic<State> =
    { action: Observable<Any>, state: StateObservable<State> ->

        merge(
            *epics.map { epic ->
                epic(action, state)
            }.toTypedArray()
        )
    }
