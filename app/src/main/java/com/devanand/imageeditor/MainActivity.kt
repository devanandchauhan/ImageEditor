package com.devanand.imageeditor

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private val REQUEST_PERMISSION = 100
    private val CAMERA_REQUEST_CODE = 1
    private val GALLERY_REQUEST_CODE = 2

    lateinit var currentPhotoPath: String
    private lateinit var photoFile:File
    private val FILE_NAME = "photo.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btCapturePhoto.setOnClickListener {
            openCamera()
        }
        btOpenGallery.setOnClickListener {
            openGallery()
        }
    }

    override fun onResume() {
        super.onResume()
        checkCameraPermission()
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_PERMISSION)
        }
    }

    private fun openCamera() {
        /*Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            intent.resolveActivity(packageManager)?.also {
                /*val photoFile: File? = try {
                    createCapturedPhoto()
                }catch (ex:IOException){
                    null
                }

                photoFile?.also {
                    val photoURI = FileProvider.getUriForFile(this,"com.devanand.imageditor.fileprovider",it)
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,photoURI)
                    @Suppress("DEPRECATION")
                    startActivityForResult(intent, CAMERA_REQUEST_CODE)
                }*/

                @Suppress("DEPRECATION")
                startActivityForResult(intent, CAMERA_REQUEST_CODE)

            }
           }*/

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile =getPhotoFile(FILE_NAME)

        //takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoFile)
        val fileProvider = FileProvider.getUriForFile(this,"com.devanand.imageditor.fileprovider",photoFile)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,fileProvider)
        if(takePictureIntent.resolveActivity(this.packageManager)!= null){
            @Suppress("DEPRECATION")
            startActivityForResult(takePictureIntent,CAMERA_REQUEST_CODE)
        }else{
            Toast.makeText(this,"Unable to open",Toast.LENGTH_SHORT).show()
        }

    }

    private fun getPhotoFile(fileName: String): File {
        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(fileName,".jpg",storageDirectory)
    }

    private fun openGallery() {
        Intent(Intent.ACTION_GET_CONTENT).also { intent ->
            intent.type = "image/*"
            intent.resolveActivity(packageManager)?.also {
                @Suppress("DEPRECATION")
                startActivityForResult(intent, GALLERY_REQUEST_CODE)
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when(requestCode){
            CAMERA_REQUEST_CODE ->{
                if(resultCode == RESULT_OK ){
                    //val bitmap = data?.extras?.get("data") as Bitmap
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    //ivImage.setImageBitmap(bitmap)


                    //intentFun(bitmap)
                    //ivImage.setImageBitmap(bitmap)
                    val editIntent = Intent(this@MainActivity, EditorActivity::class.java)
                    editIntent.putExtra("image", bitmap)
                    overridePendingTransition(0,0)
                    startActivity(editIntent)

                    //val uriImage = getImageUri(applicationContext,bitmap)
                    //val uri = Uri.parse(currentPhotoPath)

                    //launchImageCrop(uriImage)

                }else{
                    Log.e("MAINACTIVITY","Camera Image selection Error: Couldn't select that image from memory")
                }
            }

            GALLERY_REQUEST_CODE ->{
                if(resultCode == RESULT_OK){
                    data?.data?.let { uri ->
                        launchImageCrop(uri)
                    }
                }else{
                    Log.e("MAINACTIVITY","Image selection Error: Couldn't select that image from memory")
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE ->{
                val result = CropImage.getActivityResult(data)
                if(resultCode == Activity.RESULT_OK){
                    result.uri?.let {
                        setImage(result.uri)
                    }

                }else if(resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                    Log.e("MAINACTIVITY","Crop Error: ${result.error}")
                }
            }
        }

    }

    private fun intentFun(bitmap: Bitmap?) {
        overridePendingTransition(0,0)
        val editIntent = Intent(this@MainActivity, EditorActivity::class.java)
        editIntent.putExtra("image", bitmap)
        overridePendingTransition(0,0)
        startActivity(editIntent)
    }

    private fun setImage(uri: Uri?) {
        Glide.with(this)
            .load(uri)
            .into(ivImage)
    }

    private fun launchImageCrop(uri:Uri){
        CropImage.activity(uri)
            .setActivityTitle("Edit Screen")
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1920,1080)
            .setCropShape(CropImageView.CropShape.RECTANGLE)
            .start(this)


    }

    /*private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null)
        return Uri.parse(path)
    }

    @Throws(IOException::class)
    private fun createCapturedPhoto(): File {
        val timestamp: String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("PHOTO_${timestamp}",".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }*/
}