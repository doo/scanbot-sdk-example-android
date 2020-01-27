package io.scanbot.example

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.SeekBar
import io.scanbot.sdk.process.ImageFilterTuneType
import io.scanbot.sdk.process.TuneOperation
import kotlinx.android.synthetic.main.view_tune.view.*

class TuneView(context: Context) : FrameLayout(context) {
    init {
        LayoutInflater.from(context).inflate(R.layout.view_tune, this, true)
    }

    private lateinit var tuneType: ImageFilterTuneType

    fun initForTune(newTuneType: ImageFilterTuneType, listener: TuneValueChangedListener, operation: TuneOperation?) {
        tuneType = newTuneType
        tune_name.text = tuneType.filterName
        tune_seek_bar.max = 100
        val pr =
                if (operation != null) {
                    ((operation.numValue - tuneType.minValue) / (tuneType.maxValue - tuneType.minValue) * 100).toInt()
                } else {
                    50
                }

        tune_seek_bar.progress = pr
        tune_value.text = pr.toString()

        tune_seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val progress = seekBar!!.progress
                val res = tuneType.minValue + (tuneType.maxValue - tuneType.minValue) * progress / 100
                listener.tuneValueChanged(tuneType, res)
                tune_value.text = progress.toString()
            }
        })


    }
}

interface TuneValueChangedListener {
    fun tuneValueChanged(tuneType: ImageFilterTuneType, value: Float)
}