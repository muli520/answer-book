package com.answerbook.glass.data

import kotlin.random.Random

/**
 * 书页本轮的回答方向。
 *
 * SAFE 不参与普通洗牌，只在生死、爱国、政治等敏感问题中启用。
 */
enum class AnswerStance(val label: String) {
    AFFIRM("肯定"),
    NEUTRAL("中立"),
    OPPOSE("反对"),
    SAFE("安全正向")
}

/**
 * 用洗牌袋而不是独立随机数来抽立场：
 * - 每三轮一定包含肯定、中立、反对各一次；
 * - 抽完再洗，跨轮也不会出现同一个立场连续两次；
 * - 这样可以避免“每次都是肯定”的体感，同时保留随机性。
 */
class StanceDeck(private val random: Random = Random.Default) {

    private val bag = ArrayDeque<AnswerStance>()
    private var previous: AnswerStance? = null

    fun next(): AnswerStance {
        if (bag.isEmpty()) refill()
        return bag.removeFirst().also { previous = it }
    }

    private fun refill() {
        val fresh = listOf(
            AnswerStance.AFFIRM,
            AnswerStance.NEUTRAL,
            AnswerStance.OPPOSE
        ).shuffled(random).toMutableList()

        // 避免洗牌袋边界刚好把同一个立场接在一起。
        if (fresh.size > 1 && fresh.first() == previous) {
            val first = fresh[0]
            fresh[0] = fresh[1]
            fresh[1] = first
        }

        bag.addAll(fresh)
    }
}
