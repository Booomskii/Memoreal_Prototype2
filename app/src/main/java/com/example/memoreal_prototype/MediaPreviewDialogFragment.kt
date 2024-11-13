import android.app.Dialog
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.fragment.app.DialogFragment
import com.example.memoreal_prototype.R

class MediaPreviewDialogFragment : DialogFragment() {

    private lateinit var mediaUri: Uri

    companion object {
        private const val ARG_MEDIA_URI = "media_uri"

        // Use newInstance to pass Uri to the fragment safely
        fun newInstance(uri: Uri): MediaPreviewDialogFragment {
            val fragment = MediaPreviewDialogFragment()
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
        val closeButton = view.findViewById<ImageView>(R.id.closeButton) // Add close button

        // Handle media type based on mime type
        val mimeType = requireContext().contentResolver.getType(mediaUri)

        if (mimeType != null && mimeType.startsWith("video")) {
            // Show VideoView for videos
            imageView.visibility = View.GONE
            videoView.visibility = View.VISIBLE
            videoView.setVideoURI(mediaUri)
            videoView.start() // Automatically start the video
        } else {
            // Show ImageView for images
            videoView.visibility = View.GONE
            imageView.visibility = View.VISIBLE
            imageView.setImageURI(mediaUri) // Set the image URI directly

            if (imageView.drawable == null) {
                // If loading image fails, use a placeholder image
                imageView.setImageResource(R.drawable.baseline_photo_24)
            }
        }

        // Close button functionality
        closeButton.setOnClickListener {
            dismiss()
        }

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        return dialog
    }
}
