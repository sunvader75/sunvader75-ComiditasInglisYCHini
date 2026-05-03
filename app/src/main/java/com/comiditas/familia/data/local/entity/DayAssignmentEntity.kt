package com.comiditas.familia.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "day_assignments",
    primaryKeys = ["date", "memberId"],
    foreignKeys = [
        ForeignKey(
            entity = FamilyMemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MealEntity::class,
            parentColumns = ["id"],
            childColumns = ["mealId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("mealId"),
        Index("date")
    ]
)
data class DayAssignmentEntity(
    val date: String, // formato: yyyy-MM-dd
    val memberId: Long,
    val mealId: Long
)
