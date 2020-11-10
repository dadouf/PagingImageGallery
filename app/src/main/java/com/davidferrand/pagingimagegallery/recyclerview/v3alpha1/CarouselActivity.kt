package com.davidferrand.pagingimagegallery.recyclerview.v3alpha1

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.Px
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.SnapHelper
import com.bumptech.glide.Glide
import com.davidferrand.pagingimagegallery.GridActivity
import com.davidferrand.pagingimagegallery.Image
import com.davidferrand.pagingimagegallery.R
import com.davidferrand.pagingimagegallery.databinding.ActivityCarouselRecyclerviewBinding
import kotlin.math.abs
import kotlin.math.roundToInt


class CarouselActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCarouselRecyclerviewBinding

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: CarouselAdapter
    private lateinit var snapHelper: SnapHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarouselRecyclerviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = intent.getStringExtra(GridActivity.EXTRA_TITLE)

        val images: ArrayList<Image> = intent.getParcelableArrayListExtra(GridActivity.EXTRA_IMAGES)
            ?: ArrayList()
        val position: Int = intent.getIntExtra(GridActivity.EXTRA_POSITION, 0)

        layoutManager = ProminentLayoutManager(this)
        adapter = CarouselAdapter(images)
        snapHelper = PagerSnapHelper()

        with(binding.recyclerView) {
            layoutManager = this@CarouselActivity.layoutManager
            adapter = this@CarouselActivity.adapter

            val spacing = resources.getDimensionPixelSize(R.dimen.carousel_spacing)
            addItemDecoration(LinearHorizontalSpacingDecoration(spacing))
            addItemDecoration(BoundsOffsetDecoration())
        }

        snapHelper.attachToRecyclerView(binding.recyclerView)

        initRecyclerViewPosition(position)
    }

    private fun initRecyclerViewPosition(position: Int) {
        // This initial scroll will be slightly off because it doesn't respect the SnapHelper.
        // Do it anyway so that the target view is laid out, then adjust onPreDraw.
        layoutManager.scrollToPosition(position)

        binding.recyclerView.doOnPreDraw {
            val targetView = layoutManager.findViewByPosition(position) ?: return@doOnPreDraw
            val distanceToFinalSnap =
                snapHelper.calculateDistanceToFinalSnap(layoutManager, targetView)
                    ?: return@doOnPreDraw

            layoutManager.scrollToPositionWithOffset(position, -distanceToFinalSnap[0])
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

/** Offset the first and last items to center them */
class BoundsOffsetDecoration : ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        val itemPosition = parent.getChildAdapterPosition(view)

        // It is crucial to refer to layoutParams.width (view.width is 0 at this time)!
        val itemWidth = view.layoutParams.width
        val offset = (parent.width - itemWidth) / 2

        if (itemPosition == 0) {
            outRect.left = offset
        } else if (itemPosition == state.itemCount - 1) {
            outRect.right = offset
        }
    }
}

internal class CarouselAdapter(private val images: List<Image>) :
    RecyclerView.Adapter<CarouselAdapter.VH>() {

    private var hasInitParentDimensions = false
    private var maxImageWidth: Int = 0
    private var maxImageHeight: Int = 0
    private var maxImageAspectRatio: Float = 1f

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        // At this point [parent] has been measured and has valid width & height
        if (!hasInitParentDimensions) {
            maxImageWidth = (parent.width * 0.75f).roundToInt()
            maxImageHeight = parent.height
            maxImageAspectRatio = maxImageWidth.toFloat() / maxImageHeight.toFloat()
            hasInitParentDimensions = true
        }

        return VH(ImageView(parent.context))
    }

    override fun onBindViewHolder(vh: VH, position: Int) {
        val image = images[position]

        // Change aspect ratio
        val imageAspectRatio = image.aspectRatio
        val targetImageWidth: Int = if (imageAspectRatio < maxImageAspectRatio) {
            // Tall image: height = max
            (maxImageHeight * imageAspectRatio).roundToInt()
        } else {
            // Wide image: width = max
            maxImageWidth
        }
        vh.imageView.layoutParams = RecyclerView.LayoutParams(
            targetImageWidth,
            RecyclerView.LayoutParams.MATCH_PARENT
        )

        // Load image
        Glide.with(vh.imageView).load(image.url).into(vh.imageView)
    }

    override fun getItemCount(): Int = images.size

    class VH(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)
}


/**
 * Arranges items so that the central one appears prominent: its neighbors are scaled down.
 * Based on https://stackoverflow.com/a/54516315/2291104
 */
internal class ProminentLayoutManager(
    context: Context,

    /**
     * This value determines where items reach the final (minimum) scale:
     * - 1f is when their center is at the start/end of the RecyclerView
     * - <1f is before their center reaches the start/end of the RecyclerView
     * - >1f is outside the bounds of the RecyclerView
     * */
    private val minScaleDistanceFactor: Float = 1.5f,

    /** The final (minimum) scale for non-prominent items is 1-[scaleDownBy] */
    private val scaleDownBy: Float = 0.5f
) : LinearLayoutManager(context, HORIZONTAL, false) {

    override fun onLayoutCompleted(state: RecyclerView.State?) =
        super.onLayoutCompleted(state).also { scaleChildren() }

    override fun scrollHorizontallyBy(
        dx: Int,
        recycler: RecyclerView.Recycler,
        state: RecyclerView.State
    ) = super.scrollHorizontallyBy(dx, recycler, state).also {
        if (orientation == HORIZONTAL) scaleChildren()
    }

    private fun scaleChildren() {
        val containerCenter = width / 2f

        // Any view further than this threshold will be fully scaled down
        val scaleDistanceThreshold = minScaleDistanceFactor * containerCenter

        for (i in 0 until childCount) {
            val child = getChildAt(i)!!

            val childCenter = (child.left + child.right) / 2f
            val distanceToCenter = abs(childCenter - containerCenter)

            val scaleDownAmount = (distanceToCenter / scaleDistanceThreshold).coerceAtMost(1f)
            val scale = 1f - scaleDownBy * scaleDownAmount

            child.scaleX = scale
            child.scaleY = scale
        }
    }
}