package com.davidferrand.pagingimagegallery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.davidferrand.pagingimagegallery.databinding.ActivityPortalBinding

class PortalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPortalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPortalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.adapter = LauncherAdapter(items = listOf(
                LauncherAdapter.Item.Heading("Yay"),
                LauncherAdapter.Item.Launcher("RecyclerView v1", com.davidferrand.pagingimagegallery.recyclerview.v1.GalleryActivity::class.java),
                LauncherAdapter.Item.Heading("Boo"),
                LauncherAdapter.Item.Launcher("ViewPager v1", com.davidferrand.pagingimagegallery.viewpager.v1.GalleryActivity::class.java),
        ))
    }
}

class LauncherAdapter(private val items: List<Item>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    sealed class Item(val viewType: Int) {
        class Heading(val label: String) : Item(VIEW_TYPE) {
            companion object {
                const val VIEW_TYPE = 10
            }
        }

        class Launcher(val label: String, val activityClass: Class<out Activity>) : Item(VIEW_TYPE) {
            companion object {
                const val VIEW_TYPE = 20
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return items[position].viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Item.Heading.VIEW_TYPE -> VH.Heading(parent)
            else -> VH.Launcher(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is Item.Heading -> (holder as VH.Heading).bind(item)
            is Item.Launcher -> (holder as VH.Launcher).bind(item)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    sealed class VH(parent: ViewGroup, @LayoutRes layoutResId: Int) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)) {
        protected val label: TextView = itemView as TextView

        class Heading(parent: ViewGroup) : VH(parent, R.layout.item_portal_heading) {
            fun bind(item: Item.Heading) {
                label.text = item.label
            }
        }

        class Launcher(parent: ViewGroup) : VH(parent, R.layout.item_portal_launcher) {
            fun bind(item: Item.Launcher) {
                label.text = item.label
                label.setOnClickListener {
                    itemView.context.startActivity(Intent(itemView.context, item.activityClass))
                }
            }
        }
    }
}
