package com.tomtom.sdk.extension.gradle.instrumentation.androidx.room

enum class RoomMethodType {
    TRANSACTION,
    QUERY,
    QUERY_WITH_TRANSACTION;

    fun isTransaction() = this == TRANSACTION || this == QUERY_WITH_TRANSACTION
}
