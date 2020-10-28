package com.davidferrand.pagingimagegallery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.davidferrand.pagingimagegallery.databinding.ActivityGridBinding

class GridActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGridBinding

    private val implementations = listOf(
        DetailActivityImplementation(
            "RecyclerView v1",
            com.davidferrand.pagingimagegallery.recyclerview.v1.DetailActivity::class.java
        ),
        DetailActivityImplementation(
            "ViewPager v1",
            com.davidferrand.pagingimagegallery.viewpager.v1.DetailActivity::class.java
        ),
    )
    private var selectedImplementation: DetailActivityImplementation =
        implementations[0]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGridBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding.detailSelector) {
            adapter = ArrayAdapter(
                this@GridActivity,
                android.R.layout.simple_spinner_dropdown_item,
                implementations.map { it.label }
            )

            onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(parent: AdapterView<*>?) {}

                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectedImplementation = implementations[position]
                    }
                }
        }

        with(binding.recyclerView) {
            layoutManager = GridLayoutManager(this@GridActivity, 5)
            adapter = GridAdapter(Data.images) { image, position ->
                startActivity(Intent(this@GridActivity, selectedImplementation.clazz))
            }
        }
    }
}

class GridAdapter(
    private val images: List<Image>,
    private val onImageClicked: (Image, Int) -> Unit
) : RecyclerView.Adapter<GridAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH = VH(parent)

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = images[position]

        Glide.with(holder.imageView).load(item.url).into(holder.imageView)
        holder.imageView.setOnClickListener {
            onImageClicked(item, position)
        }
    }

    override fun getItemCount(): Int = images.size

    class VH(parent: ViewGroup) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context).inflate(R.layout.item_grid_image, parent, false)
    ) {
        val imageView: ImageView = itemView.findViewById(R.id.image_view)
    }
}

data class DetailActivityImplementation(
    val label: String,
    val clazz: Class<out Activity>
)