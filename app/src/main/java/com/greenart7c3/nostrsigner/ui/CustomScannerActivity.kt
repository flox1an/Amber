package com.greenart7c3.nostrsigner.ui

import android.widget.ImageButton
import com.greenart7c3.nostrsigner.R
import com.journeyapps.barcodescanner.CaptureActivity
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class CustomScannerActivity : CaptureActivity() {
    override fun initializeContent(): DecoratedBarcodeView {
        setContentView(R.layout.activity_custom_scanner)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        backButton?.setOnClickListener {
            finish()
        }

        return findViewById(R.id.zxing_barcode_scanner)
    }
}
