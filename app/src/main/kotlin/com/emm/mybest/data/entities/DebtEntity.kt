package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One debt with its balance and installment stored as cents and its payment day of the month (1..31).
 */
@Entity(tableName = "debt")
data class DebtEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "balance_cents")
    val balanceCents: Long,

    @ColumnInfo(name = "installment_cents")
    val installmentCents: Long,

    @ColumnInfo(name = "payment_day")
    val paymentDay: Int,
)
