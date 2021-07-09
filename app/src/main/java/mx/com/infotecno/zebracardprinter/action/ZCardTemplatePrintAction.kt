package mx.com.infotecno.zebracardprinter.action

import mx.com.infotecno.zebracardprinter.model.XMLCardTemplate
import mx.com.infotecno.zebracardprinter.model.ZCardTemplate

sealed class ZCardTemplatePrintAction {
	data class TemplateChanged(val zCardTemplate: ZCardTemplate, val template: XMLCardTemplate.Template): ZCardTemplatePrintAction()
	object CameraCapture: ZCardTemplatePrintAction()
	object CameraPermissionsRequested: ZCardTemplatePrintAction()
}
