package com.example.service

import android.content.Context
import android.util.Log
import com.example.model.AttendanceRecord
import com.example.model.Deacon
import com.example.model.DeaconGrade
import com.example.model.DeaconRank
import com.example.model.HymnRecitation
import com.example.model.ServantAccount
import com.example.model.ServantRole
import com.example.model.VisitationLog
import com.example.model.VisitationStatus
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Service for initializing the Firebase & Firestore SDK, managing cloud connection,
 * and handling cloud data storage for deacons.
 */
class FirebaseInitService private constructor(private val context: Context) {

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _statusMessage = MutableStateFlow("جاري تهيئة خدمات Firebase...")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    var firestore: FirebaseFirestore? = null
        private set

    companion object {
        private const val TAG = "FirebaseInitService"
        const val COLLECTION_DEACONS = "deacons"
        const val COLLECTION_SERVANTS = "servants"
        const val COLLECTION_ATTENDANCE = "attendance"
        const val COLLECTION_VISITATIONS = "visitations"
        const val COLLECTION_RECITATIONS = "curriculum_recitations"

        @Volatile
        private var INSTANCE: FirebaseInitService? = null

        fun getInstance(context: Context): FirebaseInitService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseInitService(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun initialize(context: Context): FirebaseInitService {
            val service = getInstance(context)
            service.initFirebase()
            return service
        }
    }

    /**
     * Initializes the Firebase App and Firestore SDK with offline persistence.
     */
    fun initFirebase(customProjectId: String? = null, customApiKey: String? = null): Boolean {
        return try {
            val apps = FirebaseApp.getApps(context)
            val firebaseApp: FirebaseApp = if (apps.isEmpty()) {
                val projectId = customProjectId?.ifBlank { null } ?: "deacons-service-church"
                val apiKey = customApiKey?.ifBlank { null } ?: "AIzaSyFakeKeyForDeaconsInitializationOnly"
                val applicationId = context.packageName

                val options = FirebaseOptions.Builder()
                    .setProjectId(projectId)
                    .setApplicationId(applicationId)
                    .setApiKey(apiKey)
                    .build()

                Log.d(TAG, "Initializing FirebaseApp with options for project: $projectId")
                FirebaseApp.initializeApp(context, options)
            } else {
                apps[0]
            }

            // Initialize Firestore SDK
            val db = FirebaseFirestore.getInstance(firebaseApp)

            // Configure Firestore settings (Enable offline persistent cache)
            val settings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                    PersistentCacheSettings.newBuilder()
                        .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                        .build()
                )
                .build()

            db.firestoreSettings = settings
            firestore = db
            _isInitialized.value = true
            _statusMessage.value = "تم تهيئة Firebase و Firestore بنجاح"
            Log.d(TAG, "Firestore SDK successfully initialized for project: ${firebaseApp.options.projectId}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Firebase / Firestore SDK", e)
            _isInitialized.value = false
            _statusMessage.value = "خطأ في تهيئة Firebase: ${e.localizedMessage}"
            false
        }
    }

    // --- Deacons Cloud Storage Operations ---

