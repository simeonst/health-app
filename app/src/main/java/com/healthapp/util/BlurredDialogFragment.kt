package com.healthapp.util

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Gravity
import android.view.Gravity.CENTER_HORIZONTAL
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.button.MaterialButton
import com.healthapp.R
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject


open class BlurredDialogFragment : DialogFragment() {

    //Possible issues with trying to access inflated views that are not yet attached
    private lateinit var title: AppCompatTextView
    private lateinit var desc: AppCompatTextView
    private var positive: MaterialButton? = null
    private var negative: MaterialButton? = null
    private var neutral: MaterialButton? = null

    private lateinit var blur: View

    private val events: BehaviorSubject<DialogEvents> = BehaviorSubject.create()
    fun getEventsObservable(): Observable<DialogEvents> {
        return events.hide()
    }

    private var titleStr: String? = null
    private var descStr: CharSequence? = null
    private var posStr: String? = null
    private var negStr: String? = null
    private var neutStr: String? = null

    private var posCol: Int? = null
    private var negCol: Int? = null
    private var neutCol: Int? = null

    private var verticalButtons: Boolean = false

    private var useCenteredLayout: Boolean = false

    private lateinit var positiveButtonClickedHandler: () -> Unit
    private lateinit var negativeButtonClickedHandler: () -> Unit
    private lateinit var neutralButtonClickedHandler: () -> Unit

    private var isPositiveButtonHandlerEnabled = false
    private var isNegativeButtonHandlerEnabled = false
    private var isNeutralButtonHandlerEnabled = false

