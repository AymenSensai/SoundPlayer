package com.aymen.soundplayer

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.aymen.soundplayer.ui.theme.Colors
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

@SuppressLint("UnusedContentLambdaTargetStateParameter")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListenScreen() {

    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }

    val colors = Colors.colors
    val darkColors = Colors.darkColors
    val colorIndex = remember {
        mutableIntStateOf(0)
    }
    LaunchedEffect(colorIndex.intValue) {
        delay(2100)
        if (colorIndex.intValue < darkColors.lastIndex) {
            colorIndex.intValue += 1
        } else {
            colorIndex.intValue = 0
        }
    }
    val animatedColor by animateColorAsState(
        targetValue = colors[colorIndex.intValue],
        animationSpec = tween(2000),
        label = ""
    )
    val animatedDarkColor by animateColorAsState(
        targetValue = darkColors[colorIndex.intValue],
        animationSpec = tween(2000),
        label = ""
    )

    val sounds = listOf(Sound.Rain, Sound.Nature, Sound.Ocean)
    val pagerState = rememberPagerState(pageCount = { sounds.size })
    val playingIndex = remember {
        mutableIntStateOf(0)
    }
    LaunchedEffect(Unit) {
        sounds.forEach {
            player.addMediaItem(context.getVideoUri(it.sound))
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        playingIndex.intValue = pagerState.currentPage
        player.seekTo(pagerState.currentPage, 0)
    }
    player.prepare()

    val playing = remember(player.isPlaying) {
        mutableStateOf(player.isPlaying)
    }
    val currentPosition = remember(player.currentPosition) {
        mutableLongStateOf(player.currentPosition)
    }
    val totalDuration  = remember{
        mutableLongStateOf(0)
    }
    val progressSize = remember {
        mutableStateOf(IntSize(0, 0))
    }

    LaunchedEffect(player.duration){
        if (player.duration > 0){
            totalDuration.longValue = player.duration
        }
    }
    LaunchedEffect(player.currentMediaItemIndex) {
        playingIndex.intValue = player.currentMediaItemIndex
        pagerState.animateScrollToPage(
            playingIndex.intValue,
            animationSpec = tween(500)
        )
    }

    var percentReached =
        currentPosition.longValue.toFloat() / (if (totalDuration.longValue > 0) totalDuration.longValue else 0).toFloat()

    if (percentReached.isNaN()) {
        percentReached = 0f
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(listOf(animatedColor, animatedDarkColor))),
    ) {
        val configuration = LocalConfiguration.current

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val textColor by animateColorAsState(
                targetValue = if (animatedColor.luminance() > .5f)
                    MaterialTheme.colorScheme.primary
                 else MaterialTheme.colorScheme.secondary,
                animationSpec = tween(2000), label = ""
            )
            AnimatedContent(
                targetState = playingIndex.intValue,
                transitionSpec = { (scaleIn() + fadeIn()) togetherWith (scaleOut() + fadeOut()) },
                label = ""
            ) {
                Text(text = sounds[it].name, fontSize = 52.sp, color = textColor)
            }

            Spacer(modifier = Modifier.height(32.dp))

            HorizontalPager(
                modifier = Modifier.fillMaxWidth(), state = pagerState,
                pageSize = PageSize.Fixed((configuration.screenWidthDp / (1.7)).dp),
                contentPadding = PaddingValues(horizontal = 85.dp)
            ) { page ->
                Card(
                    modifier = Modifier
                        .size((configuration.screenWidthDp / (1.8)).dp)
                        .graphicsLayer {
                            val pageOffset =
                                ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                            val alphaLerp = lerp(
                                start = 0.4f.toDp(),
                                stop = 1f.toDp(),
                                fraction = 1f - pageOffset.coerceIn(0f, 1f)
                            )
                            val scaleLerp = lerp(
                                start = 0.5f.toDp(),
                                stop = 1f.toDp(),
                                fraction = 1f - pageOffset.coerceIn(0f, 5f)
                            )
                            alpha = alphaLerp.toPx()
                            scaleX = scaleLerp.toPx()
                            scaleY = scaleLerp.toPx()
                        }
                        .border(3.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                        .padding(6.dp),
                    shape = CircleShape
                ) {
                    Image(
                        painter = painterResource(id = sounds[page].image),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(54.dp))

            Row(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = convertLongToText(currentPosition.longValue),
                    modifier = Modifier.width(55.dp),
                    color = textColor,
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .height(8.dp)
                        .padding(horizontal = 8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary)
                        .onGloballyPositioned { progressSize.value = it.size }
                        .pointerInput(Unit) {
                            detectTapGestures {
                                val xPos = it.x
                                val whereIClicked =
                                    (xPos.toLong() * totalDuration.longValue) / progressSize.value.width.toLong()
                                player.seekTo(whereIClicked)
                            }
                        }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction =percentReached)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Text(
                    text = convertLongToText(totalDuration.longValue),
                    modifier = Modifier.width(55.dp),
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Control(
                    icon = R.drawable.rewind_icon,
                    size = 60.dp,
                    onClick = { player.seekToPreviousMediaItem() })
                Control(
                    icon = if (playing.value) R.drawable.pause_icon else R.drawable.play_icon,
                    size = 80.dp,
                    onClick = { if (playing.value) player.pause() else player.play() })
                Control(
                    icon = R.drawable.forward_icon,
                    size = 60.dp,
                    onClick = { player.seekToNextMediaItem() })
            }

        }
    }
}

@Composable
private fun Control(icon: Int, size: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondary)
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(size / 2)
        )
    }
}

private fun convertLongToText(long: Long): String {
    val sec = long / 1000
    val minutes = sec / 60
    val seconds = sec % 60

    val minutesString = if (minutes < 10) {
        "0${minutes}"
    } else {
        minutes.toString()
    }
    val secondsString = if (seconds < 10) {
        "0${seconds}"
    } else {
        seconds.toString()
    }
    return "$minutesString:$secondsString"
}

private fun Context.getVideoUri(sound: Int): MediaItem {
    val path = "android.resource://$packageName/$sound"
    val mediaItem = MediaItem.fromUri(Uri.parse(path))
    return mediaItem
}