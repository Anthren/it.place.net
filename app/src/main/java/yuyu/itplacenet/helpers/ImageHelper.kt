package yuyu.itplacenet.helpers

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.util.Base64
import android.widget.ImageView
import java.io.*
import yuyu.itplacenet.BuildConfig
import android.graphics.Bitmap


class ImageHelper(private val context: Context) {

    private val publicStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

    private val imagePrefix = "JPEG_"
    private val imageSuffix = ".jpg"
    private val imageMimeType = "image/jpeg"

    /* Интенты */

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

    // Создаем файл фотографии и добавляем его в галерею
    fun createImageFile() : Uri {
        val imageFile = this.createCameraPhotoFile()
        val imageUri = this.getFileUri(imageFile)
        this.addImageToGallery(imageUri)
        return imageUri
    }

    /* Файлы изображений */

    private fun getPrivateStoragePath() : File {
        return when {
            Environment.isExternalStorageEmulated() -> context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            else -> File(context.filesDir.canonicalPath+"/photos/")
        }
    }

    // Создаем файл
    private fun createFile( path: File, fileName: String? = null ) : File {
        if( ! path.exists() ) path.mkdirs()

        val file: File
        if( fileName != null ) {
            file = File(path, fileName)
            file.createNewFile()
        } else {
            val imageFileName = imagePrefix + DateHelper(context).getDateFormat("yyyyMMdd_HHmmss") + "_"
            file = File.createTempFile( imageFileName, imageSuffix, path )
        }
        return file
    }

