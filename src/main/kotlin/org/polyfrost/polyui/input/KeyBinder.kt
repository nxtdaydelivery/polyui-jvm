/*
 * This file is part of PolyUI
 * PolyUI - Fast and lightweight UI framework
 * Copyright (C) 2023-2024 Polyfrost and its contributors.
 *   <https://polyfrost.org> <https://github.com/Polyfrost/polui-jvm>
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

package org.polyfrost.polyui.input

import org.jetbrains.annotations.ApiStatus
import org.polyfrost.polyui.PolyUI
import org.polyfrost.polyui.event.Event
import org.polyfrost.polyui.property.Settings
import org.polyfrost.polyui.utils.addOrReplace
import org.polyfrost.polyui.utils.fastEach

/**
 * # KeyBinder
 *
 * KeyBinder handles key bindings for the PolyUI instance.
 *
 * @see add
 * @see remove
 * @see removeAll
 */
class KeyBinder(private val settings: Settings) {
    private val listeners = ArrayList<Bind>()

    /**
     * Property which enables an optimization where the KeyBinder will only update after every frame if there are time-sensitive listeners.
     * @since 1.1.1
     */
    var hasTimeSensitiveListeners = false
        private set
    private val downMouseButtons = ArrayList<Int>(5)
    private val downUnmappedKeys = ArrayList<Int>(5)
    private val downKeys = ArrayList<Keys>(5)
    private var recordingTime = 0L
    private var modifiers: Byte = 0
    private var recordingFunc: (() -> Boolean)? = null
    private var callback: ((Bind) -> Unit)? = null

    /**
     * accept a keystroke event. This will call all keybindings that match the event.
     *
     * This method is public, but marked as internal. This is because it should only be called by the PolyUI instance, unless you are trying to externally force a keypress (which you probably shouldn't be)
     */
    @ApiStatus.Internal
    fun accept(event: Event): Boolean {
        when (event) {
            is Event.Mouse.Pressed -> {
                if (event.mods.isEmpty) {
                    cancelRecord("Cannot bind to just left click")
                    return false
                }
                downMouseButtons.add(event.button)
                completeRecording()
            }

            is Event.Mouse.Released -> {
                downMouseButtons.remove(event.button)
            }

            is Event.Focused.KeyPressed -> {
                if (event.key == Keys.ESCAPE) {
                    cancelRecord("ESC key pressed")
                    return false
                }
                downKeys.add(event.key)
                completeRecording()
            }

            is Event.Focused.KeyReleased -> {
                downKeys.remove(event.key)
            }

            else -> return false
        }
        return update(0L, modifiers)
    }

    /**
     * accept an unmapped keystroke event. This will call all keybindings that match the event.
     *
     * This method is public, but marked as internal. This is because it should only be called by the PolyUI instance, unless you are trying to externally force a keypress (which you probably shouldn't be)
     */
    @ApiStatus.Internal
    fun accept(key: Int, down: Boolean): Boolean {
        if (down) {
            downUnmappedKeys.add(key)
            completeRecording()
        } else {
            downUnmappedKeys.remove(key)
        }
        return update(0L, modifiers)
    }

    @ApiStatus.Internal
    fun update(deltaTimeNanos: Long, keyModifiers: Byte): Boolean {
        modifiers = keyModifiers
        if (!hasTimeSensitiveListeners && deltaTimeNanos > 0L) return false
        listeners.fastEach {
            if (it.update(downUnmappedKeys, downKeys, downMouseButtons, keyModifiers, deltaTimeNanos)) {
                return true
            }
        }
        return false
    }

