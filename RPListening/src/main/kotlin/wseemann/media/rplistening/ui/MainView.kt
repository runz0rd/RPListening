/*
 * RPListening: An Open Source desktop client for Roku private listening.
 *
 * Copyright (C) 2021 William Seemann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package wseemann.media.rplistening.ui

import javafx.scene.control.Button
import tornadofx.*
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.control.Label
import javafx.geometry.Insets
import javafx.scene.control.TextField
import wseemann.media.rplistening.protocol.PrivateListeningSession
import wseemann.media.rplistening.protocol.ConnectionListener
import javafx.scene.control.ComboBox
import com.jaku.api.DeviceRequests
import javafx.collections.FXCollections
import java.net.URL
import java.net.SocketTimeoutException
import com.jaku.model.Device
import javafx.application.Platform
import kotlinx.coroutines.*

class MyView : View("RPListening") {

    private var session: PrivateListeningSession? = null
	private var devices = listOf<Device>()

    private var deviceLabel: Label by singleAssign()
    private var deviceTextField: TextField by singleAssign()
    private var deviceComboBox: ComboBox<String> by singleAssign()
    private var statusLabel: Label by singleAssign()
    private val taskStatus = TaskStatus()
    private var startButton: Button by singleAssign()
    private var stopButton: Button by singleAssign()

    override val root = form {}

    init {
        with(root) {
            setPrefSize(325.0, 120.0)

            val deviceVbox = HBox()
            deviceLabel = Label("Select Roku Device:")
            deviceLabel.padding = Insets(5.0, 5.0, 5.0, 1.0)
            deviceComboBox = ComboBox<String>()
            deviceComboBox.setPrefSize(186.0, 120.0)
            //deviceComboBox.items = deviceNames
            //deviceComboBox.selectionModel.selectFirst()
            deviceComboBox.valueProperty().addListener { _, _, new ->
                if (new != null && new == "+ Roku Device IP Address") {
                    deviceTextField.isVisible = true
                    startButton.isDisable = true
                } else {
                    deviceTextField.isVisible = false
                    startButton.isDisable = false
                }
            }

            deviceVbox += deviceLabel
            deviceVbox += deviceComboBox

            val deviceTextVbox = HBox()
            deviceTextVbox.padding = Insets(5.0, 0.0, 5.0, 0.0)
            deviceTextVbox.alignment = Pos.BASELINE_RIGHT
            deviceTextField = TextField()
            deviceTextField.promptText = "192.168.1.1"
            deviceTextField.isFocusTraversable = false
            deviceTextField.isVisible = false
            deviceTextField.textProperty().addListener { _, _, new ->
                startButton.isDisable = new.isEmpty()
            }
            /*if (devices.isEmpty()) {
                deviceTextField.isVisible = true
            }*/

            deviceTextVbox += deviceTextField

            val statusVbox = HBox()
            statusLabel = Label()
            statusLabel.textProperty().bind(taskStatus.title)
            statusLabel.padding = Insets(5.0, 5.0, 5.0, 1.0)
            runAsync(taskStatus) {
                updateTitle("Status: Not Connected")
            }
            statusVbox += statusLabel

            val buttonVbox = HBox()
            startButton = Button("Start").apply {
                tooltip("Starts the private listening session")
                action {
                    deviceComboBox.isDisable = true
					deviceTextField.isDisable = true
					startButton.isDisable = true
					stopButton.isDisable = false

                    runAsync(taskStatus) {
                        updateTitle("Status: Connecting")

                        val deviceIpAddress = if (deviceTextField.isVisible) {
                            deviceTextField.text
                        } else {
                            var ipAddress = ""

                            for (device in devices) {
                                if (device.userDeviceName == deviceComboBox.value ||
                                        device.modelName == deviceComboBox.value) {
                                    val url = URL(device.host)
                                    ipAddress = url.host
                                }
                            }

                            ipAddress
                        }

                        PrivateListeningSession.connect(deviceIpAddress, object : ConnectionListener {

                            override fun onConnected(newSession: PrivateListeningSession) {
                                session = newSession
                                runAsync(taskStatus) {
                                    updateTitle("Status: Connected")
                                }
                            }

                            override fun onFailure(error: Throwable) {
                                PrivateListeningSession.disconnect(session)
                                session = null

                                runAsync(taskStatus) {
                                    updateTitle("Status: Not Connected")
                                }
                                deviceTextField.isDisable = false
                                startButton.isDisable = false
                                stopButton.isDisable = true
                            }
                        })
                    }
                }
            }
            startButton.isDisable = devices.isEmpty()
            stopButton = Button("Stop").apply {
                tooltip("Stops the private listening session")
                action {
                    deviceComboBox.isDisable = false
                    deviceTextField.isDisable = false
                    startButton.isDisable = false
                    stopButton.isDisable = true

                    runAsync(taskStatus) {
                        updateTitle("Status: Not Connected")

                        PrivateListeningSession.disconnect(session)
                        session = null
                    }
                }
            }
            stopButton.isDisable = true
            buttonVbox += startButton
            buttonVbox += stopButton

            this += deviceVbox
            this += deviceTextVbox
            this += statusVbox
            this += buttonVbox
        }

		CoroutineScope(Dispatchers.IO).launch {
			discoverDevices()
		}
    }

    private fun discoverDevices() {
        try {
            devices = DeviceRequests.discoverDevices()
        } catch (ex: SocketTimeoutException) {
        }

        val deviceNames = FXCollections.observableArrayList<String>()
        deviceNames.add(0, "+ Roku Device IP Address")

		for (device in devices) {
			val supportsPrivateListening = device.supportsPrivateListening?.toBoolean() ?: false

			if (supportsPrivateListening) {
				if (device.userDeviceName.isNullOrEmpty()) {
					deviceNames.add(0, device.modelName)
				} else {
					deviceNames.add(0, device.userDeviceName)
				}
			}
		}

		deviceComboBox.items = deviceNames
        Platform.runLater(Runnable {
            deviceComboBox.selectionModel.selectFirst()
        })

		if (devices.isEmpty()) {
			deviceTextField.isVisible = true
		}
    }

    override fun onDock() {
        currentWindow?.setOnCloseRequest {
            session?.let {
                PrivateListeningSession.disconnect(it)
            }
        }
    }
}