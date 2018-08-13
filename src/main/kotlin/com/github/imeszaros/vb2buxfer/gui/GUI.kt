package com.github.imeszaros.vb2buxfer.gui

import com.github.imeszaros.buxfer.Buxfer.Companion.login
import com.github.imeszaros.buxfer.BuxferException
import com.github.imeszaros.vb2buxfer.Configuration
import com.github.imeszaros.vb2buxfer.Configuration.Buxfer.password
import com.github.imeszaros.vb2buxfer.Configuration.Buxfer.username
import org.eclipse.swt.dnd.Clipboard
import org.eclipse.swt.dnd.HTMLTransfer
import org.eclipse.swt.graphics.Color
import org.eclipse.swt.graphics.Image
import org.eclipse.swt.graphics.ImageData
import org.eclipse.swt.graphics.RGB
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Shell
import kotlin.concurrent.thread

class GUI(private val config: Configuration) {

    val display = Display.getDefault()!!

    private val clipboard = Clipboard(display)
    private val images = mutableMapOf<String, Image>()
    private val colors = mutableMapOf<RGB, Color>()

    fun loop() {
        val hiddenShell = Shell(display)

        try {
            ProgressDialog.whileReturns(this, hiddenShell, "Logging in…") {
                login(config[username], config[password])
            }
        } catch (e: BuxferException) {
            hiddenShell.showError("Unable to log-in to Buxfer. Please check the configuration.")
            null
        } finally {
            hiddenShell.dispose()
        }?.let {
            val appWindow = AppWindow(it, config, this)

            appWindow.shell.open()

            while (!appWindow.shell.isDisposed) {
                if (!display.readAndDispatch()) {
                    display.sleep()
                }
            }
        }

        images.values.forEach(Image::dispose)
        colors.values.forEach(Color::dispose)

        clipboard.dispose()
        display.dispose()
    }

    fun image(path: String): Image = images.computeIfAbsent(path) {
        p -> Image(display, ImageData(GUI::class.java.getResourceAsStream(p)))
    }

    fun color(rgb: RGB): Color = colors.computeIfAbsent(rgb) { o -> Color(display, o.red, o.green, o.blue )}

    fun getClipboardText() = clipboard.getContents(HTMLTransfer.getInstance()) as String? ?: ""

    companion object {

        fun init(config: Configuration) = thread(name = "User Interface Thread") {
            Display.setAppName("Volksbank -> Buxfer")
            Display.setAppVersion("1.0")
            GUI(config).loop()
        }
    }
}