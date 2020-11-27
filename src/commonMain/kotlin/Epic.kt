import com.badoo.reaktive.observable.Observable

typealias Epic<State> = (action: Observable<Any>, state: StateObservable<State>) -> Observable<Any>






