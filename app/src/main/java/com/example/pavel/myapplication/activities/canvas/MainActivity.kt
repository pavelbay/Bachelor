package com.example.pavel.myapplication.activities.canvas

import android.app.FragmentTransaction
import android.graphics.Color
import android.os.Bundle
import android.support.annotation.ColorInt
import android.view.Menu
import android.view.MenuItem
import com.example.pavel.myapplication.R
import com.example.pavel.myapplication.di.CanvasModule
import com.example.pavel.myapplication.events.ColorPickerEvents
import com.example.pavel.myapplication.fragments.ColorPickerDialogFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.contextaware.ContextAwareActivity
import org.koin.android.ext.android.inject

@ColorInt private const val DEFAULT_COLOR = Color.GREEN
private const val COLOR_PICKER_DIALOG_TAG = "color_picker_dialog_tag"

class MainActivity : ContextAwareActivity(), MainActivityContract.View {

    override val contextName = CanvasModule.CTX_CANVAS_ACTIVITY

    override val presenter by inject<MainActivityContract.Presenter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawing_view.setColor(DEFAULT_COLOR)
//        val colorPicker = ColorPicker(this, Color.alpha(DEFAULT_COLOR), Color.red(DEFAULT_COLOR), Color.green(DEFAULT_COLOR), Color.blue(DEFAULT_COLOR))
//        colorPicker.setCallback { color ->
//            mDrawingView.setColor(color)
//            colorPicker.dismiss()
//        }
        main_activity_floating_action_button.setOnClickListener {
            //            colorPicker.show()
            displayDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.eraser -> {
                drawing_view.enableEraser()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()

        presenter.view = this
        presenter.start()
    }

    override fun onStart() {
        super.onStart()

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun onStop() {
        super.onStop()

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

    override fun displayDialog() {
        val fragmentTransaction = fragmentManager.beginTransaction()
        removeDialogIfExists(fragmentTransaction, COLOR_PICKER_DIALOG_TAG)

        val colorPickerDialog = ColorPickerDialogFragment.newInstance(R.layout.layout_color_picker, R.id.color_picker_ok_button, drawing_view.getColor())
        colorPickerDialog.show(fragmentTransaction, COLOR_PICKER_DIALOG_TAG)
    }

    private fun removeDialogIfExists(fragmentTransaction: FragmentTransaction?, tag: String) {
        val previousFragment = fragmentManager.findFragmentByTag(tag)
        previousFragment?.let {
            fragmentTransaction?.remove(previousFragment)
        }
    }

    @Subscribe
    fun onColorPickerDialogDismiss(colorPickerOkButtonEvent: ColorPickerEvents.ColorPickerOkButtonEvent) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        removeDialogIfExists(fragmentTransaction, COLOR_PICKER_DIALOG_TAG)
        fragmentTransaction.commit()
    }

    @Subscribe
    fun onColorSelectedEvent(colorSelectedEvent: ColorPickerEvents.ColorSelectedEvent) {
        drawing_view.setColor(colorSelectedEvent.color)
    }
}