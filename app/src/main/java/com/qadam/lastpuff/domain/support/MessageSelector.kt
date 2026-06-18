package com.qadam.lastpuff.domain.support

import kotlin.random.Random

class MessageSession(seed: Int = Random.nextInt()) {
    private val random = Random(seed)
    private val used = mutableMapOf<MessageCategory, MutableSet<Int>>()

    fun pick(category: MessageCategory): String {
        val pool = SupportMessageBank.get(category)
        if (pool.isEmpty()) return ""
        val usedSet = used.getOrPut(category) { mutableSetOf() }
        val available = pool.indices.filter { it !in usedSet }
        val index = if (available.isEmpty()) {
            usedSet.clear()
            random.nextInt(pool.size)
        } else {
            available[random.nextInt(available.size)]
        }
        usedSet.add(index)
        return pool[index]
    }

    fun pickHumorRarely(): String? =
        if (random.nextFloat() < 0.22f) pick(MessageCategory.HUMOR) else null

    fun pickAction(mode: SosMode): String = when (mode) {
        SosMode.BREATHING -> pick(MessageCategory.BREATH)
        SosMode.FAMILY -> pick(MessageCategory.FAMILY)
        SosMode.CHALLENGE -> pick(MessageCategory.CHALLENGE)
    }
}
