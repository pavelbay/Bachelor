package com.pavel.augmented.fragments

import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.pavel.augmented.R
import com.pavel.augmented.events.ColorPickerEvents
import com.madrapps.pikolo.HSLColorPicker
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener
import org.greenrobot.eventbus.EventBus

const val VIEW_LAYOUT_RES_KEY = "view_layout_res_key"
const val BUTTON_RES_ID_KEY = "button_res_id_key"
const val DEFAULT_COLOR_KEY = "default_color_key"

class ColorPickerDialogFragment : DialogFragment() {
    @LayoutRes private var mViewLayoutRes: Int = -1
    @IdRes private var mButtonResId: Int = -1
    @ColorInt private var mDefaultColor: Int = - 1

    companion object {
        fun newInstance(@LayoutRes viewLayoutRes: Int, @IdRes buttonResId: Int, @ColorInt defaultColor: Int): ColorPickerDialogFragment {
            val args = Bundle()
            args.putInt(VIEW_LAYOUT_RES_KEY, viewLayoutRes)
            args.putInt(BUTTON_RES_ID_KEY, buttonResId)
            args.putInt(DEFAULT_COLOR_KEY, defaultColor)
            val dialogFragment = ColorPickerDialogFragment()
            dialogFragment.arguments = args

            return dialogFragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mViewLayoutRes = arguments!!.getInt(VIEW_LAYOUT_RES_KEY)
        mButtonResId = arguments!!.getInt(BUTTON_RES_ID_KEY)
        mDefaultColor = arguments!!.getInt(DEFAULT_COLOR_KEY)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(mViewLayoutRes, container, false)
        val okButton = view?.findViewById<Button>(mButtonResId)
        okButton?.setOnClickListener { _ -> EventBus.getDefault().post(ColorPickerEvents.ColorPickerOkButtonEvent()) }

        val colorPickerView = view?.findViewById<HSLColorPicker>(R.id.color_picker_view)
        colorPickerView?.setColor(mDefaultColor)
        colorPickerView?.setColorSelectionListener(object : SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
                EventBus.getDefault().post(ColorPickerEvents.ColorSelectedEvent(color))
            }
        })

        return view
    }
}