/**
 * Wire
 * Copyright (C) 2017 Wire Swiss GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.waz.zclient.appentry.scenes

import android.app.Activity
import android.content.{Context, Intent}
import android.net.Uri
import android.view.View
import com.waz.threading.Threading
import com.waz.utils.events.EventContext
import com.waz.zclient._
import com.waz.zclient.appentry.{AppEntryButtonOnTouchListener, AppEntryDialogs}
import com.waz.zclient.common.views.InputBox
import com.waz.zclient.common.views.InputBox.NameValidator
import com.waz.zclient.controllers.SignInController.{Email, Register, SignInMethod}
import com.waz.zclient.ui.text.TypefaceTextView
import com.waz.zclient.ui.utils.KeyboardUtils
import com.waz.zclient.utils._

import scala.concurrent.Future

case class TeamNameViewHolder(root: View)(implicit val context: Context, eventContext: EventContext, injector: Injector) extends ViewHolder with Injectable {

  val appEntryController = inject[AppEntryController]

  lazy val inputField = root.findViewById[InputBox](R.id.input_field)
  lazy val about = root.findViewById[View](R.id.about)

  def onCreate(): Unit = {
    import Threading.Implicits.Ui
    inputField.setValidator(NameValidator)
    inputField.editText.setText(appEntryController.teamName)
    inputField.editText.addTextListener(appEntryController.teamName = _)
    inputField.editText.requestFocus()
    KeyboardUtils.showKeyboard(context.asInstanceOf[Activity])
    inputField.setOnClick( text =>
      appEntryController.isAB.flatMap{
        case true =>
          appEntryController.setTeamName(text).map {
            case Right(error) =>
              val errorMessage = ContextUtils.getString(EntryError(error.code, error.label, SignInMethod(Register, Email)).bodyResource)
              Some(errorMessage)
            case _ => None
          }
        case false =>
          AppEntryDialogs.showTermsAndConditions(context).flatMap {
            case true =>
              appEntryController.setTeamName(text).map {
                case Right(error) =>
                  val errorMessage = ContextUtils.getString(EntryError(error.code, error.label, SignInMethod(Register, Email)).bodyResource)
                  Some(errorMessage)
                case _ => None
              }
            case false =>
              Future.successful(None)
          }
      }
    )
    about.setOnClickListener(new View.OnClickListener {
      override def onClick(v: View): Unit = openUrl(R.string.url_about_teams)
    })
  }

  private def openUrl(id: Int): Unit ={
    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(id))))
  }
}
