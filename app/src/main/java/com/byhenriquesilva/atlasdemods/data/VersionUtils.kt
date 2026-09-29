package com.byhenriquesilva.atlasdemods.data

/**
 * Mesma ordenação do site (`compareVersionsDesc` em `src/data/mods.ts`): o
 * Minecraft trocou de esquema de versão ("1.x.x" -> "26.x"), então qualquer
 * versão sem o prefixo "1." é sempre mais nova que qualquer "1.x.x",
 * independente do valor numérico — família primeiro, depois numérico dentro
 * da mesma família.
 */
private fun versionSortKey(v: String): Pair<Boolean, List<Int>> {
    val legacy = v.startsWith("1.")
    val parts = v.split(".").map { it.toIntOrNull() ?: 0 }
    return legacy to parts
}

val versionComparatorDesc = Comparator<String> { a, b ->
    val (legacyA, partsA) = versionSortKey(a)
    val (legacyB, partsB) = versionSortKey(b)
    if (legacyA != legacyB) return@Comparator if (legacyA) 1 else -1
    val len = maxOf(partsA.size, partsB.size)
    for (i in 0 until len) {
        val diff = (partsB.getOrElse(i) { 0 }) - (partsA.getOrElse(i) { 0 })
        if (diff != 0) return@Comparator diff
    }
    0
}

/** Versões de Minecraft presentes na lista, da mais nova pra mais antiga — igual ao MC_VERSIONS do site. */
fun distinctVersionsDesc(mcValues: Iterable<String>): List<String> =
    mcValues.toSet().sortedWith(versionComparatorDesc)
