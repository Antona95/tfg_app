package com.example.app_tfg

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform