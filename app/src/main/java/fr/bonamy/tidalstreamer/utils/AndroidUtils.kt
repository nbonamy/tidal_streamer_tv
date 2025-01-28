package fr.bonamy.tidalstreamer.utils

import android.content.Context
import android.content.pm.PackageManager


class AndroidUtils {

  companion object {
      fun hasPermission(context: Context, permission: String?): Boolean {
          return PackageManager.PERMISSION_GRANTED == context.packageManager.checkPermission(
              permission!!, context.packageName
          )
      }
  }

}