package com.pavel.augmented.events

import com.pavel.augmented.model.Sketch

class SketchEvents {
    class OnSketchClick(val position: Int)
    class OnSketchLongClick(val position: Int)
    class OnSketchChosen(val sketch: Sketch)
    class OnSketchSaved()
}