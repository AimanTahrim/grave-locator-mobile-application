package com.example.glmaadmin

data class UserProfile(
    val email: String,
    val name: String = "",
    val profileImageUrl: String = ""
){
    constructor() : this("", "", "")
}

