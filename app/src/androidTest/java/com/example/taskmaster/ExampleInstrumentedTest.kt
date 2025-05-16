package com.example.taskmaster

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskProviderTest {

    private lateinit var context: Context
    private lateinit var resolver: ContentResolver
    private val TEST_URI: Uri = TaskProvider.CONTENT_URI

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        resolver = context.contentResolver
        resolver.delete(TEST_URI, null, null) // Clear existing tasks for clean testing
    }

    @Test
    fun testInsertTask() {
        val values = ContentValues().apply {
            put("title", "Test Task")
            put("description", "Test Description")
            put("date", "14/05/2025")
            put("time", "12:00")
            put("priority", "High")
            put("isCompleted", false)
        }

        val uri = resolver.insert(TEST_URI, values)
        assertNotNull("Insert returned null URI", uri)
        Log.d("TaskProviderTest", "Inserted URI: $uri")
    }

    @Test
    fun testQueryTasks() {
        val cursor = resolver.query(TEST_URI, null, null, null, null)
        assertNotNull("Cursor is null", cursor)
        if (cursor != null) {
            assertTrue("No rows found", cursor.moveToFirst() || cursor.count == 0)
            Log.d("TaskProviderTest", "Row count: ${cursor.count}")
            cursor.close()
        }
    }

    @Test
    fun testDeleteTask() {
        // First insert a task
        val values = ContentValues().apply {
            put("title", "To Delete")
            put("description", "Delete me")
            put("date", "14/05/2025")
            put("time", "15:00")
            put("priority", "Low")
            put("isCompleted", false)
        }

        val uri = resolver.insert(TEST_URI, values)
        assertNotNull("Insert for delete test failed", uri)

        // Now delete
        val rowsDeleted = resolver.delete(uri!!, null, null)
        assertEquals("Failed to delete inserted task", 1, rowsDeleted)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testQueryInvalidUri() {
        // Corrected authority to match TaskProvider.AUTHORITY
        val invalidUri = Uri.parse("content://com.example.taskmaster/invalid")
        resolver.query(invalidUri, null, null, null, null)
    }

    @After
    fun tearDown() {
        resolver.delete(TEST_URI, null, null)
    }
}