    /**
     * Saves or updates a deacon in Firestore collection "deacons".
     */
    suspend fun saveDeacon(deacon: Deacon): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val data = deaconToMap(deacon)
            db.collection(COLLECTION_DEACONS)
                .document(deacon.id)
                .set(data, SetOptions.merge())
                .awaitResult()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save deacon ${deacon.id} to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Deletes a deacon from Firestore collection "deacons".
     */
    suspend fun deleteDeacon(deaconId: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_DEACONS)
                .document(deaconId)
                .delete()
                .awaitResult()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete deacon $deaconId from Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Fetches all deacons once from Firestore.
     */
    suspend fun fetchDeacons(): Result<List<Deacon>> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val snapshot = db.collection(COLLECTION_DEACONS).get().awaitResult()
            val list = snapshot.documents.mapNotNull { doc ->
                mapToDeacon(doc.id, doc.data ?: emptyMap())
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch deacons from Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Observes real-time updates for deacons from Firestore using a Flow.
     */
    fun observeDeacons(): Flow<List<Deacon>> = callbackFlow {
        val db = firestore
        if (db == null) {
            close(IllegalStateException("Firestore is not initialized"))
            return@callbackFlow
        }

        val registration: ListenerRegistration = db.collection(COLLECTION_DEACONS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore snapshot listener error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val deacons = snapshot.documents.mapNotNull { doc ->
                        mapToDeacon(doc.id, doc.data ?: emptyMap())
                    }
                    trySend(deacons)
                }
            }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Batch saves multiple deacons to Firestore.
     */
    suspend fun batchSaveDeacons(deacons: List<Deacon>): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val batch = db.batch()
            for (deacon in deacons) {
                val docRef = db.collection(COLLECTION_DEACONS).document(deacon.id)
                batch.set(docRef, deaconToMap(deacon), SetOptions.merge())
            }
            batch.commit().awaitResult()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to batch save deacons to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Clears all deacons from Firestore collection.
     */
    suspend fun clearAllDeacons(): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val snapshot = db.collection(COLLECTION_DEACONS).get().awaitResult()
            val batch = db.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().awaitResult()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear deacons from Firestore", e)
            Result.failure(e)
        }
    }

    // --- Mappings ---

    private fun deaconToMap(deacon: Deacon): Map<String, Any?> {
        return mapOf(
            "id" to deacon.id,
            "code128" to deacon.code128,
            "name" to deacon.name,
            "grade" to deacon.grade.name,
            "dateOfBirth" to deacon.dateOfBirth,
            "rank" to deacon.rank.name,
            "phone" to deacon.phone,
            "parentPhone" to deacon.parentPhone,
            "governorate" to deacon.governorate,
            "district" to deacon.district,
            "area" to deacon.area,
            "street" to deacon.street,
            "buildingNumber" to deacon.buildingNumber,
            "floor" to deacon.floor,
            "photoUrl" to deacon.photoUrl,
            "pastoralNotes" to deacon.pastoralNotes,
            "churchName" to deacon.churchName,
            "createdAt" to deacon.createdAt,
            "updatedAt" to deacon.updatedAt
        )
    }

    private fun mapToDeacon(id: String, map: Map<String, Any?>): Deacon {
        val rankStr = map["rank"] as? String ?: DeaconRank.EPSALTOS.name
        val rank = try {
            DeaconRank.valueOf(rankStr)
        } catch (e: Exception) {
            DeaconRank.EPSALTOS
        }

        val gradeStr = map["grade"] as? String ?: DeaconGrade.PRIMARY_1.name
        val grade = try {
            DeaconGrade.valueOf(gradeStr)
        } catch (e: Exception) {
            DeaconGrade.PRIMARY_1
        }

        return Deacon(
            id = (map["id"] as? String)?.ifBlank { id } ?: id,
            code128 = map["code128"] as? String ?: "DK-${(1000..9999).random()}",
            name = map["name"] as? String ?: "",
            grade = grade,
            dateOfBirth = map["dateOfBirth"] as? String ?: "",
            rank = rank,
            phone = map["phone"] as? String ?: "",
            parentPhone = map["parentPhone"] as? String ?: "",
            governorate = map["governorate"] as? String ?: "أسيوط",
            district = map["district"] as? String ?: "القوصية",
            area = map["area"] as? String ?: "مير",
            street = map["street"] as? String ?: "",
            buildingNumber = map["buildingNumber"] as? String ?: "",
            floor = map["floor"] as? String ?: "",
            photoUrl = map["photoUrl"] as? String ?: "",
            pastoralNotes = map["pastoralNotes"] as? String ?: "",
            churchName = map["churchName"] as? String ?: "كنيستا العزب وأبي سيفين ودير الملاك بمير",
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            updatedAt = (map["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    // --- Servants Cloud Operations ---

    suspend fun saveServant(servant: ServantAccount): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val data = servantToMap(servant)
            db.collection(COLLECTION_SERVANTS)
                .document(servant.id)
                .set(data, SetOptions.merge())
                .awaitResult()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save servant ${servant.id} to Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun deleteServant(servantId: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            db.collection(COLLECTION_SERVANTS)
                .document(servantId)
                .delete()
                .awaitResult()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete servant $servantId from Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun fetchServants(): Result<List<ServantAccount>> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val snapshot = db.collection(COLLECTION_SERVANTS).get().awaitResult()
            val list = snapshot.documents.mapNotNull { doc ->
                mapToServant(doc.id, doc.data ?: emptyMap())
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch servants from Firestore", e)
            Result.failure(e)
        }
    }

    fun observeServants(): Flow<List<ServantAccount>> = callbackFlow {
        val db = firestore
        if (db == null) {
            close(IllegalStateException("Firestore is not initialized"))
            return@callbackFlow
        }

        val registration: ListenerRegistration = db.collection(COLLECTION_SERVANTS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore servants snapshot error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val servants = snapshot.documents.mapNotNull { doc ->
                        mapToServant(doc.id, doc.data ?: emptyMap())
                    }
                    trySend(servants)
                }
            }

        awaitClose {
            registration.remove()
        }
    }

    private fun servantToMap(servant: ServantAccount): Map<String, Any?> {
        return mapOf(
            "id" to servant.id,
            "name" to servant.name,
            "email" to servant.email,
            "password" to servant.password,
            "phone" to servant.phone,
            "role" to servant.role.name,
            "assignedGrade" to servant.assignedGrade?.name,
            "canEditDeacons" to servant.canEditDeacons,
            "canTakeAttendance" to servant.canTakeAttendance,
            "canScoreCurriculum" to servant.canScoreCurriculum,
            "canConductVisitation" to servant.canConductVisitation,
            "canExportReports" to servant.canExportReports,
            "canManagePermissions" to servant.canManagePermissions,
            "isActive" to servant.isActive,
            "isApproved" to servant.isApproved,
            "requestedGrade" to servant.requestedGrade?.name,
            "createdAt" to servant.createdAt
        )
    }

    private fun mapToServant(id: String, map: Map<String, Any?>): ServantAccount {
        val roleStr = map["role"] as? String ?: ServantRole.SERVANT.name
        val role = try { ServantRole.valueOf(roleStr) } catch (_: Exception) { ServantRole.SERVANT }

        val assignedGradeStr = map["assignedGrade"] as? String
        val assignedGrade = assignedGradeStr?.let {
            try { DeaconGrade.valueOf(it) } catch (_: Exception) { null }
        }

        val requestedGradeStr = map["requestedGrade"] as? String
        val requestedGrade = requestedGradeStr?.let {
            try { DeaconGrade.valueOf(it) } catch (_: Exception) { null }
        }

        return ServantAccount(
            id = (map["id"] as? String)?.ifBlank { id } ?: id,
            name = map["name"] as? String ?: "",
            email = map["email"] as? String ?: "",
            password = map["password"] as? String ?: "admin",
            phone = map["phone"] as? String ?: "",
            role = role,
            assignedGrade = assignedGrade,
            canEditDeacons = map["canEditDeacons"] as? Boolean ?: true,
            canTakeAttendance = map["canTakeAttendance"] as? Boolean ?: true,
            canScoreCurriculum = map["canScoreCurriculum"] as? Boolean ?: true,
            canConductVisitation = map["canConductVisitation"] as? Boolean ?: true,
            canExportReports = map["canExportReports"] as? Boolean ?: true,
            canManagePermissions = map["canManagePermissions"] as? Boolean ?: (role == ServantRole.ADMIN),
            isActive = map["isActive"] as? Boolean ?: true,
            isApproved = map["isApproved"] as? Boolean ?: true,
            requestedGrade = requestedGrade,
            createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    // --- Attendance Cloud Operations ---

    suspend fun saveAttendanceRecord(record: AttendanceRecord): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val data = attendanceToMap(record)
            db.collection(COLLECTION_ATTENDANCE)
                .document(record.id)
                .set(data, SetOptions.merge())
                .awaitResult()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save attendance ${record.id} to Firestore", e)
            Result.failure(e)
        }
    }

    suspend fun batchSaveAttendance(records: List<AttendanceRecord>): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val batch = db.batch()
            for (rec in records) {
                val docRef = db.collection(COLLECTION_ATTENDANCE).document(rec.id)
                batch.set(docRef, attendanceToMap(rec), SetOptions.merge())
            }
            batch.commit().awaitResult()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to batch save attendance to Firestore", e)
            Result.failure(e)
        }
    }

    fun observeAttendance(): Flow<List<AttendanceRecord>> = callbackFlow {
        val db = firestore
        if (db == null) {
            close(IllegalStateException("Firestore is not initialized"))
            return@callbackFlow
        }

        val registration: ListenerRegistration = db.collection(COLLECTION_ATTENDANCE)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore attendance snapshot error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        mapToAttendance(doc.id, doc.data ?: emptyMap())
                    }
                    trySend(list)
                }
            }

        awaitClose {
            registration.remove()
        }
    }

    private fun attendanceToMap(rec: AttendanceRecord): Map<String, Any?> {
        return mapOf(
            "id" to rec.id,
            "deaconId" to rec.deaconId,
            "date" to rec.date,
            "isFridayService" to rec.isFridayService,
            "isServicePresent" to rec.isServicePresent,
            "isMassPresent" to rec.isMassPresent,
            "recordedByServant" to rec.recordedByServant,
            "timestamp" to rec.timestamp
        )
    }

    private fun mapToAttendance(id: String, map: Map<String, Any?>): AttendanceRecord {
        return AttendanceRecord(
            id = (map["id"] as? String)?.ifBlank { id } ?: id,
            deaconId = map["deaconId"] as? String ?: "",
            date = map["date"] as? String ?: "",
            isFridayService = map["isFridayService"] as? Boolean ?: true,
            isServicePresent = map["isServicePresent"] as? Boolean ?: false,
            isMassPresent = map["isMassPresent"] as? Boolean ?: false,
            recordedByServant = map["recordedByServant"] as? String ?: "",
            timestamp = (map["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }

    // --- Visitation Cloud Operations ---

    suspend fun saveVisitation(log: VisitationLog): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val data = visitationToMap(log)
            db.collection(COLLECTION_VISITATIONS)
                .document(log.id)
                .set(data, SetOptions.merge())
                .awaitResult()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save visitation ${log.id} to Firestore", e)
            Result.failure(e)
        }
    }

    fun observeVisitations(): Flow<List<VisitationLog>> = callbackFlow {
        val db = firestore
        if (db == null) {
            close(IllegalStateException("Firestore is not initialized"))
            return@callbackFlow
        }

        val registration: ListenerRegistration = db.collection(COLLECTION_VISITATIONS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore visitations snapshot error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        mapToVisitation(doc.id, doc.data ?: emptyMap())
                    }
                    trySend(list)
                }
            }

        awaitClose {
            registration.remove()
        }
    }

    private fun visitationToMap(log: VisitationLog): Map<String, Any?> {
        return mapOf(
            "id" to log.id,
            "deaconId" to log.deaconId,
            "visitDate" to log.visitDate,
            "status" to log.status.name,
            "notes" to log.notes,
            "visitorServant" to log.visitorServant,
            "needsUrgentFollowup" to log.needsUrgentFollowup
        )
    }

    private fun mapToVisitation(id: String, map: Map<String, Any?>): VisitationLog {
        val statusStr = map["status"] as? String ?: VisitationStatus.VISITED.name
        val status = try { VisitationStatus.valueOf(statusStr) } catch (_: Exception) { VisitationStatus.VISITED }
        return VisitationLog(
            id = (map["id"] as? String)?.ifBlank { id } ?: id,
            deaconId = map["deaconId"] as? String ?: "",
            visitDate = map["visitDate"] as? String ?: "",
            status = status,
            notes = map["notes"] as? String ?: "",
            visitorServant = map["visitorServant"] as? String ?: "الخادم المفتقد",
            needsUrgentFollowup = map["needsUrgentFollowup"] as? Boolean ?: false
        )
    }

    // --- Recitations Cloud Operations ---

    suspend fun saveRecitation(recitation: HymnRecitation): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized"))
        return try {
            val data = recitationToMap(recitation)
            db.collection(COLLECTION_RECITATIONS)
                .document(recitation.id)
                .set(data, SetOptions.merge())
                .awaitResult()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save recitation ${recitation.id} to Firestore", e)
            Result.failure(e)
        }
    }

    fun observeRecitations(): Flow<List<HymnRecitation>> = callbackFlow {
        val db = firestore
        if (db == null) {
            close(IllegalStateException("Firestore is not initialized"))
            return@callbackFlow
        }

        val registration: ListenerRegistration = db.collection(COLLECTION_RECITATIONS)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Firestore recitations snapshot error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        mapToRecitation(doc.id, doc.data ?: emptyMap())
                    }
                    trySend(list)
                }
            }

        awaitClose {
            registration.remove()
        }
    }

    private fun recitationToMap(rec: HymnRecitation): Map<String, Any?> {
        return mapOf(
            "id" to rec.id,
            "deaconId" to rec.deaconId,
            "hymnId" to rec.hymnId,
            "hymnTitle" to rec.hymnTitle,
            "score" to rec.score,
            "maxScore" to rec.maxScore,
            "isPassed" to rec.isPassed,
            "deadlineDate" to rec.deadlineDate,
            "recitationDate" to rec.recitationDate,
            "examinerName" to rec.examinerName,
            "notes" to rec.notes
        )
    }

    private fun mapToRecitation(id: String, map: Map<String, Any?>): HymnRecitation {
        return HymnRecitation(
            id = (map["id"] as? String)?.ifBlank { id } ?: id,
            deaconId = map["deaconId"] as? String ?: "",
            hymnId = map["hymnId"] as? String ?: "",
            hymnTitle = map["hymnTitle"] as? String ?: "",
            score = (map["score"] as? Number)?.toInt() ?: 0,
            maxScore = (map["maxScore"] as? Number)?.toInt() ?: 100,
            isPassed = map["isPassed"] as? Boolean ?: false,
            deadlineDate = map["deadlineDate"] as? String ?: "",
            recitationDate = map["recitationDate"] as? String ?: "",
            examinerName = map["examinerName"] as? String ?: "",
            notes = map["notes"] as? String ?: ""
        )
    }

    /**
     * Tests connection to Firestore by reading or writing a heartbeat doc.
     */
    suspend fun pingFirestoreConnection(): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore is not initialized. Check Firebase connection settings."))
        return try {
            val pingData = mapOf(
                "connected" to true,
                "timestamp" to System.currentTimeMillis(),
                "app" to "DeaconsManager"
            )
            db.collection("connection_test")
                .document("status")
                .set(pingData)
                .awaitResult()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Firestore ping test failed", e)
            Result.failure(e)
        }
    }
}

/**
 * Extension function to await a Play Services Task safely in a coroutine.
 */
private suspend fun <T> Task<T>.awaitResult(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            continuation.resume(result)
        }
        addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }
}
