package com.vdotok.one2one

import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.vdotok.streaming.views.ProxyVideoSink
import org.webrtc.SurfaceViewRenderer

class CustomCallView(context: Context, attrs: AttributeSet) :
    FrameLayout(context, attrs) {
    private var avatarLayout: ConstraintLayout
    private var avatarImage: ImageView
    private var muteIcon: ImageView
    private var avatarText: TextView
    var preview: SurfaceViewRenderer
    private var borderView: View
    var proxyVideoSink: ProxyVideoSink

    init {
        inflate(context, R.layout.custom_call_view, this)
        avatarLayout = findViewById(R.id.avatarLayout)
        avatarImage = findViewById(R.id.avatar)
        muteIcon = findViewById(R.id.muteIcon)
        avatarText = findViewById(R.id.avatarName)
        proxyVideoSink = ProxyVideoSink()
        preview = findViewById(R.id.local_gl_surface_view)
        borderView = findViewById(R.id.borderView)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.surfaceView,
            0, 0
        ).apply {
            try {
                avatarImage.setImageDrawable(getDrawable(R.styleable.surfaceView_avatar))
                avatarText.text = this.getString(R.styleable.surfaceView_avatarUsername)
                setBorderValues(this)
                setMuteDrawable(this)

            } finally {
                recycle()
            }
        }
    }

    private fun setMuteDrawable(typedArray: TypedArray) {
        val muteDrawable = typedArray.getDrawable(R.styleable.surfaceView_muteIcon)
        if (muteDrawable != null) {
            muteIcon.setImageDrawable(muteDrawable)
        }
    }

    private fun setBorderValues(typedArray: TypedArray) {
//        set stroke values for the border
        val drawableGradient = borderView.background as GradientDrawable
        if (typedArray.getInt(R.styleable.surfaceView_borderStrokeWidth, 0) >= 12) {
            drawableGradient.setStroke(
                typedArray.getInt(R.styleable.surfaceView_borderStrokeWidth, 0),
                typedArray.getInt(R.styleable.surfaceView_borderStrokeColor, 0)
            )
        } else {
            drawableGradient.setStroke(
                12,
                typedArray.getInt(R.styleable.surfaceView_borderStrokeColor, 0)
            )
        }
//        show hide border for the view
        if (typedArray.getBoolean(R.styleable.surfaceView_showViewBorder, false))
            borderView.visibility = View.VISIBLE
        else
            borderView.visibility = View.GONE
    }

    fun release() {
        try {
            preview.release()
            preview.clearImage()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun swapViews(activity: Activity, remoteView: CustomCallView, ownView: CustomCallView) {
        activity.runOnUiThread {
            remoteView.proxyVideoSink.setTarget(ownView.preview)
            ownView.proxyVideoSink.setTarget(remoteView.preview)
            val remoteAvatar = remoteView.isAvatarShown()
            val ownAvatar = ownView.isAvatarShown()

            ownView.showHideAvatar(remoteAvatar)
            remoteView.showHideAvatar(ownAvatar)

            val tempVal = remoteView.preview
            remoteView.preview = ownView.preview
            ownView.preview = tempVal
            val tempImg = remoteView.avatarImage
            remoteView.avatarImage = ownView.avatarImage
            ownView.avatarImage = tempImg
            val tempParentConstraint = remoteView.avatarLayout
            remoteView.avatarLayout = ownView.avatarLayout
            ownView.avatarLayout = tempParentConstraint
            val tempUsername = remoteView.avatarText
            remoteView.avatarText = ownView.avatarText
            ownView.avatarText = tempUsername
        }
    }

//    fun getPreview(): SurfaceViewRenderer {
//        return preview
//    }

    fun setBorderStrokeColorWidth(strokeWidth: Int, borderColor: Int) {
        val drawableGradient = borderView.background as GradientDrawable
        if (strokeWidth >= 12) {
            drawableGradient.setStroke(
                strokeWidth,
                borderColor
            )
        } else {
            drawableGradient.setStroke(
                12,
                borderColor
            )
        }
    }

    fun setView(): ProxyVideoSink {
        proxyVideoSink.setTarget(preview)
        return proxyVideoSink
    }

    fun isAvatarShown(): Boolean {
        return avatarLayout.isShown
    }

    fun showHideAvatar(showAvatar: Boolean) {
        if (showAvatar) {
            avatarLayout.visibility = View.VISIBLE
        } else {
            avatarLayout.visibility = View.GONE
        }
    }

    fun isMuteIconShown(): Boolean {
        return muteIcon.isShown
    }

    fun showHideMuteIcon(showMuteIcon: Boolean) {
        if (showMuteIcon) {
            muteIcon.visibility = View.VISIBLE
        } else {
            muteIcon.visibility = View.GONE
        }
    }

    fun getAvatarView(): ImageView {
        return avatarImage
    }

}
