package com.pengxh.autodingding.utils

import android.util.Log
import com.blankj.utilcode.util.ToastUtils
import com.pengxh.autodingding.bean.HistoryRecordBean
import com.pengxh.autodingding.utils.SendMailUtil.sendAttachFileEmail
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.format.Alignment
import jxl.format.Border
import jxl.format.BorderLineStyle
import jxl.format.Colour
import jxl.write.*
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

object ExcelUtils {
    private const val TAG = "ExcelUtils"
    private var arial14format: WritableCellFormat? = null
    private var arial10format: WritableCellFormat? = null
    private var arial12format: WritableCellFormat? = null
    private const val UTF8_ENCODING = "UTF-8"

    /**
     * 初始化Excel
     *
     * @param fileName
     * @param colName
     */
    fun initExcel(fileName: String?, colName: Array<String?>) {
        format()
        var workbook: WritableWorkbook? = null
        try {
            val file = File(fileName)
            if (!file.exists()) {
                file.createNewFile()
            }
            workbook = Workbook.createWorkbook(file)
            val sheet = workbook.createSheet("打卡记录表", 0)
            //创建标题栏
            sheet.addCell(Label(0, 0, fileName, arial14format))
            for (col in colName.indices) {
                sheet.addCell(Label(col, 0, colName[col], arial10format))
            }
            sheet.setRowView(0, 340) //设置行高
            workbook.write()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (workbook != null) {
                try {
                    workbook.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * 单元格的格式设置 字体大小 颜色 对齐方式、背景颜色等...
     */
    private fun format() {
        try {
            val arial14font = WritableFont(WritableFont.ARIAL, 14, WritableFont.BOLD)
            arial14font.colour = Colour.LIGHT_BLUE
            arial14format = WritableCellFormat(arial14font)
            arial14format!!.alignment = Alignment.CENTRE
            arial14format!!.setBorder(Border.ALL, BorderLineStyle.THIN)
            arial14format!!.setBackground(Colour.VERY_LIGHT_YELLOW)
            arial10format =
                WritableCellFormat(WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD))
            arial10format!!.alignment = Alignment.CENTRE
            arial10format!!.setBorder(Border.ALL, BorderLineStyle.THIN)
            arial10format!!.setBackground(Colour.GRAY_25)
            arial12format = WritableCellFormat(WritableFont(WritableFont.ARIAL, 10))
            arial10format!!.alignment = Alignment.CENTRE //对齐格式
            arial12format!!.setBorder(Border.ALL, BorderLineStyle.THIN) //设置边框
        } catch (e: WriteException) {
            e.printStackTrace()
        }
    }

    fun writeObjListToExcel(objList: List<HistoryRecordBean>?, fileName: String?) {
        if (objList != null && objList.size > 0) {
            var writebook: WritableWorkbook? = null
            var `in`: InputStream? = null
            try {
                val setEncode = WorkbookSettings()
                setEncode.encoding = UTF8_ENCODING
                `in` = FileInputStream(File(fileName))
                val workbook = Workbook.getWorkbook(`in`)
                writebook = Workbook.createWorkbook(File(fileName), workbook)
                val sheet = writebook.getSheet(0)
                for (j in objList.indices) {
                    val historyBean = objList[j]
                    val uuid = historyBean.uuid
                    val date = historyBean.date
                    val message = historyBean.message
                    //第一行留作表头
                    sheet.addCell(Label(0, j + 1, uuid, arial12format))
                    sheet.addCell(Label(1, j + 1, date, arial12format))
                    sheet.addCell(Label(2, j + 1, message, arial12format))
                    sheet.setRowView(j + 1, 350) //设置行高
                }
                writebook.write()
                Log.d(TAG, "writeObjListToExcel: 导出表格到本地成功")
                //然后发送邮件到指定邮箱
                val emailAddress = Utils.readEmailAddress()
                if (emailAddress == "") {
                    ToastUtils.showLong("邮箱未填写，无法导出")
                    return
                }
                sendAttachFileEmail(emailAddress, fileName)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (writebook != null) {
                    try {
                        writebook.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (`in` != null) {
                    try {
                        `in`.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}