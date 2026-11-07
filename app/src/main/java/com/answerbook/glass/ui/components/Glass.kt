package com.answerbook.glass.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.answerbook.glass.ui.theme.BookColors
import com.answerbook.glass.ui.theme.BookType
import com.answerbook.glass.ui.theme.Sans
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightStyle
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle

enum class BookMode(val label: String, val english: String) {
    Mind("读心", "MIND"),
    Ask("问书", "ASK")
}

@Composable
fun BookText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = BookType.Body,
    color: Color = BookColors.Ivory,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    autoSize: TextAutoSize? = null
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = style.merge(TextStyle(color = color, textAlign = textAlign)),
        maxLines = maxLines,
        overflow = overflow,
        autoSize = autoSize
    )
}

/**
 * 一块会折射背景的液态玻璃。
 * 所有可见的卡片、输入框和按钮都建立在这个底层之上。
 */
@Composable
fun GlassPanel(
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 30.dp,
    tint: Color = BookColors.GlassWhite,
    tintAlpha: Float = 0.065f,
    shadowRadius: Dp = 28.dp,
    borderColor: Color = BookColors.Gold.copy(alpha = 0.10f),
    content: @Composable BoxScope.() -> Unit
) {
    val shape = remember(cornerRadius) { RoundedRectangle(cornerRadius) }
    Box(
        modifier = modifier.liquidGlass(
            backdrop = backdrop,
            shape = shape,
            blurRadius = 10.dp,
            refractionHeight = 16.dp,
            refractionAmount = 30.dp,
            tint = tint,
            tintAlpha = tintAlpha,
            shadowRadius = shadowRadius,
            innerShadowRadius = 12.dp,
            borderColor = borderColor
        ),
        content = content
    )
}

@Composable
fun LiquidButton(
    label: String,
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    icon: BookIcon? = BookIcon.Spark,
    enabled: Boolean = true,
    loading: Boolean = false,
    secondary: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.968f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 620f),
        label = "buttonScale"
    )
    val shape = remember { Capsule() }
    val surfaceTint = if (secondary) BookColors.GlassWhite else BookColors.Gold
    val surfaceAlpha = if (secondary) 0.07f else 0.19f
    val textColor = when {
        !enabled -> BookColors.Faint
        secondary -> BookColors.Gold
        else -> BookColors.Ivory
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.55f
            }
            .liquidGlass(
                backdrop = backdrop,
                shape = shape,
                blurRadius = 8.dp,
                refractionHeight = 14.dp,
                refractionAmount = 26.dp,
                tint = surfaceTint,
                tintAlpha = surfaceAlpha,
                shadowRadius = 16.dp,
                innerShadowRadius = 8.dp,
                borderColor = if (secondary) BookColors.Gold.copy(alpha = 0.13f)
                else BookColors.GoldBright.copy(alpha = 0.28f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !loading,
                role = Role.Button,
                onClick = onClick
            )
            .height(62.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 22.dp)
        ) {
            if (loading) {
                OrbitSpinner(
                    color = if (secondary) BookColors.Gold else BookColors.GoldBright,
                    modifier = Modifier.size(17.dp)
                )
            } else if (icon != null) {
                BookGlyph(
                    icon = icon,
                    tint = if (enabled) textColor.copy(alpha = 0.9f) else BookColors.Faint,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            BookText(
                text = label,
                style = BookType.Button,
                color = textColor,
                maxLines = 1
            )
        }
    }
}

@Composable
fun GlassIconButton(
    icon: BookIcon,
    contentDescription: String,
    onClick: () -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    enabled: Boolean = true,
    tint: Color = BookColors.Gold
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.91f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 700f),
        label = "iconScale"
    )
    val shape = remember { RoundedRectangle(15.dp) }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.4f
            }
            .liquidGlass(
                backdrop = backdrop,
                shape = shape,
                blurRadius = 6.dp,
                refractionHeight = 9.dp,
                refractionAmount = 16.dp,
                tint = BookColors.GlassWhite,
                tintAlpha = 0.055f,
                shadowRadius = 8.dp,
                innerShadowRadius = 4.dp,
                borderColor = BookColors.Gold.copy(alpha = 0.12f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        BookGlyph(icon, tint.copy(alpha = 0.9f), Modifier.size(18.dp))
    }
}

