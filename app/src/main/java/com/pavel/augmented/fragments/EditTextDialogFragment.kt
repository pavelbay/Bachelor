package com.pavel.augmented.fragments

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import com.pavel.augmented.R
import com.pavel.augmented.events.SketchNameChosenEvent
import org.greenrobot.eventbus.EventBus

class EditTextDialogFragment : DialogFragment() {

    private lateinit var editTextDialog: AlertDialog

    companion object {
        private const val TITLE_KEY = "TitleKey"
        private const val HINT_KEY = "HintKey"
        private const val MIN_LENGTH_KEY = "MinLenghtKey"
        private const val DISABLE_POSITIVE_BUTTON = "DisablePositiveButton"

        fun newInstance(title: String, hint: String?, minLength: Int, disablePositiveButton: Boolean): EditTextDialogFragment {
            val args = Bundle()
            args.putString(TITLE_KEY, title)
            args.putString(HINT_KEY, hint)
            args.putInt(MIN_LENGTH_KEY, minLength)
            args.putBoolean(DISABLE_POSITIVE_BUTTON, disablePositiveButton)
            val dialogFragment = EditTextDialogFragment()
            dialogFragment.arguments = args

            return dialogFragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val editText = EditText(context)
        editText.hint = arguments.getString(HINT_KEY)

        editTextDialog = AlertDialog.Builder(activity)
                .setTitle(arguments.getString(TITLE_KEY))
                .setView(editText)
                .setPositiveButton(android.R.string.ok, { _, _ ->
                    if (editText.text.isEmpty()) {
                        Toast.makeText(context, R.string.message_cannot_be_empty, Toast.LENGTH_SHORT).show()
                    } else {
                        EventBus.getDefault().post(SketchNameChosenEvent(editText.text.toString()))
                        dismiss()
                    }
                })
                .setNegativeButton(R.string.cancel, { _, _ -> dismiss() })
                .create()

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                editTextDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = s != null && s.length > arguments.getInt(MIN_LENGTH_KEY) - 1
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })

        return editTextDialog
    }

    override fun onResume() {
        super.onResume()

        editTextDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !arguments.getBoolean(DISABLE_POSITIVE_BUTTON, false)
    }
}