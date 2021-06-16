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
import javafx.scene.control.ComboBox
import com.jaku.api.DeviceRequests
import javafx.collections.FXCollections
import javafx.beans.property.SimpleStringProperty
import java.net.URL

class MyView: View("RPListening") {

	private var session: PrivateListeningSession? = null	
	
	private var deviceLabel: Label by singleAssign()
	private var deviceTextField: TextField by singleAssign()
	private var deviceComboBox: ComboBox<String> by singleAssign()
	private var statusLabel: Label by singleAssign()
	private val taskStatus = TaskStatus()
	private var startButton: Button by singleAssign()
	private var stopButton: Button by singleAssign()
	
	override val root = form {}
	
	init {
		val devices = DeviceRequests.discoverDevices()
		val deviceNames = FXCollections.observableArrayList<String>()
		deviceNames.add(0, "+ Roku Device IP Address")
		
		runAsync(taskStatus) {
			for (device in devices) {
				val supportsPrivateListening = device.getSupportsPrivateListening()?.toBoolean() ?: false
				
				if (supportsPrivateListening) {				
					if (device.getUserDeviceName().isNullOrEmpty()) {
						deviceNames.add(0, device.getModelName())
					} else {
						deviceNames.add(0, device.getUserDeviceName())
					}
				}
			}
		}
		
		with(root) {
			val deviceVbox = HBox()
			deviceLabel = Label("Select Roku Device:")
			deviceLabel.setPadding(Insets(5.0, 5.0, 5.0, 1.0))
			deviceComboBox = ComboBox<String>()
			deviceComboBox.items = deviceNames
			deviceComboBox.getSelectionModel().selectFirst();
			deviceComboBox.valueProperty().addListener { obs, old, new ->
				 if (new != null && new == "+ Roku Device IP Address") {
					 deviceTextField.setVisible(true)
					 startButton.setDisable(true)
				 } else {
					 deviceTextField.setVisible(false)
					 startButton.setDisable(false)
				 }
 			}
			
			deviceVbox += deviceLabel
			deviceVbox += deviceComboBox
			
			val deviceTextVbox = HBox()
			deviceTextVbox.setPadding(Insets(5.0, 0.0, 5.0, 0.0))
			deviceTextVbox.setAlignment(Pos.BASELINE_RIGHT);
			deviceTextField = TextField()
			deviceTextField.setPromptText("192.168.1.1");
			deviceTextField.setFocusTraversable(false);
			deviceTextField.setVisible(false)
			deviceTextField.textProperty().addListener { obs, old, new ->
				if (new.length == 0) {
					startButton.setDisable(true)
				} else {
					startButton.setDisable(false)
				}
 			}
			
			deviceTextVbox += deviceTextField
			
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
					deviceComboBox.setDisable(true)
					deviceTextField.setDisable(true)
					startButton.setDisable(true)
					stopButton.setDisable(false)
					
					runAsync(taskStatus) {
						updateTitle("Status: Connecting")
										
						val deviceIpAddress = if (deviceTextField.isVisible()) {
							deviceTextField.text
						} else {
							var ipAddress = ""
							
							for (device in devices) {				
								if (device.getUserDeviceName() == deviceComboBox.getValue() ||
										device.getModelName() == deviceComboBox.getValue()) {
									val url = URL(device.getHost())
									ipAddress = url.getHost()
								}
							}
							
							ipAddress
						}
						
						PrivateListeningSession.connect(deviceIpAddress, object : ConnectionListener {

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
			if (devices.size >= 1) {
				startButton.setDisable(false)
			} else {
				startButton.setDisable(true)
			}
			stopButton = Button("Stop").apply {
				tooltip("Stops the private listening session")
				action {
					deviceComboBox.setDisable(false)
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
			this += deviceTextVbox
			this += statusVbox
			this += buttonVbox
		}
	}
}