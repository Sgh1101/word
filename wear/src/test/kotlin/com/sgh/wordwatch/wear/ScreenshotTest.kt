package com.sgh.wordwatch.wear

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme
import com.github.takahirom.roborazzi.captureRoboImage
import com.sgh.wordwatch.core.WordIndex
import com.sgh.wordwatch.core.WordParser
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** 시계 화면 미리보기 이미지 (build/outputs/roborazzi 에 저장). */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w227dp-h227dp-small-notlong-round-watch-xhdpi-keyshidden-nonav")
class ScreenshotTest {

    private val words = WordParser.parse(
        """
        abandon 버리다
        ability 능력
        able ~할 수 있는
        about ~에 대해
        above ~위에
        abroad 해외로
        absence 부재
        absolute 절대적인
        absorb 흡수하다
        abstract 추상적인
        academic 학문의
        accept 받아들이다
        access 접근
        accident 사고
        account 계좌, 설명
        accurate 정확한
        achieve 성취하다
        apple 사과
        apply 지원하다
        area 지역
        banana 바나나
        borrow 빌리다
        breathe 숨쉬다
        cherry 체리
        climate 기후
        deny 부인하다
        effort 노력
        give up 포기하다
        global 세계적인
        hesitate 망설이다
        improve 개선하다
        justify 정당화하다
        maintain 유지하다
        notice 알아차리다
        obvious 명백한
        persuade 설득하다
        quality 질
        reduce 줄이다
        suggest 제안하다
        tend ~하는 경향이 있다
        urgent 긴급한
        various 다양한
        wonder 궁금해하다
        yield 산출하다
        zone 구역
        """.trimIndent(),
    )

    private fun shot(name: String, content: @Composable () -> Unit) = captureRoboImage("screenshots/$name.png") {
        MaterialTheme { Box(Modifier.fillMaxSize().clip(CircleShape)) { content() } }
    }

    @Test fun index() = shot("1_index") {
        IndexScreen("단어 ${words.size}", WordIndex.firstGroups(words), false, {}, {})
    }

    @Test fun sub() = shot("2_second_letter") {
        IndexScreen("A ${WordIndex.filter(words, "A").size}", WordIndex.secondGroups(words, "A"), false, {}, {}, "A 전체")
    }

    @Test fun list() = shot("3_list") {
        WordListScreen("A", WordIndex.filter(words, "A"), hideMeaning = false, onToggleHide = {})
    }

    @Test fun listHidden() = shot("4_list_hidden") {
        WordListScreen("A", WordIndex.filter(words, "A"), hideMeaning = true, onToggleHide = {})
    }
}
