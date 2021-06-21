package github.hotstu.camerax.qrcodec

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class LoaderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loader)
    }

    fun zxing(view: View) {
        startActivity(Intent(this, MainActivity::class.java))
    }

    fun mlkit(view: View) {
        startActivity(Intent(this, MainActivity::class.java)
            .apply {
                putExtra("useMlKitDetector", true)
            }
        )
    }
}