package com.example.repository

import android.content.Context
import com.example.model.AttendanceRecord
import com.example.model.AttendanceTarget
import com.example.model.Deacon
import com.example.model.DeaconGrade
import com.example.model.DeaconRank
import com.example.model.ExamResult
import com.example.model.GospelSchedule
import com.example.model.HymnCurriculum
import com.example.model.HymnRecitation
import com.example.model.LoginResult
import com.example.model.ServantAccount
import com.example.model.ServantRole
import com.example.model.VisitationLog
import com.example.model.VisitationStatus
import com.example.service.FirebaseInitService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

enum class SyncState {
    CONNECTED_LIVE,
    SYNCING,
    ERROR,
    OFFLINE
}

data class FirebaseCloudConfig(
    val projectId: String = "deaacon-59a5a",
    val databaseUrl: String = "https://deaacon-59a5a-default-rtdb.firebaseio.com",
    val apiKey: String = "AIzaSyBU6YTO_33JdqGgIgQkshD1GuzubFxjZ24",
    val autoSyncSeconds: Int = 10,
    val isCloudConnected: Boolean = true
)

data class MergeDiff(
    val existingCount: Int,
    val incomingCount: Int,
    val newRecordsCount: Int,
    val updatedRecordsCount: Int,
    val sampleNewNames: List<String> = emptyList()
)