    var isShowing = false
        private set

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.setOnShowListener { dialogShown() }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)

        titleStr = arguments?.getString(ARG_TITLE_STR, null)
        descStr = arguments?.getCharSequence(ARG_DESC_STR, null)
        posStr = arguments?.getString(ARG_POS_STR, null)
        negStr = arguments?.getString(ARG_NEG_STR, null)
        neutStr = arguments?.getString(ARG_NEUT_STR, null)

        if (arguments?.containsKey(ARG_POS_COL)!!) {
            posCol = arguments?.getInt(ARG_POS_COL)
        }
        if (arguments?.containsKey(ARG_NEG_COL)!!) {
            negCol = arguments?.getInt(ARG_NEG_COL)
        }
        if (arguments?.containsKey(ARG_NEUT_COL)!!) {
            neutCol = arguments?.getInt(ARG_NEUT_COL)
        }

        verticalButtons = arguments?.getBoolean(ARG_VERTICAL_BUTTONS, false) ?: false
        useCenteredLayout = arguments?.getBoolean(ARG_USE_CENTERED_LAYOUT, false) ?: false

        val v: View = activity!!.layoutInflater.inflate(DEFAULT_LAYOUT, null)

        initViews(v)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            v.clipToOutline = true
        }

        val dialog = builder.create()
        dialog.setView(v)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        if (alignDialogToBottom) {
            dialog.window?.setGravity(Gravity.BOTTOM)
        }

        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.attributes?.apply {
            dimAmount = 0f
        }
    }

    private fun initButtons(v: View) {
        positive = initButton(v, R.id.blurredDialogPositiveButton)
        negative = initButton(v, R.id.blurredDialogNegativeButton)
        neutral = initButton(v, R.id.blurredDialogNeutralButton)
    }

    private fun initButton(layout: View, @IdRes buttonResId: Int): MaterialButton? {
        return try {
            layout.findViewById(buttonResId)
        } catch (e: NullPointerException) {
            null
        }
    }

    private fun initViews(v: View) {

        title = v.findViewById(R.id.blurredDialogHeader)
        desc = v.findViewById(R.id.blurredDialogDesc)
        initButtons(v)
        blur = View(context)

        setupTextView(title, titleStr)
        setupTextView(desc, descStr)
        setupButton(positive, posStr, posCol)
        setupButton(negative, negStr, negCol)
        setupButton(neutral, neutStr, neutCol)

        if (verticalButtons) {
            val ll: LinearLayout = v.findViewById(R.id.blurred_dialog_buttons_holder)
            ll.orientation = LinearLayout.VERTICAL

            ll.gravity = CENTER_HORIZONTAL
        }

        setupActions()
    }

    private fun setupActions() {
        positive?.setOnClickListener {
            buttonClicked(DialogEvents.PositiveClicked)
            if (isPositiveButtonHandlerEnabled) positiveButtonClickedHandler()
        }
        negative?.setOnClickListener {
            buttonClicked(DialogEvents.NegativeClicked)
            if (isNegativeButtonHandlerEnabled) negativeButtonClickedHandler()
        }
        neutral?.setOnClickListener {
            buttonClicked(DialogEvents.NeutralClicked)
            if (isNeutralButtonHandlerEnabled) neutralButtonClickedHandler()
        }
    }

    private fun buttonClicked(event: DialogEvents) {
        events.onNext(event)
        dismiss()
    }

    private fun setupTextView(textView: AppCompatTextView, strVal: CharSequence?) {
        if (TextUtils.isEmpty(strVal)) {
            textView.visibility = View.GONE
        } else {
            textView.text = strVal
            textView.visibility = View.VISIBLE
            if (useCenteredLayout) {
                textView.textAlignment = AppCompatTextView.TEXT_ALIGNMENT_CENTER
            }
        }
    }

    private fun setupButton(button: MaterialButton?, strVal: String?, textCol: Int?) {
        if (TextUtils.isEmpty(strVal)) {
            button?.visibility = View.GONE
        } else {
            button?.text = strVal
            button?.visibility = View.VISIBLE
            if (textCol != null) {
                button?.setTextColor(textCol)
            }
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        isShowing = true
        super.show(manager, tag)
    }

    override fun show(transaction: FragmentTransaction, tag: String?): Int {
        isShowing = true
        return super.show(transaction, tag)
    }

    override fun showNow(manager: FragmentManager, tag: String?) {
        isShowing = true
        super.showNow(manager, tag)
    }

    override fun dismiss() {
        super.dismiss()
        isShowing = false
    }

    override fun dismissAllowingStateLoss() {
        super.dismissAllowingStateLoss()
        isShowing = false
    }

    private fun dialogShown() {
        events.onNext(DialogEvents.DialogShown)
        blurActivity()
    }

    private fun dialogDismissed() {
        clearActivityBlur()
        events.onNext(DialogEvents.DialogDismissed)
        //TODO: Maybe emit events.onComplete() as well here.
    }

    private fun blurActivity() {
        activity?.let {
            if (blur.background == null) {
                it.window.decorView.apply {
                    post {
                        blur.background = BitmapDrawable(resources,
                                this.takeViewScreenShot(.7F)
                                        .blurBitmap(25F, it.applicationContext))
                    }
                }
            }
            (it.window.decorView as ViewGroup).addView(blur)
        }
    }

    private fun clearActivityBlur() {
        activity?.let {
            (it.window.decorView as ViewGroup).removeView(blur)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        dialogDismissed()
        super.onDismiss(dialog)
    }

    fun setOnPositiveButtonClickedListener(listener: () -> Unit) {
        positiveButtonClickedHandler = listener
        isPositiveButtonHandlerEnabled = true
    }

    fun clearOnPositiveButtonClickedListener() {
        isPositiveButtonHandlerEnabled = false
    }

    fun setOnNegativeButtonClickedListener(listener: () -> Unit) {
        negativeButtonClickedHandler = listener
        isNegativeButtonHandlerEnabled = true
    }

    fun clearOnNegativeButtonClickedListener() {
        isNegativeButtonHandlerEnabled = false
    }

    fun setOnNeutralButtonClickedListener(listener: () -> Unit) {
        neutralButtonClickedHandler = listener
        isNeutralButtonHandlerEnabled = true
    }

    fun clearOnNeutralButtonClickedListener() {
        isNeutralButtonHandlerEnabled = false
    }

    companion object {

        private const val ARG_TITLE_STR = "ARG_TITLE_STR"
        private const val ARG_DESC_STR = "ARG_DESC_STR"
        private const val ARG_POS_STR = "ARG_POS_STR"
        private const val ARG_NEG_STR = "ARG_NEG_STR"
        private const val ARG_NEUT_STR = "ARG_NEUT_STR"

        private const val ARG_POS_COL = "ARG_POS_COL"
        private const val ARG_NEG_COL = "ARG_NEG_COL"
        private const val ARG_NEUT_COL = "ARG_NEUT_COL"

        private const val ARG_VERTICAL_BUTTONS = "ARG_VERTICAL_BUTTONS"
        private const val ARG_USE_CENTERED_LAYOUT = "ARG_USE_CENTERED_LAYOUT"
        private const val ARG_USE_HEAVY_BUTTONS = "ARG_USE_HEAVY_BUTTONS"

        private const val DEFAULT_LAYOUT: Int = R.layout.fragment_blurred_dialog
        private var alignDialogToBottom: Boolean = false

        @JvmOverloads
        fun newInstance(title: String? = null,
                        desc: CharSequence? = null,
                        posStr: String? = null,
                        negStr: String? = null,
                        neutralStr: String? = null,
                        @ColorInt posColor: Int? = null,
                        @ColorInt negColor: Int? = null,
                        @ColorInt neutColor: Int? = null,
                        verticalButtons: Boolean = false,
                        useIconedCancel: Boolean = false,
                        useCenteredLayout: Boolean = false

        ): BlurredDialogFragment {
            val f = BlurredDialogFragment()
            val args = Bundle()

            args.putString(ARG_TITLE_STR, title)
            args.putCharSequence(ARG_DESC_STR, desc)
            args.putString(ARG_POS_STR, posStr)
            args.putString(ARG_NEG_STR, negStr)
            args.putString(ARG_NEUT_STR, neutralStr)

            if (posColor != null) {
                args.putInt(ARG_POS_COL, posColor)
            }
            if (negColor != null) {
                args.putInt(ARG_NEG_COL, negColor)
            }
            if (neutColor != null) {
                args.putInt(ARG_NEUT_COL, neutColor)
            }

            args.putBoolean(ARG_VERTICAL_BUTTONS, verticalButtons)
            args.putBoolean(ARG_USE_CENTERED_LAYOUT, useCenteredLayout)

            alignDialogToBottom = false

            f.arguments = args
            return f
        }
    }

    enum class DialogEvents {
        DialogShown,
        DialogDismissed,
        PositiveClicked,
        NegativeClicked,
        NeutralClicked
    }
}