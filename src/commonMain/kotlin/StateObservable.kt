import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.ObservableObserver
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.subject.publish.PublishSubject

class StateObservable<S>(input: Observable<S>, private var state: S) : Observable<S> {
    private val notifier = PublishSubject<S>();

    override fun subscribe(observer: ObservableObserver<S>) {
        notifier.subscribe(observer)
        observer.onNext(state)
    }

    fun getValue(): S {
        return state
    }

    init {
        input.subscribe { value ->
            if (value != state) {
                state = value
                notifier.onNext(value)
            }
        }
    }
}
