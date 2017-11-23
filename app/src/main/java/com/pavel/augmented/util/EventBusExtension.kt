package com.pavel.augmented.util

import org.greenrobot.eventbus.EventBus

fun EventBus.toggleRegister(subscriber: Any) {
    if (isRegistered(subscriber)) {
        unregister(subscriber)
    } else {
        register(subscriber)
    }
}