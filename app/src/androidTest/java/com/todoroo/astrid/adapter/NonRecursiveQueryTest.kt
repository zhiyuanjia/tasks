package com.todoroo.astrid.adapter

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.natpryce.makeiteasy.MakeItEasy.with
import com.natpryce.makeiteasy.PropertyValue
import com.todoroo.astrid.core.BuiltInFilterExposer
import com.todoroo.astrid.dao.TaskDao
import com.todoroo.astrid.data.Task
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.tasks.R
import org.tasks.data.TaskContainer
import org.tasks.data.TaskListQuery.getQuery
import org.tasks.injection.InjectingTestCase
import org.tasks.injection.TestComponent
import org.tasks.makers.TaskMaker.PARENT
import org.tasks.makers.TaskMaker.newTask
import org.tasks.preferences.Preferences
import javax.inject.Inject

@RunWith(AndroidJUnit4::class)
class NonRecursiveQueryTest : InjectingTestCase() {

    @Inject lateinit var taskDao: TaskDao
    @Inject lateinit var preferences: Preferences

    private lateinit var adapter: TaskAdapter
    private val tasks = ArrayList<TaskContainer>()
    private val filter = BuiltInFilterExposer.getMyTasksFilter(ApplicationProvider.getApplicationContext<Context>().resources)
    private val dataSource = object : TaskAdapterDataSource {
        override fun getItem(position: Int) = tasks[position]

        override fun getTaskCount() = tasks.size
    }

    @Before
    override fun setUp() {
        super.setUp()
        preferences.clear()
        preferences.setBoolean(R.string.p_disable_subtasks, true)
        tasks.clear()
        adapter = TaskAdapter()
        adapter.setDataSource(dataSource)
    }

    @Test
    fun ignoreSubtasks() {
        val parent = addTask()
        val child = addTask(with(PARENT, parent))

        query()

        assertEquals(child, tasks[1].id)
        assertEquals(parent, tasks[1].parent)
        assertEquals(0, tasks[1].indent)
    }

    private fun addTask(vararg properties: PropertyValue<in Task?, *>): Long {
        val task = newTask(*properties)
        taskDao.createNew(task)
        return task.id
    }

    private fun query() {
        tasks.addAll(taskDao.fetchTasks { getQuery(preferences, filter, it) })
    }

    override fun inject(component: TestComponent) = component.inject(this)
}