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
    actionLabel: String,
    cancelable: Boolean = true,
    customView: View? = null,
    onAction: () -> Unit = {}
  ) {
    show(
      context = context,
      title = title,
      message = message,
      customView = customView,
      actions = listOf(TvDialogAction(actionLabel, onAction)),
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
    val messageView = content.findViewById<TextView>(R.id.dialog_message)
    val customContainer = content.findViewById<FrameLayout>(R.id.dialog_custom_content)
    val actionContainer = content.findViewById<LinearLayout>(R.id.dialog_actions)

    titleView.text = title.orEmpty()
    titleView.visibility = if (title.isNullOrBlank()) View.GONE else View.VISIBLE

    messageView.text = message.orEmpty()
    messageView.visibility = if (message.isNullOrBlank()) View.GONE else View.VISIBLE

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
      row.text = action.label
      row.setOnClickListener {
        dialog.dismiss()
        action.onSelected()
      }
      actionContainer.addView(row)
    }

    dialog.setContentView(content)
    dialog.setOnShowListener {
      actionContainer.getChildAt(0)?.requestFocus()
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
}
