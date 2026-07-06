import androidx.media3.common.TrackSelectionOverride
import androidx.media3.exoplayer.ExoPlayer

fun f(player: ExoPlayer) {
    player.trackSelectionParameters = player.trackSelectionParameters
        .buildUpon()
        .build()
}
