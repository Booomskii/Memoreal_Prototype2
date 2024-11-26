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

class MediaAdapter2(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val getMediaList: () -> List<Uri>,
    private val mediaThumbnails: MutableMap<Uri, Bitmap>, // MutableMap to store dynamically generated thumbnails
    private val onDeleteClick: (Uri) -> Unit,
    private val onMediaClick: (Uri) -> Unit
) : RecyclerView.Adapter<MediaAdapter2.MediaViewHolder>() {

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)

        fun bind(mediaUri: Uri) {
            // Check if the thumbnail has already been generated
            val thumbnail = mediaThumbnails[mediaUri]
            if (thumbnail != null) {
                // If the thumbnail is available, set it directly
                imageView.setImageBitmap(thumbnail)
            } else {
                // Generate the thumbnail if not already generated
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
                                    mediaThumbnails[mediaUri] = bitmap // Store the generated thumbnail in the map
                                } else {
                                    // If bitmap is null, set a fallback image
                                    imageView.setImageResource(R.drawable.baseline_photo_24)
                                }
                            } catch (e: Exception) {
                                Log.e("MediaAdapter2", "Error retrieving video frame: ${e.message}")
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
                            Log.e("MediaAdapter2", "Unsupported media type")
                        }
                    }
                } else {
                    Log.e("MediaAdapter2", "Failed to get MIME type")
                    imageView.setImageResource(R.drawable.baseline_photo_24) // Fallback image
                }
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
