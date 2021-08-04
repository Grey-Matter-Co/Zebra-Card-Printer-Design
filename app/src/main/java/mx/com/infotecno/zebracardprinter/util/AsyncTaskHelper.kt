package mx.com.infotecno.zebracardprinter.util

import android.os.AsyncTask

object AsyncTaskHelper {
	fun isAsyncTaskRunning(asyncTask: AsyncTask<*, *, *>?): Boolean {
		return asyncTask != null && asyncTask.status == AsyncTask.Status.RUNNING
	}
}
