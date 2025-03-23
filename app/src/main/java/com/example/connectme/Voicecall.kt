package com.example.connectme

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig

class Voicecall : AppCompatActivity() {
    private var agoraEngine: RtcEngine? = null

    // Agora configuration values (same as video call)
    private val appID = "36295f39cb944b3aa2ea0a9f0d613794"
    private val channelName = "connectme"
    private val token = "007eJxTYNB8zNNzfNYD0wUPfyzcrxXHF/92H7MN/8TgTTPKwy81qPkrMBibGVmaphlbJidZmpgkGScmGqUmGiRaphmkmBkam1ua8BU8SG8IZGRYseUkMyMDBIL4nAzJ+Xl5qckluakMDAAGjSHR"
    private val uid = 0

    private var isJoined = false

    // For voice call, we only need RECORD_AUDIO permission
    private val PERMISSION_ID = 12
    private val REQUESTED_PERMISSIONS = arrayOf(android.Manifest.permission.RECORD_AUDIO)

    // Check permissions (only microphone needed)
    private fun checkSelfPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, REQUESTED_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED
    }

    // Initialize Agora engine and enable audio only
    private fun setUpAudioSDKEngine() {
        try {
            val config = RtcEngineConfig().apply {
                mContext = baseContext
                mAppId = appID
                mEventHandler = mRtcEventHandler
            }
            agoraEngine = RtcEngine.create(config)
            agoraEngine?.enableAudio() // Enable audio-only mode
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Error initializing Agora: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_voicecall)

        // Request permissions if necessary; else, initialize and join call
        if (!checkSelfPermissions()) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, PERMISSION_ID)
        } else {
            setUpAudioSDKEngine()
            joinCall()
        }

        // End call button: Leave call and navigate back to chat screen
        val gotoChat = findViewById<ImageView>(R.id.endcall_callscreen)
        gotoChat.setOnClickListener {
            leaveCall()
            startActivity(Intent(this, ChatScreen::class.java))
        }

        // Switch to video call: Navigate to VideoCall screen
        val gotoVideo = findViewById<ImageView>(R.id.videocall_callscreen)
        gotoVideo.setOnClickListener {
            startActivity(Intent(this, VideoCall::class.java))
        }
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpAudioSDKEngine()
                joinCall()
            } else {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        agoraEngine?.leaveChannel()
        Thread {
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }

    private fun joinCall() {
        val options = ChannelMediaOptions().apply {
            channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        }
        agoraEngine?.joinChannel(token, channelName, uid, options)
    }

    private fun leaveCall() {
        if (!isJoined) {
            Toast.makeText(this, "Join a channel first", Toast.LENGTH_SHORT).show()
        } else {
            agoraEngine?.leaveChannel()
            isJoined = false
            Toast.makeText(this, "You left the channel", Toast.LENGTH_SHORT).show()
        }
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            isJoined = true
            runOnUiThread {
                Toast.makeText(this@Voicecall, "Joined channel: $channel, uid: $uid", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread {
                Toast.makeText(this@Voicecall, "User $uid joined", Toast.LENGTH_SHORT).show()
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread {
                Toast.makeText(this@Voicecall, "User $uid left", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
