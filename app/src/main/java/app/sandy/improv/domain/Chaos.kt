package app.sandy.improv.domain

class Chaos(private val unit: () -> Double = { kotlin.random.Random.nextDouble() }) {
    fun roll(): Boolean = unit() < 0.30
}
