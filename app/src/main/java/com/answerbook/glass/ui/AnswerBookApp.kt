package com.answerbook.glass.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.answerbook.glass.BuildConfig
import com.answerbook.glass.data.AnswerBank
import com.answerbook.glass.data.AnswerStance
import com.answerbook.glass.data.LlmClient
import com.answerbook.glass.data.LlmConfig
import com.answerbook.glass.data.LlmException
import com.answerbook.glass.data.SafetyGuard
import com.answerbook.glass.data.SettingsStore
import com.answerbook.glass.data.StanceDeck
import com.answerbook.glass.ui.components.BookGlyph
import com.answerbook.glass.ui.components.BookIcon
import com.answerbook.glass.ui.components.BookMode
import com.answerbook.glass.ui.components.BookText
import com.answerbook.glass.ui.components.GlassChip
import com.answerbook.glass.ui.components.GlassDivider
import com.answerbook.glass.ui.components.GlassIconButton
import com.answerbook.glass.ui.components.GlassModeSwitch
import com.answerbook.glass.ui.components.GlassPanel
import com.answerbook.glass.ui.components.GlassTextField
import com.answerbook.glass.ui.components.LiquidButton
import com.answerbook.glass.ui.components.OrbitSpinner
import com.answerbook.glass.ui.theme.BookColors
import com.answerbook.glass.ui.theme.BookType
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

private data class OracleVisual(val listening: Boolean, val answer: String?)

