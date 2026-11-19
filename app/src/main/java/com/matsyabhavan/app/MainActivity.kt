package com.matsyabhavan.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        showHome()
    }

    private fun showHome() {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(24, 30, 24, 30)
        layout.setBackgroundColor(Color.rgb(235, 248, 255))

        val title = TextView(this)
        title.text = "🐟 মৎস্য ভবন"
        title.textSize = 32f
        title.setTextColor(Color.rgb(0, 100, 120))
        title.gravity = Gravity.CENTER

        val subtitle = TextView(this)
        subtitle.text = "MAA BIRAJMOYE MATSA BHANDAR"
        subtitle.textSize = 16f
        subtitle.gravity = Gravity.CENTER

        val customer = Button(this)
        customer.text = "Customer App"
        customer.setOnClickListener {
            showMessage("Customer App")
        }

        val admin = Button(this)
        admin.text = "Admin App"
        admin.setOnClickListener {
            showMessage("Admin App")
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(customer)
        layout.addView(admin)

        setContentView(layout)
    }

    private fun showMessage(name: String) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(24, 30, 24, 30)
        layout.gravity = Gravity.CENTER

        val title = TextView(this)
        title.text = "মৎস্য ভবন - $name"
        title.textSize = 25f
        title.gravity = Gravity.CENTER

        val back = Button(this)
        back.text = "ফিরে যান"
        back.setOnClickListener {
            showHome()
        }

        layout.addView(title)
        layout.addView(back)
        setContentView(layout)
    }
}
