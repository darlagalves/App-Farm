package com.example.fazendinha.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Fazenda(
    var id: String,
    var name: String,
    var valorPropriedade: Double,
    var qtdFuncionarios: Int
): Parcelable{
    fun doesMatchSearchQueryName(query: String): Boolean {
        val matchingCombinations = listOf(
            "$name",
            "${name.first()}",
        )
        return matchingCombinations.any {
            it.contains(query, ignoreCase = true)
        }
    }
}