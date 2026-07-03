package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "food", "transport", "print", "topup"
    val timestamp: Long,
    val amount: Double,
    val isDebit: Boolean,
    val syncStatus: String, // "Synced" or "Pending"
    val hash: String = "",
    val prevHash: String = "",
    val signature: String = "",
    
    // Production & Audit Extensions
    val txRef: String = "",
    val payerId: String = "",
    val payeeId: String = "",
    val deviceId: String = "",
    val nfcCardId: String = "",
    val fee: Double = 0.0,
    val campusId: String = "Babcock-Main",
    val idempotencyKey: String = ""
)
