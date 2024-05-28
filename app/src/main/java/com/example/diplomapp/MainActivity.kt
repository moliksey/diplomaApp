package com.example.diplomapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

class MainActivity : AppCompatActivity() {
    private val CAMERA_PERMISSION_CODE = 100
    private val STORAGE_PERMISSION_CODE = 101
    private lateinit var imageProcessor: ImageProcessor
    private var processingResult: Int? = null
    //private val db: DBHelper = DBHelper(this, null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        lateinit var tflitemodel : ByteBuffer
        lateinit var tflite : Interpreter
        val takePhotoButton: ImageButton = findViewById(R.id.main_page_take_photo_button)
        val processingModSwitch: Switch = findViewById(R.id.processing_mod_switch)
        val imageProcType: ImageView =findViewById(R.id.image_proc_type)
        val loadPhotoButton: Button = findViewById(R.id.load_photo_button)
        takePhotoButton.setOnClickListener{
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED){
                this.openModel()
                takePicturePreview.launch(null)
            }else{
                checkPermission(android.Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE)
            }
        }
        loadPhotoButton.setOnClickListener{
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)==PackageManager.PERMISSION_GRANTED){
                val intent= Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                val mimeTypes= arrayOf("image/jpeg", "image/png", "image/jpg" )
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                intent.flags= Intent.FLAG_GRANT_READ_URI_PERMISSION
                this.openModel()
                onresult.launch(intent)
            }else{
                checkPermission(android.Manifest.permission.READ_MEDIA_IMAGES, STORAGE_PERMISSION_CODE)
            }
        }
        processingModSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                imageProcType.setImageResource(R.drawable.wifi_on_pic)
                Toast.makeText(applicationContext, "Онлайн обработка", Toast.LENGTH_SHORT).show()
            } else {
                imageProcType.setImageResource(R.drawable.wifi_off_pic)
                Toast.makeText(applicationContext, "Офлайн обработка", Toast.LENGTH_SHORT).show()
            }
        }

    }
    private fun openModel(){
        try{
            val assetManager = this.assets
            val model = loadModelFile(assetManager, "model.tflite")
            val interpreter = Interpreter(model)
            imageProcessor = ImageProcessor(interpreter)
        } catch(ex: Exception){
            ex.printStackTrace()
        }
    }
    private fun closeModel(){
        imageProcessor.closeModel()
    }
    private fun loadModelFile(assetManager: AssetManager, modelPath: String): ByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
    private val takePicturePreview = registerForActivityResult(ActivityResultContracts.TakePicturePreview()){bitmap->
        if(bitmap!=null){
            processingResult = imageProcessor.processImage(bitmap)
        }

    }
    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        } else {
            Toast.makeText(this@MainActivity, "Permission already granted", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private val  onresult= registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
        Log.i("TAG", "Result: ${result.data} ${result.resultCode}")
        onResultResived(STORAGE_PERMISSION_CODE, result)

    }
    private fun onResultResived(requestCode: Int, result: ActivityResult?){
        when(requestCode){
            STORAGE_PERMISSION_CODE->{
                if(result?.resultCode == Activity.RESULT_OK){
                    result.data?.data?.let{uri->
                        Log.i("TAG", "onResultReceived: $uri")
                        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri))
                        processingResult = imageProcessor.processImage(bitmap)
                        closeModel()
                        val intent1 = Intent(this, DiseaseView::class.java)
                        val res : Int= if (processingResult!=null) processingResult!! else -1
                        val data=readCsv(res)//db.getDiseaseForId(res)
                        if (data.id==null){
                            Toast.makeText(this, "Что-то пошло не так", Toast.LENGTH_SHORT).show()
                        }else{
                            intent1.putExtra("id" , data.id)
                            intent1.putExtra("plantName" , data.plant ?: "")
                            intent1.putExtra("diseaseName" , data.disease ?: "")
                            intent1.putExtra("careTips" , data.care ?: "")
                            val photoTxt1 = "picture"+data.photo!!.split("(")[1].split(")")[0]
                            val imageId = this.resources.getIdentifier(
                                photoTxt1,
                                "drawable",
                                this.packageName
                            )
                            intent1.putExtra("photo" , imageId ?: 0)}
                            startActivity(intent1)

                    }
                }else{
                    Log.e("TAG", "onActivityResult: error in selecting image")
                }
            }
        }
    }


    private fun readCsv(id: Int): Disease {
        var ans: Disease = Disease(null, null, null, null, null)
        if(id==-1) {
            return ans
        }
        try {
                val csvFilePath = "diseases.txt"
                val stream: InputStream =this.assets.open(csvFilePath)
                val lines = mutableListOf<String>()
                stream.bufferedReader().forEachLine { lines.add(it) }
                for (line in lines) {
                    val stringSep=line.split(";")
                    val dis = Disease(stringSep[0].toInt(), stringSep[1], stringSep[2], stringSep[3], stringSep[4])
                    if(dis.id == id){
                        ans= Disease(dis.id, dis.plant, dis.disease, dis.care, dis.photo)
                        break
                    }
                }
        } catch (e:Exception){
                e.printStackTrace()
        }

        return ans
    }
}