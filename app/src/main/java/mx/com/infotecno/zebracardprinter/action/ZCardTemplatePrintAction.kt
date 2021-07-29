package mx.com.infotecno.zebracardprinter.action

import android.view.View
import mx.com.infotecno.zebracardprinter.model.XMLCardTemplate
import mx.com.infotecno.zebracardprinter.model.ZCardTemplate

sealed class ZCardTemplatePrintAction {
	data class TemplateChanged(val zCardTemplate: ZCardTemplate, val template: XMLCardTemplate.Template, val listFieldViews: List<View>): ZCardTemplatePrintAction()
	data class TemplateViewCreated(val listFieldViews: MutableList<View>) : ZCardTemplatePrintAction()
	object CameraCapture: ZCardTemplatePrintAction()
	object CameraPermissionsRequested: ZCardTemplatePrintAction()
}
