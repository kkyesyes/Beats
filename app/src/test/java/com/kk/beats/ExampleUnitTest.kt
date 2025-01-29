package com.kk.beats

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kk.beats.repository.AppDatabase.Companion.getDB
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleUnitTest {

    @Test
    fun getArgsGroup() {
        Log.d("TAG", "正常输出")
    }
}