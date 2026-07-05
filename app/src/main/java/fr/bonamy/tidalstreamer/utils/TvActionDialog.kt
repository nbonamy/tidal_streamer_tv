package fr.bonamy.tidalstreamer.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import fr.bonamy.tidalstreamer.R

data class TvDialogAction(
  val label: String,
  val onSelected: () -> Unit
)

object TvActionDialog {

  fun showActions(
    context: Context,
    title: String?,
    actions: List<TvDialogAction>
  ) {
    show(context, title, null, null, actions, true)
  }

  fun showMessage(
    context: Context,
    title: String?,
    message: String?,
    actionLabel: String? = null,
    cancelable: Boolean = true,
    customView: View? = null,
    onAction: () -> Unit = {}
  ) {
    show(
      context = context,
      title = title,
      message = message,
      customView = customView,
      actions = actionLabel?.let { listOf(TvDialogAction(it, onAction)) } ?: emptyList(),
      cancelable = cancelable
    )
  }

  private fun show(
    context: Context,
    title: String?,
    message: String?,
    customView: View?,
    actions: List<TvDialogAction>,
    cancelable: Boolean
  ) {
    val dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setCancelable(cancelable)

    val inflater = LayoutInflater.from(context)
    val content = inflater.inflate(R.layout.dialog_tv_action, null)
    val titleView = content.findViewById<TextView>(R.id.dialog_title)
    val messageScroll = content.findViewById<ScrollView>(R.id.dialog_message_scroll)
    val messageView = content.findViewById<TextView>(R.id.dialog_message)
    val customContainer = content.findViewById<FrameLayout>(R.id.dialog_custom_content)
    val actionContainer = content.findViewById<LinearLayout>(R.id.dialog_actions)

    titleView.text = title.orEmpty()
    titleView.visibility = if (title.isNullOrBlank()) View.GONE else View.VISIBLE

    messageView.text = message.orEmpty()
    messageScroll.visibility = if (message.isNullOrBlank()) View.GONE else View.VISIBLE

    if (customView != null) {
      (customView.parent as? ViewGroup)?.removeView(customView)
      customContainer.addView(customView)
      customContainer.visibility = View.VISIBLE
    }

    actions.forEachIndexed { index, action ->
      val row = inflater.inflate(R.layout.item_tv_dialog_action, actionContainer, false) as TextView
      if (index > 0) {
        row.layoutParams = (row.layoutParams as LinearLayout.LayoutParams).apply {
          topMargin = row.resources.getDimensionPixelSize(R.dimen.tv_dialog_action_spacing)
        }
      }
      row.id = View.generateViewId()
      row.text = action.label
      row.setOnClickListener {
        dialog.dismiss()
        action.onSelected()
      }
      actionContainer.addView(row)
    }
    actionContainer.visibility = if (actions.isEmpty()) View.GONE else View.VISIBLE

    dialog.setContentView(content)
    dialog.setOnShowListener {
      capScrollableMessageHeight(messageScroll)
      setupScrollableMessageNavigation(messageScroll, actionContainer.getChildAt(0))
      if (messageScroll.visibility == View.VISIBLE) {
        messageScroll.requestFocus()
      } else {
        actionContainer.getChildAt(0)?.requestFocus()
      }
    }
    dialog.show()
    dialog.window?.apply {
      setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
      addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
      setLayout(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT
      )
      attributes = attributes.apply {
        dimAmount = 0.72f
        gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        y = 0
      }
    }
  }

  private fun capScrollableMessageHeight(messageScroll: ScrollView) {
    if (messageScroll.visibility != View.VISIBLE) return

    messageScroll.post {
      val maxHeight = messageScroll.resources.getDimensionPixelSize(R.dimen.tv_dialog_message_max_height)
      if (messageScroll.height > maxHeight) {
        messageScroll.layoutParams = messageScroll.layoutParams.apply {
          height = maxHeight
        }
        messageScroll.requestLayout()
      }
    }
  }

  private fun setupScrollableMessageNavigation(messageScroll: ScrollView, firstAction: View?) {
    if (messageScroll.visibility != View.VISIBLE) return

    firstAction?.let {
      messageScroll.nextFocusDownId = it.id
      it.nextFocusUpId = R.id.dialog_message_scroll
    }

    messageScroll.setOnKeyListener { view, keyCode, event ->
      if (event.action != android.view.KeyEvent.ACTION_DOWN) return@setOnKeyListener false
      val scrollView = view as ScrollView
      val scrollDelta = scrollView.resources.getDimensionPixelSize(R.dimen.tv_dialog_message_scroll_step)

      when (keyCode) {
        android.view.KeyEvent.KEYCODE_DPAD_UP -> {
          if (scrollView.scrollY <= 0) return@setOnKeyListener false
          scrollView.smoothScrollBy(0, -scrollDelta)
          true
        }

        android.view.KeyEvent.KEYCODE_DPAD_DOWN -> {
          val child = scrollView.getChildAt(0) ?: return@setOnKeyListener false
          val maxScroll = (child.height - scrollView.height).coerceAtLeast(0)
          if (maxScroll <= 0 && firstAction == null) return@setOnKeyListener true
          if (scrollView.scrollY >= maxScroll) return@setOnKeyListener false
          scrollView.smoothScrollBy(0, scrollDelta)
          true
        }

        else -> false
      }
    }
  }
}
