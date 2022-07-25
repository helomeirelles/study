package android.myexoplayertest

import android.content.res.Configuration
import android.myexoplayertest.databinding.ActivityMainBinding
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class MainActivity : AppCompatActivity() {
    private lateinit var constraintLayoutRoot: ConstraintLayout
    private lateinit var exoPlayerView: PlayerView
    private lateinit var simpleExoPlayer: ExoPlayer
    private lateinit var mediaSource: MediaSource
    private lateinit var binding: ActivityMainBinding

    private lateinit var urlType: URLType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        findView()
        initPlayer()

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val constraintSet = ConstraintSet()
        constraintSet.connect(exoPlayerView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 0)
        constraintSet.connect(exoPlayerView.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, 0)
        constraintSet.connect(exoPlayerView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, 0)
        constraintSet.connect(exoPlayerView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, 0)

        constraintSet.applyTo(constraintLayoutRoot)

        if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else {
            showSystemUI()
            val layoutParams = exoPlayerView.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.dimensionRatio = "16:9"

        }

        window.decorView.requestLayout()
    }

    private fun hideSystemUI(){
        actionBar?.hide()
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN
                )

    }

    private fun showSystemUI(){
        actionBar?.show()
    }

    override fun onResume() {
        super.onResume()
        simpleExoPlayer.playWhenReady = true
        simpleExoPlayer.play()
    }

    override fun onPause() {
        super.onPause()
        simpleExoPlayer.pause()
        simpleExoPlayer.playWhenReady = false
    }

    override fun onStop() {
        super.onStop()
        simpleExoPlayer.pause()
        simpleExoPlayer.playWhenReady = false

    }

    override fun onDestroy() {
        super.onDestroy()
        simpleExoPlayer.removeListener(playerListener)
        simpleExoPlayer.stop()
        simpleExoPlayer.clearMediaItems()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private var playerListener = object : Player.Listener {
        override fun onRenderedFirstFrame() {
            super.onRenderedFirstFrame()
            if(urlType == URLType.HLS) {
                exoPlayerView.useController = false
            }

            if (urlType == URLType.MP4) {
                exoPlayerView.useController = true
            }
        }

        override fun onPlayerError(error: PlaybackException) {
            Toast.makeText(this@MainActivity, "${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initPlayer(){
        simpleExoPlayer = ExoPlayer.Builder(this).build()
        simpleExoPlayer.addListener(playerListener)
        exoPlayerView.player = simpleExoPlayer
        createMediaSource()
        simpleExoPlayer.setMediaSource(mediaSource)
        simpleExoPlayer.prepare()
    }

    private fun findView(){
        constraintLayoutRoot = binding.constraintLayoutRoot
        exoPlayerView = binding.exoPlayerView

    }

    private fun createMediaSource(){
        urlType = URLType.MP4
        urlType.url = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"
//
//        urlType = URLType.HLS
//        urlType.url = "https://cph-p2p-msl.akamaized.net/hls/live/2000341/test/master.m3u8"

        when (urlType){
            URLType.MP4 -> {
                val dataSourceFactory: com.google.android.exoplayer2.upstream.DataSource.Factory = DefaultDataSourceFactory(
                    this, Util.getUserAgent(this, applicationInfo.name)
                )
                mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(
                    MediaItem.fromUri(Uri.parse(urlType.url))
                )
            }
            URLType.HLS -> {
                val dataSourceFactory: com.google.android.exoplayer2.upstream.DataSource.Factory = DefaultDataSourceFactory(
                    this, Util.getUserAgent(this, applicationInfo.name)
                )
                mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(
                    MediaItem.fromUri(Uri.parse(urlType.url))
                )
            }
        }
    }
}

enum class URLType(var url: String) {
    MP4(""), HLS("")
}
