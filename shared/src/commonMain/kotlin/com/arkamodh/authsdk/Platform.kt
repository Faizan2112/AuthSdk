package com.arkamodh.authsdk

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform