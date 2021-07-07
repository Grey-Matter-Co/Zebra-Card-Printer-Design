package mx.com.infotecno.zebracardprinter

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import java.io.File

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		setTheme(R.style.AppTheme)
		super.onCreate(savedInstanceState)
		setContentView(R.layout.main_activity)

	}

	companion object {
		val TEMPLATEFILEDIRECTORY =  "TemplateData" + File.separator + "TemplateFiles"
		val TEMPLATEIMAGEFILEDIRECTORY = "TemplateData" + File.separator + "TemplateFiles"
	}
//	override fun onSupportNavigateUp(): Boolean
//		= findNavController(R.id.nav_host_fragment).navigateUp()
}