    private fun completeRecording() {
        if (recordingFunc == null) return
        val b = Bind(
            if (downUnmappedKeys.size == 0) null else downUnmappedKeys.toIntArray(),
            if (downKeys.size == 0) null else downKeys.toTypedArray(),
            if (downMouseButtons.size == 0) null else downMouseButtons.toIntArray(),
            Modifiers(modifiers),
            recordingTime,
            recordingFunc ?: throw ConcurrentModificationException("unlucky"),
        )
        if (b in listeners) {
            cancelRecord("Duplicate keybind: $b")
        } else {
            if (settings.debug) PolyUI.LOGGER.info("Bind created: $b")
            callback?.invoke(b)
            add(b)
            recordingTime = 0L
            callback = null
            recordingFunc = null
            release()
        }
    }

    /**
     * Add a keybind to this PolyUI instance, that will be run when the given keys are pressed.
     * @since 1.1.7
     */
    fun add(vararg binds: Bind) {
        for (bind in binds) {
            add(bind)
        }
    }

    /**
     * Add a keybind to this PolyUI instance, that will be run when the given keys are pressed.
     * @since 0.21.0
     */
    fun add(bind: Bind) {
        if (bind.durationNanos > 0L) hasTimeSensitiveListeners = true
        val old = listeners.addOrReplace(bind)
        if (old != null && old !== bind) {
            PolyUI.LOGGER.warn("Keybind replaced: $bind")
        }
    }

    fun remove(bind: Bind) {
        listeners.remove(bind)
        if (hasTimeSensitiveListeners) {
            hasTimeSensitiveListeners = false
            listeners.fastEach {
                if (it.durationNanos > 0L) {
                    hasTimeSensitiveListeners = true
                    return
                }
            }
        }
    }

    fun remove(vararg binds: Bind) {
        for (bind in binds) {
            remove(bind)
        }
    }

    /**
     * Begin recording for a keybind.
     *
     * recording may be cancelled for the following reasons:
     * - the ESC key is pressed
     * - [record] is called again before the recording is completed, (i.e. starting a new one before the old one is finished)
     * - the instance already has a keybind which matched the one being recorded
     * - the keybind was attempted to be set to just left-click (which is not allowed)
     *
     * @param holdDurationNanos the duration that the keys have to be pressed in the resultant keybind
     * @param callback the function that will be called when recording is completed. **not called if it is cancelled for any reason**.
     * @param function the function that will be run when the keybind is pressed
     * @throws IllegalStateException if the keybind is already present
     * @since 0.24.0
     */
    fun record(holdDurationNanos: Long = 0L, callback: (Bind) -> Unit, function: () -> Boolean) {
        if (settings.debug) PolyUI.LOGGER.info("Recording keybind began")
        if (recordingFunc != null) cancelRecord("New recording started")
        release()
        this.callback = callback
        recordingTime = holdDurationNanos
        recordingFunc = function
    }

    @ApiStatus.Internal
    fun cancelRecord(reason: String) {
        if (recordingFunc != null) PolyUI.LOGGER.warn("Keybind recording cancelled: $reason")
        recordingTime = 0L
        callback = null
        recordingFunc = null
        release()
    }

    /**
     * Synthetically drop all pressed keys
     */
    private fun release() {
        downKeys.clear()
        downMouseButtons.clear()
        downUnmappedKeys.clear()
    }

    class Bind(val unmappedKeys: IntArray? = null, val keys: Array<Keys>? = null, val mouse: IntArray? = null, val mods: Modifiers = Modifiers(0), val durationNanos: Long = 0L, @Transient val action: () -> Boolean) {
        constructor(chars: CharArray? = null, keys: Array<Keys>? = null, mouse: IntArray? = null, mods: Modifiers = Modifiers(0), durationNanos: Long = 0L, action: () -> Boolean) : this(
            chars?.map {
                it.code
            }?.toIntArray(),
            keys, mouse, mods, durationNanos, action,
        )

        constructor(char: Char, keys: Array<Keys>? = null, mouse: IntArray? = null, mods: Modifiers = Modifiers(0), durationNanos: Long = 0L, action: () -> Boolean) : this(intArrayOf(char.code), keys, mouse, mods, durationNanos, action)
        constructor(unmappedKeys: IntArray? = null, keys: Array<Keys>? = null, mouse: Array<Mouse>? = null, mods: Modifiers = Modifiers(0), durationNanos: Long = 0L, action: () -> Boolean) : this(
            unmappedKeys, keys,
            mouse?.map {
                it.value.toInt()
            }?.toIntArray(),
            mods, durationNanos, action,
        )

