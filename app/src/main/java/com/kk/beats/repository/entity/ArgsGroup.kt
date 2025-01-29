package com.kk.beats.repository.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "args_group",
    indices = [Index(value = ["prog"], unique = true)]
)
data class ArgsGroup(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,

    // prog 序号
    @ColumnInfo(name = "prog") val prog: Int,

    // 歌曲配置名
    @ColumnInfo(name = "name") val name: String,

    // bpm 数值
    @ColumnInfo(name = "bpm") val bpm: Int,

    // beats 配置项
    @ColumnInfo(name = "sub_beats") val subBeats: Int,
    @ColumnInfo(name = "standard_beats") val standardBeats: Int,

    // 滑动条参数
    @ColumnInfo(name = "arg_4") val arg4: Int,
    @ColumnInfo(name = "arg_8") val arg8: Int,
    @ColumnInfo(name = "arg_16") val arg16: Int,
    @ColumnInfo(name = "arg_3") val arg3: Int,
    @ColumnInfo(name = "arg_beats") val argBeats: Int,
    @ColumnInfo(name = "arg_master") val argMaster: Int,
)