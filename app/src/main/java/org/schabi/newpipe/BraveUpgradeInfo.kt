package org.schabi.newpipe

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class BraveUpgradeInfo(

    val versionName: String?,
    val apkUrl: String?,
    val changeLog: String?,
) : Parcelable
