package com.github.imeszaros.vb2buxfer

import org.jsoup.Jsoup
import org.jsoup.select.Elements
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TransactionList(private val data: List<TransactionData>) : Iterable<TransactionList.TransactionData> {

    override fun iterator() = data.iterator()

    data class TransactionData(
            val accountingDate: LocalDate,
            val transactionDate: LocalDate,
            val description: String,
            val amount: Double,
            val tags: MutableList<String>)

    companion object {

        val dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy")!!

        val amountFormat = DecimalFormat("###,###.00", DecimalFormatSymbols().apply {
            decimalSeparator = ','
            groupingSeparator = '.'
        })

        fun parse(text: String, tagMapper: TagMapper, extendedDescriptions: List<String>): TransactionList {
            val doc = Jsoup.parseBodyFragment(text)
            val table = doc.select("table")[0]

            return table.select("tr").map { tr ->
                tr.select("td").let <Elements, TransactionData> { tds ->
                    val transactionDate = LocalDate.parse(tds[0].textNodes()[0].text(), dateFormat)!!
                    val accountingDate = LocalDate.parse(tds[0].textNodes()[1].text(), dateFormat)!!
                    val description = tds[1].select("a")[0].textNodes().let {
                        val firstLine = it[1].text()!!
                        if (extendedDescriptions.contains(firstLine) && it.size > 2) {
                            firstLine + " / " + it[2].text()!!
                        } else {
                            firstLine
                        }
                    }
                    val amount = tds[2].text().split("\\s".toRegex())[0].let(amountFormat::parse).toDouble()
                    val tags = tagMapper.map(description)
                    TransactionData(accountingDate, transactionDate, description, amount, tags)
                }
            }.toList().let(::TransactionList)
        }
    }
}