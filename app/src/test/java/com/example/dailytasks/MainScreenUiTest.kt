package com.example.dailytasks

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import java.util.Calendar
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MainScreenUiTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun fullTaskFlowWithColorNoteAndCalendar() {
        waitForText(EMPTY_STATE)
        composeRule.onNodeWithContentDescription(ADD_TASK).performClick()

        composeRule.onNodeWithText("新建任务").assertIsDisplayed()

        // 日期选择器可正常打开并确认
        composeRule.onNodeWithText("日期").performClick()
        composeRule.onNodeWithText("确定").performClick()
        // 时间选择器可正常打开并确认
        composeRule.onNodeWithText("时间").performClick()
        composeRule.onNodeWithText("确定").performClick()

        // 空标题不能保存（弹窗内容可滚动，先滚动到底部的保存按钮）
        composeRule.onNodeWithText("保存").performScrollTo().performClick()
        composeRule.onNodeWithText("任务名称不能为空").assertExists()

        composeRule.onNodeWithContentDescription("橙色").performScrollTo().performClick()
        composeRule.onNodeWithContentDescription("橙色").assertIsSelected()

        composeRule.onNodeWithTag("task_title_field")
            .performScrollTo()
            .performTextInput("买水果")
        composeRule.onNodeWithTag("task_note_field")
            .performScrollTo()
            .performTextInput("超市采购")
        composeRule.onNodeWithText("保存").performScrollTo().performClick()

        waitForText("买水果")
        composeRule.onNodeWithText("买水果").assertIsDisplayed()
        composeRule.onNodeWithText("超市采购").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("完成任务").performClick()
        waitForContentDescription("取消完成")

        // 切到日历视图，当天的任务应显示在日历下方
        composeRule.onNodeWithContentDescription("日历视图").performClick()
        composeRule.onNodeWithContentDescription("下个月").assertIsDisplayed()

        val today = Calendar.getInstance()
        val todayDescription = buildString {
            append(today.get(Calendar.YEAR))
            append("年")
            append(today.get(Calendar.MONTH) + 1)
            append("月")
            append(today.get(Calendar.DAY_OF_MONTH))
            append("日，1 个任务")
        }
        composeRule.onNodeWithContentDescription(todayDescription).assertExists()

        composeRule.onNode(hasScrollAction())
            .performScrollToNode(hasText("买水果"))
        composeRule.onNodeWithText("买水果").assertIsDisplayed()

        // 切换月份后该月第一天没有任务（下方列表需要滚动到空状态卡片）
        composeRule.onNodeWithContentDescription("上个月")
            .performScrollTo()
            .performClick()
        composeRule.onNode(hasScrollAction())
            .performScrollToNode(hasText(CALENDAR_EMPTY))
        composeRule.onNodeWithText(CALENDAR_EMPTY).assertIsDisplayed()

        composeRule.onNodeWithContentDescription("今天")
            .performScrollTo()
            .performClick()
        composeRule.onNode(hasScrollAction())
            .performScrollToNode(hasText("买水果"))
        composeRule.onNodeWithText("买水果").assertIsDisplayed()

        // 回到清单视图，编辑任务
        composeRule.onNodeWithContentDescription("清单视图").performClick()
        waitForText("买水果")
        composeRule.onNodeWithText("买水果").performClick()
        composeRule.onNodeWithText("编辑任务").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("橙色").performScrollTo().assertIsSelected()
        composeRule.onNodeWithTag("task_title_field")
            .performScrollTo()
            .performTextClearance()
        composeRule.onNodeWithTag("task_title_field").performTextInput("买菜")
        composeRule.onNodeWithText("保存").performScrollTo().performClick()

        waitForText("买菜")
        composeRule.onNodeWithText("买菜").assertIsDisplayed()

        // 更多菜单删除并确认
        composeRule.waitUntil(6_000) {
            composeRule.onAllNodesWithText("任务已更新")
                .fetchSemanticsNodes()
                .isEmpty()
        }
        composeRule.onNodeWithContentDescription("更多操作")
            .performScrollTo()
            .performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithText("删除").performClick()
        composeRule.onNodeWithText("删除任务").assertIsDisplayed()
        composeRule.onNodeWithText("删除").performClick()

        waitForText(EMPTY_STATE)
        composeRule.onNodeWithText(EMPTY_STATE).assertIsDisplayed()
    }

    private fun waitForText(text: String, timeoutMillis: Long = 5_000) {
        composeRule.waitUntil(timeoutMillis) {
            composeRule.onAllNodesWithText(text)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun waitForContentDescription(description: String, timeoutMillis: Long = 5_000) {
        composeRule.waitUntil(timeoutMillis) {
            composeRule.onAllNodesWithContentDescription(description)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    companion object {
        private const val ADD_TASK = "添加任务"
        private const val EMPTY_STATE = "今天还没有任务，点击 + 添加任务"
        private const val CALENDAR_EMPTY = "这一天还没有任务，点击 + 添加任务"
    }
}
