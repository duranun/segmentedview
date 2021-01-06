package com.duranun.segmentview

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.duranun.library.SegmentedView

class MainActivity : AppCompatActivity() {
    private lateinit var segmentedView: SegmentedView
    private lateinit var selectionTv: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        segmentedView = findViewById(R.id.segment)
        selectionTv = findViewById(R.id.selectionText)
        segmentedView.setItemsList(arrayOf("Tab1", "Tab2"))
        segmentedView.setOnSelectionListener { view, selectedIndex ->
            val tv = view as TextView
            selectionTv.text =
                String.format("Selected Text: %s , Selected Index: %d", tv.text, selectedIndex)
        }
    }
}