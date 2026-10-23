package com.emm.mybest.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The single purchase goal row, with its price stored as cents.
 */
@Entity(tableName = "purchase_goal")
data class PurchaseGoalEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = SINGLE_ROW_ID,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "price_cents")
    val priceCents: Long,
) {
    companion object {
        const val SINGLE_ROW_ID = 1
    }
}
