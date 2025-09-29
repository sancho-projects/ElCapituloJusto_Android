package es.sanchoo.capitulojusto.auxiliares

class Panel(
    image: String,
    val rightChapter: Int,
    val difficulty: Int,
    val isManga: Boolean
) {
    val image: String = if (isManga) "img$image" else "img_a$image"
}