@Composable
fun AnswerBookApp() {
    val context = LocalContext.current
    val settings = remember { SettingsStore(context) }
    val backdrop = rememberLayerBackdrop()
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val llmClient = remember { LlmClient() }
    val stanceDeck = remember { StanceDeck() }

    var modeName by rememberSaveable { mutableStateOf(BookMode.Mind.name) }
    val mode = BookMode.valueOf(modeName)

    var settingsOpen by rememberSaveable { mutableStateOf(false) }

    var mindAnswer by rememberSaveable { mutableStateOf<String?>(null) }
    var mindPage by rememberSaveable { mutableIntStateOf(0) }
    var listening by remember { mutableStateOf(false) }

    var question by rememberSaveable { mutableStateOf("") }
    var askAnswer by rememberSaveable { mutableStateOf<String?>(null) }
    var askedQuestion by rememberSaveable { mutableStateOf("") }
    var asking by remember { mutableStateOf(false) }
    var askError by rememberSaveable { mutableStateOf<String?>(null) }
    var showCrisisNote by rememberSaveable { mutableStateOf(false) }

    fun reveal() {
        if (listening) return
        scope.launch {
            listening = true
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(1180)
            val page = AnswerBank.draw(mindPage.takeIf { it > 0 })
            mindAnswer = page.text
            mindPage = page.number
            listening = false
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    fun openSettings() {
        keyboard?.hide()
        focusManager.clearFocus()
        settingsOpen = true
    }

    fun submitQuestion() {
        if (asking) return
        val trimmed = question.trim()
        if (trimmed.isEmpty()) {
            askError = "先写下一个问题。"
            return
        }
        if (!settings.config.isConfigured) {
            askError = "这本书还没有接上你的大模型。"
            openSettings()
            return
        }
        keyboard?.hide()
        focusManager.clearFocus()
        val safety = SafetyGuard.check(trimmed)
        val stance = if (safety.isSensitive) AnswerStance.SAFE else stanceDeck.next()
        showCrisisNote = safety.needsCrisisNote
        askError = null
        askAnswer = null
        asking = true
        scope.launch {
            try {
                val answer = llmClient.ask(
                    config = settings.config,
                    question = trimmed,
                    stance = stance,
                    safety = safety.category
                )
                askAnswer = answer
                askedQuestion = trimmed
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            } catch (e: LlmException) {
                askError = e.message ?: "书页没有回应，请稍后再试。"
            } catch (e: Exception) {
                askError = "书页没有回应，请稍后再试。"
            } finally {
                asking = false
            }
        }
    }

    BackHandler(enabled = settingsOpen) { settingsOpen = false }

    Box(
        Modifier
            .fillMaxSize()
            .background(BookColors.Ink)
    ) {
        DreamBackground(
            Modifier
                .fillMaxSize()
                .layerBackdrop(backdrop)
        )

        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding()
                .padding(horizontal = 22.dp)
        ) {
            Header(
                backdrop = backdrop,
                onSettings = ::openSettings
            )

            Spacer(Modifier.height(20.dp))

            GlassModeSwitch(
                selected = mode,
                onSelect = { selected ->
                    if (selected != mode) {
                        keyboard?.hide()
                        focusManager.clearFocus()
                        modeName = selected.name
                    }
                },
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            if (mode == BookMode.Mind) {
                MindContent(
                    backdrop = backdrop,
                    answer = mindAnswer,
                    page = mindPage,
                    listening = listening,
                    onReveal = ::reveal,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.height(14.dp))
                Footer("365 则内置答案 · 无需联网")
                Spacer(Modifier.height(4.dp))
            } else {
                AskContent(
                    backdrop = backdrop,
                    config = settings.config,
                    question = question,
                    onQuestionChange = {
                        question = it
                        if (askError != null) askError = null
                    },
                    answer = askAnswer,
                    askedQuestion = askedQuestion,
                    asking = asking,
                    error = askError,
                    showCrisisNote = showCrisisNote,
                    onSubmit = ::submitQuestion,
                    onOpenSettings = ::openSettings,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.height(14.dp))
                Footer(
                    if (settings.config.isConfigured) "一次一问 · 只保留此刻的回答"
                    else "连接你的模型，答案会简短而含蓄"
                )
                Spacer(Modifier.height(4.dp))
            }
        }

        SettingsSheet(
            visible = settingsOpen,
            config = settings.config,
            updateConfig = settings.updateConfig,
            currentVersion = BuildConfig.VERSION_NAME,
            backdrop = backdrop,
            onDismiss = { settingsOpen = false },
            onSave = { baseUrl, model, key ->
                settings.save(baseUrl, model, key)
                askError = null
                settingsOpen = false
            },
            onSaveUpdate = settings::saveUpdate,
            onClearKey = { settings.clearKey() }
        )
    }
}

@Composable
private fun Header(
    backdrop: Backdrop,
    onSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BookSigil()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            BookText(
                text = "答案之书",
                style = BookType.Wordmark,
                color = BookColors.Ivory,
                maxLines = 1
            )
            BookText(
                text = "THE BOOK OF ANSWERS",
                style = BookType.Kicker,
                color = BookColors.Gold.copy(alpha = 0.7f),
                maxLines = 1
            )
        }
        GlassIconButton(
            icon = BookIcon.Sliders,
            contentDescription = "设置大模型接口",
            onClick = onSettings,
            backdrop = backdrop,
            size = 42.dp
        )
    }
}

@Composable
private fun BookSigil() {
    Box(
        modifier = Modifier.size(38.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color = BookColors.Gold.copy(alpha = 0.28f),
                radius = size.minDimension / 2f - 0.6.dp.toPx(),
                style = Stroke(width = 0.8.dp.toPx())
            )
            drawCircle(
                color = BookColors.Gold.copy(alpha = 0.09f),
                radius = size.minDimension / 2f - 4.dp.toPx(),
                style = Stroke(width = 0.5.dp.toPx())
            )
        }
        BookGlyph(
            icon = BookIcon.Spark,
            tint = BookColors.GoldBright,
            modifier = Modifier.size(17.dp)
        )
    }
}

