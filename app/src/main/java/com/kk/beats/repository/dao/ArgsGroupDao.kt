package com.kk.beats.repository.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.kk.beats.repository.entity.ArgsGroup
import kotlinx.coroutines.flow.Flow

@Dao
interface ArgsGroupDao {
    @Insert
    suspend fun insert(argsGroup: ArgsGroup)

    @Query("delete from args_group where prog = :prog")
    suspend fun deleteByProg(prog: Int)

    @Query("SELECT * FROM args_group order by prog asc")
    suspend fun getAllArgGroups(): List<ArgsGroup>

    @Query("select * from args_group where prog = :prog")
    suspend fun getArgsGroupByProg(prog: Int): ArgsGroup?
}
