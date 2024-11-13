import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.example.memoreal_prototype.R

class MediaAdapter(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val getMediaList: () -> List<Uri>,
    private val onDeleteClick: (Uri) -> Unit,
    private val onMediaClick: (Uri) -> Unit // Add this parameter for media item clicks
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)

        fun bind(mediaUri: Uri) {
            val mimeType = context.contentResolver.getType(mediaUri)

            if (mimeType != null) {
                when {
                    mimeType.startsWith("video") -> {
                        // Handling video - extract frame as thumbnail
                        val retriever = MediaMetadataRetriever()
                        try {
                            retriever.setDataSource(context, mediaUri)
                            val bitmap: Bitmap? = retriever.getFrameAtTime(0)
                            if (bitmap != null) {
                                imageView.setImageBitmap(bitmap)
                            } else {
                                // If bitmap is null, set a fallback image
                                imageView.setImageResource(R.drawable.baseline_photo_24)
                            }
                        } catch (e: Exception) {
                            Log.e("MediaAdapter", "Error retrieving video frame: ${e.message}")
                            imageView.setImageResource(R.drawable.baseline_photo_24) // Fallback image
                        } finally {
                            retriever.release()
                        }
                    }
                    mimeType.startsWith("image") -> {
                        // Handling image
                        imageView.setImageURI(mediaUri)
                    }
                    else -> {
                        // Handle other mime types if necessary
                        Log.e("MediaAdapter", "Unsupported media type")
                    }
                }
            } else {
                Log.e("MediaAdapter", "Failed to get MIME type")
            }

            // Call onMediaClick when the item is clicked
            itemView.setOnClickListener {
                onMediaClick(mediaUri)
            }

            // Handle delete button click
            deleteButton.setOnClickListener {
                onDeleteClick(mediaUri)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(getMediaList()[position])
    }

    override fun getItemCount(): Int = getMediaList().size
}
