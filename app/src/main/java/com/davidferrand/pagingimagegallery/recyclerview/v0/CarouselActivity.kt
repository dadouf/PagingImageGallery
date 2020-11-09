package com.davidferrand.pagingimagegallery.recyclerview.v0

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.Px
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.davidferrand.pagingimagegallery.GridActivity
import com.davidferrand.pagingimagegallery.Image
import com.davidferrand.pagingimagegallery.R
import com.davidferrand.pagingimagegallery.databinding.ActivityCarouselRecyclerviewBinding

class CarouselActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCarouselRecyclerviewBinding

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: CarouselAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarouselRecyclerviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val images: ArrayList<Image> =
            intent.getParcelableArrayListExtra(GridActivity.EXTRA_IMAGES) ?: ArrayList()

        layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        adapter = CarouselAdapter(images)

        with(binding.recyclerView) {
            layoutManager = this@CarouselActivity.layoutManager
            adapter = this@CarouselActivity.adapter

            val spacing = resources.getDimensionPixelSize(R.dimen.carousel_spacing)
            addItemDecoration(LinearHorizontalSpacingDecoration(spacing))
        }
    }
}

/** Works best with a [LinearLayoutManager] in [LinearLayoutManager.HORIZONTAL] orientation */
class LinearHorizontalSpacingDecoration(@Px private val innerSpacing: Int) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val itemPosition = parent.getChildAdapterPosition(view)

        outRect.left = if (itemPosition == 0) 0 else innerSpacing / 2
        outRect.right = if (itemPosition == state.itemCount - 1) 0 else innerSpacing / 2
    }
}

internal class CarouselAdapter(private val images: List<Image>) :
    RecyclerView.Adapter<CarouselAdapter.VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(ImageView(parent.context).apply {
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.MATCH_PARENT
            )
        })
    }

    @Suppress("UnnecessaryVariable")
    override fun onBindViewHolder(vh: VH, position: Int) {
        val image = images[position]

        Glide.with(vh.imageView).load(image.url).into(vh.imageView)
    }

    override fun getItemCount(): Int = images.size

    class VH(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)
}
