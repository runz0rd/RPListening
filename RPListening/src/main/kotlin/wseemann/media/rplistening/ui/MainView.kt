package wseemann.media.rplistening.ui

import javafx.scene.control.Button
import javafx.scene.layout.VBox
import tornadofx.*
import javafx.geometry.Pos
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.layout.HBox
import javafx.scene.control.Label
import javafx.geometry.Insets
import javafx.scene.control.TextField
import wseemann.media.rplistening.protocol.PrivateListeningSession
import wseemann.media.rplistening.protocol.ConnectionListener

class MyView: View("RPListening") {

	private var session: PrivateListeningSession? = null	
	
	private var deviceLabel: Label by singleAssign()
	private var deviceTextField: TextField by singleAssign()	
	private var statusLabel: Label by singleAssign()
	private val taskStatus = TaskStatus()
	private var startButton: Button by singleAssign()
	private var stopButton: Button by singleAssign()
	
	override val root = form {}
	
	init {
		with(root) {
			val deviceVbox = HBox()
			deviceLabel = Label("Roku Device IP Address:")
			deviceLabel.setPadding(Insets(5.0, 5.0, 5.0, 1.0))
			deviceTextField = TextField()
			deviceTextField.setPromptText("192.168.1.1");
			deviceTextField.setFocusTraversable(false);
			deviceTextField.textProperty().addListener { obs, old, new ->
				if (new.length == 0) {
					startButton.setDisable(true)
				} else {
					startButton.setDisable(false)
				}
 			}
			deviceVbox += deviceLabel
			deviceVbox += deviceTextField
			
			val statusVbox = HBox()
			statusLabel = Label()
			statusLabel.textProperty().bind( taskStatus.title )
			statusLabel.setPadding(Insets(5.0, 5.0, 5.0, 1.0))
			runAsync(taskStatus) {
				updateTitle("Status: Not Connected")
			}
			statusVbox += statusLabel
			
			val buttonVbox = HBox()
			startButton = Button("Start").apply {
				tooltip("Starts the private listening session")
				action {
					deviceTextField.setDisable(true)
					startButton.setDisable(true)
					stopButton.setDisable(false)
					
					runAsync(taskStatus) {
						updateTitle("Status: Connecting")
										
						PrivateListeningSession.connect(deviceTextField.text, object : ConnectionListener {

							override fun onConnected(newSession: PrivateListeningSession) {
								session = newSession;
								runAsync(taskStatus) {
									updateTitle("Status: Connected")
								}
							}

							override fun onFailure(error: Throwable) {
								PrivateListeningSession.disconnect(session);
								session = null;

								runAsync(taskStatus) {
									updateTitle("Status: Not Connected")
								}
								deviceTextField.setDisable(false)
								startButton.setDisable(false)
								stopButton.setDisable(true)
							}
						})
					}
				}
			}
		    startButton.setDisable(true)
			stopButton = Button("Stop").apply {
				tooltip("Stops the private listening session")
				action {
					deviceTextField.setDisable(false)
					startButton.setDisable(false)
					stopButton.setDisable(true)
					
					runAsync(taskStatus) {
						updateTitle("Status: Not Connected")
						
						PrivateListeningSession.disconnect(session);
					}
				}
			}
			stopButton.setDisable(true)
			buttonVbox += startButton
			buttonVbox += stopButton
			
			this += deviceVbox
			this += statusVbox
			this += buttonVbox
		}
	}
}