package com.example.eventpass2

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder

class MyQRActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_qr)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My QR Code"

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.getValue(User::class.java)
                if (user != null) {
                    findViewById<TextView>(R.id.tvName).text = user.name
                    findViewById<TextView>(R.id.tvQrId).text = "ID: ${user.qr_id}"
                    generateQR(user.qr_id)
                }
            }
    }

    private fun generateQR(qrId: String) {
        try {
            val writer = MultiFormatWriter()
            val matrix = writer.encode(qrId, BarcodeFormat.QR_CODE, 500, 500)
            val encoder = BarcodeEncoder()
            val bitmap = encoder.createBitmap(matrix)
            findViewById<ImageView>(R.id.imgQR).setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}