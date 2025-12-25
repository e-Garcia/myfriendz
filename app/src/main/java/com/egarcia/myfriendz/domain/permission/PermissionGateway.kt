package com.egarcia.myfriendz.domain.permission

/**
 * Domain-level permission checker contract. Implementations live in the platform layer.
 */
interface PermissionGateway {
    fun hasPermission(permission: String): Boolean
}

