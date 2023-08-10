package com.rightbalance.minesweeper

import kotlin.random.Random

class MinesweeperGame {

    // Constants to define the size of the game board
    companion object {
        const val ROWS = 8
        const val COLS = 8
        const val MINES = 10
        const val MINE = 99
    }

    // A 2D array to represent the game board
    var board = Array(ROWS) { IntArray(COLS) { 0 } }

    // An array to keep track of the positions of mines on the board
    private val mines = mutableListOf<Pair<Int, Int>>()

    fun startGame() {
        mines.clear()

        initializeMines()
        populateMines()
    }

    private fun initializeMines() {
        // Initialize the board, state, and place mines randomly
        for (row in 0 until ROWS) {
            for (col in 0 until COLS) {
                board[row][col] = 0
            }
        }

        for (i in 0 until MINES) {
            var row: Int
            var col: Int
            do {
                row = Random.nextInt(ROWS)
                col = Random.nextInt(COLS)
            } while (board[row][col] == MINE)

            board[row][col] = MINE
            mines.add(Pair(row, col))
        }
    }

    // Function to count the number of mines around a cell
    fun countMines(row: Int, col: Int): Int {
        var count = 0
        for (r in maxOf(0, row - 1) until minOf(row + 2, ROWS)) {
            for (c in maxOf(0, col - 1) until minOf(col + 2, COLS)) {
                if (board[r][c] == MINE) {
                    count++
                }
            }
        }
        return count
    }

    // Populate the board with the number of mines around each cell
    private fun populateMines() {
        for (mine in mines) {
            val row = mine.first
            val col = mine.second
            for (r in maxOf(0, row - 1) until minOf(row + 2, ROWS)) {
                for (c in maxOf(0, col - 1) until minOf(col + 2, COLS)) {
                    if (board[r][c] != MINE) {
                        board[r][c]++
                    }
                }
            }
        }
    }

    // Function to display the game board in the console
    fun displayBoard() {
        println("  " + (0 until COLS).joinToString(" "))
        for (row in 0 until ROWS) {
            println("$row " + board[row].joinToString(" "))
        }
    }

    // Function to be done
    fun revealCell(row: Int, col: Int): List<Pair<Int, Int>> {
        // Your implementation of revealCell function goes here
        // TODO should implement something like 'a star'?
        //  not sure: "Otherwise, reveal what is in the fog. If the space is empty, reveal all squares recursively until it reaches a number"
        val arr = arrayListOf<Pair<Int, Int>>()
        for (r in maxOf(0, row - 1) until minOf(row + 2, ROWS)) {
            for (c in maxOf(0, col - 1) until minOf(col + 2, COLS)) {
                if (board[r][c] > 0) {
                    break
                } else {
                    arr.add(r to c)
                }
            }
        }
        return arr
    }

// Call the displayBoard function to display the initial state of the board
//    displayBoard()

// Example of the function to be implemented, should return the board updated
//    revealCell(2, 3)
}