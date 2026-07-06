import re

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "r") as f:
    content = f.read()

# Add imports
imports_to_add = """import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext"""
content = content.replace("import kotlinx.coroutines.delay", "import kotlinx.coroutines.delay\n" + imports_to_add)

# Add logic inside VideoPlayerScreen
calc_logic = """
    var dynamicBlurRadius by remember { mutableFloatStateOf(16f) }
    
    // Dynamic Blur Radius Calculation based on video content complexity
    LaunchedEffect(isPlaying) {
        withContext(Dispatchers.IO) {
            val retriever = android.media.MediaMetadataRetriever()
            try {
                if (video.path.startsWith("http")) {
                    retriever.setDataSource(video.path, HashMap<String, String>())
                } else {
                    retriever.setDataSource(context, Uri.parse(video.path))
                }
                while (isPlaying) {
                    val timeUs = player.currentPosition * 1000L
                    val frame = retriever.getFrameAtTime(timeUs, android.media.MediaMetadataRetriever.OPTION_CLOSEST)
                    if (frame != null) {
                        // Calculate complexity (e.g., edge detection / variance approximation)
                        val step = Math.max(1, frame.width / 10)
                        var sum = 0
                        var count = 0
                        for (x in 0 until frame.width step step) {
                            for (y in 0 until frame.height step step) {
                                val pixel = frame.getPixel(x, y)
                                val brightness = (android.graphics.Color.red(pixel) + android.graphics.Color.green(pixel) + android.graphics.Color.blue(pixel)) / 3
                                sum += brightness
                                count++
                            }
                        }
                        val avg = if(count > 0) sum / count else 0
                        var diffSum = 0f
                        for (x in 0 until frame.width step step) {
                            for (y in 0 until frame.height step step) {
                                val pixel = frame.getPixel(x, y)
                                val brightness = (android.graphics.Color.red(pixel) + android.graphics.Color.green(pixel) + android.graphics.Color.blue(pixel)) / 3
                                diffSum += Math.abs(brightness - avg).toFloat()
                            }
                        }
                        val complexityScore = if(count > 0) diffSum / count else 0f
                        // Map complexity (0-100) to blur radius (8f to 32f)
                        val calculatedBlur = 8f + (complexityScore / 100f) * 24f
                        dynamicBlurRadius = calculatedBlur.coerceIn(8f, 32f)
                    }
                    delay(2000) // update every 2 seconds
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    retriever.release()
                } catch(e: Exception) {}
            }
        }
    }
"""

# Find a good place to insert (after `var isPlaying by remember { mutableStateOf(true) }`)
content = content.replace("var isPlaying by remember { mutableStateOf(true) }", "var isPlaying by remember { mutableStateOf(true) }" + calc_logic)

# Replace all `.glossyCard(isDark = true, cornerRadius = 24.dp)` 
# with `.glossyCard(isDark = true, cornerRadius = 24.dp, blurRadius = dynamicBlurRadius)`
content = content.replace(".glossyCard(isDark = true, cornerRadius = 24.dp)", ".glossyCard(isDark = true, cornerRadius = 24.dp, blurRadius = dynamicBlurRadius)")

with open("app/src/main/java/com/example/ui/screens/VideoPlayerScreen.kt", "w") as f:
    f.write(content)

print("Added dynamic blur radius")