        constructor(unmappedKeys: IntArray? = null, keys: Array<Keys>? = null, mouse: Mouse? = null, mods: Modifiers = Modifiers(0), durationNanos: Long = 0L, action: () -> Boolean) : this(
            unmappedKeys, keys,
            mouse?.value?.let {
                intArrayOf(it.toInt())
            },
            mods, durationNanos, action,
        )

        constructor(unmappedKeys: IntArray? = null, key: Keys? = null, mouse: Array<Mouse>? = null, mods: Modifiers = Modifiers(0), durationNanos: Long = 0L, action: () -> Boolean) : this(
            unmappedKeys,
            key?.let {
                arrayOf(it)
            },
            mouse, mods, durationNanos, action,
        )

        @Transient
        private var time = 0L

        @Transient
        private var ran = false

        internal fun update(c: ArrayList<Int>, k: ArrayList<Keys>, m: ArrayList<Int>, mods: Byte, deltaTimeNanos: Long): Boolean {
            if (durationNanos == 0L && deltaTimeNanos > 0L) return false
            if (unmappedKeys?.matches(c) != false && keys?.matches(k) != false && mouse?.matches(m) != false && this.mods.equal(mods)) {
                if (!ran) {
                    if (durationNanos != 0L) {
                        time += deltaTimeNanos
                        if (time >= durationNanos) {
                            ran = true
                            return action()
                        }
                    } else {
                        ran = true
                        return action()
                    }
                }
            } else {
                time = 0L
                ran = false
            }
            return false
        }

        override fun toString(): String {
            val sb = StringBuilder()
            sb.append("KeyBind(")
            sb.append(keysToString())
            sb.append(")")
            return sb.toString()
        }

        fun keysToString(): String {
            val sb = StringBuilder()
            val s = mods.prettyName
            sb.append(s)
            if (unmappedKeys != null) {
                if (s.isNotEmpty()) sb.append(" + ")
                for (c in unmappedKeys) {
                    sb.append(c.toChar())
                    sb.append(" + ")
                }
            }
            if (keys != null) {
                if (s.isNotEmpty()) sb.append(" + ")
                for (k in keys) {
                    sb.append(Keys.toStringPretty(k))
                    sb.append(" + ")
                }
            }
            if (mouse != null) {
                if (s.isNotEmpty()) sb.append(" + ")
                for (m in mouse) {
                    sb.append(Mouse.toStringPretty(Mouse.fromValue(m)))
                    sb.append(" + ")
                }
            }
            sb.setLength(sb.length - 3)
            return sb.toString()
        }

        override fun equals(other: Any?): Boolean {
            if (other !is Bind) return false
            if (other.unmappedKeys?.contentEquals(unmappedKeys) == false) return false
            if (other.keys?.contentEquals(keys) == false) return false
            if (other.mouse?.contentEquals(mouse) == false) return false
            if (other.mods != mods) return false
            if (other.durationNanos != durationNanos) return false
            return true
        }

        override fun hashCode(): Int {
            var result = unmappedKeys?.contentHashCode() ?: 0
            result = 31 * result + (keys?.contentHashCode() ?: 0)
            result = 31 * result + (mouse?.contentHashCode() ?: 0)
            result = 31 * result + mods.hashCode()
            result = 31 * result + durationNanos.hashCode()
            return result
        }

        private fun <T> Array<T>.matches(other: ArrayList<T>): Boolean {
            if (other.size == 0) return false
            for (i in this) {
                if (i !in other) return false
            }
            return true
        }

        private fun IntArray.matches(other: ArrayList<Int>): Boolean {
            if (other.size == 0) return false
            for (i in this) {
                if (i !in other) return false
            }
            return true
        }
    }
}
