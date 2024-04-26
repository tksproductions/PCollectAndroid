import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Rect
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.abs
import kotlin.math.sqrt

object OpenCVUtils {
    fun extractPhotos(inputBitmap: Bitmap, aspectRatio: Pair<Float, Float> = Pair(5.5f, 8.5f), minPercentage: Float = 0.1f): List<Pair<Bitmap, Rect>> {
        val inputMat = bitmapToMat(inputBitmap)
        val extractedPairs = extractPhotos(inputMat, aspectRatio, minPercentage)
        val extractedBitmaps = mutableListOf<Pair<Bitmap, Rect>>()

        for (pair in extractedPairs) {
            val bitmap = matToBitmap(pair.first)
            extractedBitmaps.add(Pair(bitmap, pair.second))
        }

        inputMat.release()

        return extractedBitmaps
    }

    private fun extractPhotos(inputImage: Mat, aspectRatio: Pair<Float, Float> = Pair(5.5f, 8.5f), minPercentage: Float = 0.1f): List<Pair<Mat, Rect>> {
        val gray = Mat()
        Imgproc.cvtColor(inputImage, gray, Imgproc.COLOR_BGR2GRAY)

        val adaptiveThresholded = Mat()
        Imgproc.adaptiveThreshold(gray, adaptiveThresholded, 255.0, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 11, 2.0)

        val morphed = Mat()
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, Size(3.0, 3.0))
        Imgproc.morphologyEx(adaptiveThresholded, morphed, Imgproc.MORPH_CLOSE, kernel)

        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(morphed, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        val extractedPhotos = mutableListOf<Pair<Mat, Rect>>()
        val totalPixels = inputImage.cols() * inputImage.rows()
        val minSize = sqrt((minPercentage / 100.0 * totalPixels).toDouble()).toInt()
        val sizeGroups = mutableMapOf<Int, MutableList<Rect>>()

        for (cnt in contours) {
            val rect = Imgproc.boundingRect(cnt)
            val currentAspectRatio = rect.width.toFloat() / rect.height.toFloat()
            if (aspectRatio.first / aspectRatio.second * 0.8f <= currentAspectRatio &&
                currentAspectRatio <= aspectRatio.first / aspectRatio.second * 1.2f) {
                val area = rect.width * rect.height
                if (rect.width >= minSize && rect.height >= minSize) {
                    sizeGroups.getOrPut(area) { mutableListOf() }.add(rect)
                }
            }
        }

        val mostCommonSize = sizeGroups.maxByOrNull { it.value.size }?.key ?: 0
        val toleranceValue = (mostCommonSize * 0.1).toInt()

        if (mostCommonSize != 0) {
            for (cnt in contours) {
                val rect = Imgproc.boundingRect(cnt)
                val currentAspectRatio = rect.width.toFloat() / rect.height.toFloat()
                val area = rect.width * rect.height
                if (aspectRatio.first / aspectRatio.second * 0.8f <= currentAspectRatio &&
                    currentAspectRatio <= aspectRatio.first / aspectRatio.second * 1.2f &&
                    abs(area - mostCommonSize) < toleranceValue) {
                    val photo = Mat(inputImage, rect)
                    extractedPhotos.add(Pair(photo, rect))
                }
            }
        }

        gray.release()
        adaptiveThresholded.release()
        morphed.release()
        hierarchy.release()
        contours.forEach { it.release() }

        return extractedPhotos
    }

    private fun bitmapToMat(bitmap: Bitmap): Mat {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)
        return mat
    }

    private fun matToBitmap(mat: Mat): Bitmap {
        val bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)
        return bitmap
    }
}