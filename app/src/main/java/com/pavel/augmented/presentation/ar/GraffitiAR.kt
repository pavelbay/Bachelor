package com.pavel.augmented.presentation.ar

import android.graphics.Bitmap
import android.opengl.GLES20
import android.util.Log
import cn.easyar.*

class GraffitiAR {
    private var camera: CameraDevice? = null
    private var streamer: CameraFrameStreamer? = null
    private val trackers = ArrayList<ImageTracker>()
    private var videobg_renderer: Renderer? = null
    var bitmap: Bitmap? = null

    // TODO: Change it
    private var box_renderer: BoxRenderer? = null
    private var viewport_changed = false
    private var view_size = Vec2I(0, 0)
    private var rotation = 0
    private var viewport = Vec4I(0, 0, 1280, 720)

    private fun loadFromImage(tracker: ImageTracker, path: String) {
        val target = ImageTarget()
        val jstr = """{
  "images" :
  [
    {
      "image" : "$path",
      "name" : "${path.substring(0, path.indexOf("."))}"
    }
  ]
}"""
        target.setup(jstr, StorageType.Assets or StorageType.Json, "")
        tracker.loadTarget(target) { target, status -> Log.i("HelloAR", "load target ($status): ${target.name()} (${target.runtimeID()})") }
    }

    private fun loadFromJsonFile(tracker: ImageTracker, path: String, targetname: String) {
        val target = ImageTarget()
        target.setup(path, StorageType.Assets, targetname)
        tracker.loadTarget(target) { target, status -> Log.i("HelloAR", "load target ($status): ${target.name()} (${target.runtimeID()})") }
    }

    private fun loadAllFromJsonFile(tracker: ImageTracker, path: String) {
        for (target in ImageTarget.setupAll(path, StorageType.Assets)) {
            tracker.loadTarget(target) { target, status -> Log.i("HelloAR", "load target ($status): ${target.name()} (${target.runtimeID()})") }
        }
    }

    fun onTargetChanged(jsonTarget: String?) {
        box_renderer!!.loadTexture(bitmap)
    }

    fun initialize(): Boolean {
        camera = CameraDevice()
        streamer = CameraFrameStreamer()
        streamer!!.attachCamera(camera)

        var status = true
        status = status and camera!!.open(CameraDeviceType.Default)
        camera!!.setSize(Vec2I(1280, 720))

        if (!status) {
            return status
        }
        val tracker = ImageTracker()
        tracker.attachStreamer(streamer)
//        loadFromJsonFile(tracker, "targets.json", "argame")
//        loadFromJsonFile(tracker, "targets.json", "idback")
//        loadAllFromJsonFile(tracker, "targets2.json")
        loadFromImage(tracker, "target.png")
        trackers.add(tracker)

        return status
    }

    fun dispose() {
        for (tracker in trackers) {
            tracker.dispose()
        }
        trackers.clear()
        box_renderer = null
        if (videobg_renderer != null) {
            videobg_renderer!!.dispose()
            videobg_renderer = null
        }
        if (streamer != null) {
            streamer!!.dispose()
            streamer = null
        }
        if (camera != null) {
            camera!!.dispose()
            camera = null
        }
    }

    fun start(): Boolean {
        var status = true
        status = status and (camera != null && camera!!.start())
        status = status and (streamer != null && streamer!!.start())
        camera!!.setFocusMode(CameraDeviceFocusMode.Continousauto)
        for (tracker in trackers) {
            status = status and tracker.start()
        }
        return status
    }

    fun stop(): Boolean {
        var status = true
        for (tracker in trackers) {
            status = status and tracker.stop()
        }
        status = status and (streamer != null && streamer!!.stop())
        status = status and (camera != null && camera!!.stop())
        return status
    }

    fun initGL() {
        if (videobg_renderer != null) {
            videobg_renderer!!.dispose()
        }
        videobg_renderer = Renderer()

        // TODO: change it
        box_renderer = BoxRenderer()
        box_renderer!!.init()
    }

    fun resizeGL(width: Int, height: Int) {
        view_size = Vec2I(width, height)
        viewport_changed = true
    }

    private fun updateViewport() {
        val calib = camera?.cameraCalibration() ?: null
        val rotation = calib?.rotation() ?: 0
        if (rotation != this.rotation) {
            this.rotation = rotation
            viewport_changed = true
        }
        if (viewport_changed) {
            var size = Vec2I(1, 1)
            if (camera != null && camera!!.isOpened) {
                size = camera!!.size()
            }
            if (rotation == 90 || rotation == 270) {
                size = Vec2I(size.data[1], size.data[0])
            }
            val scaleRatio = Math.max(view_size.data[0].toFloat() / size.data[0].toFloat(), view_size.data[1].toFloat() / size.data[1].toFloat())
            val viewport_size = Vec2I(Math.round(size.data[0] * scaleRatio), Math.round(size.data[1] * scaleRatio))
            viewport = Vec4I((view_size.data[0] - viewport_size.data[0]) / 2, (view_size.data[1] - viewport_size.data[1]) / 2, viewport_size.data[0], viewport_size.data[1])

            if (camera != null && camera!!.isOpened) {
                viewport_changed = false
            }
        }
    }

    fun render() {
        GLES20.glClearColor(1f, 1f, 1f, 1f)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        if (videobg_renderer != null) {
            val default_viewport = Vec4I(0, 0, view_size.data[0], view_size.data[1])
            GLES20.glViewport(default_viewport.data[0], default_viewport.data[1], default_viewport.data[2], default_viewport.data[3])
            if (videobg_renderer!!.renderErrorMessage(default_viewport)) {
                return
            }
        }

        if (streamer == null) {
            return
        }
        val frame = streamer!!.peek()
        try {
            updateViewport()
            GLES20.glViewport(viewport.data[0], viewport.data[1], viewport.data[2], viewport.data[3])

            videobg_renderer?.render(frame, viewport)

            for (targetInstance in frame.targetInstances()) {
                val status = targetInstance.status()
                if (status == TargetStatus.Tracked) {
                    val target = targetInstance.target()
                    val imagetarget = target as? ImageTarget ?: continue
                    box_renderer?.render2(camera!!.projectionGL(0.2f, 500f), targetInstance.poseGL(), imagetarget.size())
                }
            }
        } finally {
            frame.dispose()
        }
    }
}