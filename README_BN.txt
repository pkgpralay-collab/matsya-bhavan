মৎস্য ভবন Android Source Code — Stock Auto Stop Version

এটি APK নয়; Android Studio/Kotlin source project।
নতুন স্টক নিয়ম:
- Admin অর্ডার Accept করলে Firestore transaction-এ স্টক কমবে।
- স্টক ০ হলে মাছের active=false হবে; Customer তালিকায় আর দেখাবে না।
- অর্ডার Accept করার সময় স্টক যথেষ্ট না থাকলে অর্ডার Accept হবে না।
- Accepted অর্ডার Cancel করলে স্টক একবারই ফেরত আসবে।
- Pending অর্ডার বাতিল করলে স্টক ফেরত দেওয়া হবে না, কারণ তখনও স্টক কমেনি।
- একই অর্ডার একাধিকবার Accept/Cancel করে স্টক দ্বিগুণ কমানো/বাড়ানো ঠেকাতে transaction status পরীক্ষা করে।

সেটআপ:
1. Android Studio-তে project খুলুন।
2. Firebase Console-এ Android app যোগ করে Firestore চালু করুন।
3. google-services.json ফাইল app/ ফোল্ডারে রাখুন।
4. Gradle Sync করে APK build করুন।

গুরুত্বপূর্ণ:
- এটি source code, ready APK নয়।
- Admin login/role security ও Firestore Security Rules live করার আগে অবশ্যই যোগ করুন।
- Customer order Pending হয়; Admin Accept করলে stock reserve/decrease হয়।
- UPI intent শুধু payment app খোলে; payment verification নিজে করে না। Admin UTR দেখে যাচাই করবেন।
- গোটা মাছের ক্ষেত্রে stock সংখ্যায়, কাটা/কুচো মাছের ক্ষেত্রে kg-তে লিখুন।
- লাইভ ব্যবহারের আগে Firebase rules এবং server-side validation নিরাপদভাবে configure করতে হবে।

পেমেন্ট QR:
- ব্যবহারকারীর দেওয়া PhonePe/SBI QR ছবিটি app/src/main/res/drawable-nodpi/payment_qr.png হিসেবে যুক্ত।
- QR থেকে পড়া UPI ID: Q146861508@ybl
- Customer-এর Accepted অর্ডারে QR দেখাবে।
- টাকা পাওয়ার পর Admin-কে UTR/লেনদেন নিজে যাচাই করতে হবে; QR বা UPI intent দিয়ে অ্যাপ স্বয়ংক্রিয়ভাবে পেমেন্ট নিশ্চিত করে না।
