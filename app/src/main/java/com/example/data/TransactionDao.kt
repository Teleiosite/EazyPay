package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("UPDATE transactions SET syncStatus = 'Synced' WHERE syncStatus = 'Pending'")
    suspend fun syncPendingTransactions()

    @Query("SELECT * FROM transactions ORDER BY id DESC LIMIT 1")
    suspend fun getLastTransaction(): TransactionEntity?

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}
