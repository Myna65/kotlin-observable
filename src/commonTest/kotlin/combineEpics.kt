import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.ofType
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.subject.publish.PublishSubject
import kotlin.test.Test
import kotlin.test.assertEquals

class CombineEpicsTest {
    object Action1
    object Action2
    object Delegated1
    object Delegated2

    @Test
    fun testCombineEpics() {

        val epic1 : Epic<Any> = { action : Observable<Any>, _: Observable<Any> ->
            action.ofType<Action1>()
                .map { Delegated1 }
        }

        val epic2 : Epic<Any> = { action : Observable<Any>, _: Observable<Any> ->
            action.ofType<Action2>()
                .map { Delegated2 }
        }

        val epic = combineEpics(epic1, epic2)

        val state = StateObservable<Any>(PublishSubject(), 2)
        val actions = PublishSubject<Any>()
        val result = epic(actions, state)
        val emitted = mutableListOf<Any>()

        result.subscribe { action: Any -> emitted.add(action) }

        actions.onNext(Action1)
        actions.onNext(Action2)

        assertEquals(
            listOf(Delegated1, Delegated2),
            emitted
        )
    }

}
