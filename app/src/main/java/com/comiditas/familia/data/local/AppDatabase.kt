package com.comiditas.familia.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.comiditas.familia.data.local.dao.DayAssignmentDao
import com.comiditas.familia.data.local.dao.FamilyMemberDao
import com.comiditas.familia.data.local.dao.MealDao
import com.comiditas.familia.data.local.dao.MealPreferenceDao
import com.comiditas.familia.data.local.entity.DayAssignmentEntity
import com.comiditas.familia.data.local.entity.FamilyMemberEntity
import com.comiditas.familia.data.local.entity.MealEntity
import com.comiditas.familia.data.local.entity.MealPreferenceEntity

@Database(
    entities = [
        FamilyMemberEntity::class,
        MealEntity::class,
        MealPreferenceEntity::class,
        DayAssignmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun familyMemberDao(): FamilyMemberDao
    abstract fun mealDao(): MealDao
    abstract fun mealPreferenceDao(): MealPreferenceDao
    abstract fun dayAssignmentDao(): DayAssignmentDao
}
