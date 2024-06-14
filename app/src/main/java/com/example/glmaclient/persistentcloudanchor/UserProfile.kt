package com.example.glmaclient.persistentcloudanchor

data class UserProfile(val email: String, val name: String = "", val profileImageUrl: String = ""){
    constructor() : this("", "", "")
}

