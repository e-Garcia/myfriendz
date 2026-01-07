package com.egarcia.myfriendz.data.permission

import android.content.Context
import androidx.core.content.ContextCompat
import com.egarcia.myfriendz.domain.permission.PermissionGateway
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import android.content.pm.PackageManager

class AndroidPermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context
) : PermissionGateway {
    override fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}

