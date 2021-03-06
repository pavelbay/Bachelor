package com.pavel.augmented.presentation.galerie

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.view.*
import android.widget.Toast
import com.pavel.augmented.R
import com.pavel.augmented.di.AppModule
import com.pavel.augmented.events.PermissionsEvent
import com.pavel.augmented.events.SketchEvents
import com.pavel.augmented.model.Sketch
import com.pavel.augmented.util.toggleRegister
import kotlinx.android.synthetic.main.layout_galerie_fragment.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.contextaware.ContextAwareFragment
import org.koin.android.ext.android.inject

class GalerieFragment : ContextAwareFragment(), GalerieContract.View {
    override val contextName = AppModule.CTX_GALERIE_FRAGMENT

    override val presenter by inject<GalerieContract.Presenter>()

    private lateinit var galerieAdapter: GalerieAdapter

    private var menu: Menu? = null

    private var mode = Mode.VIEW

    private var storagePermissionGranted = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        storagePermissionGranted = checkPermission()
        setHasOptionsMenu(true)
        galerieAdapter = GalerieAdapter(ArrayList())
        return inflater?.inflate(R.layout.layout_galerie_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        galerie_rec_view.layoutManager = GridLayoutManager(context, 4)

        galerie_rec_view.adapter = galerieAdapter

        galerie_swipe_refresh_layout.setOnRefreshListener { presenter.loadSketches() }

        if (storagePermissionGranted) {
            presenter.loadSketches()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt(MODE_SAVE_STATE_KEY, mode.ordinal)
        outState.putBooleanArray(LIST_SAVE_STATE_KEY, galerieAdapter?.getSelectedNumbers())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        savedInstanceState?.let {
            mode = Mode.values()[savedInstanceState.getInt(MODE_SAVE_STATE_KEY)]
            galerieAdapter.restoreSelectedItems(savedInstanceState.getBooleanArray(LIST_SAVE_STATE_KEY))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        this.menu = menu
        inflater?.inflate(R.menu.galerie_menu, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)

        toggleMenuItems()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_galerie_refresh -> {
                if (storagePermissionGranted) {
                    presenter.loadSketches()
                } else {
                    if (isAdded) {
                        Toast.makeText(context, R.string.galerie_fragmetn_no_storage_permission, Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }

            R.id.menu_galerie_exit_edit_mode -> {
                exitEditMode()
                true
            }

            R.id.menu_galerie_delete -> {
                presenter.deleteSketches(galerieAdapter.getSelectedItems())
                exitEditMode()
                true
            }

            R.id.menu_galerie_upload -> {
                presenter.publicSketches(galerieAdapter.getSelectedItems())
                exitEditMode()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun context(): Context = context!!

    override fun onStart() {
        super.onStart()

        EventBus.getDefault().toggleRegister(this)
    }

    override fun onStop() {
        super.onStop()

        EventBus.getDefault().toggleRegister(this)
    }

    override fun onResume() {
        super.onResume()

        presenter.start()
        presenter.view = this
    }

    override fun displaySketches(list: MutableList<Sketch>) {
        galerieAdapter.swapDataItems(list)

        if (galerie_swipe_refresh_layout != null && galerie_swipe_refresh_layout.isRefreshing) {
            galerie_swipe_refresh_layout.isRefreshing = false
        }

        // EventBus.getDefault().post(MayAskForPermissionsEvent())
    }

    private fun checkPermission() = ContextCompat.checkSelfPermission(context(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun exitEditMode() {
        mode = Mode.VIEW
        galerieAdapter.unselectItems()
        toggleMenuItems()
    }

    private fun toggleViewElements(position: Int) {
        galerieAdapter.toggleItem(position)
        toggleMenuItems()
    }

    private fun toggleMenuItems() {
        menu?.let {
            menu?.setGroupVisible(R.id.galerie_group_view, mode != Mode.EDIT)
            menu?.setGroupVisible(R.id.galerie_group_edit, mode == Mode.EDIT)
        }
    }

    @SuppressLint("MissingPermission")
    @Subscribe
    fun onPermissionsRequested(event: PermissionsEvent) {
        val before = storagePermissionGranted
        storagePermissionGranted = checkPermission()
        if (before != storagePermissionGranted) {
            presenter.loadSketches()
        }
    }

    @Subscribe
    fun onSketchClick(onSketchClick: SketchEvents.OnSketchClick) {
        if (mode == Mode.EDIT) {
            galerieAdapter.toggleItem(onSketchClick.position)
        } else {
            EventBus.getDefault().post(SketchEvents.OnSketchChosen(galerieAdapter.list[onSketchClick.position]))
        }
    }

    @Subscribe
    fun onSketchLongClick(onSketchLongClick: SketchEvents.OnSketchLongClick) {
//        if (mode != Mode.EDIT) {
            mode = Mode.EDIT
            toggleViewElements(onSketchLongClick.position)
            // TODO: test this
//        }
    }

    enum class Mode {
        VIEW, EDIT
    }

    companion object {
        private val TAG = GalerieFragment::class.java.simpleName
        private val MODE_SAVE_STATE_KEY = "ModeSaveStateKey"
        private val LIST_SAVE_STATE_KEY = "ListSaveStateKey"
        const val PERMISSION_STORAGE_FROM_GALERIE_FRAGMENT = 1001
    }
}