@Composable
fun GlassChip(
    text: String,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    leadingIcon: BookIcon? = null,
    tint: Color = BookColors.Muted
) {
    val shape = remember { Capsule() }
    val interactionSource = remember { MutableInteractionSource() }
    val base = modifier.liquidGlass(
        backdrop = backdrop,
        shape = shape,
        blurRadius = 4.dp,
        refractionHeight = 6.dp,
        refractionAmount = 10.dp,
        tint = BookColors.GlassWhite,
        tintAlpha = 0.035f,
        shadowRadius = 3.dp,
        innerShadowRadius = 2.dp,
        borderColor = BookColors.Gold.copy(alpha = 0.10f)
    )
    val clickableModifier = if (onClick != null) {
        base.clickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
            onClick = onClick
        )
    } else {
        base
    }

    Row(
        modifier = clickableModifier.padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (leadingIcon != null) {
            BookGlyph(leadingIcon, tint.copy(alpha = 0.9f), Modifier.size(12.dp), strokeWidth = 1.5f)
        }
        BookText(text, style = BookType.Small, color = tint, maxLines = 1)
    }
}

@Composable
fun GlassModeSwitch(
    selected: BookMode,
    onSelect: (BookMode) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val shape = remember { Capsule() }
    BoxWithConstraints(
        modifier = modifier
            .height(48.dp)
            .liquidGlass(
                backdrop = backdrop,
                shape = shape,
                blurRadius = 8.dp,
                refractionHeight = 12.dp,
                refractionAmount = 24.dp,
                tint = BookColors.GlassWhite,
                tintAlpha = 0.045f,
                shadowRadius = 10.dp,
                innerShadowRadius = 6.dp
            )
            .padding(4.dp)
    ) {
        val segmentWidth = (maxWidth - 0.dp) / 2
        val offset by animateDpAsState(
            targetValue = if (selected == BookMode.Mind) 0.dp else segmentWidth,
            animationSpec = spring(dampingRatio = 0.72f, stiffness = 420f),
            label = "modeOffset"
        )

        Box(
            Modifier
                .graphicsLayer { translationX = offset.toPx() }
                .width(segmentWidth)
                .fillMaxHeight()
                .liquidGlass(
                    backdrop = backdrop,
                    shape = shape,
                    blurRadius = 2.dp,
                    refractionHeight = 6.dp,
                    refractionAmount = 10.dp,
                    tint = BookColors.Gold,
                    tintAlpha = 0.115f,
                    shadowRadius = 3.dp,
                    innerShadowRadius = 2.dp
                )
        )

        Row(Modifier.fillMaxSize()) {
            ModeItem(
                mode = BookMode.Mind,
                selected = selected == BookMode.Mind,
                onSelect = onSelect,
                modifier = Modifier.weight(1f)
            )
            ModeItem(
                mode = BookMode.Ask,
                selected = selected == BookMode.Ask,
                onSelect = onSelect,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun RowScope.ModeItem(
    mode: BookMode,
    selected: Boolean,
    onSelect: (BookMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val color by animateColorAsState(
        targetValue = if (selected) BookColors.Ivory else BookColors.Muted,
        animationSpec = tween(220),
        label = "modeColor"
    )
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(Capsule())
            .selectable(
                selected = selected,
                interactionSource = null,
                indication = null,
                role = Role.Tab,
                onClick = { onSelect(mode) }
            ),
        contentAlignment = Alignment.Center
    ) {
        BookText(
            text = mode.label,
            style = BookType.Label.copy(fontFamily = Sans, letterSpacing = 2.5.sp),
            color = color,
            maxLines = 1
        )
    }
}

@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    backdrop: Backdrop,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minHeight: Dp = 54.dp,
    maxLength: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    leadingIcon: BookIcon? = null,
    trailing: (@Composable () -> Unit)? = null,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val focusProgress by animateFloatAsState(
        targetValue = if (focused) 1f else 0f,
        animationSpec = tween(220),
        label = "focus"
    )
    val shape = remember(singleLine) {
        RoundedRectangle(if (singleLine) 18.dp else 22.dp)
    }
    val borderColor = BookColors.Gold.copy(alpha = 0.18f + 0.45f * focusProgress)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .liquidGlass(
                backdrop = backdrop,
                shape = shape,
                blurRadius = 5.dp,
                refractionHeight = 8.dp,
                refractionAmount = 14.dp,
                tint = BookColors.GlassWhite,
                tintAlpha = 0.032f + 0.02f * focusProgress,
                shadowRadius = 4.dp,
                innerShadowRadius = 3.dp,
                borderColor = borderColor
            )
            .heightIn(min = minHeight)
    ) {
        BasicTextField(
            value = value,
            onValueChange = { next ->
                if (maxLength == null || next.length <= maxLength) onValueChange(next)
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight),
            enabled = enabled,
            textStyle = BookType.Field.copy(color = BookColors.Ivory),
            cursorBrush = SolidColor(BookColors.Gold),
            singleLine = singleLine,
            maxLines = if (singleLine) 1 else 5,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = minHeight)
                        .padding(horizontal = 16.dp, vertical = if (singleLine) 0.dp else 15.dp),
                    verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (leadingIcon != null) {
                        BookGlyph(
                            leadingIcon,
                            BookColors.Gold.copy(alpha = 0.7f),
                            Modifier
                                .padding(top = if (singleLine) 0.dp else 3.dp)
                                .size(16.dp)
                        )
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .heightIn(min = if (singleLine) 24.dp else 82.dp),
                        contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
                    ) {
                        if (value.isEmpty()) {
                            BookText(
                                text = placeholder,
                                style = BookType.Field.copy(color = BookColors.Faint),
                                maxLines = if (singleLine) 1 else 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }
                    if (trailing != null) {
                        Box(Modifier.align(Alignment.CenterVertically)) { trailing() }
                    }
                }
            }
        )
    }
}

@Composable
fun OrbitSpinner(
    color: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "spinner")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )
    androidx.compose.foundation.Canvas(modifier) {
        rotate(angle) {
            drawArc(
                color = color,
                startAngle = -80f,
                sweepAngle = 240f,
                useCenter = false,
                style = Stroke(width = 1.7.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun GlassDivider(
    modifier: Modifier = Modifier,
    color: Color = BookColors.Gold.copy(alpha = 0.18f)
) {
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, color, Color.Transparent)
                )
            )
    )
}