@Composable
private fun MindContent(
    backdrop: Backdrop,
    answer: String?,
    page: Int,
    listening: Boolean,
    onReveal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val visual = OracleVisual(listening = listening, answer = answer)
    Column(modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            MindCard(
                backdrop = backdrop,
                visual = visual,
                page = page,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.height(16.dp))
        LiquidButton(
            label = when {
                listening -> "书页翻动中"
                answer != null -> "再问一次"
                else -> "翻开一页"
            },
            onClick = onReveal,
            backdrop = backdrop,
            enabled = !listening,
            loading = listening,
            icon = if (listening) null else BookIcon.Spark,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MindCard(
    backdrop: Backdrop,
    visual: OracleVisual,
    page: Int,
    modifier: Modifier = Modifier
) {
    val driftTransition = rememberInfiniteTransition(label = "bookSway")
    val sway = driftTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    GlassPanel(
        backdrop = backdrop,
        modifier = modifier
            .graphicsLayerSway(sway)
            .padding(horizontal = 1.dp),
        cornerRadius = 32.dp,
        tint = BookColors.Ivory,
        tintAlpha = 0.072f,
        shadowRadius = 34.dp,
        borderColor = BookColors.Gold.copy(alpha = 0.14f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 26.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BookText(
                    text = if (page > 0) "第 ${AnswerBank.pageLabel(page)} 页" else "封 面",
                    style = BookType.Page,
                    color = BookColors.Gold.copy(alpha = 0.78f),
                    maxLines = 1
                )
                Spacer(Modifier.weight(1f))
                BookText(
                    text = "365",
                    style = BookType.Page,
                    color = BookColors.Faint,
                    maxLines = 1
                )
            }

            Spacer(Modifier.weight(1f))

            AnimatedContent(
                targetState = visual,
                transitionSpec = {
                    (fadeIn(tween(620)) + scaleIn(initialScale = 0.94f, animationSpec = tween(620))) togetherWith
                            (fadeOut(tween(220)) + scaleOut(targetScale = 1.035f, animationSpec = tween(220)))
                },
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth(),
                label = "oracle"
            ) { state ->
                when {
                    state.listening -> ListeningPanel()
                    state.answer != null -> AnswerPanel(state.answer)
                    else -> IdlePanel()
                }
            }

            Spacer(Modifier.weight(1f))

            GlassDivider(Modifier.width(54.dp))
            Spacer(Modifier.height(12.dp))
            BookText(
                text = when {
                    visual.listening -> "让问题在沉默里停留一会儿"
                    visual.answer != null -> "这页只属于此刻的你"
                    else -> "不需要写下问题，只需要想着它"
                },
                style = BookType.Small,
                color = BookColors.Muted.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
        Box(
            Modifier
                .fillMaxSize()
                .padding(10.dp)
                .border(
                    width = 0.5.dp,
                    color = BookColors.Gold.copy(alpha = 0.075f),
                    shape = RoundedCornerShape(24.dp)
                )
        )
    }
}

@Composable
private fun IdlePanel() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BookGlyph(
            icon = BookIcon.Spark,
            tint = BookColors.Gold.copy(alpha = 0.72f),
            modifier = Modifier.size(18.dp)
        )
        BookText(
            text = "闭上眼",
            style = BookType.Answer.copy(fontSize = 25.sp, letterSpacing = 4.sp),
            color = BookColors.Ivory,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        BookText(
            text = "想一个此刻最想知道的问题。\n然后再翻开这一页。",
            style = BookType.Question.copy(fontSize = 15.sp, lineHeight = 25.sp),
            color = BookColors.Muted,
            textAlign = TextAlign.Center,
            maxLines = 3
        )
    }
}

@Composable
private fun ListeningPanel() {
    val transition = rememberInfiniteTransition(label = "listening")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse"
    )
    val phase = (pulse * 2f * PI).toFloat()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(86.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val base = size.minDimension / 2f
                for (i in 0..2) {
                    val wave = (pulse + i / 3f) % 1f
                    drawCircle(
                        color = BookColors.Gold.copy(alpha = (1f - wave) * 0.16f),
                        radius = base * (0.38f + wave * 0.58f),
                        style = Stroke(width = 0.7.dp.toPx())
                    )
                }
                drawCircle(
                    color = BookColors.Gold.copy(alpha = 0.08f + 0.04f * sin(phase)),
                    radius = base * 0.32f
                )
            }
            OrbitSpinner(
                color = BookColors.GoldBright,
                modifier = Modifier.size(22.dp)
            )
        }
        BookText(
            text = "书页正在翻动",
            style = BookType.Answer.copy(fontSize = 21.sp, letterSpacing = 3.sp),
            color = BookColors.Ivory,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        BookText(
            text = "先别急着要一个答案。",
            style = BookType.Small,
            color = BookColors.Muted,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun AnswerPanel(answer: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        BookText(
            text = "THE PAGE READS",
            style = BookType.Kicker.copy(letterSpacing = 2.8.sp),
            color = BookColors.Gold.copy(alpha = 0.62f),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        BookText(
            text = answer,
            style = BookType.Answer.copy(
                fontSize = 30.sp,
                lineHeight = 42.sp,
                letterSpacing = 1.1.sp
            ),
            color = BookColors.Ivory,
            textAlign = TextAlign.Center,
            maxLines = 1,
            autoSize = TextAutoSize.StepBased(15.sp, 30.sp, 1.sp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier
                    .width(18.dp)
                    .height(1.dp)
                    .background(BookColors.Gold.copy(alpha = 0.28f))
            )
            BookGlyph(
                icon = BookIcon.Spark,
                tint = BookColors.Gold.copy(alpha = 0.5f),
                modifier = Modifier.size(8.dp)
            )
            Box(
                Modifier
                    .width(18.dp)
                    .height(1.dp)
                    .background(BookColors.Gold.copy(alpha = 0.28f))
            )
        }
    }
}

@Composable
private fun AskContent(
    backdrop: Backdrop,
    config: LlmConfig,
    question: String,
    onQuestionChange: (String) -> Unit,
    answer: String?,
    askedQuestion: String,
    asking: Boolean,
    error: String?,
    showCrisisNote: Boolean,
    onSubmit: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            GlassPanel(
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 26.dp,
                tint = BookColors.Ivory,
                tintAlpha = 0.05f,
                shadowRadius = 18.dp
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BookText(
                            text = "写下你的问题",
                            style = BookType.Label,
                            color = BookColors.Gold.copy(alpha = 0.9f),
                            maxLines = 1
                        )
                        Spacer(Modifier.weight(1f))
                        BookText(
                            text = "一次一问",
                            style = BookType.Page.copy(letterSpacing = 1.2.sp),
                            color = BookColors.Faint,
                            maxLines = 1
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    GlassTextField(
                        value = question,
                        onValueChange = onQuestionChange,
                        placeholder = "把问题写在这里。越具体，回声越清晰。",
                        backdrop = backdrop,
                        singleLine = false,
                        minHeight = 106.dp,
                        maxLength = 200,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Send
                        ),
                        keyboardActions = KeyboardActions(onSend = { onSubmit() })
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (config.isConfigured) {
                            GlassChip(
                                text = "已连接 · " + config.model,
                                backdrop = backdrop,
                                leadingIcon = BookIcon.Check,
                                tint = BookColors.Teal.copy(alpha = 0.9f),
                                onClick = onOpenSettings
                            )
                        } else {
                            GlassChip(
                                text = "尚未连接大模型",
                                backdrop = backdrop,
                                leadingIcon = BookIcon.Key,
                                tint = BookColors.Gold,
                                onClick = onOpenSettings
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        BookText(
                            text = question.length.toString() + "/200",
                            style = BookType.Page.copy(letterSpacing = 0.8.sp),
                            color = BookColors.Faint,
                            maxLines = 1
                        )
                    }
                    if (error != null) {
                        Spacer(Modifier.height(10.dp))
                        BookText(
                            text = error,
                            style = BookType.Small,
                            color = BookColors.Rose,
                            maxLines = 3
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            GlassPanel(
                backdrop = backdrop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(312.dp),
                cornerRadius = 28.dp,
                tint = BookColors.Ivory,
                tintAlpha = 0.048f,
                shadowRadius = 22.dp
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BookText(
                            text = "书页回答",
                            style = BookType.Page,
                            color = BookColors.Gold.copy(alpha = 0.7f),
                            maxLines = 1
                        )
                        Spacer(Modifier.weight(1f))
                        BookGlyph(
                            icon = BookIcon.Spark,
                            tint = BookColors.Gold.copy(alpha = 0.45f),
                            modifier = Modifier.size(10.dp)
                        )
                    }
                    Spacer(Modifier.weight(1f))

                    Crossfade(
                        targetState = when {
                            asking -> "asking"
                            answer != null -> "answer"
                            else -> "empty"
                        },
                        animationSpec = tween(480),
                        label = "askAnswer"
                    ) { state ->
                        when (state) {
                            "asking" -> Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                OrbitSpinner(
                                    color = BookColors.GoldBright,
                                    modifier = Modifier.size(24.dp)
                                )
                                BookText(
                                    text = "正在问书",
                                    style = BookType.AnswerSmall,
                                    color = BookColors.Ivory,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                BookText(
                                    text = "答案不会太长。",
                                    style = BookType.Small,
                                    color = BookColors.Muted,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }

                            "answer" -> Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (askedQuestion.isNotBlank()) {
                                    BookText(
                                        text = askedQuestion,
                                        style = BookType.Small,
                                        color = BookColors.Muted.copy(alpha = 0.75f),
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                BookText(
                                    text = answer.orEmpty(),
                                    style = askAnswerStyle(answer.orEmpty().length),
                                    color = BookColors.Ivory,
                                    textAlign = TextAlign.Center,
                                    maxLines = 5
                                )
                            }

                            else -> Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                BookGlyph(
                                    icon = BookIcon.Spark,
                                    tint = BookColors.Gold.copy(alpha = 0.35f),
                                    modifier = Modifier.size(16.dp)
                                )
                                BookText(
                                    text = "答案会出现在这里",
                                    style = BookType.Question.copy(fontSize = 16.sp),
                                    color = BookColors.Faint,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                                BookText(
                                    text = "每次提问都会覆盖上一次的回答",
                                    style = BookType.Small,
                                    color = BookColors.Faint.copy(alpha = 0.75f),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    if (showCrisisNote && answer != null) {
                        Spacer(Modifier.height(8.dp))
                        BookText(
                            text = "如果你此刻正处在危险中，请先联系身边可信任的人，或当地紧急服务。",
                            style = BookType.Small.copy(fontSize = 10.5.sp),
                            color = BookColors.Gold.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }

                    Spacer(Modifier.weight(1f))
                    GlassDivider(Modifier.width(38.dp))
                    Spacer(Modifier.height(9.dp))
                    BookText(
                        text = "只 留 这 一 页",
                        style = BookType.Page.copy(letterSpacing = 1.8.sp),
                        color = BookColors.Faint.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        LiquidButton(
            label = if (asking) "正在问书" else "让书回答",
            onClick = onSubmit,
            backdrop = backdrop,
            enabled = !asking,
            loading = asking,
            icon = if (asking) null else BookIcon.Send,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun askAnswerStyle(length: Int): TextStyle {
    val size = when {
        length <= 12 -> 25f
        length <= 20 -> 23f
        else -> 21f
    }
    return BookType.AnswerSmall.copy(
        fontSize = size.sp,
        lineHeight = (size * 1.48f).sp,
        letterSpacing = 0.8.sp,
        lineBreak = LineBreak.Paragraph
    )
}

@Composable
private fun Footer(text: String) {
    BookText(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        style = BookType.Kicker.copy(letterSpacing = 1.3.sp),
        color = BookColors.Faint.copy(alpha = 0.85f),
        textAlign = TextAlign.Center,
        maxLines = 1
    )
}

private fun Modifier.graphicsLayerSway(sway: State<Float>): Modifier = this
    .graphicsLayer {
        val phase = sway.value * (2f * PI).toFloat()
        translationY = sin(phase) * 2.5.dp.toPx()
        rotationZ = sin(phase + 0.7f) * 0.08f
    }
