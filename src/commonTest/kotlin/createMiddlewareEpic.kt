import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.flatMap
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.merge
import com.badoo.reaktive.observable.observableOf
import com.badoo.reaktive.observable.observableOfEmpty
import com.badoo.reaktive.observable.ofType
import org.reduxkotlin.ActionTypes
import org.reduxkotlin.Middleware
import org.reduxkotlin.Reducer
import org.reduxkotlin.applyMiddleware
import org.reduxkotlin.createStore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CreateMiddlewareEpicTest {
    object Action

    object Ping
    data class Pong(val input: Any, val state: Int)

    object First
    object Second
    object Third
    data class StateUpdate<State>(val state: State)

    @Test
    fun itShouldProvideEpicsAStreamOfActionAndAStreamOfState() {
        val reducer: Reducer<MutableList<Any>> = { state, action ->
            state.add(action)
            state
        }

        var called = false

        val epic : Epic<MutableList<Any>> = { _, _ ->
            called = true
            observableOfEmpty()
        }

        val epicMiddleware = createEpicMiddleware(epic)

        val assertMiddleware: Middleware<MutableList<Any>> = {
            {
                {
                    assertTrue { called }
                }
            }
        }

        val store = createStore(reducer, mutableListOf(), applyMiddleware(epicMiddleware, assertMiddleware))
        store.dispatch(Action)
    }

    @Test
    fun itShouldUpdateStateAfterAnActionGoesThroughReducersButBeforeEpics() {
        val actions = mutableListOf<Any>()
        val reducer: Reducer<Int> = { state, action ->
            actions.add(action)
            if(action is Ping) state+1 else state
        }
        val epic: Epic<Int> = { action, state ->
            action.ofType<Ping>()
            .map { input ->
                Pong(input, state.getValue())
            }
        }
        val epicMiddleware = createEpicMiddleware(epic)

        val store = createStore(reducer, 0, applyMiddleware(epicMiddleware))

        store.dispatch(Ping)
        store.dispatch(Ping)

        assertEquals(store.getState(), 2)
        assertEquals(listOf(
            ActionTypes.INIT,
            Ping,
            Pong(Ping, 1),
            Ping,
            Pong(Ping, 2)
        ), actions)
    }


    fun itShouldQueueStateUpdates() {
        data class State(val action: Any?, val value: Int)
        val actions = mutableListOf<Any>()


        val reducer: Reducer<State> = {
                state, action ->
            actions.add(action)

            when(action) {
                is First, is Second, is Third, is State -> State(action, state.value + 1)
                else -> state
            }
        }

        val epic: Epic<State> = {
            action, state ->
            action
                .ofType<First>()
                .flatMap {
                    merge(
                        state
                            .filter { it.value < 6 }
                            .map { StateUpdate(it) },
                        observableOf(Second, Third)
                    )
                }
        }

        val epicMiddleware = createEpicMiddleware(epic)
        val store = createStore(reducer, State(null, 0), applyMiddleware(epicMiddleware))

        store.dispatch(First)

        assertEquals(8,store.getState().value)
        assertEquals(mutableListOf(
            ActionTypes.INIT,
            First,
            StateUpdate(State(First, 1)),
            Second,
            Third,
            StateUpdate(State(State(First, 1), 2)),
            StateUpdate(State(Second, 3)),
            StateUpdate(State(Third, 4))
        ), actions)

    }
}


