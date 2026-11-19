package in.matsyabhavan.app

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration

class MainActivity : Activity() {
    private val db by lazy { FirebaseFirestore.getInstance() }
    private lateinit var root: LinearLayout
    private var adminMode = false
    private var fishListener: ListenerRegistration? = null
    private var orderListener: ListenerRegistration? = null
    private val upiId = "Q146861508@ybl"

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); showHome() }

    private fun page(title: String) {
        root = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(28,24,28,24) }
        setContentView(ScrollView(this).apply { addView(root) })
        root.addView(TextView(this).apply { text=title; textSize=25f; gravity=Gravity.CENTER; setPadding(0,8,0,20) })
    }
    private fun button(label: String, action: () -> Unit) {
        root.addView(Button(this).apply { text=label; setOnClickListener { action() } })
    }
    private fun edit(hintText: String): EditText {
        val e=EditText(this).apply { hint=hintText }; root.addView(e); return e
    }
    private fun toast(s: String) = Toast.makeText(this,s,Toast.LENGTH_LONG).show()

    private fun showHome() {
        page("মৎস্য ভবন")
        root.addView(TextView(this).apply { text="MAA BIRAJMOYE MATSA BHANDAR\\nContai"; textSize=16f; gravity=Gravity.CENTER })
        button("Admin") { showAdmin() }
        button("Customer") { showCustomer() }
    }
    private fun showAdmin() {
        adminMode=true
        page("মৎস্য ভবন Admin")
        val name=edit("মাছের নাম")
        val category=Spinner(this).apply {
            adapter=ArrayAdapter(this@MainActivity,android.R.layout.simple_spinner_dropdown_item,listOf("গোটা মাছ","কাটা মাছ","কুচো মাছ"))
        }
        root.addView(category)
        val price=edit("দাম (প্রতি কেজি টাকা; গোটা মাছের ক্ষেত্রে প্রতি মাছ)")
        val stock=edit("স্টক (গোটা মাছ = সংখ্যা; কাটা/কুচো = কেজি)")
        val photo=edit("ছবির HTTPS URL")
        button("মাছ যোগ করুন") {
            val n=name.text.toString().trim(); val p=price.text.toString().toDoubleOrNull(); val s=stock.text.toString().toDoubleOrNull()
            if(n.isEmpty()||p==null||s==null||p<=0||s<0){toast("নাম, দাম ও স্টক ঠিকভাবে দিন");return@button}
            db.collection("fish").add(hashMapOf("name" to n,"category" to category.selectedItem.toString(),
                "price" to p,"stock" to s,"photoUrl" to photo.text.toString(),"active" to (s>0),
                "updatedAt" to System.currentTimeMillis()))
                .addOnSuccessListener { toast(if(s>0) "মাছ যোগ হয়েছে" else "স্টক ০ — বিক্রি বন্ধ রাখা হয়েছে") }
                .addOnFailureListener { toast("Firebase error: ${it.message}") }
        }
        button("মাছের তালিকা / Edit") { showFishList(true) }
        button("অর্ডার দেখুন") { showOrders() }
        button("Home") { showHome() }
    }
    private fun showCustomer() {
        adminMode=false; page("মাছ কিনুন — মৎস্য ভবন")
        button("মাছের তালিকা") { showFishList(false) }
        button("আমার অর্ডার") { showOrders() }
        button("Home") { showHome() }
    }
    private fun showFishList(editable:Boolean) {
        page(if(editable) "মাছ Edit / Stock" else "মাছের তালিকা")
        fishListener?.remove()
        fishListener=db.collection("fish").whereEqualTo("active",true).addSnapshotListener { snap,err ->
            if(err!=null){toast("তালিকা লোড হয়নি: ${err.message}");return@addSnapshotListener}
            root.removeViews(1,root.childCount-1)
            if(snap==null||snap.isEmpty) root.addView(TextView(this).apply{text="এখন কোনও মাছ বিক্রির জন্য নেই"})
            else for(doc in snap.documents) {
                val n=doc.getString("name")?: "মাছ"; val c=doc.getString("category")?:""
                val p=doc.getDouble("price")?:0.0; val s=doc.getDouble("stock")?:0.0
                root.addView(TextView(this).apply{text="$n • $c\\n₹$p • স্টক: $s";textSize=18f;setPadding(0,16,0,8)})
                if(editable) {
                    val np=EditText(this).apply{hint="নতুন দাম";inputType=2}; val ns=EditText(this).apply{hint="নতুন স্টক";inputType=8194}
                    root.addView(np);root.addView(ns)
                    button("এই মাছ Update") {
                        val data=mutableMapOf<String,Any>("updatedAt" to System.currentTimeMillis())
                        np.text.toString().toDoubleOrNull()?.let{if(it>0)data["price"]=it}
                        ns.text.toString().toDoubleOrNull()?.let{if(it>=0){data["stock"]=it;data["active"]=it>0}}
                        if(data.size==1){toast("নতুন দাম বা স্টক দিন");return@button}
                        doc.reference.update(data).addOnSuccessListener{toast("আপডেট হয়েছে")}
                    }
                    button("বিক্রি বন্ধ করুন") { doc.reference.update("active",false).addOnSuccessListener{toast("বিক্রি বন্ধ")} }
                } else {
                    button("অর্ডার করুন: $n") { placeOrder(doc.id,n,c,p,s) }
                }
                root.addView(View(this).apply{layoutParams=LinearLayout.LayoutParams(-1,2).apply{topMargin=8;bottomMargin=8};setBackgroundColor(0xFFDDDDDD.toInt())})
            }
            button("Home"){showHome()}
        }
    }
    private fun placeOrder(id:String,name:String,category:String,price:Double,available:Double) {
        if(available<=0){toast("স্টক শেষ — অর্ডার বন্ধ");return}
        val qty=EditText(this).apply{hint=if(category=="গোটা মাছ") "কতটি মাছ?" else "পরিমাণ (কেজি)";inputType=8194}
        android.app.AlertDialog.Builder(this).setTitle("$name — ₹$price")
            .setView(qty).setPositiveButton("অর্ডার দিন"){_,_->
                val q=qty.text.toString().toDoubleOrNull()
                if(q==null||q<=0||q>available){toast("পরিমাণ ঠিক দিন। বর্তমান স্টক: $available");return@setPositiveButton}
                db.collection("orders").add(hashMapOf("fishId" to id,"fishName" to name,"quantity" to q,
                    "price" to price,"total" to q*price,"status" to "Pending","stockDeducted" to false,
                    "createdAt" to System.currentTimeMillis()))
                    .addOnSuccessListener{toast("অর্ডার জমা হয়েছে। Admin গ্রহণ করলে স্টক কমবে।")}
                    .addOnFailureListener{toast("অর্ডার হয়নি: ${it.message}")}
            }.setNegativeButton("বাতিল",null).show()
    }

    // Atomic transaction: checks current stock and order status before deducting.
    private fun acceptOrder(orderId:String) {
        val orderRef=db.collection("orders").document(orderId)
        db.runTransaction { tx ->
            val order=tx.get(orderRef)
            val status=order.getString("status") ?: "Pending"
            if(status!="Pending") throw IllegalStateException("এই অর্ডার আর Pending নেই")
            val fishId=order.getString("fishId") ?: throw IllegalStateException("Fish ID নেই")
            val fishRef=db.collection("fish").document(fishId)
            val fish=tx.get(fishRef)
            val stock=fish.getDouble("stock") ?: 0.0
            val qty=order.getDouble("quantity") ?: 0.0
            if(qty<=0 || stock<qty) throw IllegalStateException("পর্যাপ্ত স্টক নেই; বর্তমান স্টক $stock")
            val remaining=stock-qty
            tx.update(fishRef,mapOf("stock" to remaining,"active" to (remaining>0),"updatedAt" to System.currentTimeMillis()))
            tx.update(orderRef,mapOf("status" to "Accepted","stockDeducted" to true,"acceptedAt" to System.currentTimeMillis()))
            null
        }.addOnSuccessListener{toast("অর্ডার গ্রহণ হয়েছে; স্টক কমেছে")}
         .addOnFailureListener{toast("Accept হয়নি: ${it.message}")}
    }

    // Restock only once, and only for an accepted order that already deducted stock.
    private fun cancelOrder(orderId:String) {
        val orderRef=db.collection("orders").document(orderId)
        db.runTransaction { tx ->
            val order=tx.get(orderRef)
            val status=order.getString("status") ?: ""
            if(status=="Cancelled") throw IllegalStateException("অর্ডার আগেই বাতিল")
            val deducted=order.getBoolean("stockDeducted") ?: false
            if(deducted) {
                val fishId=order.getString("fishId") ?: throw IllegalStateException("Fish ID নেই")
                val fishRef=db.collection("fish").document(fishId)
                val fish=tx.get(fishRef)
                val old=fish.getDouble("stock") ?: 0.0
                val restored=old+(order.getDouble("quantity") ?: 0.0)
                tx.update(fishRef,mapOf("stock" to restored,"active" to (restored>0),"updatedAt" to System.currentTimeMillis()))
            }
            tx.update(orderRef,mapOf("status" to "Cancelled","stockDeducted" to false,"cancelledAt" to System.currentTimeMillis()))
            null
        }.addOnSuccessListener{toast("অর্ডার বাতিল; প্রযোজ্য হলে স্টক ফেরত এসেছে")}
         .addOnFailureListener{toast("বাতিল হয়নি: ${it.message}")}
    }
    private fun showOrders() {
        page("অর্ডার")
        orderListener?.remove()
        orderListener=db.collection("orders").addSnapshotListener{snap,err->
            if(err!=null){toast("অর্ডার লোড হয়নি");return@addSnapshotListener}
            root.removeViews(1,root.childCount-1)
            if(snap==null||snap.isEmpty)root.addView(TextView(this).apply{text="কোনও অর্ডার নেই"})
            else for(doc in snap.documents) {
                val name=doc.getString("fishName")?:"মাছ";val total=doc.getDouble("total")?:0.0;val status=doc.getString("status")?:"Pending"
                root.addView(TextView(this).apply{text="$name • ₹$total\\nঅবস্থা: $status";textSize=18f})
                if(adminMode&&status=="Pending") button("অর্ডার গ্রহণ — স্টক কমবে"){acceptOrder(doc.id)}
                if(adminMode&&(status=="Pending"||status=="Accepted")) button("অর্ডার বাতিল"){cancelOrder(doc.id)}
                if(adminMode&&status=="Accepted") button("পেমেন্ট যাচাই করে Paid"){doc.reference.update("status","Paid / Confirmed").addOnSuccessListener{toast("পেমেন্ট স্ট্যাটাস আপডেট হয়েছে")}}
                if(!adminMode&&status=="Accepted") {
                    root.addView(TextView(this).apply {
                        text = "পেমেন্টের জন্য QR স্ক্যান করুন\\nUPI ID: $upiId"
                        textSize = 16f; setPadding(0,12,0,8)
                    })
                    root.addView(ImageView(this).apply {
                        setImageResource(R.drawable.payment_qr)
                        adjustViewBounds = true
                        scaleType = ImageView.ScaleType.FIT_CENTER
                        layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 620)
                    })
                    button("UPI অ্যাপে পেমেন্ট করুন"){
                        val uri=Uri.parse("upi://pay?pa=$upiId&pn=${Uri.encode("PhonePeMerchant")}&am=$total&cu=INR")
                        try{startActivity(Intent(Intent.ACTION_VIEW,uri))}catch(_:Exception){toast("UPI অ্যাপ পাওয়া যায়নি")}
                    }
                }
            }
            button("Home"){showHome()}
        }
    }
    override fun onDestroy(){fishListener?.remove();orderListener?.remove();super.onDestroy()}
}
