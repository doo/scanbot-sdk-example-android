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
        tune_name.text = tuneType.filterName.replace("_", " ").capitalize()
        tune_seek_bar.max = SEEK_BAR_MAX_VALUE
        val progressVal =
                if (operation != null) {
                    ((operation.numValue - tuneType.minValue) / (tuneType.maxValue - tuneType.minValue) * SEEK_BAR_MAX_VALUE).toInt()
                } else {
                    SEEK_BAR_MAX_VALUE / 2
                }

        tune_seek_bar.progress = progressVal
        tune_value.text = "${progressVal * 2 - SEEK_BAR_MAX_VALUE}"

        tune_seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tune_value.text = "${progress * 2 - SEEK_BAR_MAX_VALUE}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val progress = seekBar!!.progress
                val res = tuneType.minValue + (tuneType.maxValue - tuneType.minValue) * progress / SEEK_BAR_MAX_VALUE
                listener.tuneValueChanged(tuneType, res)
            }
        })
    }

    companion object {
        private const val SEEK_BAR_MAX_VALUE = 100
    }
}

interface TuneValueChangedListener {
    fun tuneValueChanged(tuneType: ImageFilterTuneType, value: Float)
}