package yuyu.itplacenet.helpers

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.util.Base64
import android.widget.ImageView
import java.text.SimpleDateFormat
import java.util.*
import java.io.*
import yuyu.itplacenet.BuildConfig
import yuyu.itplacenet.utils.BlurBuilder


class ImageHelper(private val context: Context) {

    // Создаем интент галереи
    fun createGalleryIntent() : Intent {
        val photoPickerIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        photoPickerIntent.type = "image/*"
        return photoPickerIntent
    }

    // Создаем интент камеры
    fun createCameraIntent() : Intent {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        return cameraIntent
    }

    // Создаем файл фотографии с камеры
    fun createImageFile() : Uri {
        val imageFile = this.createTempImageFile()
        val imageUri = this.getFileUri(imageFile)
        this.addImageToGallery(imageUri)
        return imageUri
    }

    // Создаем временный файл
    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createTempImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile( imageFileName,".jpg", storageDir )
        val currentPhotoPath = image.absolutePath

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.DATA, currentPhotoPath)
        }
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        return image
    }

    // получаем URI файла
    private fun getFileUri(file: File) : Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID+".fileprovider", file)
        } else {
            Uri.fromFile(file)
        }
    }

    // Добавляем файл в галерею
    private fun addImageToGallery(imageUri: Uri) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        mediaScanIntent.data = imageUri
        context.sendBroadcast(mediaScanIntent)
    }

    // Загружаем картинку из галереи
    fun loadPhotoFromGallery(intent: Intent) : Uri {
        return intent.data
    }

    // Загружаем миниатюру с камеры
    fun loadSmallCameraPhoto(intent: Intent) : Bitmap {
        return intent.getParcelableExtra("data")
    }

    // Загружаем фото с камеры
    fun loadPhotoFromCamera(photoUri: String) : Uri {
        return Uri.parse(photoUri)
    }

    // Сжимаем картинку под размер поля

    fun scale(imageView: ImageView, imageUri: Uri?, imageBitmap: Bitmap? = null) : Bitmap? {
        var photoBitmap: Bitmap? = null

        if( imageUri != null ) {
            photoBitmap = scaleImage(imageView, imageUri)
        } else if( imageBitmap != null ) {
            photoBitmap = scaleBitmap(imageView, imageBitmap)
        }

        return photoBitmap
    }

    fun scaleImage(imageView: ImageView, imageUri: Uri) : Bitmap {
        val bmOptions = BitmapFactory.Options()

        val decodeImageStream = context.contentResolver.openInputStream(imageUri)
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeStream(decodeImageStream, null, bmOptions)
        decodeImageStream.close()

        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight
        val targetW = imageView.width
        val targetH = imageView.height
        val scaleFactor = calculateScaleFactor(photoW, photoH, targetW, targetH)

        val resultImageSteam = context.contentResolver.openInputStream(imageUri)
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        val imageBitmap = BitmapFactory.decodeStream(resultImageSteam, null, bmOptions)
        resultImageSteam.close()

        return imageBitmap
    }

    fun scaleBitmap(imageView: ImageView, imageBitmap: Bitmap) : Bitmap {
        val targetW = imageView.width
        val targetH = imageView.height
        return Bitmap.createScaledBitmap(imageBitmap, targetW, targetH, false)
    }

    private fun calculateScaleFactor(photoW: Int, photoH: Int, targetW: Int, targetH: Int ) : Int {
        var scaleFactor = 1
        if (photoH > targetH || photoW > targetW) {
            scaleFactor = Math.round(
                            Math.min(
                                photoH.toFloat() / targetH.toFloat(),
                                photoW.toFloat() / targetW.toFloat()
                            )
            )
        }
        return scaleFactor
    }

    // Размытие
    fun blurImage( imageBitmap: Bitmap ) : Bitmap {
        return BlurBuilder(context).blur(imageBitmap)
    }

    // Кодировка
    fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun base64ToBitmap(b64: String): Bitmap {
        val imageAsBytes = Base64.decode(b64.toByteArray(), Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.size)
    }

}