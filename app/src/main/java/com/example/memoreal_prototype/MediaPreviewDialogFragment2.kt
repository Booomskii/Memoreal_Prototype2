import android.app.Dialog
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.VideoView
import androidx.fragment.app.DialogFragment
import com.example.memoreal_prototype.R

class MediaPreviewDialogFragment2 : DialogFragment() {

    private lateinit var mediaUri: Uri

    companion object {
        private const val ARG_MEDIA_URI = "media_uri"

        // Use newInstance to pass Uri to the fragment safely
        fun newInstance(uri: Uri): MediaPreviewDialogFragment2 {
            val fragment = MediaPreviewDialogFragment2()
            val args = Bundle()
            args.putParcelable(ARG_MEDIA_URI, uri)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mediaUri = it.getParcelable(ARG_MEDIA_URI) ?: Uri.EMPTY
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_media_preview_dialog, container, false)

        val imageView = view.findViewById<ImageView>(R.id.previewImageView)
        val videoView = view.findViewById<VideoView>(R.id.previewVideoView)
        val closeButton = view.findViewById<ImageView>(R.id.closeButton)

        // Handle media type based on file extension
        val filePath = mediaUri.toString()
        if (filePath.endsWith(".mp4", ignoreCase = true) ||
            filePath.endsWith(".mkv", ignoreCase = true) ||
            filePath.endsWith(".avi", ignoreCase = true)) {
            // Handle video
            setupVideoView(videoView, imageView)
        } else {
            // Handle image
            setupImageView(imageView, videoView)
        }

        // Close button functionality
        closeButton.setOnClickListener {
            dismiss()
        }

        return view
    }

    private fun setupVideoView(videoView: VideoView, imageView: ImageView) {
        // Hide imageView and show videoView
        imageView.visibility = View.GONE
        videoView.visibility = View.VISIBLE

        // Setting up MediaController for better playback controls
        val mediaController = MediaController(requireContext())
        mediaController.setAnchorView(videoView)
        videoView.setMediaController(mediaController)

        // Set the video URI and prepare to play
        videoView.setVideoURI(mediaUri)

        // Adding a listener to handle video playback events
        videoView.setOnPreparedListener {
            videoView.start() // Start video once it is ready
        }

        // Adding error listener in case the video fails to play
        videoView.setOnErrorListener { _, _, _ ->
            // Show an error message or a static thumbnail if needed
            generateFallbackThumbnail(imageView)
            true
        }
    }

    private fun setupImageView(imageView: ImageView, videoView: VideoView) {
        // Hide videoView and show imageView
        videoView.visibility = View.GONE
        imageView.visibility = View.VISIBLE

        // Set image URI directly
        try {
            imageView.setImageURI(mediaUri)

            // If loading image fails, use a placeholder image
            if (imageView.drawable == null) {
                imageView.setImageResource(R.drawable.baseline_photo_24)
            }
        } catch (e: Exception) {
            // Handle exception and set placeholder
            imageView.setImageResource(R.drawable.baseline_photo_24)
        }
    }

    private fun generateFallbackThumbnail(imageView: ImageView) {
        // Hide videoView, show imageView with a placeholder thumbnail
        imageView.visibility = View.VISIBLE

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(requireContext(), mediaUri)
            val bitmap: Bitmap? = retriever.getFrameAtTime(0)

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                // If thumbnail generation fails, use a placeholder image
                imageView.setImageResource(R.drawable.baseline_photo_24)
            }
        } catch (e: Exception) {
            // Handle exception, set placeholder
            imageView.setImageResource(R.drawable.baseline_photo_24)
        } finally {
            retriever.release()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return dialog
    }
}
