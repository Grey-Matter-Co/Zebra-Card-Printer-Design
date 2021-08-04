package mx.com.infotecno.zebracardprinter.util

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

object UIHelper {
	fun hideSoftKeyboard(activity: Activity) {
		val currentFocus = activity.currentFocus
		if (currentFocus != null) {
			val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
			imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
		}
	}
}
