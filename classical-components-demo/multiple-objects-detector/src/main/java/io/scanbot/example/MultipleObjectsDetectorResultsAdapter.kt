package io.scanbot.example

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

internal class MultipleObjectsDetectorResultsAdapter(private val dataSet: List<String>): RecyclerView.Adapter<MultipleObjectsDetectorResultsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_multiple_object_detector_result, parent, false)

        return ViewHolder(itemView)
    }

    override fun getItemCount() = dataSet.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataSet[position])
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        fun bind(filePath: String) {
            val imageView = itemView.findViewById<ImageView>(R.id.itemImageView)
            ImageLoader(imageView).loadFromFilePath(filePath)
        }
    }
}
