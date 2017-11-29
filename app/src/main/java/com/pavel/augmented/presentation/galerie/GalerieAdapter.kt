package com.pavel.augmented.presentation.galerie

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.pavel.augmented.R
import com.pavel.augmented.model.Sketch
import com.pavel.augmented.util.GlideApp
import com.pavel.augmented.util.getImagesFolder
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.layout_galerie_item.*
import java.io.File

class GalerieAdapter(var list: List<Sketch>, private val onClick: (Sketch) -> Unit) : RecyclerView.Adapter<GalerieAdapter.GalerieHolder>() {

    override fun getItemCount(): Int = list.size

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): GalerieHolder {
        val view = LayoutInflater.from(parent?.context).inflate(R.layout.layout_galerie_item, parent, false)
        return GalerieHolder(view)
    }

    override fun onBindViewHolder(holder: GalerieHolder?, position: Int) {
        holder?.bind(list[position], onClick)
    }

    class GalerieHolder(override val containerView: View?) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bind(sketch: Sketch, onClick: (Sketch) -> Unit) {

            containerView?.let {
                containerView.setOnClickListener { onClick(sketch) }

                sketch_name.text = sketch.name

                GlideApp.with(containerView)
                        .load(File(getImagesFolder(containerView.context), sketch.name + ".png"))
                        .centerCrop()
                        .placeholder(R.drawable.ic_placeholder_image)
                        .into(sketch_image)
            }
        }
    }
}