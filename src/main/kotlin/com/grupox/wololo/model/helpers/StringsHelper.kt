package com.grupox.wololo.model.helpers

import java.text.Normalizer

fun formatLine(line: String): String =
        line.substringBefore('.')
                .removeSurrounding(" ")
                .replace('_', ' ')
                .toLowerCase()
                .split(' ')
                .joinToString(" ") { if (it.length > 3) it.capitalize() else it }
                .capitalize()

fun unaccent(str: String): String {
    return str.map { unaccentChar(it) }.joinToString("")
}

private fun unaccentChar(char: Char): Char {
    val escapeRegex = "[ñÑ]".toRegex()
    val asString = char.toString()

    if(asString.matches(escapeRegex))
        return char

    val regexUnaccent = "\\p{InCombiningDiacriticalMarks}+".toRegex()
    val temp = Normalizer.normalize(asString, Normalizer.Form.NFD)
    return regexUnaccent.replace(temp, "")[0]
}

private fun unpunctuate(str: String): String {
    val regexPunctuation = "[.,;]".toRegex()
    return regexPunctuation.replace(str, "")
}

fun formatTownName(townName: String) = unpunctuate(unaccent(townName)).toUpperCase()