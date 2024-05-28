package com.example.diplomapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.InputStream


class DBHelper(private val context: Context, private val factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, "disease", factory, 1){
        private var isFilled=false
    override fun onCreate(db: SQLiteDatabase?) {
        val query = "CREATE TABLE diseases (id INT PRIMARY KEY, plantname TEXT, diseasename TEXT, caretips TEXT, photo TEXT)"
        db!!.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS diseases")
        onCreate(db)

    }
    private fun fillDatabase(){
        if (!isFilled){
            val diseases = readCsv()
            for (dis in diseases){
                addDisease(dis)
            }
            isFilled=true
        }
    }

    private fun addDisease(disease: Disease){
        val values = ContentValues()
        values.put("id", disease.id)
        values.put("plantname", disease.plant)
        values.put("diseasename", disease.disease)
        values.put("caretips", disease.care)
        values.put("photo", disease.photo)
        val db = this.writableDatabase
        db.insert("diseases", null, values)
        db.close()
    }
    fun getDiseaseForId(id : Int): Disease {
        fillDatabase()
        val db = this.readableDatabase
        var ans:Disease= Disease(null,null, null, null, null)
        val result = db.rawQuery("SELECT * FROM diseases WHERE id ='$id'", null)
        if( result.moveToFirst()){
            val r=result.columnNames
            val idId=result.getColumnIndexOrThrow(r[0])
            val plantNameId=result.getColumnIndexOrThrow(r[1])
            val diseaseNameId=result.getColumnIndexOrThrow(r[2])
            val careTipsId=result.getColumnIndexOrThrow(r[3])
            val photoId=result.getColumnIndexOrThrow(r[4])
            val id = result.getInt(idId)
            val plantName = result.getString(plantNameId)
            val diseaseName = result.getString(diseaseNameId)
            val careTips = result.getString(careTipsId)
            val photo = result.getString(photoId)
            ans = Disease(id, plantName, diseaseName, careTips, photo)
        }
        db.close()
        return ans
    }
    private fun readCsv(): MutableList<Disease> {
        val ans: MutableList<Disease> = mutableListOf()
        try {
            val csvFilePath = "diseases.txt"
            val stream: InputStream =context.assets.open(csvFilePath)
            val lines = mutableListOf<String>()
            stream.bufferedReader().forEachLine { lines.add(it) }
            for (line in lines) {
                val stringSep=line.split(";")
                val dis = Disease(stringSep[0].toInt(), stringSep[1], stringSep[2], stringSep[3], stringSep[4])
                ans.add(dis)
            }
        } catch (e:Exception){
            e.printStackTrace()
        }
        return ans
    }

}
