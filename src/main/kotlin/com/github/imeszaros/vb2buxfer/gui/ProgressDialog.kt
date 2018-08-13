package com.github.imeszaros.vb2buxfer.gui

import org.eclipse.swt.SWT
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.Display
import org.eclipse.swt.widgets.Label
import org.eclipse.swt.widgets.ProgressBar
import org.eclipse.swt.widgets.Shell
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors

class ProgressDialog(gui: GUI, parent: Shell, message: String) {

    val shell = Shell(parent,SWT.TOOL or SWT.APPLICATION_MODAL).apply {

        images = arrayOf(
                gui.image("/images/icon-16.png"),
                gui.image("/images/icon-32.png"),
                gui.image("/images/icon-48.png"),
                gui.image("/images/icon-64.png"),
                gui.image("/images/icon-128.png"),
                gui.image("/images/icon-256.png"))

        text = Display.getAppName()

        layout = GridLayout().apply {
            marginWidth = 10
            marginHeight = 10
        }

        Label(this, SWT.NONE).apply {
            text = message
        }

        ProgressBar(this, SWT.INDETERMINATE).apply {
            gridData(minimumWidth = 200)
        }

        pack()
        center()
    }

    companion object {

        private val executorService = Executors.newCachedThreadPool { r ->
            Executors.defaultThreadFactory().newThread(r).apply {
                isDaemon = true
            }
        }

        fun <T> whileReturns(gui: GUI, parent: Shell, message: String, supplier: () -> T): T {
            val dialog = ProgressDialog(gui, parent, message)
            val future = executorService.submit(supplier::invoke)

            with (dialog.shell) {
                open()
                while (!future.isDone) {
                    if (!display.readAndDispatch()) {
                        display.sleep()
                    }
                }
                close()

                try {
                    return future.get()
                } catch (e: ExecutionException) {
                    throw e.cause ?: e
                }
            }
        }

        fun whileExecutes(gui: GUI, parent: Shell, message: String, runnable: () -> Unit) {
            whileReturns(gui, parent, message) {
                runnable.invoke()
            }
        }
    }
}