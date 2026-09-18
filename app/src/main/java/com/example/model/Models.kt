package com.example.model

import java.util.UUID

/**
 * رتبة الشماسية في الكنيسة القبطية الأرثوذكسية
 */
enum class DeaconRank(val arabicTitle: String) {
    EPSALTOS("إبصالتيس (مرتل)"),
    ANAGNOSTES("أغنسطس (قارئ)"),
    EPIDIAKON("إيبودياكون (مساعد شماس)"),
    DIAKON("دياكون (شماس كامل)")
}

/**
 * فصول ومراحل خدمة الشمامسة بمير (من حضانة إلى ٣ ثانوي)
 */
enum class DeaconGrade(val arabicTitle: String, val stage: String, val sortOrder: Int) {
    NURSERY("حضانة", "مرحلة الطفولة", 1),
    PRIMARY_1("أولى ابتدائي", "المرحلة الابتدائية", 2),
    PRIMARY_2("ثانية ابتدائي", "المرحلة الابتدائية", 3),
    PRIMARY_3("ثالثة ابتدائي", "المرحلة الابتدائية", 4),
    PRIMARY_4("رابعة ابتدائي", "المرحلة الابتدائية", 5),
    PRIMARY_5("خامسة ابتدائي", "المرحلة الابتدائية", 6),
    PRIMARY_6("سادسة ابتدائي", "المرحلة الابتدائية", 7),
    PREP_1("أولى إعدادي", "المرحلة الإعدادية", 8),
    PREP_2("ثانية إعدادي", "المرحلة الإعدادية", 9),
    PREP_3("ثالثة إعدادي", "المرحلة الإعدادية", 10),
    SEC_1("أولى ثانوي", "المرحلة الثانوية", 11),
    SEC_2("ثانية ثانوي", "المرحلة الثانوية", 12),
    SEC_3("ثالثة ثانوي", "المرحلة الثانوية", 13)
}

/**
 * بيانات المخدوم الكاملة - خدمة الشمامسة بمير
 */
data class Deacon(
    val id: String = UUID.randomUUID().toString(),
    val code128: String = "DK-${(1000..9999).random()}", // باركود دائم
    val name: String = "",
    val grade: DeaconGrade = DeaconGrade.PRIMARY_1,
    val dateOfBirth: String = "", // YYYY-MM-DD
    val rank: DeaconRank = DeaconRank.EPSALTOS,
    val phone: String = "",
    val parentPhone: String = "",
    val governorate: String = "أسيوط",
    val district: String = "القوصية",
    val area: String = "مير",
    val street: String = "",
    val buildingNumber: String = "",
    val floor: String = "",
    val photoUrl: String = "",
    val pastoralNotes: String = "",
    val churchName: String = "كنيستا العزب وأبي سيفين ودير الملاك بمير",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * فحص البيانات الناقصة للتنبيه
     */
    fun getMissingDataList(): List<String> {
        val missing = mutableListOf<String>()
        if (name.isBlank()) missing.add("الاسم مفقود")
        if (dateOfBirth.isBlank()) missing.add("تاريخ الميلاد مفقود")
        if (phone.isBlank() && parentPhone.isBlank()) missing.add("أرقام الهواتف مفقودة")
        if (street.isBlank() || buildingNumber.isBlank()) missing.add("العنوان التفصيلي غير مكتمل")
        if (photoUrl.isBlank()) missing.add("الصورة الشخصية غير مدرجة")
        return missing
    }

    val isDataIncomplete: Boolean
        get() = getMissingDataList().isNotEmpty()

    val fullAddress: String
        get() = buildString {
            if (governorate.isNotBlank()) append(governorate).append(" - ")
            if (district.isNotBlank()) append(district).append(" - ")
            if (area.isNotBlank()) append(area).append(" - ")
            if (street.isNotBlank()) append("ش ").append(street).append(" ")
            if (buildingNumber.isNotBlank()) append("عقار ").append(buildingNumber)
            if (floor.isNotBlank()) append(" دور ").append(floor)
        }
}

/**
 * سجل الحضور والقداسات
 */
