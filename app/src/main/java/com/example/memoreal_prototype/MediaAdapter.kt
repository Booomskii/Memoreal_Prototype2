import android.content.ContentResolver
import android.content.ContentUris
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.memoreal_prototype.R
import android.media.MediaMetadataRetriever

class MediaAdapter(
    private val getMediaList: () -> List<Uri>,
    private val onDeleteClick: (Uri) -> Unit,
    private val onMediaClick: (Uri) -> Unit
) : RecyclerView.Adapter<MediaAdapter.MediaViewHolder>() {

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)

        fun bind(mediaUri: Uri) {
            val mimeType = itemView.context.contentResolver.getType(mediaUri)

            if (mimeType != null && mimeType.startsWith("video")) {
                // For videos, get the thumbnail using MediaMetadataRetriever
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(itemView.context, mediaUri)
                    val bitmap: Bitmap? = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    imageView.setImageBitmap(bitmap) // Set the video thumbnail
                } catch (e: Exception) {
                    e.printStackTrace() // Handle any exceptions
                } finally {
                    retriever.release() // Always release the retriever
                }
            } else {
                // For images, display the image
                imageView.setImageURI(mediaUri)
            }

            // Make media item clickable to view/play
            itemView.setOnClickListener {
                onMediaClick(mediaUri) // Call the onMediaClick function
            }

            // Set up the delete button click listener
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