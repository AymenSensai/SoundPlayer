package com.aymen.soundplayer

sealed class Sound(
    val name: String,
    val sound: Int,
    val image: Int
) {
    data object Rain : Sound(name = "Rain", sound = R.raw.rain_sound, image = R.drawable.rain_image)
    data object Nature : Sound(name = "Nature", sound = R.raw.nature_sound, image = R.drawable.nature_image)
    data object Ocean : Sound(name = "Ocean", sound = R.raw.ocean_sound, image = R.drawable.ocean_image)
}
