package com.radmir.storyteller

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform