package com.rightbalance.minesweeper

import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import kotlin.math.abs


val baseStrokePaint = Paint().apply {
    style = Paint.Style.STROKE
    color = android.graphics.Color.GRAY
    strokeWidth = 6f
}

val strokePaint = Paint().apply {
    style = Paint.Style.STROKE
    color = android.graphics.Color.RED
    strokeWidth = 10f
}

val minePaint = Paint().apply {
    style = Paint.Style.FILL
    color = android.graphics.Color.RED
}

val emptyPaint = Paint().apply {
    style = Paint.Style.FILL
    color = android.graphics.Color.GRAY
}

val hiddenPaint = Paint().apply {
    style = Paint.Style.FILL
    color = android.graphics.Color.BLACK
}

val visibleTextPaint = Paint().apply {
    color = android.graphics.Color.BLACK
    textSize = 50f
}

val testTextPaint = Paint().apply {
    color = android.graphics.Color.YELLOW
    textSize = 50f
}

@Composable
fun MinesweeperScreen(
    modifier: Modifier,
    minesweeperGame: MinesweeperGame,
) {
    val isGameOverState = remember {
        mutableStateOf(false)
    }

    val gameState = remember {
        mutableStateOf(
            GameState(
                minesweeperGame = minesweeperGame,
            )
        )
    }

    ConstraintLayout(modifier = Modifier.fillMaxWidth()) {
        val (options, game) = createRefs()

        Options(
            modifier = Modifier
                .constrainAs(options) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            gameState = gameState,
        )

        BoxWithConstraints(
            modifier = modifier.constrainAs(game) {
                top.linkTo(options.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
                height = Dimension.fillToConstraints
            }
        ) {
            val screenWidthPx = maxWidth.dpToPx()
            val screenWidthDp = maxWidth

            val screenHeightPx = maxHeight.dpToPx()
            val screenHeightDp = maxHeight

            val slotWidth = screenWidthPx / MinesweeperGame.COLS
            val slotHeight = screenHeightPx / MinesweeperGame.ROWS

            gameState.value.configureSlots(slotWidth, slotHeight)

            Log.i("Testing", "Mine MinesweeperScreen")

            DrawMines(
                modifier = modifier,
                screenWidthDp = screenWidthDp,
                screenHeightDp = screenHeightDp,
                gameState = gameState,
                isGameOverState = isGameOverState,
            )
        }
    }
}

@Composable
fun Options(
    modifier: Modifier,
    gameState: MutableState<GameState>,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (gameState.value.isGameDone) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = "You Won!!",
            )
        }

        if (gameState.value.isGameOver) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = "Game Over",
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = {
                    if (gameState.value.isHideNumbers) {
                        gameState.value = gameState.value.showNumbers()
                    } else {
                        gameState.value = gameState.value.hideNumbers()
                    }
                },
            ) {
                if (gameState.value.isHideNumbers) {
                    Text("Show numbers")
                } else {
                    Text("Hide numbers")
                }
            }

            Button(
                onClick = {
                    gameState.value = gameState.value.startGame()
                },
            ) {
                Text("Play")
            }
        }
    }
}

@Composable
fun DrawMines(
    modifier: Modifier,
    screenWidthDp: Dp,
    screenHeightDp: Dp,
    gameState: MutableState<GameState>,
    isGameOverState: MutableState<Boolean>,
) {
    Log.i("Testing", "Mine DrawMines")
    Canvas(
        modifier = modifier
            .size(
                screenWidthDp,
                screenHeightDp,
            )
            .pointerInput(Unit) {
                detectTapGestures {
                    if (gameState.value.isGameDone.not()) {
                        for (i in gameState.value.slots.indices) {
                            val slot = gameState.value.slots[i]

                            if (slot.isVisible.not() && slot.contains(it.x, it.y)) {
                                if (slot.slotType == SlotType.MINE) {
                                    gameState.value = gameState.value.gameOver()
                                    isGameOverState.value = true
                                } else {
                                    gameState.value = gameState.value.revealSlot(
                                        index = i,
                                        slot = slot,
                                    )
                                }

                                break
                            }
                        }
                    }
                }
            }
    ) {
        gameState.value.slots.forEach { slot ->
            drawContext.canvas.nativeCanvas.drawRect(
                slot.x,
                slot.y,
                slot.end(),
                slot.bottom(),
                if (slot.isVisible) {
                    when (slot.slotType) {
                        SlotType.MINE -> {
                            minePaint
                        }

                        SlotType.EMPTY -> {
                            emptyPaint
                        }

                        SlotType.NUMBER -> {
                            strokePaint
                        }
                    }
                } else {
                    hiddenPaint
                },
            )

            drawContext.canvas.nativeCanvas.drawRect(
                slot.x,
                slot.y,
                slot.end(),
                slot.bottom(),
                baseStrokePaint,
            )

            if (slot.isVisible) {
                drawContext.canvas.nativeCanvas.drawText(
                    slot.number.toString(),
                    slot.x + (slot.width / 2),
                    slot.y + (slot.height / 2),
                    visibleTextPaint,
                )
            } else {
                if (gameState.value.isHideNumbers.not()) {
                    // TODO shows only for number slot or number and empty
                    drawContext.canvas.nativeCanvas.drawText(
                        slot.number.toString(),
                        slot.x + (slot.width / 2),
                        slot.y + (slot.height / 2),
                        testTextPaint,
                    )
                }
            }
        }
    }
}

