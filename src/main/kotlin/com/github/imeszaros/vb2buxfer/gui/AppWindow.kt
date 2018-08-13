package com.github.imeszaros.vb2buxfer.gui

import com.github.imeszaros.buxfer.Buxfer
import com.github.imeszaros.buxfer.BuxferResponse
import com.github.imeszaros.buxfer.Transaction
import com.github.imeszaros.vb2buxfer.Configuration
import com.github.imeszaros.vb2buxfer.Configuration.Settings.account
import com.github.imeszaros.vb2buxfer.Configuration.Settings.tagMappings
import com.github.imeszaros.vb2buxfer.TagMapper
import com.github.imeszaros.vb2buxfer.TransactionList
import org.eclipse.swt.SWT
import org.eclipse.swt.graphics.RGB
import org.eclipse.swt.layout.GridLayout
import org.eclipse.swt.widgets.*

class AppWindow(
        private val buxfer: Buxfer,
        private val config: Configuration,
        private val gui: GUI) {

    private val pendingColor = gui.color(RGB(210, 163, 0))
    private val successColor = gui.color(RGB(0, 128, 0))
    private val failureColor = gui.color(RGB(255, 0, 0))

    private val tagMapper = TagMapper(config[tagMappings])

    private lateinit var table: Table
    private lateinit var accountsCombo: Combo

    private var clipboardText: String = ""

    val shell = Shell(gui.display,SWT.SHELL_TRIM or SWT.RESIZE).apply {

        images = arrayOf(
                gui.image("/images/icon-16.png"),
                gui.image("/images/icon-32.png"),
                gui.image("/images/icon-48.png"),
                gui.image("/images/icon-64.png"),
                gui.image("/images/icon-128.png"),
                gui.image("/images/icon-256.png"))

        text = Display.getAppName()

        layout = GridLayout().apply {
            numColumns = 3
            marginWidth = 10
            marginHeight = 10
        }

        table = Table(this, SWT.FULL_SELECTION or SWT.BORDER or SWT.CHECK).apply tabFolder@ {
            layoutData = gridData(
                    horizontalAlignment = SWT.FILL,
                    verticalAlignment =  SWT.FILL,
                    grabExcessHorizontalSpace = true,
                    grabExcessVerticalSpace = true,
                    horizontalSpan = 3,
                    minimumHeight = 500)

            headerVisible = true

            TableColumn(this, SWT.NONE).apply {
                text = "Upload"
                width = 60
            }

            TableColumn(this, SWT.NONE).apply {
                text = "Date"
                width = 100
            }

            TableColumn(this, SWT.NONE).apply {
                text = "Description"
                width = 400
            }

            TableColumn(this, SWT.NONE).apply {
                text = "Amount"
                width = 100
                alignment = SWT.RIGHT
            }

            TableColumn(this, SWT.NONE).apply {
                text = "Tags"
                width = 300
            }

            TableColumn(this, SWT.NONE).apply {
                text = "Result"
                width = 100
            }
        }

        Label(this, SWT.NONE).apply {
            text = "Select target account:"
        }

        accountsCombo = Combo(this, SWT.READ_ONLY).apply {
            addListener(SWT.Selection) {
                config[account] = ((data as List<*>)[selectionIndex] as BuxferResponse.Account).name
                config.save()
            }
        }

        Button(this, SWT.PUSH).apply {
            layoutData = gridData(
                    widthHint = 80,
                    grabExcessHorizontalSpace = true,
                    horizontalAlignment = SWT.RIGHT)

            text = "Upload"

            addListener(SWT.Selection) { upload() }
        }

        pack()
        center()
    }

    init {
        timer()

        try {
            ProgressDialog.whileReturns(gui, shell, "Loading accounts…") {
                buxfer.accounts().accounts
            }?.let { accounts ->
                accountsCombo.data = accounts

                accounts.map { it.name }.forEach(accountsCombo::add)

                with(config[account]) {
                    accounts.find { it.name == this }
                            ?.let(accounts::indexOf)
                            ?.let(accountsCombo::select)
                }
            }
        } catch (e: RuntimeException) {
            shell.showError(e)
        }
    }

    private fun timer() {
        if (!shell.isDisposed) {
            checkClipBoard()
            shell.display.timerExec(1000, this::timer)
        }
    }

    private fun checkClipBoard() {
        val text = gui.getClipboardText()

        if (clipboardText != text) {
            clipboardText = text

            try {
                table.removeAll()

                TransactionList.parse(text, tagMapper).forEach {
                    TableItem(table, SWT.NONE).apply {
                        data = it
                        checked = true
                        setText(1, TransactionList.dateFormat.format(it.transactionDate))
                        setText(2, it.description)
                        setText(3, TransactionList.amountFormat.format(it.amount))
                        setText(4, it.tags.joinToString(", "))
                        setText(5, "Pending")
                        setForeground(5, pendingColor)
                    }
                }
            } catch (e: RuntimeException) {
                // ignore
            }
        }
    }

    private fun setState(transaction: TransactionList.TransactionData, state: Boolean, message: String? = null) {
        table.items.find { it.data === transaction }?.run {
            if (state) {
                setText(5, "Success")
                setForeground(5, successColor)
                checked = false
            } else {
                setText(5, message ?: "Failure")
                setForeground(5, failureColor)
            }
        }
    }

    private fun upload() {
        val account = (accountsCombo.data as List<*>)[accountsCombo.selectionIndex] as BuxferResponse.Account

        val transactions = table.items
                .filter { it.checked }
                .map { it.data as TransactionList.TransactionData }

        ProgressDialog.whileExecutes(gui, shell, "Uploading transactions…") {
            transactions.forEach {
                try {
                    if (it.amount < 0) {
                        buxfer.addTransaction(Transaction.Expense(
                                accountId = account.id,
                                date = it.transactionDate,
                                description = it.description,
                                amount = it.amount,
                                tags = it.tags))
                    } else {
                        buxfer.addTransaction(Transaction.Income(
                                accountId = account.id,
                                date = it.transactionDate,
                                description = it.description,
                                amount = it.amount,
                                tags = it.tags))
                    }

                    shell.display.asyncExec { setState(it, true) }
                } catch (e: RuntimeException) {
                    shell.display.asyncExec { setState(it, false, e.message) }
                }
            }
        }
    }
}