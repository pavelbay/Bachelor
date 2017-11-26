package com.pavel.augmented.presentation.canvas

import android.graphics.Color
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.v4.app.FragmentTransaction
import android.view.*
import android.widget.Toast
import com.pavel.augmented.R
import com.pavel.augmented.di.AppModule
import com.pavel.augmented.events.ColorPickerEvents
import com.pavel.augmented.fragments.ColorPickerDialogFragment
import com.pavel.augmented.fragments.EditTextDialogFragment
import com.pavel.augmented.util.toggleRegister
import kotlinx.android.synthetic.main.layout_canvas_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.contextaware.ContextAwareFragment
import org.koin.android.ext.android.inject

class CanvasFragment : ContextAwareFragment(), CanvasContract.View {
    override val contextName = AppModule.CTX_CANVAS_FRAGMENT

    override val presenter by inject<CanvasContract.Presenter>()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.layout_canvas_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        drawing_view.setColor(DEFAULT_COLOR)
        main_activity_floating_action_button.setOnClickListener {
            displayDialog()
        }
    }

    override fun onResume() {
        super.onResume()

        presenter.view = this
        presenter.start()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.main_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.eraser -> {
                drawing_view.enableEraser()
                true
            }

            R.id.save_to_gallery -> {
                // TODO: permissions check
                val dialogFragment =
                        EditTextDialogFragment.newInstance(
                                getString(R.string.title_name_dialog), getString(R.string.hint_name_dialog), 2, true
                        )
                dialogFragment.show(fragmentManager, NAME_DIALOG_TAG)
                //presenter.saveToGallery(drawing_view.bitmap)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun displayMessageSavedToGallery() = Toast.makeText(context, getString(R.string.message_saved_to_gallery), Toast.LENGTH_SHORT).show()

    override fun displayDialog() {
        val fragmentTransaction = fragmentManager.beginTransaction()
        removeDialogIfExists(fragmentTransaction, COLOR_PICKER_DIALOG_TAG)

        val colorPickerDialog = ColorPickerDialogFragment.newInstance(R.layout.layout_color_picker, R.id.color_picker_ok_button, drawing_view.getColor())
        colorPickerDialog.show(fragmentTransaction, COLOR_PICKER_DIALOG_TAG)
    }

    override fun onStart() {
        super.onStart()

        EventBus.getDefault().toggleRegister(this)
    }

    override fun onStop() {
        super.onStop()

        EventBus.getDefault().toggleRegister(this)
    }

    private fun removeDialogIfExists(fragmentTransaction: FragmentTransaction?, tag: String) {
        val previousFragment = fragmentManager.findFragmentByTag(tag)
        previousFragment?.let {
            fragmentTransaction?.remove(previousFragment)
        }
    }

    @Subscribe
    fun onSketchNameChosen(name: String) {

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

    companion object {
        @ColorInt private const val DEFAULT_COLOR = Color.GREEN
        private const val COLOR_PICKER_DIALOG_TAG = "ColorPickerDialogTag"
        private const val NAME_DIALOG_TAG = "NameDialogTag"
    }
}


