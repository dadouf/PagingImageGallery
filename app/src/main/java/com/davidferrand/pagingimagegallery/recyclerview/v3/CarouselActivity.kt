package com.davidferrand.pagingimagegallery.recyclerview.v3

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.Px
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.davidferrand.pagingimagegallery.GridActivity
import com.davidferrand.pagingimagegallery.Image
import com.davidferrand.pagingimagegallery.R
import com.davidferrand.pagingimagegallery.databinding.ActivityCarouselRecyclerviewBinding
import kotlin.math.roundToInt

class CarouselActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCarouselRecyclerviewBinding

    private lateinit var snapHelper: PageByPageSnapHelper
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
        snapHelper = PageByPageSnapHelper()

        with(binding.recyclerView) {
            setItemViewCacheSize(4)
            layoutManager = this@CarouselActivity.layoutManager
            adapter = this@CarouselActivity.adapter

            val spacing = resources.getDimensionPixelSize(R.dimen.carousel_spacing)
            addItemDecoration(
                LinearHorizontalSpacingDecoration(
                    innerSpacing = spacing,
                    outerSpacing = spacing / 2 // we need it balanced
                )
            )
        }

        snapHelper.attachToRecyclerView(binding.recyclerView)
    }
}

/** Works best with a [LinearLayoutManager] in [LinearLayoutManager.HORIZONTAL] orientation */
class LinearHorizontalSpacingDecoration(
    @Px private val innerSpacing: Int,
    @Px private val outerSpacing: Int = 0
) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        child: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val itemPosition = parent.getChildAdapterPosition(child)
        val itemCount = parent.adapter?.itemCount ?: 0

        outRect.top = 0
        outRect.bottom = 0
        outRect.left = if (itemPosition == 0) outerSpacing else innerSpacing / 2
        outRect.right = if (itemPosition == itemCount - 1) outerSpacing else innerSpacing / 2
    }
}

internal class CarouselAdapter(private val images: List<Image>) :
    RecyclerView.Adapter<CarouselAdapter.VH>() {

    private var hasInitParentDimensions = false
    private var maxImageWidth: Int = 0
    private var maxImageHeight: Int = 0
    private var maxImageAspectRatio: Float = 1f

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        // When onCreateViewHolder is called, parent has been measured and has valid width & height
        if (!hasInitParentDimensions) {
            initParentDimensions(parent)
        }

        return VH(ImageView(parent.context))
    }

    private fun initParentDimensions(parent: ViewGroup) {
        maxImageWidth =
            parent.width - parent.resources.getDimensionPixelSize(R.dimen.carousel_horizontal_padding_observed) * 2
        maxImageHeight = parent.height

        maxImageAspectRatio = maxImageWidth.toFloat() / maxImageHeight
        hasInitParentDimensions = true
    }

    @Suppress("UnnecessaryVariable")
    override fun onBindViewHolder(vh: VH, position: Int) {
        val image = images[position]

        vh.imageView.let {
            val imageAspectRatio = image.aspectRatio

            val targetImageWidth: Int
            val targetImageHeight: Int
            if (imageAspectRatio < maxImageAspectRatio) {
                // Tall image: height = max
                targetImageWidth = (maxImageHeight * imageAspectRatio).roundToInt()
                targetImageHeight = maxImageHeight
            } else {
                // Wide image: width = max
                targetImageWidth = maxImageWidth
                targetImageHeight = (maxImageWidth / imageAspectRatio).roundToInt()
            }

            it.layoutParams = RecyclerView.LayoutParams(
                targetImageWidth,
                targetImageHeight
            ).apply {
                // The topMargin acts as a center_vertical gravity
                topMargin = (maxImageHeight - (targetImageHeight)) / 2
            }

            Glide.with(vh.imageView).load(image.url).into(vh.imageView)
        }
    }

    override fun getItemCount(): Int = images.size

    class VH(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)
}

/**
 * Largely a [PagerSnapHelper] optimized for cases where pages are not full-width.
 * This ensures that pages are visited one by one and not skipped when flinging.
 */
internal class PageByPageSnapHelper : PagerSnapHelper() {
    private var recyclerView: RecyclerView? = null
    var snappedPosition: Int = -1

    /**
     * The only role of the scroll listener is to update the [snappedPosition] when scroll finishes.
     */
    private val scrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            private var hasScrolled = false

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE && hasScrolled) {
                    hasScrolled = false
                    updateSnappedPosition()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dx != 0 || dy != 0) {
                    hasScrolled = true
                }
            }
        }

    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        super.attachToRecyclerView(recyclerView)

        if (this.recyclerView != recyclerView) {
            this.recyclerView?.removeOnScrollListener(scrollListener)

            this.recyclerView = recyclerView?.apply {
                addOnScrollListener(scrollListener)
            }

            updateSnappedPosition()
        }
    }

    private fun updateSnappedPosition() {
        val lm = recyclerView?.layoutManager
        val centerView = lm?.let { findSnapView(it) }

        snappedPosition = centerView?.let { lm.getPosition(it) } ?: -1
    }

    override fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
        velocityY: Int
    ): Int {
        // This gets called onFling. The default behavior is buggy when snapping backwards
        // (it scrolls way too far, reason: it doesn't play well with the fact that we have
        // so many items offscreen to the left) so we override its logic to simply snap to
        // the next neighbor.

        val forwardDirection: Boolean = velocityX > 0

        return if (forwardDirection) {
            snappedPosition + 1
        } else {
            snappedPosition - 1
        }
    }
}
