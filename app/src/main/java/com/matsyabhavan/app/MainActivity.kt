
package com.matsyabhavan.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        val admin = Button(this)
        admin.text = "Admin App"

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(customer)
        layout.addView(admin)

        setContentView(layout)
    }
}
