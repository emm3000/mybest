package com.emm.mybest.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.emm.mybest.domain.models.Habit
import com.emm.mybest.domain.models.HabitRecord
import com.emm.mybest.domain.models.HabitType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class HabitRepositoryRoomIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: HabitRepositoryImpl

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = HabitRepositoryImpl(
            habitDao = database.habitDao(),
            habitRecordDao = database.habitRecordDao(),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insert_habit_and_record_then_query_habits_with_record_for_date() = runBlocking {
        val date = LocalDate(2026, 3, 9)
        val habit = Habit(
            id = "h1",
            name = "Caminar",
            icon = "walk",
            color = 1,
            category = "Salud",
            type = HabitType.TIME,
            goalValue = 30f,
            unit = "min",
            isEnabled = true,
            scheduledDays = setOf(DayOfWeek.MONDAY),
        )
        val record = HabitRecord(
            id = "r1",
            habitId = "h1",
            date = date,
            value = 30f,
            isCompleted = true,
            notes = "ok",
        )

        repository.insertHabit(habit)
        repository.insertRecord(record)

        val list = repository.getHabitsWithRecordsForDate(date).first()
        assertEquals(1, list.size)
        assertEquals("h1", list.first().habit.id)
        assertNotNull(list.first().record)
        assertEquals(true, list.first().record?.isCompleted)
    }

    @Test
    fun update_and_delete_habit_are_reflected_in_getAllHabits() = runBlocking {
        val habit = Habit(
            id = "h2",
            name = "Leer",
            icon = "book",
            color = 2,
            category = "Mind",
            type = HabitType.BOOLEAN,
            scheduledDays = setOf(DayOfWeek.TUESDAY),
        )
        repository.insertHabit(habit)
        repository.updateHabit(habit.copy(name = "Leer 20 min"))

        val updated = repository.getAllHabits().first().first()
        assertEquals("Leer 20 min", updated.name)

        repository.deleteHabit(updated)
        assertEquals(0, repository.getAllHabits().first().size)
    }
}