data class AttendanceRecord(
    val id: String = UUID.randomUUID().toString(),
    val deaconId: String,
    val date: String, // YYYY-MM-DD
    val isFridayService: Boolean = false,
    val isServicePresent: Boolean = false,
    val isMassPresent: Boolean = false,
    val recordedByServant: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * أهداف الحضور المستهدفة
 */
data class AttendanceTarget(
    val serviceTargetPercent: Int = 85,
    val massTargetPercent: Int = 75
)

/**
 * مقرر الألحان
 */
data class HymnCurriculum(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val season: String = "سنوي", // سنوي، كيهكي، صوم كبير، أسبوع الآلام، قيامة وخماسين
    val maxScore: Int = 100,
    val targetLevel: String = "مرحلة ابتدائية"
)

/**
 * تسميع وتقييم لحن
 */
data class HymnRecitation(
    val id: String = UUID.randomUUID().toString(),
    val deaconId: String,
    val hymnId: String,
    val hymnTitle: String,
    val score: Int = 0,
    val maxScore: Int = 100,
    val isPassed: Boolean = false,
    val deadlineDate: String = "", // YYYY-MM-DD
    val recitationDate: String = "",
    val examinerName: String = "",
    val notes: String = ""
)

/**
 * حفظ الإنجيل
 */
data class GospelSchedule(
    val id: String = UUID.randomUUID().toString(),
    val deaconId: String,
    val passageRef: String, // e.g. متى 5: 1-12
    val deadlineDate: String = "",
    val isCompleted: Boolean = false,
    val score: Int = 0,
    val notes: String = ""
)

/**
 * اختبارات مرحلية
 */
data class ExamResult(
    val id: String = UUID.randomUUID().toString(),
    val deaconId: String,
    val examTitle: String,
    val score: Int = 0,
    val maxScore: Int = 100,
    val date: String = "",
    val examiner: String = ""
)

/**
 * سجل الافتقاد الرعوي
 */
data class VisitationLog(
    val id: String = UUID.randomUUID().toString(),
    val deaconId: String,
    val visitDate: String, // YYYY-MM-DD
    val status: VisitationStatus = VisitationStatus.VISITED,
    val notes: String = "",
    val visitorServant: String = "الخادم المفتقد",
    val needsUrgentFollowup: Boolean = false,
    val servantName: String = visitorServant,
    val statusNote: String = status.titleArabic,
    val pastoralNotes: String = notes,
    val requiresFollowUp: Boolean = needsUrgentFollowup
)

enum class VisitationStatus(val titleArabic: String) {
    VISITED("تمت الزيارة بالمنزل"),
    NOT_AVAILABLE("غير متواجد بالمنزل"),
    PHONE_CALL("افتقاد هاتفي"),
    SPECIAL_CARE("يحتاج رعاية خاصة"),
    POSTPONED("تم تأجيل الموعد")
}

/**
 * مجموعة افتقاد جغرافية مرتبة حسب العقار
 */
data class VisitationGroup(
    val governorate: String,
    val district: String,
    val area: String,
    val street: String,
    val deaconsInOrder: List<Deacon>
)

/**
 * حساب الخادم والصلاحيات مع كلمة المرور لتسجيل الدخول
 */
enum class ServantRole(val arabicName: String) {
    ADMIN("مدير الخدمة (أمين)"),
    SERVANT("خادم مرحلة"),
    ASSISTANT("خادم مساعد / متدرب")
}

data class ServantAccount(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "الخادم المسؤول",
    val email: String = "admin@deacons.church",
    val password: String = "admin", // كلمة المرور للتحقق
    val phone: String = "",
    val role: ServantRole = ServantRole.ADMIN,
    val assignedGrade: DeaconGrade? = null, // الفصل المسؤول عنه الخادم
    val canEditDeacons: Boolean = true,
    val canTakeAttendance: Boolean = true,
    val canScoreCurriculum: Boolean = true,
    val canConductVisitation: Boolean = true,
    val canExportReports: Boolean = true,
    val canManagePermissions: Boolean = true,
    val isActive: Boolean = true,
    val isApproved: Boolean = true, // حالة اعتماد الحساب من أمين الخدمة
    val requestedGrade: DeaconGrade? = null, // الفصل المطلوب عند إنشاء الحساب
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * نتيجة محاولة تسجيل الدخول
 */
sealed class LoginResult {
    object Success : LoginResult()
    data class PendingApproval(val servantName: String) : LoginResult()
    data class Inactive(val message: String) : LoginResult()
    data class Error(val message: String) : LoginResult()
}

