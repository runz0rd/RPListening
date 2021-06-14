package wseemann.media.rplistening.ui

import tornadofx.*

class RPListeningApp : App(MyView::class, Style::class) {
	 init {
        reloadStylesheetsOnFocus()
    }
}