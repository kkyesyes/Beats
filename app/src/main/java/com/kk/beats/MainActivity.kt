package com.kk.beats

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.kk.beats.repository.AppDatabase.Companion.getDB
import com.kk.beats.repository.entity.ArgsGroup
import com.kk.beats.ui.theme.BeatsTheme
import com.kk.beats.ui.theme.WineRed
import com.kk.beats.ui.utils.VerticalSlider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BeatsTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {

    // 从数据库中选中的配置组
    var selectedArgsGroup by remember {
        mutableStateOf(
            ArgsGroup(
                id = 0,
                prog = 1,
                name = "未配置",
                bpm = 0,
                subBeats = 4,
                standardBeats = 4,
                arg4 = 0,
                arg8 = 0,
                arg16 = 0,
                arg3 = 0,
                argBeats = 0,
                argMaster = 0
            )
        )
    }


    // 当前参数组名称
    var name by remember {
        mutableStateOf(selectedArgsGroup.name)
    }

    // 关于 BPM 显示
    var bpm by remember {
        mutableStateOf(selectedArgsGroup.bpm)
    }
    var bpmString by remember {
        mutableStateOf("$bpm")
    }
    var bpmColor by remember {
        mutableStateOf(Color.DarkGray)
    }
    val coroutineScope = rememberCoroutineScope()

    // 关于 BPM 计算
    var preSecond = Instant.now()
    var nowSecond = preSecond

    // progNo 变量
    var progNo by remember {
        mutableStateOf(selectedArgsGroup.prog)
    }
    var progNoStr by remember {
        mutableStateOf("$progNo")
    }

    // Beats 变量
    var subBeats by remember {
        mutableStateOf(selectedArgsGroup.subBeats)
    }
    var standardBeats by remember {
        mutableStateOf(selectedArgsGroup.standardBeats)
    }
    var beats by remember {
        mutableStateOf("$subBeats/$standardBeats")
    }

    // Toast context
    val context = LocalContext.current

    // 播放
    var isPlaying by remember { mutableStateOf(false) }
    val handler = remember { Handler(Looper.getMainLooper()) }
    val scheduler = remember { Executors.newScheduledThreadPool(1) }
    var scheduledFuture: ScheduledFuture<*>? by remember { mutableStateOf(null) }

    // 功能拖动条值变量
    var _4 by remember {
        mutableStateOf(selectedArgsGroup.arg4)
    }
    var _8 by remember {
        mutableStateOf(selectedArgsGroup.arg8)
    }
    var _16 by remember {
        mutableStateOf(selectedArgsGroup.arg16)
    }
    var _3 by remember {
        mutableStateOf(selectedArgsGroup.arg3)
    }
    var _Beats by remember {
        mutableStateOf(selectedArgsGroup.argBeats)
    }
    var _Master by remember {
        mutableStateOf(selectedArgsGroup.argMaster)
    }


    // 音频设置
    val playOne = MediaPlayer.create(context, R.raw.one)
    val playTwo = MediaPlayer.create(context, R.raw.two)
    val playThree = MediaPlayer.create(context, R.raw.three)
    val playFour = MediaPlayer.create(context, R.raw.four)
    val playGo = MediaPlayer.create(context, R.raw.go)
    val playE = MediaPlayer.create(context, R.raw.e)
    val playAn = MediaPlayer.create(context, R.raw.an)
    val playA = MediaPlayer.create(context, R.raw.a)


    // Store dialog
    var isShowStoreDialog by remember {
        mutableStateOf(false)
    }

    // ProgNo dialog
    var isShowProgNoDialog by remember {
        mutableStateOf(false)
    }

    // BPMNo dialog
    var isShowBpmNoDialog by remember {
        mutableStateOf(false)
    }

    // 抽屉页
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // 关于信息是否展示
    var isAboutDialogShow by remember {
        mutableStateOf(false)
    }

    // subBeats 输入框是否展示
    var isShowSubBeatsDialog by remember {
        mutableStateOf(false)
    }

    // standardBeats 输入框是否展示
    var isShowStandardBeatsDialog by remember {
        mutableStateOf(false)
    }

    // 歌曲名输入框是否展示
    var isShowNameDialog by remember {
        mutableStateOf(false)
    }

    // 参数组替换确定框是否展示
    var isShowReplaceArgsGroupShow by remember {
        mutableStateOf(false)
    }

    // 数据库配置
    var db = getDB(context)
    var argsGroupDao = db.getArgsGroupDao()

    // 当 progNo 更新会出发数据库查询
    LaunchedEffect(progNo) {
        var currentArgsGroup = argsGroupDao.getArgsGroupByProg(progNo)
        if (currentArgsGroup == null) {
            name = "未配置"
            return@LaunchedEffect
        }
        progNo = currentArgsGroup.prog
        progNoStr = "$progNo"
        name = currentArgsGroup.name
        bpm = currentArgsGroup.bpm
        bpmString = "$bpm"
        subBeats = currentArgsGroup.subBeats
        standardBeats = currentArgsGroup.standardBeats
        _4 = currentArgsGroup.arg4
        _8 = currentArgsGroup.arg8
        _16 = currentArgsGroup.arg16
        _Beats = currentArgsGroup.argBeats
        _Master = currentArgsGroup.argMaster
    }


    // 设置抽屉
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent({ newArgsGroup ->
                progNo = newArgsGroup.prog
                progNoStr = "$progNo"
                name = newArgsGroup.name
                bpm = newArgsGroup.bpm
                bpmString = "$bpm"
                subBeats = newArgsGroup.subBeats
                standardBeats = newArgsGroup.standardBeats
                _4 = newArgsGroup.arg4
                _8 = newArgsGroup.arg8
                _16 = newArgsGroup.arg16
                _Beats = newArgsGroup.argBeats
                _Master = newArgsGroup.argMaster
            })
        },
        modifier = Modifier,
        content = {
            // 布局
            Scaffold(
                modifier = modifier
                    .fillMaxSize(),
                topBar = {
                    TopAppBar(
                        modifier = Modifier.height(85.dp),
                        title = {
                            Text(
                                text = "Beats",
                                fontSize = 35.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Cursive,
                                fontStyle = FontStyle.Italic
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        navigationIcon = {
                            IconButton(onClick = {
                                // 开启左抽屉
                                scope.launch {
                                    drawerState.open()
                                }
                            }) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_dialog_dialer),
                                    contentDescription = "programs 列表",
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                isAboutDialogShow = true
                            }) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_dialog_info),
                                    contentDescription = "关于信息",
                                    tint = Color.White
                                )
                            }
                            if (isAboutDialogShow) {
                                AlertDialog(
                                    onDismissRequest = {
                                        isAboutDialogShow = false
                                    },
                                    confirmButton = {
                                        Column {
                                            Text(
                                                text = "关于 Beats",
                                                fontSize = 20.sp,
                                                modifier = Modifier,
                                            )
                                            Spacer(modifier = Modifier.height(20.dp))
                                            Text(
                                                text = "\t你好呀，我是 KK ，一个酷爱软件开发的无名小卒，也是一个热爱音乐的架子鼓手。\n\n" +
                                                        "\t本 APP 为平替 TAMA RW200 而存在\n\n" +
                                                        "欢迎反馈：crosskyhu@gmail.com"
                                            )


                                        }
                                    })
                            }
                        },
                    )
                }
            )
            { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 0.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {

                    // 配置名称
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable {
                                // 打开歌名输入框
                                isShowNameDialog = true
                            }
                    ) {

                        Text(
                            text = "歌曲名：${name}",
                            fontSize = 20.sp,
                            color = WineRed,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 5.dp)
                                .padding(bottom = 10.dp)
                                .padding(horizontal = 10.dp)
                        )

                        // 歌曲名输入
                        if (isShowNameDialog) {
                            var tempName by remember {
                                mutableStateOf("")
                            }
                            AlertDialog(
                                onDismissRequest = {
                                    isShowNameDialog = false
                                },
                                confirmButton = {
                                    OutlinedTextField(
                                        value = tempName,
                                        onValueChange = { newName ->
                                            tempName = newName
                                        },
                                        label = {
                                            Text(text = "请输入歌曲名")
                                        },
                                    )
                                    Button(onClick = {
                                        if (!tempName.isBlank()) name = tempName
                                        isShowNameDialog = false
                                    }) {
                                        Text(text = "Confirm")
                                    }
                                }
                            )
                        }
                    }


                    // 音符配置横向滚动条
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        VerticalSlider(
                            "4",
                            250,
                            65,
                            _4.toFloat(),
                            { newValue -> _4 = newValue.toInt() })
                        VerticalSlider(
                            "8",
                            250,
                            65,
                            _8.toFloat(),
                            { newValue -> _8 = newValue.toInt() })
                        VerticalSlider(
                            "16",
                            250,
                            65,
                            _16.toFloat(),
                            { newValue -> _16 = newValue.toInt() })
                        VerticalSlider(
                            "3",
                            250,
                            65,
                            _3.toFloat(),
                            { newValue -> _3 = newValue.toInt() })
                        VerticalSlider(
                            "Beats",
                            250,
                            65,
                            _Beats.toFloat(),
                            { newValue -> _Beats = newValue.toInt() })
                        VerticalSlider(
                            "Master",
                            250,
                            65,
                            _Master.toFloat(),
                            { newValue -> _Master = newValue.toInt() })
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 数显
                    Row(
                        modifier = Modifier
                            .padding(bottom = 0.dp)
                            .height(130.dp)
                            .width(400.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {

                        // Prog No
                        Column(
                            modifier = Modifier
                                .width(100.dp)
                                .padding(horizontal = 0.dp)
                                .clickable {
                                    // 输入 PROG.NO 功能
                                    isShowProgNoDialog = true
                                }
                        ) {
                            // progNo alertDialog
                            if (isShowProgNoDialog) {
                                AlertDialog(
                                    onDismissRequest = {
                                        isShowProgNoDialog = false
                                    },
                                    confirmButton = {
                                        var tempProgValue by remember {
                                            mutableStateOf("")
                                        }
                                        OutlinedTextField(
                                            value = tempProgValue,
                                            onValueChange = { newvalue ->
                                                tempProgValue = newvalue
                                            },
                                            label = {
                                                Text(text = "PROG.$progNo")
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            maxLines = 1
                                        )
                                        Button(onClick = {
                                            if (tempProgValue == "") {
                                                Toast.makeText(
                                                    context,
                                                    "输入无效！！！",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                try {
                                                    var tempProgNo = tempProgValue.toInt()
                                                    if (tempProgNo > 99) {
                                                        Toast.makeText(
                                                            context,
                                                            "输入序号最大为 99，请重新输入！",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else if (tempProgNo < 1) {
                                                        Toast.makeText(
                                                            context,
                                                            "输入序号最小为 1，请重新输入！",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        progNo = tempProgNo
                                                        progNoStr = progNo.toString()
                                                        isShowProgNoDialog = false
                                                    }
                                                } catch (e: Exception) {
                                                    Toast.makeText(
                                                        context,
                                                        "请输入数字！！！",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }

                                        }) {
                                            Text(text = "Confirm")
                                        }
                                    }

                                )
                            }


                            Text(
                                text = progNoStr,
                                fontSize = 80.sp,
                                color = Color.Black,
                                fontFamily = FontFamily.Cursive,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)

                            )
                            Text(
                                text = "PROG.",
                                color = Color.DarkGray,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // BPM 数字内容
                        Box(
                            modifier = Modifier
                                .size(170.dp)
                        ) {

                            // bpmNo alertDialog 点击输入
                            if (isShowBpmNoDialog) {
                                AlertDialog(
                                    onDismissRequest = {
                                        isShowBpmNoDialog = false
                                    },
                                    confirmButton = {
                                        var tempBpmValue by remember {
                                            mutableStateOf("")
                                        }
                                        OutlinedTextField(
                                            value = tempBpmValue,
                                            onValueChange = { newValue ->
                                                tempBpmValue = newValue
                                            },
                                            label = {
                                                Text(text = "BPM: $bpm")
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        )
                                        Button(onClick = {
                                            if (tempBpmValue == "") {
                                                Toast.makeText(
                                                    context,
                                                    "输入无效！！！",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                try {
                                                    var tempBpmNo = tempBpmValue.toInt()
                                                    if (tempBpmNo > 400) {
                                                        Toast.makeText(
                                                            context,
                                                            "输入 BPM 最大为 400，请重新输入！",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else if (tempBpmNo < 1) {
                                                        Toast.makeText(
                                                            context,
                                                            "输入 BPM 最小为 1，请重新输入！",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    } else {
                                                        bpm = tempBpmNo
                                                        bpmString = bpm.toString()
                                                        isShowBpmNoDialog = false
                                                    }
                                                } catch (e: Exception) {
                                                    Toast.makeText(
                                                        context,
                                                        "请输入数字！！！",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }

                                        }) {
                                            Text(text = "Confirm")
                                        }
                                    }
                                )
                            }

                            Text(
                                text = bpmString,
                                // 以下是一些样式修改示例
                                color = bpmColor, // 颜色
                                fontSize = 100.sp, // 字体大小
                                fontWeight = FontWeight.Bold, // 字体粗细
                                fontStyle = FontStyle.Italic, // 字体样式
                                fontFamily = FontFamily.Serif, // 字体系列
                                textAlign = TextAlign.Right, // 文本对齐方式
                                modifier = Modifier
                                    .fillMaxWidth() // 让 Text 充满 Column 的宽度
                                    .align(Alignment.Center)
                                    .clickable {
                                        // 显示输入框
                                        isShowBpmNoDialog = true
                                    }
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Beats
                        Column(
                            modifier = Modifier
                                .padding()

                        ) {


                            Row(
                                modifier = Modifier
                                    .padding(top = 40.dp)
                                    .padding(horizontal = 10.dp)
                            ) {
                                Text(
                                    text = subBeats.toString(),
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 30.sp,
                                    color = Color.Black,
                                    modifier = Modifier.clickable {
                                        isShowSubBeatsDialog = true
                                    }
                                )
                                // subBeats dialog
                                if (isShowSubBeatsDialog) {
                                    AlertDialog(
                                        onDismissRequest = {
                                            isShowSubBeatsDialog = false
                                        },
                                        confirmButton = {
                                            var tempSubBeatsValue by remember {
                                                mutableStateOf("")
                                            }
                                            OutlinedTextField(
                                                value = tempSubBeatsValue,
                                                onValueChange = { newvalue ->
                                                    tempSubBeatsValue = newvalue
                                                },
                                                label = {
                                                    Text(text = "subBeats:$subBeats")
                                                },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                maxLines = 1
                                            )
                                            Button(onClick = {

                                                if (tempSubBeatsValue.isBlank()) {
                                                    Toast.makeText(
                                                        context,
                                                        "请输入有效数据！",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                var tempSubBeatsValueNum = tempSubBeatsValue.toInt()
                                                when {
                                                    tempSubBeatsValueNum < 1 ->
                                                        Toast.makeText(
                                                            context,
                                                            "输入不可小于 1",
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                    tempSubBeatsValueNum > 9 ->
                                                        Toast.makeText(
                                                            context,
                                                            "输入不可大于 9",
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                    else -> {
                                                        subBeats = tempSubBeatsValueNum
                                                        isShowSubBeatsDialog = false
                                                    }

                                                }
                                            }) {
                                                Text(text = "Confirm")
                                            }
                                        }
                                    )
                                }

                                Text(
                                    text = "/",
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 30.sp,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 5.dp)
                                )


                                Text(
                                    text = standardBeats.toString(),
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 30.sp,
                                    color = Color.Black,
                                    modifier = Modifier.clickable {
                                        isShowStandardBeatsDialog = true
                                    }
                                )

                                // standardBeats dialog
                                if (isShowStandardBeatsDialog) {
                                    AlertDialog(
                                        onDismissRequest = {
                                            isShowStandardBeatsDialog = false
                                        },
                                        confirmButton = {
                                            var tempStandardBeatsValue by remember {
                                                mutableStateOf("")
                                            }
                                            OutlinedTextField(
                                                value = tempStandardBeatsValue,
                                                onValueChange = { newvalue ->
                                                    tempStandardBeatsValue = newvalue
                                                },
                                                label = {
                                                    Text(text = "standardBeats:$standardBeats")
                                                },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                maxLines = 1
                                            )
                                            Button(onClick = {
                                                if (tempStandardBeatsValue.isBlank()) {
                                                    Toast.makeText(
                                                        context,
                                                        "请输入有效数据！",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                // todo 异常处理有 bug
                                                var tempStandardBeatsValueNum = 0
                                                try {
                                                    tempStandardBeatsValueNum =
                                                        tempStandardBeatsValue.toInt()
                                                } catch (e: Exception) {
                                                    Toast.makeText(
                                                        context,
                                                        "请勿输入非法数据！！！",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }

                                                when {
                                                    tempStandardBeatsValueNum < 1 ->
                                                        Toast.makeText(
                                                            context,
                                                            "输入不可小于 1",
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                    tempStandardBeatsValueNum > 9 ->
                                                        Toast.makeText(
                                                            context,
                                                            "输入不可大于 9",
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                    else -> {
                                                        standardBeats = tempStandardBeatsValueNum
                                                        isShowStandardBeatsDialog = false


                                                    }

                                                }

                                            }) {
                                                Text(text = "Confirm")
                                            }
                                        }
                                    )
                                }


                            }
                            Text(
                                text = "Beats",
                                color = Color.DarkGray,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(bottom = 0.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 调整 BPM
                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Column(
                            modifier = Modifier
                                .width(100.dp)
                                .padding(top = 10.dp)
                                .padding(horizontal = 10.dp)
                        ) {
                            // 下一个 PROG
                            Button(
                                onClick = {
                                    if (progNo < 99) {
                                        progNo++
                                        progNoStr = progNo.toString()
                                        // todo 下一个 PROG
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = "+",
                                    fontSize = 35.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            // 上一个 PROG
                            Button(
                                onClick = {
                                    if (progNo > 1) {
                                        progNo--
                                        progNoStr = progNo.toString()
                                        // todo 上一个 PROG
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = "-",
                                    fontSize = 35.sp
                                )
                            }
                        }

                        Column {
                            // TAP 组合按键
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 5.dp)
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                // 减少 BPM
                                Button(
                                    onClick = {
                                        bpm--
                                        bpmString = bpm.toString()
                                    },
                                    modifier = Modifier.size(50.dp)
                                ) {
                                    Text(
                                        text = "<",
                                        fontSize = 25.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(20.dp))

                                // TAP
                                Button(onClick = {
                                    preSecond = nowSecond
                                    nowSecond = Instant.now()
                                    bpm = (60000.0 / Duration.between(preSecond, nowSecond)
                                        .toMillis()).toInt()
                                    bpmString = "$bpm"
                                }) {
                                    Text(
                                        text = "TAP",
                                        fontSize = 25.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(20.dp))

                                // 增加 BPM
                                Button(
                                    onClick = {
                                        bpm++
                                        bpmString = bpm.toString()
                                    },
                                    modifier = Modifier.size(50.dp)
                                ) {
                                    Text(
                                        text = ">",
                                        fontSize = 25.sp
                                    )
                                }
                            }
                            // Go / BPM 功能
                            Row(
                                modifier = Modifier
                                    .height(100.dp)
                                    .width(400.dp)
                                    .padding(horizontal = 30.dp)
                                    .padding(top = 10.dp)

                            ) {
                                // Go 功能
                                Button(
                                    onClick = {
                                        if (!isPlaying) {
                                            // 定时器实现节拍循环
                                            if (bpm <= 0) {
                                                // 如果没有设置 BPM
                                                Toast.makeText(
                                                    context,
                                                    "请设置有效 BPM ！",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                            } else {
                                                // 正常播放
                                                val interval = (60000 / bpm).toLong()
                                                isPlaying = true

                                                // 4 8 16 协程
                                                coroutineScope.launch {
                                                    var counter = 0L
                                                    while (isPlaying) {
                                                        // Voice
                                                        when (counter % subBeats) {
                                                            // 第一拍
                                                            0L -> {
                                                                playOne.setVolume(
                                                                    _4 / 400.toFloat(),
                                                                    _4 / 400.toFloat()
                                                                )
                                                                playOne.start()
                                                            }
                                                            // 第二拍
                                                            1L -> {
                                                                playTwo.setVolume(
                                                                    _4 / 400.toFloat(),
                                                                    _4 / 400.toFloat()
                                                                )
                                                                playTwo.start()
                                                            }
                                                            // 第三拍
                                                            2L -> {
                                                                playThree.setVolume(
                                                                    _4 / 400.toFloat(),
                                                                    _4 / 400.toFloat()
                                                                )
                                                                playThree.start()
                                                            }
                                                            // 第四拍
                                                            3L -> {
                                                                playFour.setVolume(
                                                                    _4 / 400.toFloat(),
                                                                    _4 / 400.toFloat()
                                                                )
                                                                playFour.start()
                                                            }
                                                        }


                                                        bpmColor = Color.White

                                                        // Delay
                                                        delay(interval / 2)



                                                        bpmColor = Color.DarkGray

                                                        // Delay
                                                        delay(interval / 2)

                                                        counter++
                                                    }
                                                }
                                            }
                                        } else {
                                            isPlaying = false
                                            bpmColor = Color.DarkGray
                                        }
                                    },
                                    modifier = Modifier
                                        .height(100.dp)
                                        .width(110.dp)
                                ) {
                                    Text(
                                        text = "Go",
                                        fontSize = 30.sp,
                                        fontStyle = FontStyle.Italic,

                                        )
                                }
                                Spacer(modifier = Modifier.width(35.dp))
                                // BPM Slider
                                VerticalSlider("BPM", 100, 50, 0F, { newValue ->
                                    bpm = newValue.toInt()
                                    bpmString = bpm.toString()
                                })
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 功能按键
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                    ) {
                        // STORE
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = "STORE",
                                color = Color.Black,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Button(onClick = {
                                // todo STORE 功能
                                CoroutineScope(Dispatchers.IO).launch {
                                    if (argsGroupDao.getArgsGroupByProg(progNo) != null) {
                                        isShowReplaceArgsGroupShow = true
                                    } else {
                                        isShowStoreDialog = true
                                    }
                                }
                            }) {
                                // 更新配置
                                if (isShowReplaceArgsGroupShow) {
                                    AlertDialog(
                                        onDismissRequest = {
                                            isShowReplaceArgsGroupShow = false
                                        },
                                        confirmButton = {
                                            // 确认替换
                                            Column {
                                                Text(text = "当前序号参数组已存在，是否替换？")

                                                Spacer(modifier = Modifier.height(20.dp))

                                                Row {
                                                    Button(onClick = {
                                                        CoroutineScope(Dispatchers.IO).launch {
                                                            try {
                                                                argsGroupDao.deleteByProg(progNo)
                                                                argsGroupDao.insert(
                                                                    ArgsGroup(
                                                                        0,
                                                                        progNo,
                                                                        name,
                                                                        bpm,
                                                                        subBeats,
                                                                        standardBeats,
                                                                        _4,
                                                                        _8,
                                                                        _16,
                                                                        _3,
                                                                        _Beats,
                                                                        _Master
                                                                    )
                                                                )
                                                            } catch (e: Exception) {
                                                                withContext(Dispatchers.Main) {
                                                                    Toast.makeText(
                                                                        context,
                                                                        "参数组保存失败！",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            }
                                                        }
                                                        Toast.makeText(
                                                            context,
                                                            "参数组保存成功",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        isShowReplaceArgsGroupShow = false
                                                    }) {
                                                        Text(text = "Yes")
                                                    }
                                                }
                                            }

                                        })
                                } else if (isShowStoreDialog) {
                                    // 直接插入配置
                                    AlertDialog(
                                        onDismissRequest = {
                                            isShowStoreDialog = false
                                        },
                                        confirmButton = {
                                            Column {
                                                // 确认是否保存
                                                Text(text = "确定存储当前节拍器配置于 PROG.$progNo 中吗")

                                                Spacer(modifier = Modifier.height(20.dp))

                                                Row {
                                                    // store prog.
                                                    Button(
                                                        onClick = {
                                                            // 将当前设置存储到本地数据库
                                                            try {
                                                                CoroutineScope(Dispatchers.IO).launch {
                                                                    argsGroupDao.insert(
                                                                        ArgsGroup(
                                                                            0,
                                                                            progNo,
                                                                            name,
                                                                            bpm,
                                                                            subBeats,
                                                                            standardBeats,
                                                                            _4,
                                                                            _8,
                                                                            _16,
                                                                            _3,
                                                                            _Beats,
                                                                            _Master
                                                                        )
                                                                    )
                                                                }
                                                                Toast.makeText(
                                                                    context,
                                                                    "参数组保存成功",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            } catch (e: Exception) {
                                                                Toast.makeText(
                                                                    context,
                                                                    "参数组保存失败！",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }

                                                            // 关闭 dialog
                                                            isShowStoreDialog = false
                                                        },
                                                        modifier = Modifier
                                                    ) {
                                                        Text(text = "Yes")
                                                    }
                                                }

                                            }

                                        })
                                }
                            }
                        }
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = "SOUND",
                                color = Color.Black,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Button(onClick = {
                                // todo SOUND 功能
                                Toast.makeText(
                                    context,
                                    "SOUND 功能开发ing",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }) {

                            }
                        }
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = "MODE",
                                color = Color.Black,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Button(onClick = {
                                // todo MODE 功能
                                Toast.makeText(
                                    context,
                                    "MODE 功能开发ing",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }) {

                            }
                        }
                        Column(
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = "BEAT",
                                color = Color.Black,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                            Button(onClick = {
                                // todo BEAT 功能
                                Toast.makeText(
                                    context,
                                    "BEAT 功能开发ing",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }) {

                            }
                        }

                    }

                }
            }
        }
    )
}


@Composable
fun DrawerContent(onArgsGroupClicked: (ArgsGroup) -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope() // 记住 CoroutineScope

    // 使用 remember 保存数据
    var allArgsGroups by remember { mutableStateOf<List<ArgsGroup>>(emptyList()) }

    var db = getDB(context)
    var argsGroupDao = db.getArgsGroupDao()

    // 初次进入使用 LaunchedEffect 进行异步加载
    LaunchedEffect(Unit) {
        allArgsGroups = argsGroupDao.getAllArgGroups()
    }

    // UI
    Box(
        modifier = Modifier
            .padding(all = 30.dp)
            .fillMaxSize()
            .background(WineRed)
    ) {
        Row(
            modifier = Modifier
                .padding(top = 20.dp)
                .padding(horizontal = 20.dp)
        ) {
            Text(text = "本地已保存参数组：")
        }

        // 刷新悬浮按钮
        FloatingActionButton(
            onClick = {
                // 刷新
                coroutineScope.launch {
                    allArgsGroups = argsGroupDao.getAllArgGroups() // 正确调用 suspend 方法
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd) // 对齐到右下角
                .padding(horizontal = 20.dp) // 适当的内边距，防止按钮过于贴边
                .padding(top = 8.dp)
                .zIndex(1f)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = "刷新")
        }


        // 数据列表
        Column(
            modifier = Modifier
                .padding(top = 80.dp)
                .padding(bottom = 20.dp)
                .verticalScroll(scrollState)
        ) {
            for (argsGroup in allArgsGroups) {
                Box(modifier = Modifier
                    .padding(horizontal = 30.dp)
                    .padding(bottom = 30.dp)
                    .border(BorderStroke(1.dp, Color.White))
                    .clickable {
                        onArgsGroupClicked(argsGroup)
                        Log.e("已选中", argsGroup.toString())
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .padding(all = 8.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "${argsGroup.prog}",
                            modifier = Modifier
                                .padding(all = 12.dp)
                        )
                        Column() {
                            Text(
                                text = "${argsGroup.name}",
                                fontSize = 15.sp,
                                modifier = Modifier
                            )
                            Text(
                                text = "BPM: ${argsGroup.bpm}     Beats: ${argsGroup.subBeats}/${argsGroup.standardBeats}",
                                fontSize = 13.sp,
                                modifier = Modifier
                            )
                        }
                    }
                }
            }
        }


    }


//}
}


// 滚轮
//@OptIn(ExperimentalFoundationApi::class)
//@Composable
//fun <T> WheelPicker(
//    data: List<T>,
//    selectIndex: Int,
//    visibleCount: Int,
//    modifier: Modifier = Modifier,
//    onSelect: (index: Int, item: T) -> Unit,
//    content: @Composable (item: T) -> Unit,
//) {
//    BoxWithConstraints(modifier = modifier, propagateMinConstraints = true) {
//        val density = LocalDensity.current
//        val size = data.size
//        val count = size * 10000
//        val pickerHeight = maxHeight
//        val pickerHeightPx = density.run { pickerHeight.toPx() }
//        val pickerCenterLinePx = pickerHeightPx / 2
//        val itemHeight = pickerHeight / visibleCount
//        val itemHeightPx = pickerHeightPx / visibleCount
//        val startIndex = count / 2
//        val listState = rememberLazyListState(
//            initialFirstVisibleItemIndex =
//                startIndex - startIndex.floorMod(size) + selectIndex,
//            initialFirstVisibleItemScrollOffset =
//                ((itemHeightPx - pickerHeightPx) / 2).roundToInt(),
//        )
//        val layoutInfo by remember { derivedStateOf { listState.layoutInfo } }
//        LazyColumn(
//            modifier = Modifier,
//            state = listState,
//            flingBehavior = rememberSnapFlingBehavior(listState),
//        ) {
//            items(count) { index ->
//                val currIndex = (index - startIndex).floorMod(size)
//                val item = layoutInfo.visibleItemsInfo.find { it.index == index }
//                var percent = 1f
//                if (item != null) {
//                    val itemCenterY = item.offset + item.size / 2
//                    percent = if (itemCenterY < pickerCenterLinePx) {
//                        itemCenterY / pickerCenterLinePx
//                    } else {
//                        1 - (itemCenterY - pickerCenterLinePx) / pickerCenterLinePx
//                    }
//                    if (!listState.isScrollInProgress
//                        && item.offset < pickerCenterLinePx
//                        && item.offset + item.size > pickerCenterLinePx
//                    ) {
//                        onSelect(currIndex, data[currIndex])
//                    }
//                }
//                Box(
//                    modifier = Modifier
//                        .graphicsLayer {
//                            alpha = 0.75f + 0.25f * percent
//                            scaleX = 0.75f + 0.25f * percent
//                            scaleY = 0.75f + 0.25f * percent
//                            rotationX = (1 + (0.75f + 0.25f * percent)) * 180
//                        }
//                        .fillMaxWidth()
//                        .height(itemHeight),
//                    contentAlignment = Alignment.Center,
//                ) {
//                    content(data[currIndex])
//                }
//            }
//        }
//    }
//}
//
//private fun Int.floorMod(other: Int): Int = when (other) {
//    0 -> this
//    else -> this - floorDiv(other) * other
//}