@Preview(heightDp = 800)
@Composable
fun Preview_MinesweeperScreen() {
    MinesweeperScreen(
        modifier = Modifier.fillMaxSize(),
        minesweeperGame = MinesweeperGame()
    )
}

data class GameState(
    val index: Int = 0,
    val isGameOver: Boolean = false,
    val isGameDone: Boolean = false,
    val isHideNumbers: Boolean = false,
    val slots: ArrayList<Slot> = arrayListOf(),
    val minesweeperGame: MinesweeperGame,
) {

    private var slotWidth: Float = 0f
    private var slotHeight: Float = 0f

    fun gameOver(): GameState {
        for (i in slots.indices) {
            if (slots[i].slotType == SlotType.MINE) {
                slots[i] = slots[i].copy(isVisible = true)
            }
        }
        return this.copy(isGameOver = true)
    }

    fun startGame(): GameState {
        minesweeperGame.startGame()

        slots.clear()

        buildSlots()

        return this.copy(isGameOver = false, isGameDone = false)
    }

    fun revealSlot(index: Int, slot: Slot): GameState {
        slots[index] = slot.copy(
            isVisible = true,
        )

        if (slot.slotType == SlotType.EMPTY) {
            minesweeperGame.revealCell(
                row = slot.position.first,
                col = slot.position.second,
            ).forEach { slotToReveal ->
                val sIndex = slots.indexOfFirst { it.position == slotToReveal }

                if (sIndex > -1) {
                    slots[sIndex] = slots[sIndex].copy(isVisible = true)
                }
            }
        }

        val isDone = slots.filter { it.slotType == SlotType.EMPTY }.all { it.isVisible }

        if (isDone) {
            return copy(isGameDone = true)
        }

        return nextIndex()
    }

    fun configureSlots(w: Float, h: Float) {
        slotWidth = w
        slotHeight = h
    }

    private fun buildSlots() {
        for (r in 0 until MinesweeperGame.ROWS) {
            for (c in 0 until MinesweeperGame.COLS) {

                val b = minesweeperGame.board[r][c]

                val slotType = when {
                    b == MinesweeperGame.MINE -> SlotType.MINE
                    b > 0 -> SlotType.NUMBER
                    else -> SlotType.EMPTY
                }

                slots.add(
                    Slot(
                        x = c * slotWidth,
                        y = r * slotHeight,
                        width = slotWidth,
                        height = slotHeight,
                        slotType = slotType,
                        number = b,
                        isVisible = false,
                        position = r to c,
                    )
                )
            }
        }
    }

    // Useful to force the recomposition
    private fun nextIndex() = copy(
        index = index + 1,
    )

    fun hideNumbers(): GameState {
        return copy(isHideNumbers = true)
    }

    fun showNumbers(): GameState {
        return copy(isHideNumbers = false)
    }
}

data class Slot(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val slotType: SlotType,
    val number: Int,
    val isVisible: Boolean,
    val position: Pair<Int, Int>,
) {
    fun end(): Float = x + width
    fun bottom(): Float = y + height

    fun contains(pX: Float, pY: Float): Boolean {
        return abs(x) < (abs(x) + width) &&
                abs(y) < (abs(y) + height) &&
                pX >= abs(x) &&
                pX < (abs(x) + width) &&
                pY >= abs(y) &&
                pY < (abs(y) + height)
    }
}

enum class SlotType {
    MINE, EMPTY, NUMBER
}


@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }