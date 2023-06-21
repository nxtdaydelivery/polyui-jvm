/*
 * This file is part of PolyUI
 * PolyUI - Fast and lightweight UI framework
 * Copyright (C) 2023 Polyfrost and its contributors.
 *   <https://polyfrost.cc> <https://github.com/Polyfrost/polui-jvm>
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *     PolyUI is licensed under the terms of version 3 of the GNU Lesser
 * General Public License as published by the Free Software Foundation,
 * AND the simple request that you adequately accredit us if you use PolyUI.
 * See details here <https://github.com/Polyfrost/polyui-jvm/ACCREDITATION.md>.
 *     This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 * License.  If not, see <https://www.gnu.org/licenses/>.
 */

/** This file contains various utilities for arrays and arraylists. */
@file:Suppress("ReplaceManualRangeWithIndicesCalls", "ReplaceSizeZeroCheckWithIsEmpty", "UNCHECKED_CAST")
@file:JvmName("ArrayUtils")

package cc.polyfrost.polyui.utils

/**
 * [forEach] re-implementation that doesn't allocate any memory.
 *
 * Utilizes the [RandomAccess] trait.
 *
 * @param f The function to apply to each element.
 *
 * @see [fastEachReversed]
 */
inline fun <L, E> L.fastEach(f: (E) -> Unit) where L : List<E>, L : RandomAccess {
    if (this.size == 0) return
    for (i in 0 until this.size) {
        f(this[i])
    }
}

/**
 * [forEach] re-implementation that doesn't allocate any memory, but it traverses the list backwards.
 *
 * Utilizes the [RandomAccess] trait.
 *
 * @param f The function to apply to each element.
 *
 * @see [fastEach]
 * @since 0.18.5
 */
inline fun <L, E> L.fastEachReversed(f: (E) -> Unit) where L : List<E>, L : RandomAccess {
    if (this.size == 0) return
    for (i in this.size - 1 downTo 0) {
        f(this[i])
    }
}

/**
 * [forEachIndexed] re-implementation that doesn't allocate any memory.
 *
 * Utilizes the [RandomAccess] trait.
 *
 * @param f The function to apply to each element.
 *
 * @see [fastRemoveIf]
 */
inline fun <L, E> L.fastEachIndexed(f: (Int, E) -> Unit) where L : List<E>, L : RandomAccess {
    if (this.size == 0) return
    for (i in 0 until this.size) {
        f(i, this[i])
    }
}

/**
 * [forEachIndexed] re-implementation that doesn't allocate any memory, but it traverses the list backwards.
 *
 * Utilizes the [RandomAccess] trait.
 *
 * @param f The function to apply to each element.
 *
 * @see [fastEachIndexed]
 * @since 0.18.5
 */
inline fun <L, E> L.fastEachIndexedReversed(f: (Int, E) -> Unit) where L : List<E>, L : RandomAccess {
    if (this.size == 0) return
    for (i in this.size - 1 downTo 0) {
        f(i, this[i])
    }
}

/**
 * [removeIf][kotlin.collections.filterInPlace] re-implementation that doesn't allocate any memory.
 *
 * Utilizes the [RandomAccess] trait.
 *
 * @see [fastEach]
 * @see [fastEachIndexedReversed]
 * @return false if the list is empty, true otherwise.
 */
inline fun <L, E> L.fastRemoveIf(f: (E) -> Boolean): Boolean where L : MutableList<E>, L : RandomAccess {
    if (this.size == 0) return false
    var max = this.size
    var i = 0
    while (i < max) {
        if (f(this[i])) {
            this.removeAt(i)
            max--
        }
        i++
    }
    return true
}

/** Returns `true` if **at least one element** matches the given [predicate][f]. */
inline fun <L, E> L.anyAre(f: (E) -> Boolean): Boolean where L : List<E>, L : RandomAccess {
    if (this.size == 0) return false
    for (i in 0 until this.size) {
        if (f(this[i])) return true
    }
    return false
}

/** Returns `true` if **no elements** match the given [predicate][f].
 *
 * **Note this function** will return `true` if the list is empty, according to the principle of [Vacuous truth](https://en.wikipedia.org/wiki/Vacuous_truth),
 * because there are no elements that match the given predicate.
 */
inline fun <L, E> L.noneAre(f: (E) -> Boolean): Boolean where L : List<E>, L : RandomAccess {
    if (this.size == 0) return true
    for (i in 0 until this.size) {
        if (f(this[i])) return false
    }
    return true
}

/**
 * Returns `true` if **all elements** match the given [predicate][f].
 *
 * **Note this function** will return `true` if the list is empty, according to the principle of [Vacuous truth](https://en.wikipedia.org/wiki/Vacuous_truth),
 * and the fact that there are no elements that don't match the given predicate.
 */
inline fun <L, E> L.allAre(f: (E) -> Boolean): Boolean where L : List<E>, L : RandomAccess {
    if (this.size == 0) return true
    for (i in 0 until this.size) {
        if (!f(this[i])) return false
    }
    return true
}

/**
 * Returns a list of all elements sorted descending according to natural sort order of the value returned by specified selector function.
 *
 * The sort is *stable*. It means that equal elements preserve their order relative to each other after sorting.
 */
inline fun <L, reified E, R : Comparable<R>> L.sortedByDescending(crossinline selector: (E) -> R?): L where L : MutableList<E>, L : RandomAccess {
    if (this.size <= 1) return this
    return this.toTypedArray().sortedByDescending(selector).toMutableList() as L
}

/**
 * Returns the sum of all values produced by [selector] function applied to
 * each element in the collection, for floats.
 *
 * @param selector a function that extracts a [Float] property of an element
 * @return the sum of all values produced by [selector]
 *
 * @see [fastEach]
 */
inline fun <L, E> L.sumOf(selector: (E) -> Float): Float where L : List<E>, L : RandomAccess {
    if (this.size == 0) return 0f
    var sum = 0f
    fastEach {
        sum += selector(it)
    }
    return sum
}

/**
 * Moves the given element from the [from] index to the [to] index.
 *
 * **Note**: this method makes absolutely no attempt to verify if the given
 * indices are valid.
 *
 * @param from the index of the element to move
 * @param to the index to move the element to
 */
fun <E> Array<E>.moveElement(from: Int, to: Int) {
    val item = this[from]
    this[from] = this[to]
    this[to] = item
}

/**
 * Append [element] to the end of this array, that is, the first empty index.
 *
 * @param element the element to append
 * @param stillPutOnFail set this to `true` if you want it to [add][Array.plus]
 *                       the element (causes a reallocation!) even if the array
 *                       is full. (default: `false`)
 * @throws IndexOutOfBoundsException if the array is full and [stillPutOnFail]
 *                                   is set to `false`.
 */
fun <E> Array<E>.append(element: E, stillPutOnFail: Boolean = false): Array<E> {
    forEachIndexed { i, it ->
        if (it == null) {
            this[i] = element
            return this
        }
    }
    if (stillPutOnFail) {
        return this.plus(element)
    } else {
        throw IndexOutOfBoundsException("Array is already full!")
    }
}

fun <T> Iterable<T>.toArrayList(): ArrayList<T> {
    return if (this is ArrayList) {
        this
    } else {
        this.toMutableList() as ArrayList<T>
    }
}

fun <T> Array<T>.toArrayList(): ArrayList<T> = this.toMutableList() as ArrayList<T>

/**
 * Returns the index of the [element] in the list or runs the [or] parameter if the element is not found.
 *
 * This returns [Nothing], and is used by default to throw an exception; hence why it's called "or die".
 */
fun <E> List<E>.indexOfOrDie(element: E, or: () -> Nothing = throw IllegalArgumentException("Element $element not found in list $this!")): Int {
    val index = this.indexOf(element)
    return if (index == -1) or() else index
}

/**
 * Represents a generic pair of two values.
 *
 * There is no meaning attached to values in this class, it can be used for any purpose.
 * Pair exhibits value semantics, i.e. two pairs are equal if both components are equal.
 *
 * An example of decomposing it into values:
 *
 * @param A type of the first value.
 * @param B type of the second value.
 * @property first First value.
 * @property second Second value.
 * @constructor Creates a new instance of Pair.
 * @see Pair
 */
data class MutablePair<A, B>(
    var first: A,
    var second: B
) {

    /**
     * Returns string representation of the [MutablePair] including its [first] and [second] values.
     */
    override fun toString(): String = "($first, $second)"

    /**
     * Converts this pair into a list.
     */
    fun toList() = mutableListOf(first, second)

    /**
     * Converts this mutable pair into an immutable [Pair].
     */
    fun toPair() = Pair(first, second)
}

/**
 * Represents a triad of values.
 *
 * There is no meaning attached to values in this class, it can be used for any purpose.
 * Triple exhibits value semantics, i.e. two triples are equal if all three components are equal.
 * An example of decomposing it into values:
 *
 * @param A type of the first value.
 * @param B type of the second value.
 * @param C type of the third value.
 * @property first First value.
 * @property second Second value.
 * @property third Third value.
 * @see Triple
 */
data class MutableTriple<A, B, C>(
    var first: A,
    var second: B,
    var third: C
) {

    /**
     * Returns string representation of the [MutableTriple] including its [first], [second] and [third] values.
     */
    override fun toString(): String = "($first, $second, $third)"

    /**
     * Converts this triple into a list.
     */
    fun toList() = mutableListOf(first, second, third)

    /**
     * Converts this mutable pair into an immutable [Triple].
     */
    fun toTriple() = Triple(first, second, third)
}

/** convert this triple to a [mutable triple][MutableTriple]. */
fun <A, B, C> Triple<A, B, C>.toMutableTriple() = MutableTriple(first, second, third)

/** convert this pair to a [mutable pair][MutablePair]. */
fun <A, B> Pair<A, B>.toMutablePair() = MutablePair(first, second)
