package mx.com.infotecno.zebracardprinter.action

import mx.com.infotecno.zebracardprinter.model.ZCardTemplate

sealed class ZCardTemplatePrintAction {
	data class TemplateChanged(val zCardTemplate: ZCardTemplate): ZCardTemplatePrintAction()
	object CameraCapture: ZCardTemplatePrintAction()
	object CameraPermissionsRequested: ZCardTemplatePrintAction()
}
