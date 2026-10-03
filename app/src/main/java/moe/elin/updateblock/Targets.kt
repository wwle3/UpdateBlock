package moe.elin.updateblock

object Targets {
    const val NAGRAM = "xyz.nextalone.nagram"
    const val NAGRAM_X = "nu.gpu.nagram"

    val PACKAGES: Set<String> = setOf(NAGRAM, NAGRAM_X)
    val RECOMMENDED_SCOPE: List<String> = PACKAGES.toList()

    fun packageNameOf(processName: String): String = processName.substringBefore(':')
}
