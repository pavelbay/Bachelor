package com.pavel.augmented.presentation.galerie

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.Log
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

    override fun onBindViewHolder(holder: GalerieHolder?, position: Int, payloads: MutableList<Any>?) {
        holder?.bind(list[position], selectedItems.get(position, false), payloads)
    }

    fun toggleItem(position: Int) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position)
        } else {
            selectedItems.put(position, true)
        }

        notifyItemChanged(position, selectedItems.get(position, false))
    }

    fun getSelectedNumbers(): BooleanArray {
        val ret = BooleanArray(list.size)
        for (i in 0 until list.size) {
            Log.d("Bla", "Count: $i, keyAt: ${selectedItems.keyAt(i)}, value: ${selectedItems.get(i, false)}")
            ret[i] = selectedItems.get(i, false)
        }

        return ret
    }

    fun getSelectedItems(): Array<Sketch?> {
//        val ret = ArrayList<Sketch>(selectedItems.size())

        val ret = arrayOfNulls<Sketch>(selectedItems.size())
        for (i in 0 until selectedItems.size()) {
            ret[i] = list[selectedItems.keyAt(i)]
        }

        return ret
    }

    fun restoreSelectedItems(array: BooleanArray?) {
        array?.let {
            for (i in 0 until array.size) {
                selectedItems.put(i, array[i])
            }

            notifyDataSetChanged()
        }
    }

    fun unselectItems() {
        for (i in 0 until list.size) {
            if (selectedItems.get(i, false)) {
                notifyItemChanged(i)
                selectedItems.delete(i)
            }
        }
        selectedItems.clear()
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

        fun bind(sketch: Sketch, itemChecked: Boolean, payloads: MutableList<Any>?) {
            payloads?.let {
                if (payloads.size > 0) {
                    val checked = payloads[0]
                    if (checked is Boolean) {
                        sketch_name.isSelected = checked
                    }
                } else {
                    bind(sketch, itemChecked)
                }
            }
        }

        fun bind(sketch: Sketch, itemChecked: Boolean) {

            containerView?.let {
                containerView.setOnClickListener { EventBus.getDefault().post(SketchEvents.OnSketchClick(adapterPosition)) }

                containerView.setOnLongClickListener {
                    EventBus.getDefault().post(SketchEvents.OnSketchLongClick(adapterPosition))
                    return@setOnLongClickListener true
                }

                sketch_name.text = sketch.name

                sketch_name.isSelected = itemChecked

                GlideApp.with(containerView)
                        .load(File(getImagesFolder(containerView.context), sketch.name + ".jpeg"))
                        .centerCrop()
                        .placeholder(R.drawable.ic_placeholder_image)
                        .into(sketch_image)
            }
        }
    }
}