    // Создаем файл фотографии
    @Throws(IOException::class)
    private fun createCameraPhotoFile(): File {
        val image = this.createFile( publicStorageDir )

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DATE_TAKEN, DateHelper(context).getTimeInMillis())
            put(MediaStore.Images.Media.MIME_TYPE, imageMimeType)
            put(MediaStore.MediaColumns.DATA, image.absolutePath)
        }
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        return image
    }

    // Получаем URI файла
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

    // Проверка существования файла
    private fun isFileExist( path: File, fileName: String ) : Boolean {
        val file = File(path.toString(), fileName)
        if( file.exists() ) {
            return true
        }
        return false
    }

    // Сохранение Bitmap в файл
    private fun saveBitmapToFile( bitmap: Bitmap, path: File, fileName: String? = null ) : File {
        val file = this.createFile( path, fileName )

        try {
            val fOut = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
            fOut.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return file
    }

    // Проверка существования изображения во внутренней папке приложения
    fun isInternalImageExist( fileName: String ) : Boolean {
        return this.isFileExist(this.getPrivateStoragePath(), fileName + imageSuffix)
    }

    // Сохранение изображения во внутренней папке приложения
    fun saveInternalImage( bitmap: Bitmap, fileName: String ) : String {
        return this.saveBitmapToFile(bitmap, this.getPrivateStoragePath(), fileName + imageSuffix).path
    }

    /* Загрузка изображения из файлов */

    // Загрузка из ресурсов
    fun loadFromRes(id: Int) : Bitmap {
        return BitmapFactory.decodeResource(context.resources, id)
    }

    // Чтение изображения из файла по пути
    private fun loadBitmapFromPath( path: String ) : Bitmap {
        return BitmapFactory.decodeFile(path)
    }

    // Чтение изображения из файла по имени
    fun loadBitmapFromName( name: String ) : Bitmap {
        return this.loadBitmapFromPath( this.getPrivateStoragePath().toString() + "/" + name + imageSuffix )
    }

    /* Загрузка картинки из приложений */

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

    /* Изменение размера */

    // Сжимаем картинку под размер поля
    fun scale(imageView: ImageView, imageUri: Uri?, imageBitmap: Bitmap? = null) : Bitmap? {
        var photoBitmap: Bitmap? = null

        if( imageUri != null ) {
            photoBitmap = scaleImage(imageUri, imageView.width, imageView.height)
        } else if( imageBitmap != null ) {
            photoBitmap = scaleBitmap(imageBitmap, imageView.width, imageView.height)
        }

        return photoBitmap
    }

    private fun scaleImage(imageUri: Uri, targetW: Int, targetH: Int) : Bitmap {
        val bmOptions = BitmapFactory.Options()

        val decodeImageStream = context.contentResolver.openInputStream(imageUri)
        bmOptions.inJustDecodeBounds = true
        BitmapFactory.decodeStream(decodeImageStream, null, bmOptions)
        decodeImageStream.close()

        val photoW = bmOptions.outWidth
        val photoH = bmOptions.outHeight
        val scaleFactor = calculateScaleFactor(photoW, photoH, targetW, targetH)

        val resultImageSteam = context.contentResolver.openInputStream(imageUri)
        bmOptions.inJustDecodeBounds = false
        bmOptions.inSampleSize = scaleFactor
        val imageBitmap = BitmapFactory.decodeStream(resultImageSteam, null, bmOptions)
        resultImageSteam.close()

        return imageBitmap
    }

    fun scaleBitmap(imageBitmap: Bitmap, targetW: Int, targetH: Int) : Bitmap {
        return Bitmap.createScaledBitmap(imageBitmap, targetW, targetH, true)
    }

    private fun calculateScaleFactor(photoW: Int, photoH: Int, targetW: Int, targetH: Int) : Int {
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
    fun blurImage(imageBitmap: Bitmap) : Bitmap {
        return BlurBuilder(context).blur(imageBitmap)
    }

    /* Кодировка */

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

    /* Рисование на канве */

    // Склейка изображений
    fun glue(bmp1: Bitmap, bmp1Options: Map<String,Int>?, bmp2: Bitmap, bmp2Options: Map<String,Int>?) : Bitmap {
        val newWidth = bmp1.width + bmp2.width +
                       ( bmp1Options?.get("margin_left") ?: 0 ) + ( bmp1Options?.get("margin_right") ?: 0 ) +
                       ( bmp2Options?.get("margin_left") ?: 0 ) + ( bmp2Options?.get("margin_right") ?: 0 )

        val newHeight = Math.max(
                bmp1.height + ( bmp1Options?.get("margin_top") ?: 0 ) + ( bmp1Options?.get("margin_bottom") ?: 0 ) ,
                bmp2.height + ( bmp2Options?.get("margin_top") ?: 0 ) + ( bmp2Options?.get("margin_bottom") ?: 0 )
        )

        val posX1 = bmp1Options?.get("margin_left") ?: 0
        val posY1 = bmp1Options?.get("margin_top") ?: 0
        val posX2 = bmp1.width + ( bmp1Options?.get("margin_right") ?: 0 ) + ( bmp2Options?.get("margin_left") ?: 0 )
        val posY2 = bmp2Options?.get("margin_top") ?: 0

        val canvasBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(canvasBitmap)
        canvas.drawBitmap(bmp1, posX1.toFloat(), posY1.toFloat(), Paint())
        canvas.drawBitmap(bmp2, posX2.toFloat(), posY2.toFloat(), Paint())
        return canvasBitmap
    }

    // Вписать в круг
    fun fitIntoCircleWithBoard(sourceBitmap: Bitmap, size: Int, borderWidth: Int, borderColor: Int = Color.WHITE) : Bitmap {
        val fullSize = size + borderWidth * 2
        val halfSize = fullSize.toFloat() / 2f
        val borderW = borderWidth.toFloat()
        val radius = halfSize - borderW / 2

        val scaledBitmap = if( sourceBitmap.width > size || sourceBitmap.height > size ) {
            this.scaleBitmap(sourceBitmap, fullSize, fullSize)
        } else {
            sourceBitmap
        }

        val canvasBitmap = Bitmap.createBitmap(fullSize, fullSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(canvasBitmap)

        val photoPaint = Paint()
            photoPaint.shader = BitmapShader(scaledBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
            photoPaint.isAntiAlias = true
        canvas.drawCircle(halfSize, halfSize, radius, photoPaint)
        scaledBitmap.recycle()

        if (borderWidth > 0) {
            val borderPaint = Paint()
                borderPaint.style = Paint.Style.STROKE
                borderPaint.strokeWidth = borderW
                borderPaint.color = borderColor
                borderPaint.isAntiAlias = true
            canvas.drawCircle(halfSize, halfSize, radius, borderPaint)
        }

        return canvasBitmap
    }

}