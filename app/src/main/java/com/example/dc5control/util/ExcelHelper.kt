package com.example.dc5control.util

import android.content.Context
import android.net.Uri
import com.example.dc5control.data.model.Employee
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream

object ExcelHelper {
    fun readWorkersFromExcel(context: Context, uri: Uri): List<Employee> {
        val workers = mutableListOf<Employee>()
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        
        inputStream?.use { isStream ->
            val workbook = WorkbookFactory.create(isStream)
            val sheet = workbook.getSheetAt(0)
            
            // Assuming row 0 is header
            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue
                val name = row.getCell(0)?.toString() ?: ""
                val curp = row.getCell(1)?.toString() ?: ""
                val occupation = row.getCell(2)?.toString() ?: ""
                val position = row.getCell(3)?.toString() ?: ""
                
                if (name.isNotEmpty()) {
                    workers.add(Employee(name = name, curp = curp, occupation = occupation, position = position))
                }
            }
        }
        return workers
    }
}
