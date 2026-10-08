package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.TutorialsProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("JARVIS AI", appName)
  }

  @Test
  fun `tutorials provider contains comprehensive guides`() {
    val guides = TutorialsProvider.guides
    assertTrue(guides.isNotEmpty())
    val jarvisSetupGuide = guides.find { it.id == "guide_1_jarvis_setup" }
    assertTrue(jarvisSetupGuide != null)
    assertTrue(jarvisSetupGuide?.steps?.isNotEmpty() == true)
  }

  @Test
  fun `gemini client gracefully handles query and provides intelligent response`() = kotlinx.coroutines.runBlocking {
    val result = com.example.data.api.GeminiClient.queryJarvis("who are you")
    assertTrue(result.isSuccess)
    val text = result.getOrNull()
    assertTrue(!text.isNullOrBlank())
    assertTrue(text!!.contains("JARVIS") || text.contains("J.A.R.V.I.S."))
  }

  @Test
  fun `apk download manager initializes in idle state with formatters`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = com.example.service.ApkDownloadManager(context)
    assertEquals(com.example.service.DownloadState.Idle, manager.downloadState.value)
    assertEquals("10.0 MB", manager.formatFileSize(10 * 1024 * 1024L))
    assertEquals("500.0 KB", manager.formatFileSize(500 * 1024L))
  }
}