@Composable
fun EmptyHint(text: String, modifier: Modifier = Modifier) {
    BookText(
        text = text,
        modifier = modifier,
        style = BookType.Small,
        color = BookColors.Faint,
        textAlign = TextAlign.Center
    )
}

private fun Modifier.liquidGlass(
    backdrop: Backdrop,
    shape: Shape,
    blurRadius: Dp,
    refractionHeight: Dp,
    refractionAmount: Dp,
    tint: Color,
    tintAlpha: Float,
    shadowRadius: Dp,
    innerShadowRadius: Dp,
    borderColor: Color? = null
): Modifier {
    return drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            vibrancy()
            blur(blurRadius.toPx())
            lens(
                refractionHeight = refractionHeight.toPx(),
                refractionAmount = refractionAmount.toPx(),
                depthEffect = true
            )
        },
        highlight = {
            Highlight(
                width = 0.75.dp,
                blurRadius = 0.4.dp,
                alpha = 0.72f,
                style = HighlightStyle.Default(angle = 45f, falloff = 1.25f)
            )
        },
        shadow = {
            Shadow(
                radius = shadowRadius,
                color = Color.Black.copy(alpha = 0.32f),
                alpha = 0.8f
            )
        },
        innerShadow = {
            InnerShadow(
                radius = innerShadowRadius,
                offset = DpOffset(0.dp, 1.5.dp),
                color = Color.Black.copy(alpha = 0.3f),
                alpha = 0.5f
            )
        },
        onDrawSurface = {
            drawRect(
                Brush.verticalGradient(
                    colors = listOf(
                        tint.copy(alpha = tintAlpha * 1.35f),
                        tint.copy(alpha = tintAlpha * 0.55f),
                        Color.Black.copy(alpha = 0.035f)
                    )
                )
            )
            drawRect(
                Brush.radialGradient(
                    colors = listOf(
                        BookColors.Gold.copy(alpha = tintAlpha * 0.75f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.22f, 0f),
                    radius = size.maxDimension
                )
            )
        },
        onDrawFront = {
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = 0.22f),
                        Color.Transparent
                    )
                ),
                start = Offset(size.width * 0.16f, 1.dp.toPx()),
                end = Offset(size.width * 0.84f, 1.dp.toPx()),
                strokeWidth = 0.7.dp.toPx()
            )
            if (borderColor != null) {
                drawOutline(
                    outline = shape.createOutline(size, layoutDirection, this),
                    color = borderColor,
                    style = Stroke(width = 0.8.dp.toPx())
                )
            }
        }
    )
}
