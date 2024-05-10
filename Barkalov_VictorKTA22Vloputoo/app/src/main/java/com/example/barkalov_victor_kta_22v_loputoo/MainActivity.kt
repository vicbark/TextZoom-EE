package com.example.barkalov_victor_kta_22v_loputoo

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.barkalov_victor_kta_22v_loputoo.databinding.ActivityMainBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.*


@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private val bvRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private lateinit var bvBinding: ActivityMainBinding
    private var bvImageBitmap: Bitmap? = null
    private var bvIsTextToSpeechEnabled = false
    private lateinit var bvTextToSpeech: TextToSpeech
    private val bvRequestImageCapture = 1
    private val bvRequestImagePermission = 2

    // Переменная для получения изображения из галереи
    // Muutuja pildi saamiseks galeriist
    private val bvTakePicture = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val bvData: Intent? = result.data
            val bvImageUri = bvData?.data
            bvImageUri?.let { uri ->
                val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                bvBinding.bvImageView.setImageBitmap(bitmap)
                bvImageBitmap = bitmap
                bvProcessImage()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.apply {
            statusBarColor = Color.BLACK // цвет statusbar
            // statusbari värv
        }
        bvBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        bvSetupTextToSpeech()
        bvSetupButtons()

        // Настройка кнопки выхода из приложения
        // Rakendusest väljumise nupu seadistamine
        val bvExitBtn: Button = bvBinding.bvExitButton
        bvExitBtn.setOnClickListener{
            finishAffinity()
        }

        // Получение ссылок на элементы интерфейса
        // Liidese elementidele viidete hankimine
        val bvTextBackground: TextView = bvBinding.bvExtractedText
        val bvTextBackgroundBtn: ImageButton = bvBinding.bvBacgroudBtn
        val bvButtonPlus: ImageButton = bvBinding.bvButtonPlus
        val bvButtonMinus: ImageButton = bvBinding.bvButtonMinus

        // Настройка кнопок увеличения и уменьшения размера текста
        // Teksti suuruse suurendamise ja vähendamise nuppude seadistamine
        bvButtonPlus.setOnClickListener {
            bvMakeTextSizeBigger()
        }
        bvButtonMinus.setOnClickListener {
            bvMakeTextSizeSmaller()
        }

        // Переключение цвета фона TextView
        // TextView taustavärvi vahetamine
        var bvIsWhiteBackground = false
        bvTextBackgroundBtn.setOnClickListener {
            if (bvIsWhiteBackground) {
                bvTextBackground.setBackgroundColor(Color.WHITE)
                bvTextBackground.setTextColor(Color.BLACK)
            } else {
                bvTextBackground.setBackgroundColor(Color.BLACK)
                bvTextBackground.setTextColor(Color.WHITE)
            }
            bvIsWhiteBackground = !bvIsWhiteBackground
        }
    }

    // Увеличение размера текста
    // Teksti suuruse suurendamine
    private fun bvMakeTextSizeBigger() {
        val bvCurrentTextSize = bvBinding.bvExtractedText.textSize
        val bvScale = resources.displayMetrics.scaledDensity
        val bvSpToPx = bvCurrentTextSize / bvScale
        val bvNewTextSizePx = bvSpToPx + 2
        bvBinding.bvExtractedText.textSize = bvNewTextSizePx
    }

    // Уменьшение размера текста
    // Teksti suuruse vähendamine
    private fun bvMakeTextSizeSmaller() {
        val bvCurrentTextSize = bvBinding.bvExtractedText.textSize
        val bvScale = resources.displayMetrics.scaledDensity
        val bvSpToPx = bvCurrentTextSize / bvScale
        val bvNewTextSizePx = bvSpToPx - 2
        bvBinding.bvExtractedText.textSize = bvNewTextSizePx
    }

    // Настройка TextToSpeech
    // TextToSpeech seadistamine
    private fun bvSetupTextToSpeech() {
        bvTextToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Проверяем наличие пакета языков
                // Kontrollime keelepaketi olemasolu
                val bvSupportedLanguages = bvTextToSpeech.availableLanguages
                val bvEstonianLocale = Locale("et")
                val bvEstonianLanguageSupported = bvSupportedLanguages.any { it.language == bvEstonianLocale.language }
                val bvFinnishLocale = Locale("fi")
                val bvFinnishLanguageSupported = bvSupportedLanguages.any { it.language == bvFinnishLocale.language }

                // Установка языка для озвучивания
                // Keele valimine
                if (bvEstonianLanguageSupported) {
                    bvTextToSpeech.language = bvEstonianLocale
                }
                else if (bvFinnishLanguageSupported) {
                    bvTextToSpeech.language = bvFinnishLocale
                }
                else {
                    // Уведомление о том, что озвучивание будет на английском языке
                    Toast.makeText(this, "Häälitamine toimub inglise keeles.", Toast.LENGTH_SHORT)
                        .show()
                    bvTextToSpeech.language = Locale.ENGLISH
                }
            } else {
                Toast.makeText(this, "Teksti häälestamine ebaõnnestus", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    // Настройка кнопок интерфейса
    // Nuppude seadistamine
    private fun bvSetupButtons() {
        bvBinding.apply {
            bvGalleryBtn.setOnClickListener {
                bvTakeImageFromGallery()
                bvExtractedText.text = ""
                bvImageView.foreground = null
            }

            bvCameraBtn.setOnClickListener {
                bvTakeImageWithCamera()
                bvExtractedText.text = ""
                bvImageView.foreground = null
            }

            bvVoiceButton.setOnClickListener {
                bvToggleTextToSpeech()
            }
        }
    }

    // Получение изображения из галереи
    // Pildi saamine galeriist
    private fun bvTakeImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        bvTakePicture.launch(intent)
    }

    // Обработка изображения
    // Pildi töötlemine
    private fun bvProcessImage() {
        bvImageBitmap?.let { bitmap ->
            val bvImage = InputImage.fromBitmap(bitmap, 0)
            bvRecognizer.process(bvImage)
                .addOnSuccessListener { visionText ->
                    bvBinding.bvExtractedText.text = visionText.text
                }
                .addOnSuccessListener { visionText ->
                    if (visionText.text.isNotEmpty()) {
                        bvBinding.bvExtractedText.text = visionText.text
                    } else {
                        Toast.makeText(this,
                            "Pildilt teksti ei leitud", Toast.LENGTH_SHORT).show()
                        // Pildilt teksti ei leitud
                    }
                }
        } ?: run {
            Toast.makeText(this, "Palun valige foto", Toast.LENGTH_SHORT).show()
        }
    }

    // Получение изображения с камеры
    // Pildi saamine kaamerast
    private fun bvTakeImageWithCamera() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            val bvIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(bvIntent, bvRequestImageCapture)
            } catch (e: Exception) {
                Toast.makeText(this, "Kaamera avamine ebaõnnestus", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), bvRequestImagePermission)
        }
    }

    // Переключение TextToSpeech
    // Teksti kõneks muutmise lülitamine
    private fun bvToggleTextToSpeech() {
        if (bvIsTextToSpeechEnabled) {
            bvTextToSpeech.stop()
            bvIsTextToSpeechEnabled= false
        } else {
            val bvText = bvBinding.bvExtractedText.text.toString()
            if (bvText.isNotEmpty()) {
                bvTextToSpeech.speak(bvText, TextToSpeech.QUEUE_FLUSH, null, null)
                bvIsTextToSpeechEnabled = true
            } else {
                Toast.makeText(this, "Palun tuvastage kõigepealt tekst", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Обработка результата  полученного изображения с камеры или галереи
    // Käivitab kaamera või galerii rakendusest saadud pildi töötlemise
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Проверка, что результат получен успешно и запрос совпадает с запросом для получения изображения с камеры
        // Kontrollib, et tulemus saadi edukalt и päring ühtib kaamerast pildi saamise päringuga
        if (requestCode == bvRequestImageCapture && resultCode == RESULT_OK) {
            val bvExtras: Bundle? = data?.extras
            // Извлечение изображение из полученных  данных
            // Saab pildi  andmetest
            bvImageBitmap = bvExtras?.get("data") as Bitmap?
            bvImageBitmap?.let {
                // Установка полученное изображение в ImageView и запуск обработки изображения
                // Määrab saadud pildi ImageView-sse ja käivitab pildi töötlemise
                bvBinding.bvImageView.setImageBitmap(it)
                bvProcessImage()
            }
        }
    }

    // Метод, вызываемый при уничтожении активности
    // Aktiivsuse hävitamise metood
    override fun onDestroy() {
        super.onDestroy()
        // Проверка, был ли инициализирован объект bvTextToSpeech, и если да,то останавливаем его и закрываем
        // Kontrollib, kas bvTextToSpeech objekt on initsialiseeritud ja kui jah, peatab selle ja sulgeb
        if (::bvTextToSpeech.isInitialized) {
            bvTextToSpeech.stop()
            bvTextToSpeech.shutdown()
        }
        // Закрытие ресурсов распознавателя текста
        // Teksti tuvastaja ressurside sulgemине
        bvRecognizer.close()
    }
}
