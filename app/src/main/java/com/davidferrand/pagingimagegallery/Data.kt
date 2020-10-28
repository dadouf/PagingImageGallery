package com.davidferrand.pagingimagegallery

object Data {
    val images = listOf(
        Image("https://media2.giphy.com/media/ZEBoAmoK5NVNso29xo/200.gif", 356, 200),
        Image("https://media3.giphy.com/media/W6XOLBwKcMyCeTtO3e/200.gif", 200, 200),
        Image("https://media0.giphy.com/media/3owypi68EvEuiasmDS/200.gif", 372, 200),
        Image("https://media4.giphy.com/media/QZybZmviJW0Yn9K1nn/200.gif", 356, 200),
        Image("https://media3.giphy.com/media/SXqnLsJpKhLU8eLjf1/200.gif", 200, 200),
        Image("https://media3.giphy.com/media/9kcBLYxWztmmc/200.gif", 356, 200),
        Image("https://media3.giphy.com/media/26FPBOzkRdjVkUdSo/200.gif", 355, 200),
        Image("https://media3.giphy.com/media/ehV5y42VAqn8vqKtAB/200.gif", 200, 200),
        Image("https://media0.giphy.com/media/Kfkk8BPdtQceKQUQxq/200.gif", 200, 200),
        Image("https://media1.giphy.com/media/Ws3ABiQMrbMv9H7Gnk/200.gif", 150, 200),
        Image("https://media1.giphy.com/media/1yn0SW0UMOwbprs4pI/200.gif", 356, 200),
        Image("https://media4.giphy.com/media/xUOxf8izqVvHEBhRO8/200.gif", 200, 200),
        Image("https://media2.giphy.com/media/3o7aD8HIYCudcacsr6/200.gif", 113, 200),
        Image("https://media0.giphy.com/media/TKFvmat1Z6mbt17dAw/200.gif", 208, 200),
        Image("https://media1.giphy.com/media/VI9Q3ZzkOgB0urXE82/200.gif", 200, 200),
        Image("https://media4.giphy.com/media/3o7TKqiUTQEoewY6eA/200.gif", 291, 200),
        Image("https://media0.giphy.com/media/l0HlJRPhFnk4qXZKw/200.gif", 356, 200),
        Image("https://media4.giphy.com/media/QC7k4oOd9lcJEru1cy/200.gif", 356, 200),
        Image("https://media0.giphy.com/media/l3q2DScd44jwBjpSg/200.gif", 154, 200),
        Image("https://media2.giphy.com/media/l46C93LNM33JJ1SMw/200.gif", 398, 200),
        Image("https://media3.giphy.com/media/xUOwG2G6UQ6o6OQgNy/200.gif", 355, 200),
        Image("https://media1.giphy.com/media/3o84syshVj2Iq8Mf04/200.gif", 356, 200),
        Image("https://media0.giphy.com/media/DrhMpwvQlIxGw/200.gif", 185, 200),
        Image("https://media0.giphy.com/media/lqpfJwb30kkVymm1W8/200.gif", 200, 200),
        Image("https://media2.giphy.com/media/3o84soHWYqD1G8AL5K/200.gif", 356, 200)
    )
}

data class Image(
    val url: String,
    val width: Int,
    val height: Int
)