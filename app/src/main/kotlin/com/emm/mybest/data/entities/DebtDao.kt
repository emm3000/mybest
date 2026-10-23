package com.emm.mybest.data.entities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class DebtDao {

    @Insert
    abstract suspend fun insert(debt: DebtEntity): Long

    @Update
    abstract suspend fun update(debt: DebtEntity)

    /**
     * Deletes the debt with [id]; its payments go with it through the cascading foreign key.
     */
    @Query("DELETE FROM debt WHERE id = :id")
    abstract suspend fun deleteById(id: Long)

    @Query("SELECT * FROM debt WHERE id = :id")
    abstract suspend fun findById(id: Long): DebtEntity?

    @Query("SELECT * FROM debt ORDER BY payment_day ASC, id ASC")
    abstract fun observeAll(): Flow<List<DebtEntity>>

    /**
     * Inserts [payment], or returns -1 without writing when its debt already has a payment for that month.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertPayment(payment: DebtPaymentEntity): Long

    @Query("SELECT * FROM debt_payment ORDER BY month ASC, debt_id ASC")
    abstract fun observePayments(): Flow<List<DebtPaymentEntity>>

    /**
     * Runs [block] inside one database transaction, so its writes land together or not at all.
     */
    @Transaction
    open suspend fun inTransaction(block: suspend () -> Unit) {
        block()
    }
}