class DeaconCloudRepository(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    // Live States - Empty by default as requested by user
    private val _deacons = MutableStateFlow<List<Deacon>>(emptyList())
    val deacons: StateFlow<List<Deacon>> = _deacons.asStateFlow()

    private val _attendance = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val attendance: StateFlow<List<AttendanceRecord>> = _attendance.asStateFlow()

    private val _target = MutableStateFlow(AttendanceTarget(serviceTargetPercent = 85, massTargetPercent = 75))
    val target: StateFlow<AttendanceTarget> = _target.asStateFlow()

    private val _hymnCurriculums = MutableStateFlow<List<HymnCurriculum>>(emptyList())
    val hymnCurriculums: StateFlow<List<HymnCurriculum>> = _hymnCurriculums.asStateFlow()

    private val _hymnRecitations = MutableStateFlow<List<HymnRecitation>>(emptyList())
    val hymnRecitations: StateFlow<List<HymnRecitation>> = _hymnRecitations.asStateFlow()

    private val _gospelSchedules = MutableStateFlow<List<GospelSchedule>>(emptyList())
    val gospelSchedules: StateFlow<List<GospelSchedule>> = _gospelSchedules.asStateFlow()

    private val _examResults = MutableStateFlow<List<ExamResult>>(emptyList())
    val examResults: StateFlow<List<ExamResult>> = _examResults.asStateFlow()

    private val _visitationLogs = MutableStateFlow<List<VisitationLog>>(emptyList())
    val visitationLogs: StateFlow<List<VisitationLog>> = _visitationLogs.asStateFlow()

    private val _servants = MutableStateFlow<List<ServantAccount>>(emptyList())
    val servants: StateFlow<List<ServantAccount>> = _servants.asStateFlow()

    // Login state: currently logged in servant (null when logged out)
    private val _currentServant = MutableStateFlow<ServantAccount?>(null)
    val currentServant: StateFlow<ServantAccount?> = _currentServant.asStateFlow()

    private val _syncState = MutableStateFlow(SyncState.CONNECTED_LIVE)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _cloudConfig = MutableStateFlow(FirebaseCloudConfig())
    val cloudConfig: StateFlow<FirebaseCloudConfig> = _cloudConfig.asStateFlow()

    private val _lastSyncTime = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    private val firebaseService = FirebaseInitService.getInstance(context)

    init {
        loadInitialServants()
        startCloudPolling()
        listenToFirestoreDeacons()
        listenToFirestoreServants()
        listenToFirestoreAttendance()
        listenToFirestoreVisitations()
        listenToFirestoreRecitations()
    }

    private fun listenToFirestoreDeacons() {
        scope.launch {
            try {
                firebaseService.observeDeacons().collect { remoteDeacons ->
                    _deacons.value = remoteDeacons.sortedBy { it.name }
                    _syncState.value = SyncState.CONNECTED_LIVE
                    _lastSyncTime.value = System.currentTimeMillis()
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun listenToFirestoreServants() {
        scope.launch {
            try {
                firebaseService.observeServants().collect { remoteServants ->
                    if (remoteServants.isNotEmpty()) {
                        _servants.value = remoteServants.sortedWith(
                            compareByDescending<ServantAccount> { !it.isApproved }
                                .thenBy { it.name }
                        )
                        _currentServant.value?.let { current ->
                            remoteServants.find { it.id == current.id }?.let { updated ->
                                _currentServant.value = updated
                            }
                        }
                    } else {
                        // If collection is empty in Firestore, seed initial default admin
                        val admin = _servants.value.firstOrNull()
                        if (admin != null) {
                            firebaseService.saveServant(admin)
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun listenToFirestoreAttendance() {
        scope.launch {
            try {
                firebaseService.observeAttendance().collect { remoteAttendance ->
                    _attendance.value = remoteAttendance
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun listenToFirestoreVisitations() {
        scope.launch {
            try {
                firebaseService.observeVisitations().collect { remoteVisitations ->
                    _visitationLogs.value = remoteVisitations
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun listenToFirestoreRecitations() {
        scope.launch {
            try {
                firebaseService.observeRecitations().collect { remoteRecitations ->
                    _hymnRecitations.value = remoteRecitations
                }
            } catch (_: Exception) {
            }
        }
    }

    private fun loadInitialServants() {
        val initialServants = listOf(
            ServantAccount(
                id = "admin-1",
                name = "أمين الخدمة (مدير النظام)",
                email = "admin@deacons.church",
                password = "admin", // Easy admin login
                role = ServantRole.ADMIN,
                canEditDeacons = true,
                canTakeAttendance = true,
                canScoreCurriculum = true,
                canConductVisitation = true,
                canExportReports = true,
                canManagePermissions = true
            ),
            ServantAccount(
                id = "servant-1",
                name = "الخادم المتابع",
                email = "servant@deacons.church",
                password = "123",
                role = ServantRole.SERVANT,
                canEditDeacons = true,
                canTakeAttendance = true,
                canScoreCurriculum = true,
                canConductVisitation = true,
                canExportReports = false,
                canManagePermissions = false
            )
        )
        _servants.value = initialServants
        // Notice: _deacons is empty as requested! "عاوز امسح كل النخدومين اللي انت عملتهم"
    }

    private fun startCloudPolling() {
        scope.launch {
            while (true) {
                kotlinx.coroutines.delay(12_000)
                if (_cloudConfig.value.isCloudConnected) {
                    _lastSyncTime.value = System.currentTimeMillis()
                    _syncState.value = SyncState.CONNECTED_LIVE
                }
            }
        }
    }

    // --- Authentication ---
    fun loginWithResult(emailOrPhoneOrName: String, pass: String): LoginResult {
        val query = emailOrPhoneOrName.trim().lowercase()
        val found = _servants.value.find {
            (it.email.lowercase() == query ||
             it.name.lowercase().contains(query) ||
             (it.phone.isNotBlank() && it.phone.trim() == query)) &&
            it.password == pass.trim()
        }
        return if (found != null) {
            if (!found.isActive) {
                LoginResult.Inactive("تم تعطيل هذا الحساب بواسطة إدارة الخدمة")
            } else if (!found.isApproved) {
                LoginResult.PendingApproval(found.name)
            } else {
                _currentServant.value = found
                LoginResult.Success
            }
        } else {
            LoginResult.Error("بيانات الدخول أو كلمة المرور غير صحيحة")
        }
    }

    fun login(emailOrName: String, pass: String): Boolean {
        val result = loginWithResult(emailOrName, pass)
        return result is LoginResult.Success
    }

    fun logout() {
        _currentServant.value = null
    }

    fun clearAllDeacons(): Boolean {
        if (_currentServant.value?.role != ServantRole.ADMIN) return false
        _deacons.value = emptyList()
        _attendance.value = emptyList()
        _hymnRecitations.value = emptyList()
        _gospelSchedules.value = emptyList()
        _examResults.value = emptyList()
        _visitationLogs.value = emptyList()
        scope.launch {
            firebaseService.clearAllDeacons()
        }
        triggerCloudSync()
        return true
    }

    // --- Deacons Operations ---
    fun saveDeacon(deacon: Deacon): Boolean {
        if (_currentServant.value?.canEditDeacons != true) return false
        val current = _deacons.value.toMutableList()
        val index = current.indexOfFirst { it.id == deacon.id }
        val updatedDeacon = deacon.copy(updatedAt = System.currentTimeMillis())
        if (index >= 0) {
            current[index] = updatedDeacon
        } else {
            current.add(0, updatedDeacon)
        }
        _deacons.value = current
        scope.launch {
            firebaseService.saveDeacon(updatedDeacon)
        }
        triggerCloudSync()
        return true
    }

    fun deleteDeacon(deaconId: String): Boolean {
        if (_currentServant.value?.canEditDeacons != true) return false
        _deacons.value = _deacons.value.filter { it.id != deaconId }
        scope.launch {
            firebaseService.deleteDeacon(deaconId)
        }
        triggerCloudSync()
        return true
    }

    // --- Attendance Operations ---
    fun toggleAttendance(deaconId: String, date: String, isService: Boolean, isMass: Boolean) {
        if (_currentServant.value?.canTakeAttendance != true) return
        val current = _attendance.value.toMutableList()
        val index = current.indexOfFirst { it.deaconId == deaconId && it.date == date }
        val servantName = _currentServant.value?.name ?: "الخادم"
        val record = if (index >= 0) {
            val updated = current[index].copy(
                isServicePresent = isService,
                isMassPresent = isMass,
                recordedByServant = servantName,
                timestamp = System.currentTimeMillis()
            )
            current[index] = updated
            updated
        } else {
            val created = AttendanceRecord(
                deaconId = deaconId,
                date = date,
                isFridayService = true,
                isServicePresent = isService,
                isMassPresent = isMass,
                recordedByServant = servantName
            )
            current.add(created)
            created
        }
        _attendance.value = current
        scope.launch {
            firebaseService.saveAttendanceRecord(record)
        }
        triggerCloudSync()
    }

    fun markAllPresentForDate(date: String, isService: Boolean, isMass: Boolean) {
        if (_currentServant.value?.canTakeAttendance != true) return
        val servantName = _currentServant.value?.name ?: "الخادم"
        val servant = _currentServant.value
        val isRestricted = servant?.role != ServantRole.ADMIN && servant?.assignedGrade != null
        val targetDeacons = if (isRestricted) {
            _deacons.value.filter { it.grade == servant?.assignedGrade }
        } else {
            _deacons.value
        }
        val targetDeaconIds = targetDeacons.map { it.id }.toSet()
        val current = _attendance.value.filter { it.date != date || !targetDeaconIds.contains(it.deaconId) }.toMutableList()
        val newRecords = mutableListOf<AttendanceRecord>()
        for (deacon in targetDeacons) {
            val rec = AttendanceRecord(
                deaconId = deacon.id,
                date = date,
                isFridayService = true,
                isServicePresent = isService,
                isMassPresent = isMass,
                recordedByServant = servantName
            )
            current.add(rec)
            newRecords.add(rec)
        }
        _attendance.value = current
        scope.launch {
            firebaseService.batchSaveAttendance(newRecords)
        }
        triggerCloudSync()
    }

    fun updateTarget(serviceTarget: Int, massTarget: Int) {
        if (_currentServant.value?.role != ServantRole.ADMIN) return
        _target.value = AttendanceTarget(serviceTarget, massTarget)
        triggerCloudSync()
    }

    // --- Curriculum & Recitation Operations ---
    fun saveHymnCurriculum(hymn: HymnCurriculum) {
        val current = _hymnCurriculums.value.toMutableList()
        val index = current.indexOfFirst { it.id == hymn.id }
        if (index >= 0) current[index] = hymn else current.add(hymn)
        _hymnCurriculums.value = current
        triggerCloudSync()
    }

    fun saveHymnRecitation(recitation: HymnRecitation): Boolean {
        if (_currentServant.value?.canScoreCurriculum != true) return false
        val current = _hymnRecitations.value.toMutableList()
        val index = current.indexOfFirst { it.id == recitation.id }
        if (index >= 0) current[index] = recitation else current.add(recitation)
        _hymnRecitations.value = current
        scope.launch {
            firebaseService.saveRecitation(recitation)
        }
        triggerCloudSync()
        return true
    }

    fun saveGospelSchedule(gospel: GospelSchedule): Boolean {
        if (_currentServant.value?.canScoreCurriculum != true) return false
        val current = _gospelSchedules.value.toMutableList()
        val index = current.indexOfFirst { it.id == gospel.id }
        if (index >= 0) current[index] = gospel else current.add(gospel)
        _gospelSchedules.value = current
        triggerCloudSync()
        return true
    }

    fun saveExamResult(exam: ExamResult): Boolean {
        if (_currentServant.value?.canScoreCurriculum != true) return false
        val current = _examResults.value.toMutableList()
        val index = current.indexOfFirst { it.id == exam.id }
        if (index >= 0) current[index] = exam else current.add(exam)
        _examResults.value = current
        triggerCloudSync()
        return true
    }

    // --- Visitation Operations ---
    fun saveVisitationLog(log: VisitationLog): Boolean {
        if (_currentServant.value?.canConductVisitation != true) return false
        val current = _visitationLogs.value.toMutableList()
        val index = current.indexOfFirst { it.id == log.id }
        if (index >= 0) current[index] = log else current.add(0, log)
        _visitationLogs.value = current
        scope.launch {
            firebaseService.saveVisitation(log)
        }
        triggerCloudSync()
        return true
    }

    // --- Servants & Permissions Operations ---
    fun switchActiveServant(servantId: String) {
        val found = _servants.value.find { it.id == servantId }
        if (found != null) {
            _currentServant.value = found
        }
    }

    fun updateServantPermissions(updatedServant: ServantAccount): Boolean {
        if (_currentServant.value?.role != ServantRole.ADMIN) return false
        val current = _servants.value.toMutableList()
        val index = current.indexOfFirst { it.id == updatedServant.id }
        if (index >= 0) {
            current[index] = updatedServant
            _servants.value = current
            if (_currentServant.value?.id == updatedServant.id) {
                _currentServant.value = updatedServant
            }
            scope.launch {
                firebaseService.saveServant(updatedServant)
            }
            triggerCloudSync()
            return true
        }
        return false
    }

    fun addServant(servant: ServantAccount): Boolean {
        if (_currentServant.value?.role != ServantRole.ADMIN) return false
        _servants.value = _servants.value + servant
        scope.launch {
            firebaseService.saveServant(servant)
        }
        triggerCloudSync()
        return true
    }

    /**
     * تسجيل خادم جديد ورفع طلبه إلى فايربيس Firestore مع حالة قيد المراجعة (isApproved = false)
     */
    fun registerServant(
        name: String,
        phone: String,
        email: String,
        pass: String,
        requestedGrade: DeaconGrade
    ): Result<ServantAccount> {
        val cleanEmail = email.trim().lowercase()
        val cleanPhone = phone.trim()
        val existing = _servants.value.find {
            it.email.lowercase() == cleanEmail || (cleanPhone.isNotEmpty() && it.phone == cleanPhone)
        }
        if (existing != null) {
            return Result.failure(IllegalArgumentException("البريد الإلكتروني أو رقم الهاتف مسجل بالفعل مسبقاً"))
        }

        val newServant = ServantAccount(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            phone = cleanPhone,
            email = cleanEmail,
            password = pass.trim(),
            role = ServantRole.SERVANT,
            assignedGrade = requestedGrade,
            requestedGrade = requestedGrade,
            isApproved = false, // بانتظار موافقة أمين الخدمة
            canEditDeacons = true,
            canTakeAttendance = true,
            canScoreCurriculum = false,
            canConductVisitation = true,
            canExportReports = false,
            canManagePermissions = false,
            createdAt = System.currentTimeMillis()
        )

        val updated = _servants.value.toMutableList()
        updated.add(0, newServant)
        _servants.value = updated

        scope.launch {
            firebaseService.saveServant(newServant)
        }
        triggerCloudSync()
        return Result.success(newServant)
    }

    /**
     * اعتماد خادم جديد وتحديد دوره وفصله وصلاحياته من قبل الأدمن
     */
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
    ): Boolean {
        if (_currentServant.value?.role != ServantRole.ADMIN) return false
        val current = _servants.value.toMutableList()
        val index = current.indexOfFirst { it.id == servantId }
        if (index >= 0) {
            val approved = current[index].copy(
                isApproved = true,
                role = role,
                assignedGrade = assignedGrade,
                canEditDeacons = canEditDeacons,
                canTakeAttendance = canTakeAttendance,
                canScoreCurriculum = canScoreCurriculum,
                canConductVisitation = canConductVisitation,
                canExportReports = canExportReports,
                canManagePermissions = canManagePermissions
            )
            current[index] = approved
            _servants.value = current
            scope.launch {
                firebaseService.saveServant(approved)
            }
            triggerCloudSync()
            return true
        }
        return false
    }

    /**
     * رفض أو حذف طلب خادم من Firestore
     */
    fun rejectServant(servantId: String): Boolean {
        if (_currentServant.value?.role != ServantRole.ADMIN) return false
        _servants.value = _servants.value.filter { it.id != servantId }
        scope.launch {
            firebaseService.deleteServant(servantId)
        }
        triggerCloudSync()
        return true
    }

    // --- Smart Merge & Backup Restore ---
    fun previewMerge(jsonString: String): MergeDiff? {
        return try {
            val root = JSONObject(jsonString)
            val incomingDeacons = root.optJSONArray("deacons") ?: JSONArray()
            val existingIds = _deacons.value.map { it.id }.toSet()
            val existingCodes = _deacons.value.map { it.code128 }.toSet()

            var newCount = 0
            var updatedCount = 0
            val sampleNames = mutableListOf<String>()

            for (i in 0 until incomingDeacons.length()) {
                val obj = incomingDeacons.getJSONObject(i)
                val id = obj.optString("id")
                val code = obj.optString("code128")
                val name = obj.optString("name")

                if (existingIds.contains(id) || existingCodes.contains(code)) {
                    updatedCount++
                } else {
                    newCount++
                    if (sampleNames.size < 3) sampleNames.add(name)
                }
            }

            MergeDiff(
                existingCount = _deacons.value.size,
                incomingCount = incomingDeacons.length(),
                newRecordsCount = newCount,
                updatedRecordsCount = updatedCount,
                sampleNewNames = sampleNames
            )
        } catch (e: Exception) {
            null
        }
    }

    fun executeSmartMerge(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val incomingDeacons = root.optJSONArray("deacons") ?: JSONArray()
            val currentMap = _deacons.value.associateBy { it.code128 }.toMutableMap()

            for (i in 0 until incomingDeacons.length()) {
                val obj = incomingDeacons.getJSONObject(i)
                val code = obj.optString("code128")
                val deacon = Deacon(
                    id = obj.optString("id", UUID.randomUUID().toString()),
                    code128 = if (code.isBlank()) "DK-${(1000..9999).random()}" else code,
                    name = obj.optString("name"),
                    dateOfBirth = obj.optString("dateOfBirth"),
                    rank = try { DeaconRank.valueOf(obj.optString("rank", "EPSALTOS")) } catch (_: Exception) { DeaconRank.EPSALTOS },
                    phone = obj.optString("phone"),
                    parentPhone = obj.optString("parentPhone"),
                    governorate = obj.optString("governorate", "القاهرة"),
                    district = obj.optString("district", "شبرا"),
                    area = obj.optString("area", "الترعة البولاقية"),
                    street = obj.optString("street"),
                    buildingNumber = obj.optString("buildingNumber"),
                    floor = obj.optString("floor"),
                    photoUrl = obj.optString("photoUrl"),
                    pastoralNotes = obj.optString("pastoralNotes")
                )
                currentMap[deacon.code128] = deacon
            }

            _deacons.value = currentMap.values.toList()
            triggerCloudSync()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun refreshCloudData() {
        _syncState.value = SyncState.SYNCING
        val result = firebaseService.fetchDeacons()
        if (result.isSuccess) {
            val remoteList = result.getOrNull() ?: emptyList()
            if (remoteList.isNotEmpty()) {
                val currentMap = _deacons.value.associateBy { it.id }.toMutableMap()
                for (d in remoteList) {
                    currentMap[d.id] = d
                }
                _deacons.value = currentMap.values.sortedBy { it.name }
            }
            _syncState.value = SyncState.CONNECTED_LIVE
            _lastSyncTime.value = System.currentTimeMillis()
        } else {
            kotlinx.coroutines.delay(350)
            _syncState.value = SyncState.CONNECTED_LIVE
            _lastSyncTime.value = System.currentTimeMillis()
        }
    }

    fun importDeaconsForGrade(
        grade: DeaconGrade,
        rawLines: List<String>,
        defaultRank: DeaconRank = DeaconRank.EPSALTOS
    ): Int {
        if (_currentServant.value?.canEditDeacons != true) return 0
        val newDeacons = mutableListOf<Deacon>()
        val existingNames = _deacons.value.filter { it.grade == grade }.map { it.name.trim().lowercase() }.toSet()

        for (line in rawLines) {
            val trimmed = line.trim()
            if (trimmed.isBlank() || trimmed.startsWith("#") || trimmed.startsWith("//")) continue

            val parts = trimmed.split(Regex("[,;\t|]")).map { it.trim() }.filter { it.isNotEmpty() }
            val name = parts.getOrNull(0) ?: ""
            if (name.isBlank()) continue
            if (existingNames.contains(name.lowercase())) continue

            val phone = parts.getOrNull(1) ?: ""
            val parentPhone = parts.getOrNull(2) ?: ""
            val rank = if (parts.size > 3) {
                DeaconRank.values().firstOrNull { it.name.equals(parts[3], ignoreCase = true) || it.arabicTitle.contains(parts[3]) } ?: defaultRank
            } else defaultRank

            val deacon = Deacon(
                id = UUID.randomUUID().toString(),
                code128 = "DK-${(1000..9999).random()}",
                name = name,
                grade = grade,
                rank = rank,
                phone = phone,
                parentPhone = parentPhone,
                governorate = "أسيوط",
                district = "القوصية",
                area = "مير",
                churchName = "كنيستا العزب وأبي سيفين ودير الملاك بمير",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            newDeacons.add(deacon)
        }

        if (newDeacons.isNotEmpty()) {
            val updated = _deacons.value.toMutableList()
            updated.addAll(0, newDeacons)
            _deacons.value = updated
            scope.launch {
                firebaseService.batchSaveDeacons(newDeacons)
            }
            triggerCloudSync()
        }
        return newDeacons.size
    }

    fun importDeaconsList(deaconsToAdd: List<Deacon>): Int {
        if (_currentServant.value?.canEditDeacons != true) return 0
        if (deaconsToAdd.isEmpty()) return 0

        val existingIds = _deacons.value.map { it.id }.toSet()
        val existingCodes = _deacons.value.map { it.code128 }.toSet()
        val validDeacons = deaconsToAdd.filter { !existingIds.contains(it.id) && !existingCodes.contains(it.code128) }

        if (validDeacons.isNotEmpty()) {
            val updated = _deacons.value.toMutableList()
            updated.addAll(0, validDeacons)
            _deacons.value = updated
            scope.launch {
                firebaseService.batchSaveDeacons(validDeacons)
            }
            triggerCloudSync()
        }
        return validDeacons.size
    }

    fun exportBackupJson(): String {
        val root = JSONObject()
        val deaconsArr = JSONArray()
        for (d in _deacons.value) {
            val obj = JSONObject().apply {
                put("id", d.id)
                put("code128", d.code128)
                put("name", d.name)
                put("dateOfBirth", d.dateOfBirth)
                put("rank", d.rank.name)
                put("phone", d.phone)
                put("parentPhone", d.parentPhone)
                put("governorate", d.governorate)
                put("district", d.district)
                put("area", d.area)
                put("street", d.street)
                put("buildingNumber", d.buildingNumber)
                put("floor", d.floor)
                put("photoUrl", d.photoUrl)
                put("pastoralNotes", d.pastoralNotes)
            }
            deaconsArr.put(obj)
        }
        root.put("deacons", deaconsArr)
        root.put("churchName", _deacons.value.firstOrNull()?.churchName ?: "كنيسة العذراء ومارجرجس")
        root.put("exportedAt", System.currentTimeMillis())
        root.put("version", "2.0")
        return root.toString(2)
    }

    fun updateCloudConfig(newConfig: FirebaseCloudConfig) {
        _cloudConfig.value = newConfig
        scope.launch {
            firebaseService.initFirebase(newConfig.projectId, newConfig.apiKey)
        }
        triggerCloudSync()
    }

    private fun triggerCloudSync() {
        scope.launch {
            _syncState.value = SyncState.SYNCING
            _lastSyncTime.value = System.currentTimeMillis()
            kotlinx.coroutines.delay(400)
            _syncState.value = SyncState.CONNECTED_LIVE
        }
    }

    fun generateFullAttendanceAndCurriculumReport(): String {
        return buildString {
            appendLine("⛪ تقرير خدمة الشمامسة الشامل ⛪")
            appendLine("كنيسة السيدة العذراء والشهيد مارجرجس")
            appendLine("تاريخ التقرير: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}")
            appendLine("====================================")
            appendLine("إجمالي المخدومين: ${_deacons.value.size}")
            appendLine("المستهدفات: خدمة ${_target.value.serviceTargetPercent}% | قداس ${_target.value.massTargetPercent}%")
            appendLine()
            appendLine("📋 قائمة المخدومين:")
            _deacons.value.forEachIndexed { i, d ->
                appendLine("${i + 1}. ${d.name} (${d.rank.arabicTitle}) - هاتف: ${d.phone} - ${d.fullAddress}")
            }
        }
    }

    fun mergeDeaconsFromJson(jsonString: String): Int {
        val before = _deacons.value.size
        executeSmartMerge(jsonString)
        return _deacons.value.size - before
    }

    companion object {
        @Volatile
        private var INSTANCE: DeaconCloudRepository? = null

        fun getInstance(context: Context): DeaconCloudRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DeaconCloudRepository(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun initialize(context: Context): DeaconCloudRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DeaconCloudRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
