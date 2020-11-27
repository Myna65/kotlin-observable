import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.subject.publish.PublishSubject
import kotlin.test.Test
import kotlin.test.assertEquals

class StateObservableTest {
    @Test
    fun testShouldMirrorTheSourceObject() {
        val input = PublishSubject<String>()
        val state = StateObservable(input, "first")
        var result: String? = null;

        state.subscribe { newState -> result = newState }

        assertEquals("first", result)
        input.onNext("second")
        assertEquals("second", result)
        input.onNext("third")
        assertEquals("third", result)
    }

    @Test
    fun shouldCacheLastStateOnTheValueProperty() {
        val input = PublishSubject<String>()
        val state = StateObservable(input, "first")

        assertEquals("first", state.getValue())
        input.onNext("second")
        assertEquals("second", state.getValue())
    }

    data class Box(var value: String)

    @Test
    fun testShouldOnlyUpdateWhenTheNextValueShallowlyDiffers() {
        val input = PublishSubject<Any>()
        val first = Box("First")
        val state = StateObservable(input, first)

        var callCount = 0
        state.subscribe { callCount++ }

        assertEquals(first, state.getValue())
        assertEquals(1, callCount)

        input.onNext(first)
        assertEquals(first, state.getValue())
        assertEquals(1, callCount)


        first.value = "something else"
        input.onNext(first)
        assertEquals(first, state.getValue())
        assertEquals(1, callCount)

        val second = Box("second")
        input.onNext(second)

        assertEquals(second, state.getValue())
        assertEquals(2, callCount)
    }
}
