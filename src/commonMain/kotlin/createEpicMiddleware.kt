import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.subject.publish.PublishSubject
import org.reduxkotlin.Dispatcher
import org.reduxkotlin.Store

fun <State> createEpicMiddleware(epic: Epic<State>) = { store: Store<State> ->
    val actionSubject = PublishSubject<Any>()
    val stateSubject = PublishSubject<State>()

    val state = StateObservable(stateSubject, store.getState())

    val result = epic(actionSubject, state)

    result.subscribe {
        println(it)
    }

    result.subscribe {
        store.dispatch(it)
    };


    { next: Dispatcher ->
        { action: Any ->
            val newResult = next(action)
            stateSubject.onNext(store.state)
            actionSubject.onNext(action)

            newResult
        }
    }

}
