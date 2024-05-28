package com.example.game3

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class ActivityLevel1 : AppCompatActivity() {

    //Game Views initialization
    private lateinit var restartButton: ImageButton
    private lateinit var Textscore: TextView
    private lateinit var paddle: View
    private lateinit var ball: View
    private lateinit var balloonContainer: LinearLayout

    //Game variables
    private var firstball = 0f
    private var secondball = 0f
    private var firstballspeed = 4f
    private var secondballspeed = 1f
    private var Apaddle = 0f
    private var score = 0
    private var highestScore = 0
    private var lives = 3
    private var isBallLaunched = false

    //Balloon container and shape initializing
    private val baloonwidth = 80
    private val baloonheight = 80
    private val baloonmargin = 4
    private val baloonrow = 7
    private val balooncolumn = 16


    // SharedPreferences for storing highest score
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        Textscore = findViewById(R.id.scoreText)
        paddle = findViewById(R.id.paddle)
        ball = findViewById(R.id.ball)
        balloonContainer = findViewById(R.id.brickContainer1)

        sharedPreferences = getSharedPreferences("GamePrefs", Context.MODE_PRIVATE)

        //Play button process
        val newGameButton = findViewById<Button>(R.id.newgame)
        newGameButton.setOnClickListener {
            startGame()
        }

        //Exit button process
        findViewById<View>(R.id.exit).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        //Again button process
        val againButton = findViewById<Button>(R.id.again)
        againButton.setOnClickListener {
            againGame()
        }

    }



    //start a new game
    private fun startGame() {
        resetGame()
        ballooninitialize()
        start()
        findViewById<Button>(R.id.newgame).visibility = View.INVISIBLE //buttons invisible once game is start
        findViewById<Button>(R.id.exit).visibility = View.INVISIBLE
        findViewById<Button>(R.id.again).visibility = View.INVISIBLE

    }

    //Reset the game
    private fun resetGame() {
        score = 0
        lives = 2
        updateScoreText()
    }

    //Initialize balloons
    private fun ballooninitialize() {
        val ballooncon = baloonwidth - 2 * baloonmargin // Adjusted diameter to fit within baloonwidth

        for (row in 0 until baloonrow) {
            val rowLayout = LinearLayout(this)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            rowLayout.layoutParams = params

            for (col in 0 until balooncolumn) {
                val balloon = View(this)
                val balloonParams = LinearLayout.LayoutParams(baloonwidth, baloonheight)
                balloonParams.setMargins(baloonmargin, baloonmargin, baloonmargin, baloonmargin)
                balloon.layoutParams = balloonParams

                // Set different colors for balloons
                when ((row + col) % 3) {
                    0 -> balloon.setBackgroundResource(R.drawable.red_balloon)
                    1 -> balloon.setBackgroundResource(R.drawable.yellow_balloon)
                    2 -> balloon.setBackgroundResource(R.drawable.green_balloon)
                }



                rowLayout.addView(balloon)
            }

            balloonContainer.addView(rowLayout)
        }
    }

    //Ball moving process
    private fun movingBall() {
        firstball += firstballspeed
        secondball += secondballspeed

        ball.x = firstball
        ball.y = secondball
    }

    //start the game again
    private fun againGame(){
        resetGame()
        start()
        findViewById<Button>(R.id.newgame).visibility = View.INVISIBLE   //buttons invisible once game is start
        findViewById<Button>(R.id.exit).visibility = View.INVISIBLE
        findViewById<Button>(R.id.again).visibility = View.INVISIBLE

    }

    //Paddle moving process
    private fun movingPaddle(x: Float) {
        Apaddle = x - paddle.width / 2
        paddle.x = Apaddle
    }

    //Process of the balloons
    private fun checkCollision() {

        // Check collision with ballonns
        val screenWidth = resources.displayMetrics.widthPixels.toFloat()
        val screenHeight = resources.displayMetrics.heightPixels.toFloat()

        if (firstball <= 0 || firstball + ball.width >= screenWidth) {
            firstballspeed *= -1
        }

        if (secondball <= 0) {
            secondballspeed *= -1
        }

        // Check collision with paddle
        if (secondball + ball.height >= paddle.y && secondball + ball.height <= paddle.y + paddle.height
            && firstball + ball.width >= paddle.x && firstball <= paddle.x + paddle.width
        ) {
            secondballspeed *= -1
            score++
            Textscore.text = "Score: $score"
        }

        // Check collision with bottom wall (paddle misses the ball)
        if (secondball + ball.height >= screenHeight) {

            resetBallPosition() //  Reset the ball to its initial position
        }

        // Check collision with balloons
        for (row in 0 until baloonrow) {
            val rowLayout = balloonContainer.getChildAt(row) as LinearLayout

            val rowTop = rowLayout.y + balloonContainer.y
            val rowBottom = rowTop + rowLayout.height

            for (col in 0 until balooncolumn) {
                val balloon = rowLayout.getChildAt(col) as View

                if (balloon.visibility == View.VISIBLE) {
                    val balloonLeft = balloon.x + rowLayout.x
                    val balloonRight = balloonLeft + balloon.width
                    val balloonTop = balloon.y + rowTop
                    val balloonBottom = balloonTop + balloon.height

                    if (firstball + ball.width >= balloonLeft && firstball <= balloonRight
                        && secondball + ball.height >= balloonTop && secondball <= balloonBottom
                    ) {
                        balloon.visibility = View.INVISIBLE
                        secondballspeed *= -1
                        score++
                        Textscore.text = "Score: $score"
                        return  // Exit the function after finding a collision with a balloon
                    }
                }
            }
        }

        // Check collision with bottom wall (paddle misses the ball)
        if (secondball + ball.height >= screenHeight - 100) {

            // Reduce the number of lives
            lives--

            if (lives > 0 ) {
                Toast.makeText(this, "$lives balls left ", Toast.LENGTH_SHORT).show()
            }

            if (lives <= 0) {

                // Game over condition: No more lives left
                gameOver()
            } else {

                // Reset the ball to its initial position
                resetBallPosition()
                start()
            }
        }
    }

    //change ball positions
    private fun resetBallPosition() {

        // Reset the ball to its initial position
        val displayMetrics = resources.displayMetrics
        val screenDensity = displayMetrics.density

        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()

        firstball = screenWidth / 2 - ball.width / 2
        secondball = screenHeight / 2 - ball.height / 2 +525

        ball.x = firstball
        ball.y = secondball

        // Reset the ball's speed
        firstballspeed = 2 * screenDensity
        secondballspeed = 2 * screenDensity

        Apaddle = screenWidth / 2 - paddle.width / 2
        paddle.x = Apaddle
    }

    //Game over process
    private fun gameOver() {

        //Display buttons once game is over
        findViewById<Button>(R.id.newgame).visibility = View.INVISIBLE
        findViewById<Button>(R.id.exit).visibility = View.VISIBLE
        findViewById<Button>(R.id.again).visibility = View.VISIBLE


        val editor = sharedPreferences.edit()
        if (score > highestScore) {
            highestScore = score
            editor.putInt("highestScore", highestScore)
            editor.apply()
        }
        Textscore.text = getString(R.string.game_over, score, highestScore)  //Display scores
    }

    //padlle moving
    @SuppressLint("ClickableViewAccessibility")
    private fun movepaddle() {
        paddle.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE -> movingPaddle(event.rawX)
            }
            true
        }
    }


    //Start process of the game
    private fun start() {
        movepaddle()
        val displayMetrics = resources.displayMetrics
        val screenDensity = displayMetrics.density

        val screenWidth = displayMetrics.widthPixels.toFloat()
        val screenHeight = displayMetrics.heightPixels.toFloat()

        Apaddle = screenWidth / 2 - paddle.width / 2
        paddle.x = Apaddle

        firstball = screenWidth / 2 - ball.width / 2
        secondball = screenHeight / 2 - ball.height / 2

        val balloonHeightWithMargin = (baloonheight + baloonmargin * screenDensity).toInt()

        firstballspeed = 5 * screenDensity
        secondballspeed = -2 * screenDensity

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = Long.MAX_VALUE
        animator.addUpdateListener { animation ->
            movingBall()
            checkCollision()
        }
        animator.start()
    }

    //Update score
    private fun updateScoreText() {
        Textscore.text = getString(R.string.score, score)
    }
}
