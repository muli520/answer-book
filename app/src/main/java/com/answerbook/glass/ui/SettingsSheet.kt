package com.answerbook.glass.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.answerbook.glass.data.LlmConfig
import com.answerbook.glass.data.UpdateChecker
import com.answerbook.glass.data.UpdateException
import com.answerbook.glass.data.UpdateInfo
import com.answerbook.glass.data.UpdateResult
import com.answerbook.glass.ui.components.BookGlyph
import com.answerbook.glass.ui.components.BookIcon
import com.answerbook.glass.ui.components.BookText
import com.answerbook.glass.ui.components.GlassChip
import com.answerbook.glass.ui.components.GlassDivider
import com.answerbook.glass.ui.components.GlassIconButton
import com.answerbook.glass.ui.components.GlassPanel
import com.answerbook.glass.ui.components.GlassTextField
import com.answerbook.glass.ui.components.LiquidButton
import com.answerbook.glass.ui.theme.BookColors
import com.answerbook.glass.ui.theme.BookType
import com.kyant.backdrop.Backdrop
import kotlinx.coroutines.launch

@Composable
fun SettingsSheet(
    visible: Boolean,
    config: LlmConfig,
    currentVersion: String,
    backdrop: Backdrop,
    onDismiss: () -> Unit,
    onSave: (baseUrl: String, model: String, apiKey: String) -> Unit,
    onClearKey: () -> Unit
) {
    var baseUrl by remember(visible) { mutableStateOf(config.baseUrl) }
    var model by remember(visible) { mutableStateOf(config.model) }
    var apiKey by remember(visible) { mutableStateOf(config.apiKey) }
    var showKey by remember(visible) { mutableStateOf(false) }
    var error by remember(visible) { mutableStateOf<String?>(null) }

    var checkingUpdate by remember(visible) { mutableStateOf(false) }
    var updateInfo by remember(visible) { mutableStateOf<UpdateInfo?>(null) }
    var updateStatus by remember(visible) { mutableStateOf<String?>(null) }
    var updateStatusError by remember(visible) { mutableStateOf(false) }

    val updateChecker = remember { UpdateChecker() }
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val maxSheetHeight = (LocalConfiguration.current.screenHeightDp * 0.92f).dp

    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.56f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            GlassPanel(
                backdrop = backdrop,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxSheetHeight)
                    .navigationBarsPadding()
                    .imePadding(),
                cornerRadius = 34.dp,
                tint = BookColors.Ivory,
                tintAlpha = 0.085f,
                shadowRadius = 36.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(38.dp)
                            .height(4.dp)
                            .background(BookColors.Ivory.copy(alpha = 0.18f), RoundedCornerShape(50))
                    )
                    Spacer(Modifier.height(22.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            BookText(
                                text = "连接你的大模型",
                                style = BookType.Wordmark.copy(fontSize = 23.sp),
                                color = BookColors.Ivory,
                                maxLines = 1
                            )
                            BookText(
                                text = "OPENAI-COMPATIBLE · ONE TURN ONLY",
                                style = BookType.Kicker.copy(letterSpacing = 1.9.sp),
                                color = BookColors.Gold.copy(alpha = 0.62f),
                                maxLines = 1
                            )
                        }
                        GlassIconButton(
                            icon = BookIcon.Close,
                            contentDescription = "关闭设置",
                            onClick = onDismiss,
                            backdrop = backdrop,
                            size = 40.dp,
                            tint = BookColors.IvorySoft
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    BookText(
                        text = "填入任意兼容 OpenAI Chat Completions 的接口。书灵只读取当前这一问，答完即止。",
                        style = BookType.Small,
                        color = BookColors.Muted,
                        maxLines = 3
                    )

                    Spacer(Modifier.height(20.dp))
                    BookText(
                        text = "快速填写",
                        style = BookType.Page.copy(letterSpacing = 1.8.sp),
                        color = BookColors.Faint,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassChip(
                            text = "DeepSeek",
                            backdrop = backdrop,
                            onClick = {
                                baseUrl = LlmConfig.DEFAULT_BASE_URL
                                model = LlmConfig.DEFAULT_MODEL
                                error = null
                            },
                            tint = BookColors.Gold
                        )
                        GlassChip(
                            text = "OpenAI",
                            backdrop = backdrop,
                            onClick = {
                                baseUrl = "https://api.openai.com/v1"
                                model = "gpt-4o-mini"
                                error = null
                            },
                            tint = BookColors.Teal
                        )
                    }

                    Spacer(Modifier.height(22.dp))
                    SheetFieldLabel("接口地址", "BASE URL")
                    Spacer(Modifier.height(8.dp))
                    GlassTextField(
                        value = baseUrl,
                        onValueChange = {
                            baseUrl = it
                            error = null
                        },
                        placeholder = "https://api.deepseek.com/v1",
                        backdrop = backdrop,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Next
                        )
                    )
                    Spacer(Modifier.height(7.dp))
                    BookText(
                        text = "可直接粘贴到 /v1，也可以包含 /chat/completions。",
                        style = BookType.Small,
                        color = BookColors.Faint,
                        maxLines = 1
                    )

                    Spacer(Modifier.height(17.dp))
                    SheetFieldLabel("模型名称", "MODEL")
                    Spacer(Modifier.height(8.dp))
                    GlassTextField(
                        value = model,
                        onValueChange = {
                            model = it
                            error = null
                        },
                        placeholder = "deepseek-chat",
                        backdrop = backdrop,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(Modifier.height(17.dp))
                    SheetFieldLabel("API KEY", "仅存于本机")
                    Spacer(Modifier.height(8.dp))
                    GlassTextField(
                        value = apiKey,
                        onValueChange = {
                            apiKey = it
                            error = null
                        },
                        placeholder = "sk-...",
                        backdrop = backdrop,
                        singleLine = true,
                        visualTransformation = if (showKey) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        trailing = {
                            GlassIconButton(
                                icon = if (showKey) BookIcon.EyeOff else BookIcon.Eye,
                                contentDescription = if (showKey) "隐藏密钥" else "显示密钥",
                                onClick = { showKey = !showKey },
                                backdrop = backdrop,
                                size = 34.dp,
                                tint = BookColors.Gold
                            )
                        }
                    )

                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                BookColors.Gold.copy(alpha = 0.05f),
                                RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 13.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        BookGlyph(
                            icon = BookIcon.Key,
                            tint = BookColors.Gold.copy(alpha = 0.65f),
                            modifier = Modifier.size(14.dp)
                        )
                        BookText(
                            text = "密钥只保存在本机应用私有空间，不参与系统备份。",
                            style = BookType.Small.copy(fontSize = 10.5.sp),
                            color = BookColors.Muted,
                            maxLines = 2
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                    GlassDivider()
                    Spacer(Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BookText(
                            text = "应用更新",
                            style = BookType.Label,
                            color = BookColors.IvorySoft,
                            maxLines = 1
                        )
                        Spacer(Modifier.weight(1f))
                        BookText(
                            text = "CURRENT · v" + currentVersion,
                            style = BookType.Page.copy(letterSpacing = 1.2.sp),
                            color = BookColors.Faint,
                            maxLines = 1
                        )
                    }
                    Spacer(Modifier.height(7.dp))
                    BookText(
                        text = "优先从 Gitee 镜像检查最新 Release，失败自动回退 GitHub；只打开下载页，不自动安装。",
                        style = BookType.Small,
                        color = BookColors.Muted,
                        maxLines = 2
                    )

                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlassChip(
                            text = if (checkingUpdate) "检查中…" else "检查更新",
                            backdrop = backdrop,
                            onClick = if (checkingUpdate) {
                                null
                            } else {
                                {
                                    checkingUpdate = true
                                    updateInfo = null
                                    updateStatus = null
                                    updateStatusError = false
                                    scope.launch {
                                        try {
                                            when (val result = updateChecker.check(currentVersion)) {
                                                is UpdateResult.Available -> {
                                                    updateInfo = result.info
                                                }

                                                is UpdateResult.UpToDate -> {
                                                    updateStatusError = false
                                                    updateStatus = "已是最新版本 " + result.latestVersion
                                                }
                                            }
                                        } catch (e: UpdateException) {
                                            updateStatusError = true
                                            updateStatus = e.message ?: "检查更新失败。"
                                        } catch (e: Exception) {
                                            updateStatusError = true
                                            updateStatus = "检查更新失败，请稍后再试。"
                                        } finally {
                                            checkingUpdate = false
                                        }
                                    }
                                }
                            },
                            leadingIcon = if (checkingUpdate) null else BookIcon.Spark,
                            tint = BookColors.Gold
                        )
                        Spacer(Modifier.weight(1f))
                        BookText(
                            text = "v" + currentVersion,
                            style = BookType.Page.copy(letterSpacing = 1.2.sp),
                            color = BookColors.Faint,
                            maxLines = 1
                        )
                    }

                    val info = updateInfo
                    if (info != null) {
                        Spacer(Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    BookColors.Gold.copy(alpha = 0.05f),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 13.dp, vertical = 11.dp)
                        ) {
                            BookText(
                                text = "发现新版本 · " + info.versionLabel,
                                style = BookType.Label,
                                color = BookColors.Gold,
                                maxLines = 1
                            )
                            if (info.notes.isNotBlank()) {
                                Spacer(Modifier.height(5.dp))
                                BookText(
                                    text = info.notes.take(240),
                                    style = BookType.Small.copy(fontSize = 10.5.sp),
                                    color = BookColors.Muted,
                                    maxLines = 4
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            GlassChip(
                                text = if (info.apkUrl != null) "下载 APK" else "打开 Release 页面",
                                backdrop = backdrop,
                                onClick = {
                                    uriHandler.openUri(info.apkUrl ?: info.releaseUrl)
                                },
                                leadingIcon = BookIcon.ArrowRight,
                                tint = BookColors.Gold
                            )
                        }
                    } else if (updateStatus != null) {
                        Spacer(Modifier.height(8.dp))
                        BookText(
                            text = updateStatus.orEmpty(),
                            style = BookType.Small,
                            color = if (updateStatusError) BookColors.Rose else BookColors.Teal,
                            maxLines = 3
                        )
                    }

                    if (error != null) {
                        Spacer(Modifier.height(12.dp))
                        BookText(
                            text = error.orEmpty(),
                            style = BookType.Small,
                            color = BookColors.Rose,
                            maxLines = 2
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                    LiquidButton(
                        label = "保存并返回",
                        onClick = {
                            val cleanUrl = baseUrl.trim().trimEnd('/')
                            val cleanModel = model.trim()
                            val cleanKey = apiKey.trim()
                            when {
                                !cleanUrl.startsWith("http://") &&
                                        !cleanUrl.startsWith("https://") -> {
                                    error = "接口地址需要以 http:// 或 https:// 开头。"
                                }

                                cleanModel.isBlank() -> {
                                    error = "请填写模型名称。"
                                }

                                cleanKey.isBlank() -> {
                                    error = "请填写 API Key。"
                                }

                                else -> {
                                    error = null
                                    onSave(cleanUrl, cleanModel, cleanKey)
                                }
                            }
                        },
                        backdrop = backdrop,
                        icon = BookIcon.Check,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        GlassChip(
                            text = "清除已保存的密钥",
                            backdrop = backdrop,
                            onClick = {
                                onClearKey()
                                apiKey = ""
                                showKey = false
                                error = null
                            },
                            leadingIcon = BookIcon.Trash,
                            tint = BookColors.Rose.copy(alpha = 0.8f)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    BookText(
                        text = "APP & UI · APACHE-2.0 · GLASS BY KYANT",
                        modifier = Modifier.fillMaxWidth(),
                        style = BookType.Kicker.copy(fontSize = 8.sp, letterSpacing = 1.2.sp),
                        color = BookColors.Faint.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun SheetFieldLabel(title: String, hint: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BookText(
            text = title,
            style = BookType.Label,
            color = BookColors.IvorySoft,
            maxLines = 1
        )
        Spacer(Modifier.weight(1f))
        BookText(
            text = hint,
            style = BookType.Page.copy(letterSpacing = 1.4.sp),
            color = BookColors.Faint,
            textAlign = TextAlign.End,
            maxLines = 1
        )
    }
}
