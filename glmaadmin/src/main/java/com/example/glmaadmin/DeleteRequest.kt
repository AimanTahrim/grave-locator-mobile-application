package com.example.glmaadmin

data class DeleteRequest(
    var id: String? = null,
    val deceasedId: String? = null,
    val deceasedName: String? = null,
    val birthDate: String? = null,
    val deathDate: String? = null,
    val lotNumber: String? = null,
    val lotPhoto: String? = null,
    val requestedBy: String? = null,
    val status: String? = null
)
