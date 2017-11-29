package com.pavel.augmented.presentation.galerie

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pavel.augmented.R
import com.pavel.augmented.events.SketchEvents
import com.pavel.augmented.model.Sketch
import com.pavel.augmented.util.GlideApp
import com.pavel.augmented.util.getImagesFolder
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.layout_galerie_item.*
import org.greenrobot.eventbus.EventBus
import java.io.File

class GalerieAdapter(var list: MutableList<Sketch>) : RecyclerView.Adapter<GalerieAdapter.GalerieHolder>() {

    private val selectedItems = SparseBooleanArray()

    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): GalerieHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.layout_galerie_item, parent, false)
        return GalerieHolder(view)
    }

    override fun onBindViewHolder(holder: GalerieHolder?, position: Int) {
        holder?.bind(list[position], selectedItems.get(position, false))
    }

    fun toggleItem(position: Int) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position)
        } else {
            selectedItems.put(position, true)
        }

        notifyItemChanged(position)
    }

    fun swapDataItems(items: MutableList<Sketch>?) {
        val dataDiffCallback = DataDiffCallback(list, items)
        val diffResult = DiffUtil.calculateDiff(dataDiffCallback, true)

        list.clear()
        items?.let {
            list.addAll(items)
        }

        diffResult.dispatchUpdatesTo(this)
    }

    class DataDiffCallback(private var oldList: MutableList<Sketch>?, private var newList: MutableList<Sketch>?) : DiffUtil.Callback() {

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            if (oldList == null || newList == null) {
                return false
            }

            if (oldList?.get(oldItemPosition) == null && newList?.get(newItemPosition) == null) {
                return true
            }

            if (newList?.get(oldItemPosition) == null && newList?.get(newItemPosition) != null) {
                return false
            }

            return oldList!![oldItemPosition] == newList!![newItemPosition]
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            if (oldList == null || newList == null) return false

            return oldList!![oldItemPosition] == newList!![newItemPosition]
        }

        override fun getNewListSize(): Int {
            return if (newList != null) {
                newList!!.size
            } else {
                0
            }
        }

        override fun getOldListSize(): Int {
            var size = 0
            oldList?.let {
                size = oldList!!.size
            }

            return size
        }
    }

    class GalerieHolder(override val containerView: View?) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(sketch: Sketch, itemChecked: Boolean) {

            containerView?.let {
                containerView.setOnClickListener { EventBus.getDefault().post(SketchEvents.OnSketchClick(adapterPosition)) }

                containerView.setOnLongClickListener {
                    EventBus.getDefault().post(SketchEvents.OnSketchLongClick(adapterPosition))
                    return@setOnLongClickListener true
                }

                sketch_name.text = sketch.name
                card_view.isSelected = itemChecked

                GlideApp.with(containerView)
                        .load(File(getImagesFolder(containerView.context), sketch.name + ".png"))
                        .centerCrop()
                        .placeholder(R.drawable.ic_placeholder_image)
                        .into(sketch_image)
            }
        }
    }
}