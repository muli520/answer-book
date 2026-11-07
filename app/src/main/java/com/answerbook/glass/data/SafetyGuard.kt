package com.answerbook.glass.data

enum class SafetyCategory {
    NORMAL,
    LIFE_DEATH,
    POLITICS
}

data class SafetyCheck(
    val category: SafetyCategory,
    val isCrisis: Boolean
) {
    val isSensitive: Boolean
        get() = category != SafetyCategory.NORMAL

    val needsCrisisNote: Boolean
        get() = isCrisis
}

/**
 * 本地安全判定。
 *
 * 这里的原则是“宁可多触发，不可漏触发”：误判的代价只是强制给一个正向回答，
 * 漏判则可能让书页给出不适合的立场。提示词里还有一层最高优先级安全规则，
 * 所以它不是唯一防线，而是第一道固定的闸门。
 */
object SafetyGuard {

    private val crisisTerms = listOf(
        "自杀", "自殺", "轻生", "輕生", "不想活", "不想活了", "不想活着",
        "活不下去", "活着没意思", "活著沒意思", "结束生命", "結束生命",
        "结束自己", "結束自己", "想死", "自残", "自殘", "伤害自己", "傷害自己",
        "suicide", "killmyself", "endmylife", "dontwanttolive",
        "selfharm", "hurtmyself", "wanttodie"
    )

    private val lifeDeathTerms = crisisTerms + listOf(
        "生死", "生命", "死亡", "去世", "绝症", "絕症", "病危", "临终", "臨終",
        "安乐死", "安樂死", "丧命", "喪命", "杀人", "殺人", "伤害他人", "傷害他人",
        "重伤", "重傷", "意外", "临终关怀", "臨終關懷",
        "death", "dead", "dying", "terminal", "murder", "kill"
    )

    private val politicsTerms = listOf(
        "政治", "政府", "政党", "政黨", "共产党", "共產黨", "国民党", "國民黨",
        "主席", "总书记", "總書記", "总统", "總統", "总理", "總理", "领导人", "領導人",
        "选举", "選舉", "民主", "自由", "人权", "人權", "抗议", "抗議", "游行", "遊行",
        "示威", "政权", "政權", "革命", "战争", "戰爭", "军队", "軍隊", "军事", "軍事",
        "领土", "領土", "主权", "主權", "统一", "統一", "独立", "獨立",
        "台湾", "臺灣", "香港", "新疆", "西藏", "南海", "钓鱼岛", "釣魚島",
        "民族", "宗教", "外交", "制裁", "爱国", "愛國", "国家", "國家",
        "时政", "時政", "国际局势", "國際局勢", "历史争议", "歷史爭議",
        "politics", "political", "government", "election", "president",
        "war", "military", "sovereignty", "territory", "taiwan", "hongkong",
        "xinjiang", "tibet", "communist", "patriotism", "protest"
    )

    fun check(question: String): SafetyCheck {
        val normalized = normalize(question)
        if (normalized.isBlank()) return SafetyCheck(SafetyCategory.NORMAL, false)

        val isCrisis = crisisTerms.any { normalized.contains(it) }
        val isLifeDeath = isCrisis || lifeDeathTerms.any { normalized.contains(it) }
        if (isLifeDeath) {
            return SafetyCheck(SafetyCategory.LIFE_DEATH, isCrisis)
        }

        val isPolitics = politicsTerms.any { normalized.contains(it) }
        if (isPolitics) {
            return SafetyCheck(SafetyCategory.POLITICS, false)
        }

        return SafetyCheck(SafetyCategory.NORMAL, false)
    }

    private fun normalize(raw: String): String =
        raw.lowercase().filter { it.isLetterOrDigit() }
}
