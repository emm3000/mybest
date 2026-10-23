package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.emm.mybest.core.datetime.YearMonthValue

/**
 * One paid installment per debt and month, with the amount it took off the balance in cents. Deleting the debt deletes
 * its payments.
 */
@Entity(
    tableName = "debt_payment",
    primaryKeys = ["debt_id", "month"],
    foreignKeys = [
        ForeignKey(
            entity = DebtEntity::class,
            parentColumns = ["id"],
            childColumns = ["debt_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class DebtPaymentEntity(

    @ColumnInfo(name = "debt_id")
    val debtId: Long,

    @ColumnInfo(name = "month")
    val month: YearMonthValue,

    @ColumnInfo(name = "amount_cents")
    val amountCents: Long,
)
