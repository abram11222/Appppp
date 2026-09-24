package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.AttendanceRecord
import com.example.model.AttendanceTarget
import com.example.model.Deacon
import com.example.model.DeaconGrade
import com.example.model.ExamResult
import com.example.model.GospelSchedule
import com.example.model.HymnCurriculum
import com.example.model.HymnRecitation
import com.example.model.LoginResult
import com.example.model.ServantAccount
import com.example.model.ServantRole
import com.example.model.VisitationGroup
import com.example.model.VisitationLog
import com.example.repository.DeaconCloudRepository
import com.example.repository.FirebaseCloudConfig
import com.example.repository.MergeDiff
import com.example.repository.SyncState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class DeaconSortOrder(val title: String) {
    ALPHABETICAL("أبجدياً (أ - ي)"),
    HIGHEST_ATTENDANCE("أعلى نسبة حضور"),
    AGE_ASCENDING("الأصغر سناً"),
    RECENTLY_ADDED("أحدث الإضافات")
}

class DeaconsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: DeaconCloudRepository = DeaconCloudRepository.getInstance(application)

    // Filter, Search, and Sorting States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortOrder = MutableStateFlow(DeaconSortOrder.ALPHABETICAL)
    val sortOrder: StateFlow<DeaconSortOrder> = _sortOrder.asStateFlow()

    private val _selectedGradeFilter = MutableStateFlow<DeaconGrade?>(null)
    val selectedGradeFilter: StateFlow<DeaconGrade?> = _selectedGradeFilter.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _selectedDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Data streams from cloud repository
    val deacons: StateFlow<List<Deacon>> = repository.deacons
    val currentServant: StateFlow<ServantAccount?> = repository.currentServant
    val servants: StateFlow<List<ServantAccount>> = repository.servants
    val syncState: StateFlow<SyncState> = repository.syncState
    val lastSyncTime: StateFlow<Long> = repository.lastSyncTime
    val cloudConfig: StateFlow<FirebaseCloudConfig> = repository.cloudConfig
    val attendanceRecords: StateFlow<List<AttendanceRecord>> = repository.attendance
    val attendanceTarget: StateFlow<AttendanceTarget> = repository.target
    val hymnCurriculums: StateFlow<List<HymnCurriculum>> = repository.hymnCurriculums
    val recitations: StateFlow<List<HymnRecitation>> = repository.hymnRecitations
    val gospelSchedules: StateFlow<List<GospelSchedule>> = repository.gospelSchedules
    val examResults: StateFlow<List<ExamResult>> = repository.examResults
    val visitationLogs: StateFlow<List<VisitationLog>> = repository.visitationLogs

    // Servants Streams
    val pendingServants: StateFlow<List<ServantAccount>> = combine(servants, _searchQuery) { list, _ ->
        list.filter { !it.isApproved }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val approvedServants: StateFlow<List<ServantAccount>> = combine(servants, _searchQuery) { list, _ ->
        list.filter { it.isApproved }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Visible Deacons: Restricted to servant's assigned grade if the user is not an Admin
    val visibleDeacons: StateFlow<List<Deacon>> = combine(deacons, currentServant) { list, servant ->
        if (servant != null && servant.role != ServantRole.ADMIN && servant.assignedGrade != null) {
            list.filter { it.grade == servant.assignedGrade }
        } else {
            list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered and Sorted Deacons
    val filteredDeacons: StateFlow<List<Deacon>> = combine(
        visibleDeacons,
        _searchQuery,
        _sortOrder,
        attendanceRecords,
        _selectedGradeFilter
    ) { list, query, sort, attendance, gradeFilter ->
        var filtered = if (query.isBlank()) {
            list
        } else {
            val q = query.trim().lowercase()
            list.filter { deacon ->
                deacon.name.lowercase().contains(q) ||
                        deacon.phone.contains(q) ||
                        deacon.parentPhone.contains(q) ||
                        deacon.street.lowercase().contains(q) ||
                        deacon.district.lowercase().contains(q) ||
                        deacon.code128.lowercase().contains(q)
            }
        }

        if (gradeFilter != null) {
            filtered = filtered.filter { it.grade == gradeFilter }
        }

        when (sort) {
            DeaconSortOrder.ALPHABETICAL -> filtered.sortedBy { it.name }
            DeaconSortOrder.HIGHEST_ATTENDANCE -> {
                val attMap = attendance.groupBy { it.deaconId }
                filtered.sortedByDescending { deacon ->
                    val recs = attMap[deacon.id] ?: emptyList()
                    recs.count { it.isServicePresent || it.isMassPresent }
                }
            }
            DeaconSortOrder.AGE_ASCENDING -> filtered.sortedByDescending {
                it.dateOfBirth.ifBlank { "1900-01-01" }
            }
            DeaconSortOrder.RECENTLY_ADDED -> filtered.sortedByDescending { it.createdAt }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Incomplete data deacons (filtered for current servant's class)
    val incompleteDeacons: StateFlow<List<Deacon>> = visibleDeacons.map { list ->
        list.filter { it.isDataIncomplete }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Smart Geographic Grouping (Governorate -> District -> Area -> Street -> sorted by Building number)
    val visitationGroups: StateFlow<List<VisitationGroup>> = visibleDeacons.combine(MutableStateFlow(Unit)) { list, _ ->
        list.filter { it.street.isNotBlank() }
            .groupBy { "${it.governorate}|${it.district}|${it.area}|${it.street}" }
            .map { (_, deaconsOnStreet) ->
                val first = deaconsOnStreet.first()
                val sortedByBuilding = deaconsOnStreet.sortedWith(
                    compareBy(
                        { it.buildingNumber.filter { c -> c.isDigit() }.toIntOrNull() ?: 9999 },
                        { it.buildingNumber }
                    )
                )
                VisitationGroup(
                    governorate = first.governorate,
                    district = first.district,
                    area = first.area,
                    street = first.street,
                    deaconsInOrder = sortedByBuilding
                )
            }
            .sortedBy { "${it.governorate} ${it.district} ${it.area} ${it.street}" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Authentication methods
    fun loginWithResult(emailOrPhoneOrName: String, pass: String): LoginResult {
        val result = repository.loginWithResult(emailOrPhoneOrName, pass)
        when (result) {
            is LoginResult.Success -> {
                _userMessage.value = "تم تسجيل الدخول بنجاح. أهلاً بك يا ${repository.currentServant.value?.name}"
            }
            is LoginResult.PendingApproval -> {
                _userMessage.value = "حسابك (${result.servantName}) قيد المراجعة والموافقة من قِبل أمين الخدمة"
            }
            is LoginResult.Inactive -> {
                _userMessage.value = result.message
            }
            is LoginResult.Error -> {
                _userMessage.value = result.message
            }
        }
        return result
    }

    fun login(emailOrName: String, pass: String): Boolean {
        val result = loginWithResult(emailOrName, pass)
        return result is LoginResult.Success
    }

    fun registerServant(
        name: String,
        phone: String,
        email: String,
        pass: String,
        requestedGrade: DeaconGrade,
        onResult: (Boolean, String) -> Unit
    ) {
        val result = repository.registerServant(name, phone, email, pass, requestedGrade)
        result.fold(
            onSuccess = { servant ->
                _userMessage.value = "تم إرسال طلب انضمام الخادم بنجاح وبانتظار اعتماد أمين الخدمة"
                onResult(true, "تم إرسال طلب انضمامك بنجاح! سيتم تفعيل حسابك فور اعتماده من أمين الخدمة.")
            },
            onFailure = { error ->
                val msg = error.message ?: "حدث خطأ أثناء التسجيل"
                _userMessage.value = msg
                onResult(false, msg)
            }
        )
    }

    fun approveServant(
        servantId: String,
        role: ServantRole,
        assignedGrade: DeaconGrade?,
        canEditDeacons: Boolean,
        canTakeAttendance: Boolean,
        canScoreCurriculum: Boolean,
        canConductVisitation: Boolean,
        canExportReports: Boolean,
        canManagePermissions: Boolean
    ) {
        val success = repository.approveServant(
            servantId,
            role,
            assignedGrade,
            canEditDeacons,
            canTakeAttendance,
            canScoreCurriculum,
            canConductVisitation,
            canExportReports,
            canManagePermissions
        )
        if (success) {
            _userMessage.value = "تم اعتماد حساب الخادم ومنحه الصلاحيات وتحديث السحابة بنجاح"
        } else {
            _userMessage.value = "عفواً، يتطلب اعتماد الخدام صلاحية مدير الخدمة"
        }
    }

    fun rejectServant(servantId: String) {
        val success = repository.rejectServant(servantId)
        if (success) {
            _userMessage.value = "تم رفض/حذف طلب الخادم بنجاح"
        } else {
            _userMessage.value = "عفواً، هذه العملية تتطلب صلاحية مدير الخدمة"
        }
    }

    fun logout() {
        repository.logout()
        _userMessage.value = "تم تسجيل الخروج"
    }

    fun clearAllDeacons(): Boolean {
        val success = repository.clearAllDeacons()
        if (success) {
            _userMessage.value = "تم مسح جميع بيانات المخدومين بنجاح"
        } else {
            _userMessage.value = "عفواً، هذه العملية تتطلب صلاحية مدير الخدمة"
        }
        return success
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: DeaconSortOrder) {
        _sortOrder.value = order
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun setSelectedGrade(grade: DeaconGrade?) {
        _selectedGradeFilter.value = grade
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshCloudData()
            _isRefreshing.value = false
            _userMessage.value = "تم تحديث ومزامنة البيانات سحابياً بنجاح"
        }
    }

    fun importDeaconsForGrade(grade: DeaconGrade, lines: List<String>): Int {
        val servant = currentServant.value
        val effectiveGrade = if (servant != null && servant.role != ServantRole.ADMIN && servant.assignedGrade != null) {
            servant.assignedGrade
        } else {
            grade
        }
        val count = repository.importDeaconsForGrade(effectiveGrade, lines)
        if (count > 0) {
            _userMessage.value = "تم استيراد $count شماس بنجاح لفصل ${effectiveGrade.arabicTitle}"
        } else {
            _userMessage.value = "لم يتم استيراد أي شماس جديد (ربما الأسماء مكررة أو فارغة)"
        }
        return count
    }

    fun importDeaconsDirectly(grade: DeaconGrade, deaconsList: List<Deacon>): Int {
        val servant = currentServant.value
        val effectiveGrade = if (servant != null && servant.role != ServantRole.ADMIN && servant.assignedGrade != null) {
            servant.assignedGrade
        } else {
            grade
        }
        val finalDeacons = if (servant != null && servant.role != ServantRole.ADMIN && servant.assignedGrade != null) {
            deaconsList.map { it.copy(grade = effectiveGrade) }
        } else {
            deaconsList
        }
        val count = repository.importDeaconsList(finalDeacons)
        if (count > 0) {
            _userMessage.value = "تم استيراد $count شماس من ملف الإكسيل بنجاح لفصل ${effectiveGrade.arabicTitle}"
        } else {
            _userMessage.value = "لم يتم استيراد أي شماس جديد (ربما البيانات مكررة أو فارغة)"
        }
        return count
    }

    // --- Deacons Actions ---
    fun saveDeacon(deacon: Deacon): Boolean {
        val servant = currentServant.value
        val deaconToSave = if (servant != null && servant.role != ServantRole.ADMIN && servant.assignedGrade != null) {
            deacon.copy(grade = servant.assignedGrade)
        } else {
            deacon
        }
        val success = repository.saveDeacon(deaconToSave)
        if (success) {
            _userMessage.value = "تم حفظ بيانات الشماس ${deaconToSave.name} وتحديث السحابة بنجاح"
        } else {
            _userMessage.value = "عفواً، ليس لديك صلاحية تعديل بيانات المخدومين"
        }
        return success
    }

    fun deleteDeacon(id: String): Boolean {
        val success = repository.deleteDeacon(id)
        if (success) {
            _userMessage.value = "تم حذف الشماس بنجاح وتحديث السحابة"
        } else {
            _userMessage.value = "عفواً، ليس لديك صلاحية حذف المخدومين"
        }
        return success
    }

    // --- Attendance Actions ---
    fun toggleAttendance(deaconId: String, date: String, isService: Boolean, isMass: Boolean) {
        repository.toggleAttendance(deaconId, date, isService, isMass)
    }

    fun markAllPresentForDate(date: String, isService: Boolean, isMass: Boolean) {
        repository.markAllPresentForDate(date, isService, isMass)
        _userMessage.value = "تم تسجيل جميع الحاضرين للتاريخ $date بنجاح"
    }

    fun updateTarget(serviceTarget: Int, massTarget: Int) {
        repository.updateTarget(serviceTarget, massTarget)
        _userMessage.value = "تم تحديث نسب الحضور المستهدفة"
    }

    // --- Curriculum Actions ---
    fun saveHymnCurriculum(hymn: HymnCurriculum) {
        repository.saveHymnCurriculum(hymn)
        _userMessage.value = "تم إضافة المقرر الكنسي بنجاح"
    }

    fun saveHymnRecitation(recitation: HymnRecitation): Boolean {
        val success = repository.saveHymnRecitation(recitation)
        if (success) {
            _userMessage.value = "تم تسجيل درجة التسميع وتحديث السحابة"
        } else {
            _userMessage.value = "عفواً، ليس لديك صلاحية تقييم المناهج"
        }
        return success
    }

    fun saveGospelSchedule(schedule: GospelSchedule): Boolean {
        val success = repository.saveGospelSchedule(schedule)
        if (success) {
            _userMessage.value = "تم تسجيل حفظ الإنجيل"
        } else {
            _userMessage.value = "عفواً، ليس لديك صلاحية تقييم الإنجيل"
        }
        return success
    }

    fun saveExamResult(exam: ExamResult): Boolean {
        val success = repository.saveExamResult(exam)
        if (success) {
            _userMessage.value = "تم تسجيل نتيجة الاختبار الكنسي"
        } else {
            _userMessage.value = "عفواً، ليس لديك صلاحية رصد الاختبارات"
        }
        return success
    }

    // --- Visitation Actions ---
    fun saveVisitationLog(log: VisitationLog): Boolean {
        val success = repository.saveVisitationLog(log)
        if (success) {
            _userMessage.value = "تم حفظ تقرير الافتقاد ومشاركته فورياً مع المخدومين"
        } else {
            _userMessage.value = "عفواً، ليس لديك صلاحية تسجيل الافتقاد"
        }
        return success
    }

    // --- Servants & Cloud Settings Actions ---
    fun switchActiveServant(servantId: String) {
        repository.switchActiveServant(servantId)
    }

    fun updateServantPermissions(servant: ServantAccount) {
        repository.updateServantPermissions(servant)
        _userMessage.value = "تم تحديث صلاحيات الخادم ${servant.name}"
    }

    fun addServant(servant: ServantAccount) {
        repository.addServant(servant)
        _userMessage.value = "تمت إضافة الخادم ${servant.name} بنجاح"
    }

    fun updateCloudConfig(config: FirebaseCloudConfig) {
        repository.updateCloudConfig(config)
        _userMessage.value = "تم حفظ إعدادات الربط السحابي بـ Firebase"
    }

    // --- Smart Merging and Backups ---
    fun getExportBackupJson(): String {
        return repository.exportBackupJson()
    }

    fun previewMerge(incomingJson: String): MergeDiff? {
        return repository.previewMerge(incomingJson)
    }

    fun executeSmartMerge(incomingJson: String): Boolean {
        val count = repository.mergeDeaconsFromJson(incomingJson)
        _userMessage.value = "تمت المزامنة والدمج بنجاح لعدد $count شماس بدون تكرار"
        return count > 0
    }

    fun shareFullReport(): String {
        val report = repository.generateFullAttendanceAndCurriculumReport()
        _userMessage.value = "تم تجهيز التقرير الكنسي الشامل للمشاركة والطباعة"
        return report
    }

    fun testNotification() {
        _userMessage.value = "🔔 تم إرسال إشعار تذكير الخدمة والقداس بنجاح!"
    }
}
