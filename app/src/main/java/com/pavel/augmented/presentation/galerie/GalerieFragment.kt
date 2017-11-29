package com.pavel.augmented.presentation.galerie

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.*
import com.pavel.augmented.R
import com.pavel.augmented.di.AppModule
import com.pavel.augmented.events.MayAskForPermissionsEvent
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

    private val gridLayoutManager by inject<GridLayoutManager>()
    private lateinit var galerieAdapter: GalerieAdapter

    private var mode = Mode.VIEW

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.layout_galerie_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        galerie_rec_view.layoutManager = gridLayoutManager

        galerieAdapter = GalerieAdapter(ArrayList())
        galerie_rec_view.adapter = galerieAdapter

        galerie_swipe_refresh_layout.setOnRefreshListener { presenter.loadSketches() }

        presenter.loadSketches()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.galerie_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_refresh -> {
                presenter.loadSketches()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

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
        galerieAdapter.list = list
        galerieAdapter.notifyDataSetChanged()

        if (galerie_swipe_refresh_layout.isRefreshing) {
            galerie_swipe_refresh_layout.isRefreshing = false
        }

        EventBus.getDefault().post(MayAskForPermissionsEvent())
    }

    @Subscribe
    fun onSketchClick(onSketchClick: SketchEvents.OnSketchClick) {
        if (mode == Mode.EDIT) {
            galerieAdapter.toggleItem(onSketchClick.position)
        } else {
            // TODO: implement this
        }
    }

    @Subscribe
    fun onSketchLongClick(onSketchLongClick: SketchEvents.OnSketchLongClick) {
        if (mode != Mode.EDIT) {
            mode = Mode.EDIT
            galerieAdapter.toggleItem(onSketchLongClick.position)
            // TODO: implement this
        }
    }

    enum class Mode {
        VIEW, EDIT
    }
}