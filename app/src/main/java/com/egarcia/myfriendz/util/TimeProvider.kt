package com.egarcia.myfriendz.util

import java.time.LocalDate
import javax.inject.Inject

class TimeProvider @Inject constructor() {
    fun today(): LocalDate = LocalDate.now()
}

